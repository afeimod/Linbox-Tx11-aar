package com.winfusion.fragment;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.MutableLiveData;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.GridLayoutManager;

import com.winfusion.R;
import com.winfusion.adapter.CommonPropertyAdapter;
import com.winfusion.databinding.FragmentShortcutPropertiesBinding;
import com.winfusion.dialog.MenuDialogFragment;
import com.winfusion.dialog.MenuDialogFragmentArgs;
import com.winfusion.feature.manager.Shortcut;
import com.winfusion.feature.manager.ShortcutManager;
import com.winfusion.feature.setting.key.SettingWrapper;
import com.winfusion.model.CommonPropertyModel;
import com.winfusion.model.MainActivityViewModel;
import com.winfusion.model.MenuModel;
import com.winfusion.utils.LaunchMode;
import com.winfusion.utils.ThemeUtils;
import com.winfusion.utils.UiUtils;

import java.nio.file.Path;
import java.util.ArrayList;

public class ShortcutPropertiesFragment extends Fragment {

    private FragmentShortcutPropertiesBinding binding;
    private ShortcutPropertiesFragmentArgs args;
    private CommonPropertyAdapter adapter;
    private ArrayList<CommonPropertyModel> sourceList;
    private MutableLiveData<String> containerName;
    private MutableLiveData<String> shortcutName;
    private Shortcut shortcut;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        args = ShortcutPropertiesFragmentArgs.fromBundle(getArguments());
        shortcut = ShortcutManager.getInstance().getShortcutByUUID(args.getShortcutUuid());
        if (shortcut == null)
            throw new IllegalArgumentException("Invalid shortcut: " + args.getShortcutUuid());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        binding = FragmentShortcutPropertiesBinding.inflate(inflater, container, false);
        MainActivityViewModel.setShowNavigation(requireActivity(), false);
        setupButtons();
        setupPropertyList();
        setupLiveData();
        updateShortcutName();
        updateShortcutIcon();
        binding.textTitle.setSelected(true);
        binding.textDescription.setSelected(true);
        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        adapter = null;
        sourceList = null;
        containerName = null;
        shortcutName = null;
    }

    private void setupButtons() {
        binding.buttonBack.setOnClickListener(v ->
                requireActivity().getOnBackPressedDispatcher().onBackPressed());
        binding.buttonShortcut.setOnClickListener(v -> {
            // TODO: 在桌面创建快捷方式
        });
        binding.buttonStart.setOnClickListener(v -> {
            // TODO: 启动当前快捷方式
        });
        binding.cardIcon.setOnClickListener(v -> showIconSelectMenu());
    }

    private void setupPropertyList() {
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(),
                getResources().getInteger(R.integer.shortcut_property_list_grid_columns));
        binding.listProperties.setLayoutManager(gridLayoutManager);
        setupModelList();
        adapter = new CommonPropertyAdapter(sourceList);
        binding.listProperties.setAdapter(adapter);
    }

    private void setupLiveData() {
        containerName = new MutableLiveData<>();
        containerName.observe(getViewLifecycleOwner(),
                s -> binding.textDescription.setText(s));
        shortcutName = new MutableLiveData<>();
        shortcutName.observe(getViewLifecycleOwner(),
                s -> binding.textTitle.setText(s));
    }

    private void updateShortcutName() {
        SettingWrapper wrapper = new SettingWrapper(shortcut.getConfig());
        containerName.setValue(wrapper.getContainerInfoName());
        shortcutName.setValue(wrapper.getShortcutInfoName());
    }

    private void updateShortcutIcon() {

    }

    private void updateIconByDefault() {
        binding.imageIcon.setColorFilter(ThemeUtils.getColorFromAttr(requireContext(),
                com.google.android.material.R.attr.colorOnSurface));

        binding.imageIcon.setImageResource(R.drawable.ic_settings_filled);
    }

    private void updateIconByBitmap(@NonNull Bitmap icon) {
        binding.imageIcon.clearColorFilter();
        binding.imageIcon.setImageBitmap(icon);
    }

    private void setupModelList() {
        sourceList = new ArrayList<>();

        // Shortcut -> Info
        sourceList.add(
                new CommonPropertyModel.Builder()
                        .setTitleId(R.string.info)
                        .setDescriptionId(R.string.shortcut_info_description)
                        .setIconId(R.drawable.ic_info)
                        .setClickTask(() -> {
                            ShortcutPropertiesFragmentDirections.ActionToShortcutInfo action =
                                    ShortcutPropertiesFragmentDirections.actionToShortcutInfo(
                                            args.getShortcutUuid(),
                                            true
                                    );
                            NavHostFragment.findNavController(ShortcutPropertiesFragment.this)
                                    .navigate(action);
                        })
                        .build()
        );

        // Shortcut -> Settings
        sourceList.add(
                new CommonPropertyModel.Builder()
                        .setTitleId(R.string.settings)
                        .setDescriptionId(R.string.shortcut_settings_description)
                        .setIconId(R.drawable.ic_settings_filled)
                        .setClickTask(() -> {
                            ShortcutPropertiesFragmentDirections.ActionToShortcutSettings action =
                                    ShortcutPropertiesFragmentDirections.actionToShortcutSettings(
                                            LaunchMode.Shortcut.name(),
                                            args.getShortcutUuid(),
                                            true
                                    );
                            NavHostFragment.findNavController(ShortcutPropertiesFragment.this)
                                    .navigate(action);
                        })
                        .build()
        );
    }

    private void showIconSelectMenu() {
        ArrayList<MenuModel> models = new ArrayList<>();
        models.add(new MenuModel(R.drawable.ic_photo_library, R.string.choose_icon_from_gallery,
                true, this::chooseIconFromGallery));
        models.add(new MenuModel(R.drawable.ic_build, R.string.extract_icon_from_target_exe_file,
                true, this::extractIconFromTargetExeFile));
        models.add(new MenuModel(R.drawable.ic_file_download, R.string.save_icon_to_storage,
                true, this::saveIconToStorage));
        MenuDialogFragment dialog = new MenuDialogFragment(models);
        dialog.setArguments(new MenuDialogFragmentArgs.Builder(R.string.icon).build().toBundle());
        dialog.show(getParentFragmentManager(), null);
    }

    private void chooseIconFromGallery() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        systemFilePickerLauncher.launch(intent);
    }

    private void extractIconFromTargetExeFile() {
        shortcut.getIconLoader().deleteIcon();
        // TODO: 实现图标加载
        //requestLoadIcon();
    }

    private void saveIconToStorage() {
        Path path = shortcut.getIconLoader().exportIcon();
        if (path != null)
            UiUtils.showShortToast(requireContext(), path.toString());
    }

    private final ActivityResultLauncher<Intent> systemFilePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Uri uri = result.getData().getData();
                    if (uri == null)
                        return;
//                    if (shortcut.getIconLoader().setIcon(uri, requireContext()))
//                        requestLoadIcon();
                }
            }
    );
}
