package com.winfusion.activity;

import static com.winfusion.core.registry.exporter.WineRegExporter.RegType.System;
import static com.winfusion.core.registry.exporter.WineRegExporter.RegType.User;
import static com.winfusion.core.registry.exporter.WineRegExporter.RegType.UserDef;
import static com.winfusion.model.RegistryItemModel.Action.GoBack;
import static com.winfusion.model.RegistryItemModel.Action.OpenItem;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.winfusion.R;
import com.winfusion.adapter.RegistryItemAdapter;
import com.winfusion.adapter.RegistryValueAdapter;
import com.winfusion.adapter.common.ItemClickCallback;
import com.winfusion.core.registry.Registry;
import com.winfusion.core.registry.RegistryItemContext;
import com.winfusion.core.registry.RegistryKey;
import com.winfusion.core.registry.RegistryKeys;
import com.winfusion.core.registry.data.RegistryData;
import com.winfusion.core.registry.exception.RegistryException;
import com.winfusion.core.registry.exception.RegistryExporterException;
import com.winfusion.core.registry.exporter.RegeditRegExporter;
import com.winfusion.core.registry.exporter.WineRegExporter;
import com.winfusion.core.registry.parser.RegeditRegParser;
import com.winfusion.core.registry.parser.WineRegParser;
import com.winfusion.databinding.ActivityRegistryEditorBinding;
import com.winfusion.dialog.EditRegistryValueDialogFragment;
import com.winfusion.dialog.EditTextDialogFragment;
import com.winfusion.dialog.EditTextDialogFragmentArgs;
import com.winfusion.dialog.InfiniteProgressDialogFragment;
import com.winfusion.dialog.InfiniteProgressDialogFragmentArgs;
import com.winfusion.dialog.MenuDialogFragment;
import com.winfusion.dialog.MenuDialogFragmentArgs;
import com.winfusion.dialog.WrappedDialogFragment;
import com.winfusion.feature.manager.Container;
import com.winfusion.feature.manager.ContainerManager;
import com.winfusion.feature.setting.key.SettingWrapper;
import com.winfusion.model.MenuModel;
import com.winfusion.model.RegistryItemModel;
import com.winfusion.model.RegistryValueModel;
import com.winfusion.utils.FileUtils;
import com.winfusion.utils.TextChecker;
import com.winfusion.utils.TextUtils;
import com.winfusion.utils.UiUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

public class RegistryEditorActivity extends AppCompatActivity {

    private static final String CD__ = "..";
    private static final String REG_BASENAME = "registry";
    private static final String REG_SUFFIX = "reg";
    private static final String ProgressDialogTag = "ProgressDialog";

    private final LinkedList<Position> backStack = new LinkedList<>();
    private final RegistryKey hkeyLocalMachine = RegistryKeys.getHkeyLocalMachine();
    private final RegistryKey hkeyCurrentUser = RegistryKeys.getHkeyCurrentUser();
    private final RegistryKey hkeyUsersDefault = RegistryKeys.getHkeyUsersDefault();
    private ActivityRegistryEditorBinding binding;
    private RegistryItemAdapter itemAdapter;
    private RegistryValueAdapter valueAdapter;
    private LinearLayoutManager itemLayoutManager;
    private RegistryKey currentKey = RegistryKeys.root();
    private boolean needSave = false;
    private Registry registry;
    private Path[] regs;
    private Container container;

    private record Position(int index, int offset) {

    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        UiUtils.setActivityNotFullscreen(this);
        RegistryEditorActivityArgs args = RegistryEditorActivityArgs.fromBundle(getIntent().getExtras());
        binding = ActivityRegistryEditorBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        container = ContainerManager.getInstance().getContainerByUUID(args.getContainerUuid());
        if (container == null) {
            showErrorDialog(getString(R.string.invalid_container));
            return;
        }
        regs = new Path[]{
                container.getRegistryPath(System),
                container.getRegistryPath(User),
                container.getRegistryPath(UserDef)
        };

        binding.layoutAppbar.textTitle.setText(R.string.registry_editor);
        setupContainerName();
        setupItemAdapter();
        setupValueAdapter();
        setupButtons();
        setupBackPress();
        showProgressDialog();
        if (!checkRegistryFiles())
            return;
        loadRegistry();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }

