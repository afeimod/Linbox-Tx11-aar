package com.winfusion.feature.content;

import static android.view.View.GONE;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.GridLayoutManager;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.winfusion.R;
import com.winfusion.adapter.common.ItemCallback;
import com.winfusion.core.compression.exception.CompressorException;
import com.winfusion.core.compression.exception.TarEntryNotFoundException;
import com.winfusion.core.wfp.WfpParser;
import com.winfusion.core.wfp.WfpType;
import com.winfusion.databinding.ActivityContentManagerBinding;
import com.winfusion.databinding.ListItemMenuBinding;
import com.winfusion.dialog.ContentDetailsDialogFragment;
import com.winfusion.dialog.ContentDetailsDialogFragmentArgs;
import com.winfusion.dialog.InfiniteProgressDialogFragment;
import com.winfusion.dialog.InfiniteProgressDialogFragmentArgs;
import com.winfusion.dialog.MenuDialogFragment;
import com.winfusion.dialog.MenuDialogFragmentArgs;
import com.winfusion.dialog.WrappedDialogFragment;
import com.winfusion.feature.content.model.BaseContentModel;
import com.winfusion.feature.content.model.SoundfontModel;
import com.winfusion.feature.content.model.WfpModel;
import com.winfusion.feature.manager.Container;
import com.winfusion.feature.manager.ContainerManager;
import com.winfusion.feature.manager.ContentManager;
import com.winfusion.feature.manager.Installable;
import com.winfusion.feature.manager.Shortcut;
import com.winfusion.feature.manager.ShortcutManager;
import com.winfusion.feature.setting.key.SettingWrapper;
import com.winfusion.model.MenuModel;
import com.winfusion.utils.LaunchMode;
import com.winfusion.utils.NoFilterArrayAdapter;
import com.winfusion.utils.UiUtils;
import com.winfusion.utils.UriUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class ContentManagerActivity extends AppCompatActivity {

    private static final String ProgressDialogTag = "ProgressDialog";

    private ActivityContentManagerBinding binding;
    private ContentManagerActivityArgs args;
    private LaunchMode mode;
    private Filter modeFilter = Filter.All;
    private Filter currentFilter;
    private SettingWrapper settingWrapper;
    private List<BaseContentModel> contentModels;
    private ContentAdapter adapter;
    private GridLayoutManager layoutManager;
    private String selectedContentId;

    public enum Filter {
        All,
        Box64,              // for config
        Wine,               // for config
        DirectX_Wrapper,
        DirectX_8_to_11,    // for config
        DirectX_12,         // for config
        Graphics_Driver,
        Vulkan_Driver,      // for config
        OpenGL_Driver,      // for config
        Soundfont           // for config
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityContentManagerBinding.inflate(getLayoutInflater());
        args = ContentManagerActivityArgs.fromBundle(getIntent().getExtras());

        setContentView(binding.getRoot());
        UiUtils.setActivityNotFullscreen(this);

        if (!setupConfig()) {
            showErrorDialog(getString(R.string.invalid_parameter), true);
            return;
        }

        updateContentModels();
        setupAdapter();
        setupTitle();
        setupSwipeRefresh();
        setupButtons();

        if (mode == LaunchMode.Standalone) {
            setupContentDropdownMenu();
            updateFilter(Filter.All);
        } else {
            binding.textLayoutType.setVisibility(GONE);
            updateFilter(modeFilter);
        }
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        layoutManager.setSpanCount(getResources().getInteger(R.integer.content_list_grid_columns));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }

    private boolean setupConfig() {
        mode = LaunchMode.valueOf(args.getMode());
        if (args.getFilter() != null)
            modeFilter = Filter.valueOf(args.getFilter());

        if (mode != LaunchMode.Standalone &&
                modeFilter != Filter.Box64 &&
                modeFilter != Filter.Wine &&
                modeFilter != Filter.DirectX_8_to_11 &&
                modeFilter != Filter.DirectX_12 &&
                modeFilter != Filter.Vulkan_Driver &&
                modeFilter != Filter.OpenGL_Driver &&
                modeFilter != Filter.Soundfont) {
            return false;
        }

        if (mode == LaunchMode.Standalone)
            return true;
        if (args.getUuid() == null)
            return false;
        if (mode == LaunchMode.Container) {
            Container container = ContainerManager.getInstance().getContainerByUUID(args.getUuid());
            if (container == null)
                return false;
            settingWrapper = new SettingWrapper(container.getConfig());
        } else if (mode == LaunchMode.Shortcut) {
            Shortcut shortcut = ShortcutManager.getInstance().getShortcutByUUID(args.getUuid());
            if (shortcut == null)
                return false;
            settingWrapper = new SettingWrapper(shortcut.getConfig());
        }
        selectedContentId = getSelectedContentIdFromConfig();
        return true;
    }

    private void setupAdapter() {
        adapter = new ContentAdapter(new ItemCallback() {

            @Override
            public void onClick(int position) {
                List<BaseContentModel> models = adapter.getCurrentList();
                if (position < 0 || position >= models.size())
                    return;

                if (mode == LaunchMode.Standalone)
                    showContentDetailsDialog(models.get(position));
            }

            @Override
            public boolean onLongClick(int position) {
                List<BaseContentModel> models = adapter.getCurrentList();
                if (position < 0 || position >= models.size())
                    return false;

                showMenuDialog(models.get(position));
                return false;
            }

            @Override
            public void onSelected(int position) {
                List<BaseContentModel> models = adapter.getCurrentList();
                if (position < 0 || position >= models.size())
                    return;

                BaseContentModel model = models.get(position);
                if (model == null)
                    return;

                String contentId = getContentId(model);
                if (contentId == null)
                    return;

                updateSelectToConfig(contentId);
            }
        });

        layoutManager = new GridLayoutManager(this,
                getResources().getInteger(R.integer.content_list_grid_columns));
        binding.listContents.setAdapter(adapter);
        binding.listContents.setLayoutManager(layoutManager);
        UiUtils.hideExtendedFloatingActionButtonOnScroll(binding.listContents, binding.buttonAdd);
    }

    private void updateSelectToConfig(@NonNull String value) {
        switch (modeFilter) {
            case Box64 -> settingWrapper.setContainerBox64Version(value);
            case Wine -> settingWrapper.setContainerWineVersion(value);
            case DirectX_8_to_11 -> settingWrapper.setContainerGraphicsDirectXWrapper8_11(value);
            case DirectX_12 -> settingWrapper.setContainerGraphicsDirectXWrapper12(value);
            case Vulkan_Driver -> settingWrapper.setContainerGraphicsDriverVulkan(value);
            case OpenGL_Driver -> settingWrapper.setContainerGraphicsDriverOpenGL(value);
            case Soundfont -> settingWrapper.setContainerAudioMIDISoundFont(value);
        }
    }

    private void setupTitle() {
        if (mode == LaunchMode.Standalone) {
            binding.layoutAppbar.textTitle.setText(R.string.contents_manager);
            binding.layoutAppbar.textDescription.setVisibility(GONE);
        } else {
            updateTitleByMode();
            updateTitleByModeFilter();
        }
    }

    private void setupSwipeRefresh() {
        binding.swipeRefresh.setOnRefreshListener(() -> {
            updateContentModels();
            updateFilter(currentFilter);
            binding.swipeRefresh.setRefreshing(false);
        });
    }

    private void setupButtons() {
        binding.buttonAdd.setOnClickListener(v -> {
            int msgId;
            if (mode == LaunchMode.Standalone)
                msgId = R.string.install_content_description;
            else if (modeFilter == Filter.Soundfont)
                msgId = R.string.install_sf2_description;
            else
                msgId = R.string.install_wfp_description;

            new WrappedDialogFragment(
                    new MaterialAlertDialogBuilder(this)
                            .setTitle(R.string.select_file)
                            .setMessage(msgId)
                            .setPositiveButton(android.R.string.ok, (dialog, which) ->
                                    openSystemFilePicker())
                            .create()
            ).show(getSupportFragmentManager(), null);
        });

        binding.layoutAppbar.toolbar.setOnClickListener(v -> finish());
    }

    private void setupContentDropdownMenu() {
        ArrayList<ContentDropdownArrayAdapter.Item> items = new ArrayList<>();
        items.add(new ContentDropdownArrayAdapter.Item(R.drawable.ic_widgets, R.string.all, Filter.All,
                getString(R.string.all)));
        items.add(new ContentDropdownArrayAdapter.Item(R.drawable.ic_box64, R.string.box64, Filter.Box64,
                getString(R.string.box64)));
        items.add(new ContentDropdownArrayAdapter.Item(R.drawable.ic_winehq, R.string.wine, Filter.Wine,
                getString(R.string.wine)));
        items.add(new ContentDropdownArrayAdapter.Item(R.drawable.ic_developer_board,
                R.string.directx_wrapper, Filter.DirectX_Wrapper, getString(R.string.directx_wrapper)));
        items.add(new ContentDropdownArrayAdapter.Item(R.drawable.ic_developer_board,
                R.string.graphics_driver, Filter.Graphics_Driver, getString(R.string.graphics_driver)));
        items.add(new ContentDropdownArrayAdapter.Item(R.drawable.ic_piano, R.string.soundfont,
                Filter.Soundfont, getString(R.string.soundfont)));

        ContentDropdownArrayAdapter adapter = new ContentDropdownArrayAdapter(this, items);
        binding.autoTextType.setAdapter(adapter);
        binding.autoTextType.setText(R.string.all);
        binding.autoTextType.setOnItemClickListener((parent, view, position, id) -> {
            ContentDropdownArrayAdapter.Item item =
                    (ContentDropdownArrayAdapter.Item) parent.getItemAtPosition(position);
            if (item == null)
                return;
            updateFilter(item.filter);
        });
    }

    private void showContentDetailsDialog(@NonNull BaseContentModel model) {
        ContentDetailsDialogFragment dialog;

        if (model instanceof SoundfontModel soundfontModel)
            dialog = new ContentDetailsDialogFragment(soundfontModel.getInfo(), null, null);
        else if (model instanceof WfpModel wfpModel)
            dialog = new ContentDetailsDialogFragment(wfpModel.getWfp(), null, null);
        else
            return;

        dialog.setArguments(new ContentDetailsDialogFragmentArgs.Builder().build().toBundle());
        dialog.show(getSupportFragmentManager(), null);
    }

    private void showMenuDialog(@NonNull BaseContentModel model) {
        ArrayList<MenuModel> list = new ArrayList<>();
        list.add(new MenuModel(R.drawable.ic_file, R.string.details, true, () ->
                showContentDetailsDialog(model)));
        if (!model.isBuiltin())
            list.add(new MenuModel(R.drawable.ic_delete_forever, R.string.uninstall, true, () ->
                    showDeleteContentDialog(model)));

        MenuDialogFragment dialog = new MenuDialogFragment(list);
        dialog.setArguments(new MenuDialogFragmentArgs.Builder(R.string.edit).build().toBundle());
        dialog.show(getSupportFragmentManager(), null);
    }

    private void showDeleteContentDialog(@NonNull BaseContentModel model) {
        new WrappedDialogFragment(
                new MaterialAlertDialogBuilder(this)
                        .setTitle(R.string.uninstall)
                        .setMessage(R.string.uninstall_content_description)
                        .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                            if (model.isBuiltin())
                                return;
                            if (model instanceof SoundfontModel soundfontModel) {
                                ContentManager.getInstance().uninstallSoundfont(soundfontModel.getFileName());
                            } else if (model instanceof WfpModel wfpModel) {
                                if (wfpModel.getWfp().getWfpHome() == null)
                                    return;
                                try {
                                    ContentManager.getInstance().uninstallWfp(wfpModel.getWfp().getWfpHome());
                                } catch (IOException e) {
                                    showErrorDialog(e.toString(), false);
                                }
                            }

                            updateContentModels();
                            updateFilter(currentFilter);
                        })
                        .setNegativeButton(android.R.string.cancel, null)
                        .create()
        ).show(getSupportFragmentManager(), null);
    }

    private void showInstallConfirmDialog(@NonNull Installable installable) {
        ContentDetailsDialogFragment dialog;

        Runnable onPositiveCallback = () -> {
            try {
                installable.install();
                UiUtils.showShortToast(this, R.string.install_success_description);
            } catch (Installable.InstallFailedException e) {
                showErrorDialog(getErrorMsg(e.toString()), false);
            }

            updateContentModels();
            updateFilter(currentFilter);
            ContentManager.getInstance().clearInstallCache();
        };

        Runnable onNegativeCallback = () -> ContentManager.getInstance().clearInstallCache();

        if (installable instanceof ContentManager.WfpInstaller wfpInstaller)
            dialog = new ContentDetailsDialogFragment(wfpInstaller.getWfp(), onPositiveCallback,
                    onNegativeCallback);
        else if (installable instanceof ContentManager.SoundfontInstaller soundfontInstaller)
            dialog = new ContentDetailsDialogFragment(soundfontInstaller.getInfo(), onPositiveCallback,
                    onNegativeCallback);
        else
            return;

        dialog.setArguments(new ContentDetailsDialogFragmentArgs.Builder().setPositiveBtn(R.string._continue)
                .build().toBundle());
        dialog.show(getSupportFragmentManager(), null);
    }

    private void showErrorDialog(@NonNull String errorMsg, boolean exit) {
        new WrappedDialogFragment(
                new MaterialAlertDialogBuilder(this)
                        .setTitle(R.string.error)
                        .setMessage(errorMsg)
                        .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                            if (exit)
                                finish();
                        })
                        .setCancelable(false)
                        .create()
        ).show(getSupportFragmentManager(), null);
    }

    private void showProgressDialog() {
        InfiniteProgressDialogFragment dialog = new InfiniteProgressDialogFragment();
        dialog.setArguments(new InfiniteProgressDialogFragmentArgs.Builder(R.string.loading)
                .build().toBundle());
        dialog.show(getSupportFragmentManager(), ProgressDialogTag);
    }

    private void dismissProgressDialog() {
        DialogFragment dialog = (DialogFragment) getSupportFragmentManager().findFragmentByTag(ProgressDialogTag);
        if (dialog == null)
            return;
        dialog.dismiss();
    }

    private void updateTitleByMode() {
        if (mode == LaunchMode.Container)
            binding.layoutAppbar.textDescription.setText(settingWrapper.getContainerInfoName());
        else if (mode == LaunchMode.Shortcut)
            binding.layoutAppbar.textDescription.setText(settingWrapper.getShortcutInfoName());
    }

    private void updateTitleByModeFilter() {
        int titleId = 0;

        if (modeFilter == Filter.Wine)
            titleId = R.string.choose_wine;
        else if (modeFilter == Filter.Box64)
            titleId = R.string.choose_box64;
        else if (modeFilter == Filter.Soundfont)
            titleId = R.string.choose_soundfont;
        else if (modeFilter == Filter.DirectX_8_to_11)
            titleId = R.string.choose_directx_8_11_wrapper;
        else if (modeFilter == Filter.DirectX_12)
            titleId = R.string.choose_directx_12_wrapper;
        else if (modeFilter == Filter.Vulkan_Driver)
            titleId = R.string.choose_vulkan_driver;
        else if (modeFilter == Filter.OpenGL_Driver)
            titleId = R.string.choose_opengl_driver;

        if (titleId != 0)
            binding.layoutAppbar.textTitle.setText(titleId);
    }

    private void updateContentModels() {
        List<BaseContentModel> models = ContentManager.getInstance().generateModels();
        if (mode == LaunchMode.Shortcut || mode == LaunchMode.Container) {
            for (BaseContentModel model : models)
                model.setSelectable(true);
        }
        contentModels = models;
    }

    private void updateFilter(@NonNull Filter filter) {
        ArrayList<BaseContentModel> list = new ArrayList<>();

        if (filter == Filter.All) {
            list.addAll(contentModels);
        } else if (filter == Filter.Box64) {
            applyFilterWfp(list, Set.of(WfpType.Box64));
        } else if (filter == Filter.Wine) {
            applyFilterWfp(list, Set.of(WfpType.Wine));
        } else if (filter == Filter.DirectX_Wrapper) {
            applyFilterWfp(list, Set.of(WfpType.DXVK, WfpType.VKD3D, WfpType.WineD3D));
        } else if (filter == Filter.DirectX_8_to_11) {
            applyFilterWfp(list, Set.of(WfpType.DXVK, WfpType.WineD3D));
        } else if (filter == Filter.DirectX_12) {
            applyFilterWfp(list, Set.of(WfpType.VKD3D));
        } else if (filter == Filter.Graphics_Driver) {
            applyFilterWfp(list, Set.of(WfpType.MesaTurnip, WfpType.MesaVenus, WfpType.MesaWrapper,
                    WfpType.MesaZink, WfpType.MesaVirGL));
        } else if (filter == Filter.Vulkan_Driver) {
            applyFilterWfp(list, Set.of(WfpType.MesaTurnip, WfpType.MesaVenus, WfpType.MesaWrapper));
        } else if (filter == Filter.OpenGL_Driver) {
            applyFilterWfp(list, Set.of(WfpType.MesaZink, WfpType.MesaVirGL));
        } else if (filter == Filter.Soundfont) {
            applyFilterSoundfont(list);
        }

        currentFilter = filter;
        adapter.submitList(list, () -> {
            if (mode == LaunchMode.Container || mode == LaunchMode.Shortcut)
                updateSelection();
        });
    }

    private void updateSelection() {
        if (selectedContentId == null)
            return;
        List<BaseContentModel> models = adapter.getCurrentList();
        int i = 0;
        for (; i < models.size(); i++) {
            if (Objects.equals(getContentId(models.get(i)), selectedContentId))
                break;
        }
        if (i < models.size())
            adapter.setSelectedPosition(i);
        else
            adapter.setSelectedPosition(-1);
    }

    private void applyFilterWfp(@NonNull List<BaseContentModel> list, @NonNull Set<WfpType> types) {
        for (BaseContentModel model : contentModels) {
            if (model instanceof WfpModel wfpModel && types.contains(wfpModel.getWfp().getWfpType()))
                list.add(model);
        }
    }

    private void applyFilterSoundfont(@NonNull List<BaseContentModel> list) {
        for (BaseContentModel model : contentModels) {
            if (model instanceof SoundfontModel)
                list.add(model);
        }
    }

    private void openSystemFilePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        systemFilePickerLauncher.launch(intent);
    }

    private final ActivityResultLauncher<Intent> systemFilePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Uri uri = result.getData().getData();
                    if (uri == null)
                        return;
                    openContentFromUri(uri);
                }
            }
    );

    private void openContentFromUri(@NonNull Uri uri) {
        showProgressDialog();
        new Thread(() -> {
            String fileName = UriUtils.getFileNameFromUri(this, uri);
            Installable installable = null;
            String errorMsg = null;

            if (fileName == null)
                return;

            if (fileName.endsWith(".sf2") || fileName.endsWith(".sf3")) {
                installable = ContentManager.getInstance().installSoundfont(uri);
                if (installable == null)
                    errorMsg = getErrorMsg(R.string.invalid_file_format_description);
            } else if (fileName.endsWith(".wfp") || fileName.endsWith(".tar.xz") || fileName.endsWith(".tar.zst")
                    || fileName.endsWith("tar.gz") || fileName.endsWith(".zip")) {
                try {
                    installable = ContentManager.getInstance().installWfp(uri);
                } catch (TarEntryNotFoundException | WfpParser.BadWfpFormatException e) {
                    errorMsg = getErrorMsg(R.string.wfp_broken_description);
                } catch (IOException | CompressorException e) {
                    errorMsg = getErrorMsg(e.toString());
                } catch (ContentManager.WfpAlreadyExistsException e) {
                    errorMsg = getErrorMsg(R.string.wfp_already_exists_description);
                }
            } else {
                errorMsg = getErrorMsg(R.string.invalid_file_format_description);
            }

            Installable finalInstallable = installable;
            String finalErrorMsg = errorMsg;
            runOnUiThread(() -> {
                dismissProgressDialog();
                if (finalInstallable == null) {
                    showErrorDialog(finalErrorMsg, false);
                } else {
                    showInstallConfirmDialog(finalInstallable);
                }
            });
        }).start();
    }

    @NonNull
    private String getErrorMsg(@NonNull String errorReason) {
        return getString(R.string.install_failed) + ": " + errorReason;
    }

    @NonNull
    private String getErrorMsg(@StringRes int errorReasonId) {
        return getErrorMsg(getString(errorReasonId));
    }

    @Nullable
    private String getSelectedContentIdFromConfig() {
        return switch (modeFilter) {
            case All, DirectX_Wrapper, Graphics_Driver -> null;
            case Box64 -> settingWrapper.getContainerBox64Version();
            case Wine -> settingWrapper.getContainerWineVersion();
            case DirectX_8_to_11 -> settingWrapper.getContainerGraphicsDirectXWrapper8_11();
            case DirectX_12 -> settingWrapper.getContainerGraphicsDirectXWrapper12();
            case Vulkan_Driver -> settingWrapper.getContainerGraphicsDriverVulkan();
            case OpenGL_Driver -> settingWrapper.getContainerGraphicsDriverOpenGL();
            case Soundfont -> settingWrapper.getContainerAudioMIDISoundFont();
        };
    }

    @Nullable
    private String getContentId(@NonNull BaseContentModel model) {
        if (model instanceof SoundfontModel soundfontModel)
            return soundfontModel.getFileName();
        else if (model instanceof WfpModel wfpModel)
            return wfpModel.getWfp().toIdentifier();
        else
            return null;
    }

    protected static class ContentDropdownArrayAdapter
            extends NoFilterArrayAdapter<ContentDropdownArrayAdapter.Item> {

        public ContentDropdownArrayAdapter(@NonNull Context context,
                                           @NonNull List<Item> objects) {

            super(context, 0, objects);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            if (convertView == null) {
                convertView = ListItemMenuBinding.inflate(LayoutInflater.from(parent.getContext()),
                        parent, false).getRoot();
            }

            ListItemMenuBinding binding = ListItemMenuBinding.bind(convertView);
            Item item = getItem(position);
            if (item != null) {
                binding.textTitle.setText(item.titleId);
                binding.imageIcon.setImageResource(item.iconId);
            }
            return convertView;
        }

        protected record Item(int iconId, int titleId, Filter filter, String description) {

            @NonNull
            @Override
            public String toString() {
                return description;
            }
        }
    }
}
