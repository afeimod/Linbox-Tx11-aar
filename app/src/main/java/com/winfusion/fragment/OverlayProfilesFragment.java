package com.winfusion.fragment;

import static android.view.View.GONE;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.GridLayoutManager;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.winfusion.R;
import com.winfusion.adapter.OverlayProfileAdapter;
import com.winfusion.adapter.common.ItemCallback;
import com.winfusion.databinding.FragmentOverlayProfilesBinding;
import com.winfusion.dialog.EditTextDialogFragment;
import com.winfusion.dialog.EditTextDialogFragmentArgs;
import com.winfusion.dialog.MenuDialogFragment;
import com.winfusion.dialog.MenuDialogFragmentArgs;
import com.winfusion.dialog.WrappedDialogFragment;
import com.winfusion.feature.manager.Container;
import com.winfusion.feature.manager.ContainerManager;
import com.winfusion.feature.manager.Overlay;
import com.winfusion.feature.manager.OverlayManager;
import com.winfusion.feature.manager.Shortcut;
import com.winfusion.feature.manager.ShortcutManager;
import com.winfusion.feature.setting.key.SettingWrapper;
import com.winfusion.model.MainActivityViewModel;
import com.winfusion.model.MenuModel;
import com.winfusion.model.OverlayProfileModel;
import com.winfusion.utils.LaunchMode;
import com.winfusion.utils.UiUtils;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class OverlayProfilesFragment extends Fragment {

    private static final String DEFAULT_OVERLAY_FILENAME = "default-1.json";

    private FragmentOverlayProfilesBinding binding;
    private OverlayProfileAdapter profileAdapter;
    private LaunchMode mode;
    private Container container;
    private Shortcut shortcut;
    private SettingWrapper settingWrapper;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        OverlayProfilesFragmentArgs args = OverlayProfilesFragmentArgs.fromBundle(getArguments());
        mode = LaunchMode.valueOf(args.getMode());
        if (mode == LaunchMode.Container) {
            container = ContainerManager.getInstance().getContainerByUUID(Objects.requireNonNull(args.getUuid()));
            settingWrapper = new SettingWrapper(Objects.requireNonNull(container).getConfig());
        } else if (mode == LaunchMode.Shortcut) {
            shortcut = ShortcutManager.getInstance().getShortcutByUUID(Objects.requireNonNull(args.getUuid()));
            settingWrapper = new SettingWrapper(Objects.requireNonNull(shortcut).getConfig());
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        binding = FragmentOverlayProfilesBinding.inflate(inflater);
        MainActivityViewModel.setShowNavigation(requireActivity(), false);
        setupTitle();
        updateSelectedProfile();
        setupProfileList();
        binding.swipeRefresh.setOnRefreshListener(() -> {
            refreshProfiles();
            binding.swipeRefresh.setRefreshing(false);
        });
        binding.buttonAdd.setOnClickListener(v -> showAddOverlayMenu());
        binding.layoutAppbar.toolbar.setOnClickListener(v ->
                requireActivity().getOnBackPressedDispatcher().onBackPressed());
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        refreshProfiles();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        saveConfig();
        binding = null;
        profileAdapter = null;
    }

    private void saveConfig() {
        if (mode == LaunchMode.Shortcut)
            shortcut.saveConfig();
        else if (mode == LaunchMode.Container)
            container.saveConfig();
    }

    private void setupTitle() {
        binding.layoutAppbar.textTitle.setText(R.string.custom_overlay_manager);
        switch (mode) {
            case Container ->
                    binding.layoutAppbar.textDescription.setText(settingWrapper.getContainerInfoName());
            case Shortcut ->
                    binding.layoutAppbar.textDescription.setText(settingWrapper.getShortcutInfoName());
            case Standalone -> binding.layoutAppbar.textDescription.setVisibility(GONE);
        }
    }

    private void setupProfileList() {
        GridLayoutManager layoutManager = new GridLayoutManager(requireContext(),
                getResources().getInteger(R.integer.overlay_profile_list_grid_columns));
        binding.listProfiles.setLayoutManager(layoutManager);
        profileAdapter = new OverlayProfileAdapter(new ItemCallback() {
            @Override
            public void onClick(int position) {
                if (mode == LaunchMode.Standalone)
                    openEditorActivity(profileAdapter.getCurrentList().get(position));
            }

            @Override
            public boolean onLongClick(int position) {
                showEditOverlayMenu(position);
                return true;
            }

            @Override
            public void onSelected(int position) {
                if (mode == LaunchMode.Shortcut || mode == LaunchMode.Container) {
                    List<OverlayProfileModel> models = profileAdapter.getCurrentList();
                    if (position < 0 || position >= models.size())
                        return;
                    settingWrapper.setContainerControlOverlayProfile(
                            models.get(position).getOverlay().getFileName());
                }
            }
        });
        binding.listProfiles.setAdapter(profileAdapter);
        UiUtils.hideExtendedFloatingActionButtonOnScroll(binding.listProfiles, binding.buttonAdd);
    }

    private void refreshProfiles() {
        boolean selectable = mode == LaunchMode.Shortcut || mode == LaunchMode.Container;
        ArrayList<OverlayProfileModel> models = new ArrayList<>();
        for (Overlay overlay : OverlayManager.getInstance().getOverlays()) {
            OverlayProfileModel model = new OverlayProfileModel(overlay);
            model.setSelectable(selectable);
            models.add(model);
        }
        profileAdapter.submitList(models, this::updateSelectedProfile);
    }

    private void updateSelectedProfile() {
        if (mode == LaunchMode.Standalone)
            return;

        String fileName = settingWrapper.getContainerControlOverlayProfile();
        new Handler(Looper.getMainLooper()).post(() -> {
            int defaultIndex = -1;
            List<OverlayProfileModel> models = profileAdapter.getCurrentList();
            for (int i = 0; i < models.size(); i++) {
                String fn = models.get(i).getOverlay().getFileName();
                if (Objects.equals(fn, fileName)) {
                    profileAdapter.setSelectedPosition(i);
                    return;
                } else if (Objects.equals(fn, DEFAULT_OVERLAY_FILENAME)) {
                    defaultIndex = i;
                }
            }
            settingWrapper.setContainerControlOverlayProfile(DEFAULT_OVERLAY_FILENAME);
            profileAdapter.setSelectedPosition(defaultIndex);
        });
    }

    private void showAddOverlayMenu() {
        ArrayList<MenuModel> models = new ArrayList<>();
        models.add(new MenuModel(R.drawable.ic_add, R.string.create, true,
                this::showCreateProfileDialog));
        models.add(new MenuModel(R.drawable.ic_upload, R.string._import, true,
                this::chooseProfile));
        showMenuDialog(R.string.add, models);
    }

    private void showEditOverlayMenu(int position) {
        OverlayProfileModel model = profileAdapter.getCurrentList().get(position);
        ArrayList<MenuModel> models = new ArrayList<>();

        models.add(new MenuModel(R.drawable.ic_design_services, R.string.edit_layout, true,
                () -> openEditorActivity(model)));
        models.add(new MenuModel(R.drawable.ic_file_download, R.string.export, true,
                () -> exportProfile(model)));
        models.add(new MenuModel(R.drawable.ic_content_copy, R.string.duplicate, true,
                () -> duplicateProfile(model)));

        if (model.getOverlay().isBuiltin()) {
            models.add(new MenuModel(R.drawable.ic_restore, R.string.restore_to_default, true,
                    () -> OverlayManager.getInstance().resetBuiltinOverlay(model.getOverlay().getFileName())));
        } else {
            models.add(new MenuModel(R.drawable.ic_edit, R.string.rename, true,
                    () -> showRenameProfileDialog(model)));
            models.add(new MenuModel(R.drawable.ic_delete, R.string.delete, true,
                    () -> showDeleteProfileDialog(model)));
        }

        showMenuDialog(R.string.edit, models);
    }

    private void showMenuDialog(int titleId, @NonNull List<MenuModel> models) {
        MenuDialogFragment dialog = new MenuDialogFragment(models);
        dialog.setArguments(new MenuDialogFragmentArgs.Builder(titleId).build().toBundle());
        dialog.show(getParentFragmentManager(), null);
    }

    private void showCreateProfileDialog() {
        EditTextDialogFragment dialogFragment = new EditTextDialogFragment(
                result -> {
                    binding.swipeRefresh.setRefreshing(true);
                    OverlayManager.getInstance().createOverlay(result);
                    refreshProfiles();
                    binding.swipeRefresh.setRefreshing(false);
                }, null);
        dialogFragment.setArguments(new EditTextDialogFragmentArgs.Builder(R.string.overlay_name,
                getString(R.string.untitled_overlay)).build().toBundle());
        dialogFragment.show(getParentFragmentManager(), null);
    }

    private final ActivityResultLauncher<Intent> systemFilePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Uri uri = result.getData().getData();
                    if (uri == null)
                        return;
                    importProfile(uri);
                }
            }
    );

    private void chooseProfile() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("application/json");
        systemFilePickerLauncher.launch(intent);
    }

    private void importProfile(@NonNull Uri uri) {
        boolean ret = OverlayManager.getInstance().importOverlay(uri) != null;
        refreshProfiles();
        if (ret)
            UiUtils.showShortToast(requireContext(), R.string.import_success);
    }

    private void duplicateProfile(@NonNull OverlayProfileModel model) {
        new WrappedDialogFragment(
                new MaterialAlertDialogBuilder(requireContext())
                        .setTitle(R.string.duplicate)
                        .setMessage(R.string.duplicate_overlay_description)
                        .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                            binding.swipeRefresh.setRefreshing(true);
                            OverlayManager.getInstance().duplicateOverlay(model.getOverlay());
                            refreshProfiles();
                            binding.swipeRefresh.setRefreshing(false);
                        })
                        .setNegativeButton(android.R.string.cancel, null)
                        .create()
        ).show(getParentFragmentManager(), null);
    }

    private void showRenameProfileDialog(@NonNull OverlayProfileModel model) {
        EditTextDialogFragment dialogFragment = new EditTextDialogFragment(
                result -> {
                    if (Objects.equals(model.getOverlay().getFileName(), result))
                        return;
                    binding.swipeRefresh.setRefreshing(true);
                    OverlayManager.getInstance().renameOverlay(model.getOverlay(), result);
                    refreshProfiles();
                    binding.swipeRefresh.setRefreshing(false);
                }, null);
        dialogFragment.setArguments(new EditTextDialogFragmentArgs.Builder(R.string.overlay_name,
                model.getOverlay().getProfile().getName()).build().toBundle());
        dialogFragment.show(getParentFragmentManager(), null);
    }

    private void showDeleteProfileDialog(@NonNull OverlayProfileModel model) {
        new WrappedDialogFragment(
                new MaterialAlertDialogBuilder(requireContext())
                        .setTitle(R.string.delete)
                        .setMessage(R.string.delete_overlay_description)
                        .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                            binding.swipeRefresh.setRefreshing(true);
                            OverlayManager.getInstance().deleteOverlay(model.getOverlay());
                            refreshProfiles();
                            binding.swipeRefresh.setRefreshing(false);
                        })
                        .setNegativeButton(android.R.string.cancel, null)
                        .create()
        ).show(getParentFragmentManager(), null);
    }

    private void openEditorActivity(@NonNull OverlayProfileModel model) {
        OverlayProfilesFragmentDirections.ActionToControlsOverlayEditor action =
                OverlayProfilesFragmentDirections.actionToControlsOverlayEditor(
                        model.getOverlay().getFileName());
        NavHostFragment.findNavController(OverlayProfilesFragment.this)
                .navigate(action);
    }

    private void exportProfile(@NonNull OverlayProfileModel model) {
        Path target = model.getOverlay().export();
        if (target != null) {
            UiUtils.showShortToast(requireContext(),
                    getString(R.string.saved_to) + target.toAbsolutePath().toString());
        }
    }
}