    private boolean checkRegistryFiles() {
        for (Path reg : regs) {
            if (!Files.isRegularFile(reg)) {
                dismissProgressDialog();
                showErrorDialog(getString(R.string.registry_not_found_description));
                return false;
            }
        }
        return true;
    }

    private void loadRegistry() {
        new Thread(() -> {
            Registry registry = new Registry();
            WineRegParser parser = new WineRegParser();
            for (Path reg : regs) {
                try {
                    registry.merge(parser.parse(reg, StandardCharsets.UTF_8));
                } catch (Exception e) {
                    runOnUiThread(() -> showErrorDialog(e.toString()));
                }
            }
            this.registry = registry;

            runOnUiThread(() -> {
                currentKey = RegistryKeys.root();
                navigateTo(currentKey, false, null);
                dismissProgressDialog();
            });
        }).start();
    }

    private void setupContainerName() {
        SettingWrapper wrapper = new SettingWrapper(container.getConfig());
        binding.layoutAppbar.textDescription.setText(wrapper.getContainerInfoName());
    }

    private void setupItemAdapter() {
        itemAdapter = new RegistryItemAdapter(new ItemClickCallback() {

            @Override
            public void onClick(int position) {
                List<RegistryItemModel> list = itemAdapter.getCurrentList();
                if (position < 0 || position > list.size())
                    return;
                RegistryItemModel model = list.get(position);
                RegistryKey key = model.getItemKey();
                if (model.getAction() == OpenItem && key != null)
                    navigateTo(currentKey.resolve(key), true, null);
                else if (model.getAction() == GoBack)
                    navigateForward();
            }

            @Override
            public boolean onLongClick(int position) {
                List<RegistryItemModel> list = itemAdapter.getCurrentList();
                if (position < 0 || position > list.size())
                    return false;
                RegistryItemModel model = list.get(position);
                RegistryKey subkey = list.get(position).getItemKey();
                if (subkey == null)
                    return false;
                RegistryKey key = currentKey.resolve(subkey);
                if (model.getAction() != OpenItem || Objects.equals(key, hkeyLocalMachine) ||
                        Objects.equals(key, hkeyCurrentUser) || Objects.equals(key, hkeyUsersDefault) ||
                        key.isParentOf(hkeyUsersDefault)) {
                    return false;
                }

                ArrayList<MenuModel> models = new ArrayList<>();
                models.add(new MenuModel(R.drawable.ic_edit, R.string.rename, true,
                        () -> showRenameItemDialog(key)));
                models.add(new MenuModel(R.drawable.ic_save_alt, R.string.export_to_reg, true, () -> {
                    RegeditRegExporter exporter = new RegeditRegExporter(registry);
                    Path target = FileUtils.getStoragePath(REG_BASENAME, REG_SUFFIX);
                    try {
                        exporter.export(target, key);
                    } catch (IOException | RegistryExporterException e) {
                        return;
                    }
                    UiUtils.showShortToast(RegistryEditorActivity.this,
                            getString(R.string.saved_to) + target);
                }));
                models.add(new MenuModel(R.drawable.ic_delete, R.string.delete, true,
                        () -> showDeleteItemDialog(key)));
                showMenuDialog(R.string.edit_registry_item, models);

                return true;
            }
        });

        itemLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        binding.listItem.setLayoutManager(itemLayoutManager);
        binding.listItem.setAdapter(itemAdapter);
    }

    private void setupValueAdapter() {
        valueAdapter = new RegistryValueAdapter(new ItemClickCallback() {

            @Override
            public void onClick(int position) {
                List<RegistryValueModel> list = valueAdapter.getCurrentList();
                if (position < 0 || position > list.size())
                    return;
                boolean isDefault = position == 0;
                RegistryValueModel model = list.get(position);
                RegistryItemContext context = getContextOfCurrentItem();

                if (isDefault) {
                    showCreateValueDialog(result -> {
                        if (result == null || result.data() == null)
                            return;
                        context.setDefaultValue(result.data());
                        navigateUpdateValueList(currentKey);
                        needSave = true;
                    }, new EditRegistryValueDialogFragment.RegistryValue(null,
                            model.getValueData(), true));
                } else {
                    showCreateValueDialog(result -> {
                        if (result == null || result.data() == null)
                            return;
                        context.deleteValue(model.getValueName());
                        context.addValue(result.name(), result.data());
                        navigateUpdateValueList(currentKey);
                        needSave = true;
                    }, new EditRegistryValueDialogFragment.RegistryValue(model.getValueName(),
                            model.getValueData(), false));
                }
            }

            @Override
            public boolean onLongClick(int position) {
                List<RegistryValueModel> list = valueAdapter.getCurrentList();
                if (position < 0 || position > list.size())
                    return false;
                boolean isDefault = position == 0;
                RegistryItemContext context = getContextOfCurrentItem();

                if (isDefault) {
                    new WrappedDialogFragment(new MaterialAlertDialogBuilder(RegistryEditorActivity.this)
                            .setTitle(R.string.delete)
                            .setMessage(R.string.delete_default_registry_value_description)
                            .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                                context.setDefaultValue(null);
                                navigateUpdateValueList(currentKey);
                                needSave = true;
                            })
                            .setNegativeButton(android.R.string.cancel, null)
                            .create()
                    ).show(getSupportFragmentManager(), null);
                } else {
                    ArrayList<MenuModel> models = new ArrayList<>();
                    models.add(new MenuModel(R.drawable.ic_delete, R.string.delete, true,
                            () -> showDeleteValueDialog(valueAdapter.getCurrentList().get(position)
                                    .getValueName())));
                    showMenuDialog(R.string.edit_registry_value, models);
                }

                return true;
            }
        });

        binding.listValue.setAdapter(valueAdapter);
        binding.listValue.setLayoutManager(new LinearLayoutManager(this,
                LinearLayoutManager.VERTICAL, false));
        UiUtils.hideExtendedFloatingActionButtonOnScroll(binding.listValue, binding.buttonImport,
                binding.buttonCreate);
    }

    private void setupButtons() {
        binding.buttonCreate.setOnClickListener(v -> {
            ArrayList<MenuModel> models = new ArrayList<>();
            models.add(new MenuModel(R.drawable.ic_folder, R.string.key, true,
                    this::showCreateItemDialog));
            models.add(new MenuModel(R.drawable.ic_file, R.string.value, true,
                    () -> showCreateValueDialog(result -> {
                        if (result == null)
                            return;
                        RegistryData data = result.data();
                        if (data == null)
                            throw new IllegalStateException("Data must not be null.");
                        RegistryItemContext context = getContextOfCurrentItem();
                        context.addValue(result.name(), result.data());
                        navigateUpdateValueList(currentKey);
                        needSave = true;
                    }, new EditRegistryValueDialogFragment.RegistryValue(null, null, false))));
            showMenuDialog(R.string.create, models);
        });
        binding.buttonImport.setOnClickListener(v -> chooseReg());
        binding.cardPath.setOnClickListener(v -> showJumpDialog());
        binding.cardPath.setOnLongClickListener(v -> {
            TextUtils.copyTextToClipboard(this, currentKey.toString());
            return true;
        });
        binding.layoutAppbar.toolbar.setOnClickListener(v -> {
            if (needSave)
                showSaveDialog();
            else
                finish();
        });
    }

    private void setupBackPress() {
        getOnBackPressedDispatcher().addCallback(
                this,
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        if (currentKey.isRoot()) {
                            if (needSave)
                                showSaveDialog();
                            else
                                finish();
                        } else {
                            navigateForward();
                        }
                    }
                }
        );
    }

    private void navigateTo(@NonNull RegistryKey key, boolean needPush,
                            @Nullable Runnable commitCallback) {

        if (needPush) {
            int position = itemLayoutManager.findFirstVisibleItemPosition();
            View firstVisbleView = itemLayoutManager.findViewByPosition(position);
            backStack.push(new RegistryEditorActivity.Position(position, firstVisbleView == null ? 0 :
                    firstVisbleView.getTop()));
        }

        navigateUpdateItemList(key, commitCallback);
        navigateUpdateValueList(key);
        binding.buttonCreate.setEnabled(!currentKey.isRoot() && !currentKey.isParentOf(hkeyUsersDefault));
    }

    private void navigateUpdateItemList(@NonNull RegistryKey key, @Nullable Runnable commitCallback) {
        ArrayList<RegistryItemModel> models = new ArrayList<>();
        if (!key.isRoot())
            models.add(new RegistryItemModel(CD__, null, GoBack));

        RegistryItemContext context = registry.getItemContext(key);
        if (context == null)
            throw new IllegalStateException("context of key must not be null.");
        for (RegistryKey k : context.getKeyOfChildren())
            models.add(new RegistryItemModel(k.toString(), k, OpenItem));

        currentKey = key;
        itemAdapter.submitList(models, () -> {
            binding.textPath.setText(key.toString());
            if (commitCallback != null)
                commitCallback.run();
        });
    }

    private void navigateUpdateValueList(@NonNull RegistryKey key) {
        ArrayList<RegistryValueModel> models = new ArrayList<>();
        if (key.isRoot() || key.isParentOf(hkeyUsersDefault)) {
            valueAdapter.submitList(models);
            return;
        }
        RegistryItemContext context = getContextOfCurrentItem();
        RegistryData defaultValue = context.getDefaultValue();
        if (defaultValue == null)
            models.add(new RegistryValueModel(getString(R.string.registry_default), getString(R.string.unset)));
        else
            models.add(new RegistryValueModel(getString(R.string.registry_default), defaultValue));
        for (Map.Entry<String, RegistryData> entry : context.toItem().namedValues.entrySet())
            models.add(new RegistryValueModel(entry.getKey(), entry.getValue()));
        valueAdapter.submitList(models);
    }

    private void navigateForward() {
        navigateTo(currentKey.getParent(), false, () -> {
            RegistryEditorActivity.Position p;
            try {
                p = backStack.pop();
            } catch (NoSuchElementException e) {
                return;
            }
            itemLayoutManager.scrollToPositionWithOffset(p.index, p.offset);
        });
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

    private void showErrorDialog(@NonNull String errorMsg) {
        new WrappedDialogFragment(
                new MaterialAlertDialogBuilder(this)
                        .setTitle(R.string.error)
                        .setMessage(errorMsg)
                        .setPositiveButton(android.R.string.ok, (dialog, which) -> finish())
                        .setCancelable(false)
                        .create()
        ).show(getSupportFragmentManager(), null);
    }

    private void showSaveDialog() {
        new WrappedDialogFragment(
                new MaterialAlertDialogBuilder(this)
                        .setTitle(R.string.save)
                        .setMessage(R.string.registry_need_save_description)
                        .setPositiveButton(R.string.yes, (dialog, which) -> {
                            WineRegExporter exporter = new WineRegExporter(registry);
                            try {
                                exporter.export(container.getRegistryPath(System), System);
                                exporter.export(container.getRegistryPath(User), User);
                                exporter.export(container.getRegistryPath(UserDef), UserDef);
                            } catch (IOException | RegistryExporterException e) {
                                showErrorDialog(e.toString());
                            }
                            finish();
                        })
                        .setNegativeButton(R.string.no, (dialog, which) -> finish())
                        .setNeutralButton(android.R.string.cancel, null)
                        .create()
        ).show(getSupportFragmentManager(), null);
    }

    private void showMenuDialog(@StringRes int titleId, @NonNull List<MenuModel> models) {
        MenuDialogFragment dialog = new MenuDialogFragment(models);
        dialog.setArguments(new MenuDialogFragmentArgs.Builder(titleId).build().toBundle());
        dialog.show(getSupportFragmentManager(), null);
    }

    private void showRenameItemDialog(@NonNull RegistryKey key) {
        RegistryItemContext currentContext = getContextOfCurrentItem();
        EditTextDialogFragment dialog = new EditTextDialogFragment(
                s -> {
                    RegistryItemContext context = registry.getItemContext(key);
                    if (context == null)
                        throw new IllegalStateException("context of key must not be null.");
                    RegistryKey newKey = RegistryKeys.get(s);
                    context.setSelfKey(newKey);
                    navigateUpdateItemList(currentKey, null);
                    needSave = true;
                }, createItemNameChecker(currentContext)
        );
        dialog.setArguments(new EditTextDialogFragmentArgs.Builder(R.string.rename, key.getSelf().toString())
                .build().toBundle());
        dialog.show(getSupportFragmentManager(), null);
    }

    private void showDeleteItemDialog(@NonNull RegistryKey key) {
        new WrappedDialogFragment(
                new MaterialAlertDialogBuilder(this)
                        .setTitle(R.string.delete)
                        .setMessage(R.string.delete_registry_item_description)
                        .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                            registry.deleteItem(key);
                            navigateUpdateItemList(currentKey, null);
                            needSave = true;
                        })
                        .setNegativeButton(android.R.string.cancel, null)
                        .create()
        ).show(getSupportFragmentManager(), null);
    }

    private void showCreateValueDialog(@NonNull Consumer<EditRegistryValueDialogFragment.RegistryValue> callback,
                                       @NonNull EditRegistryValueDialogFragment.RegistryValue original) {

        HashSet<String> unavailableNames = new HashSet<>();

        if (!original.isDefault()) {
            for (Map.Entry<String, RegistryData> entry : getContextOfCurrentItem().toItem().namedValues.entrySet())
                unavailableNames.add(entry.getKey().toLowerCase());
            if (original.name() != null)
                unavailableNames.remove(original.name().toLowerCase());
        }

        EditRegistryValueDialogFragment dialog = new EditRegistryValueDialogFragment(callback,
                original, unavailableNames);
        dialog.show(getSupportFragmentManager(), null);
    }

    private void showDeleteValueDialog(@NonNull String valueName) {
        new WrappedDialogFragment(
                new MaterialAlertDialogBuilder(this)
                        .setTitle(R.string.delete)
                        .setMessage(R.string.delete_registry_value_description)
                        .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                            getContextOfCurrentItem().deleteValue(valueName);
                            navigateUpdateValueList(currentKey);
                            needSave = true;
                        })
                        .setNegativeButton(android.R.string.cancel, null)
                        .create()
        ).show(getSupportFragmentManager(), null);
    }

    private void showCreateItemDialog() {
        EditTextDialogFragment dialog = new EditTextDialogFragment(
                s -> {
                    RegistryKey key = currentKey;
                    registry.createItem(key.resolve(s));
                    navigateUpdateItemList(key, null);
                    needSave = true;
                }, createItemNameChecker(getContextOfCurrentItem()));
        dialog.setArguments(new EditTextDialogFragmentArgs.Builder(
                R.string.name, getString(R.string._new)).build().toBundle());
        dialog.show(getSupportFragmentManager(), null);
    }

    private void showJumpDialog() {
        EditTextDialogFragment dialog = new EditTextDialogFragment(s -> {
            try {
                RegistryKey key = RegistryKeys.get(s);
                if (Objects.equals(currentKey, key))
                    return;
                if (!registry.hasItem(key)) {
                    UiUtils.showShortToast(this, R.string.invalid_registry_key);
                    return;
                }
                backStack.clear();
                navigateTo(key, true, null);
            } catch (IllegalArgumentException e) {
                // skip
            }
        }, null);
        dialog.setArguments(new EditTextDialogFragmentArgs.Builder(R.string.jump_to_key, currentKey.toString())
                .build().toBundle());
        dialog.show(getSupportFragmentManager(), null);
    }

    @NonNull
    private RegistryItemContext getContextOfCurrentItem() {
        RegistryItemContext context = registry.getItemContext(currentKey);
        if (context == null)
            throw new IllegalStateException("context of current key must not be null.");
        return context;
    }

    @NonNull
    private TextChecker createItemNameChecker(@NonNull RegistryItemContext context) {
        return new TextChecker() {

            private final Set<String> unavailableNames = createUnavailableNameSet();
            private int tipsId;

            @Override
            public boolean check(@NonNull String textStr) {
                if (textStr.isEmpty() || textStr.contains(RegistryKey.KEY_SPLIT)) {
                    tipsId = R.string.name_invalid_description;
                    return false;
                }
                if (unavailableNames.contains(textStr.toLowerCase())) {
                    tipsId = R.string.name_duplicate_description;
                    return false;
                }
                return true;
            }

            @Override
            public int getTipsId() {
                return tipsId;
            }

            private Set<String> createUnavailableNameSet() {
                HashSet<String> set = new HashSet<>();
                for (RegistryKey key : context.getKeyOfChildren())
                    set.add(key.toNormalize());
                return set;
            }
        };
    }

    private void chooseReg() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
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
                    try {
                        Registry r = new RegeditRegParser().prase(uri, this, StandardCharsets.UTF_16LE);
                        registry.merge(r);
                        UiUtils.showShortToast(this, R.string.import_success);
                    } catch (IOException | RegistryException e) {
                        UiUtils.showShortToast(this, R.string.import_reg_failed_description);
                    }
                }
            }
    );
}
