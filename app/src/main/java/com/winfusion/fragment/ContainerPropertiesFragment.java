package com.winfusion.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.MutableLiveData;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.GridLayoutManager;

import com.winfusion.R;
import com.winfusion.adapter.CommonPropertyAdapter;
import com.winfusion.databinding.FragmentContainerPropertiesBinding;
import com.winfusion.dialog.ConfirmByInputDialogFragment;
import com.winfusion.dialog.ConfirmByInputDialogFragmentArgs;
import com.winfusion.feature.manager.Container;
import com.winfusion.feature.manager.ContainerManager;
import com.winfusion.feature.setting.key.SettingWrapper;
import com.winfusion.model.CommonPropertyModel;
import com.winfusion.model.MainActivityViewModel;
import com.winfusion.utils.LaunchMode;
import com.winfusion.utils.UiUtils;

import java.util.ArrayList;

public class ContainerPropertiesFragment extends Fragment {

    private FragmentContainerPropertiesBinding binding;
    private ContainerPropertiesFragmentArgs args;
    private CommonPropertyAdapter adapter;
    private ArrayList<CommonPropertyModel> sourceList;
    private MutableLiveData<String> containerName;
    private Container container;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        args = ContainerPropertiesFragmentArgs.fromBundle(getArguments());
        container = ContainerManager.getInstance().getContainerByUUID(args.getContainerUuid());
        if (container == null)
            throw new IllegalArgumentException("Invalid container: " + args.getContainerUuid());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        binding = FragmentContainerPropertiesBinding.inflate(inflater, container, false);
        MainActivityViewModel.setShowNavigation(requireActivity(), false);

        setupButtons();
        setupPropertyList();
        setupLiveData();
        updateContainerName();
        updateContainerIcon();
        binding.textTitle.setSelected(true);
        UiUtils.hideExtendedFloatingActionButtonOnScroll(binding.listProperties, binding.buttonStart);

        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        adapter = null;
    }

    private void setupButtons() {
        binding.buttonBack.setOnClickListener(v ->
                requireActivity().getOnBackPressedDispatcher().onBackPressed());
        binding.buttonShortcut.setOnClickListener(v -> {
            // TODO: 在桌面创建快捷方式
        });
        binding.buttonStart.setOnClickListener(v -> {
            ContainerPropertiesFragmentDirections.ActionToWine action =
                    ContainerPropertiesFragmentDirections.actionToWine(LaunchMode.Container.name(),
                            args.getContainerUuid());
            NavHostFragment.findNavController(this).navigate(action);
        });
    }

    private void setupPropertyList() {
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(),
                getResources().getInteger(R.integer.container_property_list_grid_columns));
        binding.listProperties.setLayoutManager(gridLayoutManager);
        setupModelList();
        adapter = new CommonPropertyAdapter(sourceList);
        binding.listProperties.setAdapter(adapter);
    }

    private void setupLiveData() {
        containerName = new MutableLiveData<>("");
        containerName.observe(getViewLifecycleOwner(),
                s -> binding.textTitle.setText(s));
    }

    private void updateContainerName() {
        SettingWrapper wrapper = new SettingWrapper(container.getConfig());
        containerName.setValue(wrapper.getContainerInfoName());
    }

    private void updateContainerIcon() {
        binding.imageIcon.setImageResource(R.drawable.ic_settings_filled);
    }

    private void setupModelList() {
        sourceList = new ArrayList<>();

        // Container -> Info
        sourceList.add(
                new CommonPropertyModel.Builder()
                        .setTitleId(R.string.info)
                        .setDescriptionId(R.string.container_info_description)
                        .setIconId(R.drawable.ic_info)
                        .setClickTask(() -> {
                            ContainerPropertiesFragmentDirections.ActionToContainerInfo action =
                                    ContainerPropertiesFragmentDirections.actionToContainerInfo(
                                            args.getContainerUuid(),
                                            true
                                    );
                            NavHostFragment.findNavController(this).navigate(action);
                        })
                        .build()
        );

        // Container -> Setting
        sourceList.add(
                new CommonPropertyModel.Builder()
                        .setTitleId(R.string.settings)
                        .setDescriptionId(R.string.container_settings_description)
                        .setIconId(R.drawable.ic_settings_filled)
                        .setClickTask(() -> {
                            ContainerPropertiesFragmentDirections.ActionToContainerSettings action =
                                    ContainerPropertiesFragmentDirections.actionToContainerSettings(
                                            LaunchMode.Container.name(),
                                            args.getContainerUuid(),
                                            true
                                    );
                            NavHostFragment.findNavController(this).navigate(action);
                        })
                        .build()
        );

        // Container -> Controls overlay
        sourceList.add(
                new CommonPropertyModel.Builder()
                        .setTitleId(R.string.controls_overlay)
                        .setDescriptionId(R.string.cusom_overlay_description)
                        .setIconId(R.drawable.ic_gamepad)
                        .setClickTask(() -> {
                            ContainerPropertiesFragmentDirections.ActionToOverlayProfiles action =
                                    ContainerPropertiesFragmentDirections.actionToOverlayProfiles(
                                            args.getContainerUuid(),
                                            LaunchMode.Container.name()
                                    );
                            NavHostFragment.findNavController(this).navigate(action);
                        }).build()
        );

        // Container -> Registry editor
        sourceList.add(
                new CommonPropertyModel.Builder()
                        .setTitleId(R.string.registry_editor)
                        .setDescriptionId(R.string.registry_editor_description)
                        .setIconId(R.drawable.ic_registry)
                        .setClickTask(() -> {
                            ContainerPropertiesFragmentDirections.ActionToRegistryEditor action =
                                    ContainerPropertiesFragmentDirections.actionToRegistryEditor(
                                            args.getContainerUuid()
                                    );
                            NavHostFragment.findNavController(this).navigate(action);
                        })
                        .build()
        );

        // Container -> Open in file manager
        sourceList.add(
                new CommonPropertyModel.Builder()
                        .setTitleId(R.string.open_in_file_manager)
                        .setDescriptionId(R.string.open_in_file_manager_description)
                        .setIconId(R.drawable.ic_upload)
                        .setClickTask(() -> {

                        })
                        .build()
        );

        // Container -> Create shortcut
        sourceList.add(
                new CommonPropertyModel.Builder()
                        .setTitleId(R.string.create_shortcut)
                        .setDescriptionId(R.string.create_shortcut_description)
                        .setIconId(R.drawable.ic_shortcut)
                        .setClickTask(() -> {

                        })
                        .build()
        );

        // Container -> Clear cache
        sourceList.add(
                new CommonPropertyModel.Builder()
                        .setTitleId(R.string.clear_cache)
                        .setDescriptionId(R.string.clear_cache_description)
                        .setIconId(R.drawable.ic_cached)
                        .setClickTask(() -> {

                        })
                        .build()
        );

        // Container -> Duplicate
        sourceList.add(
                new CommonPropertyModel.Builder()
                        .setTitleId(R.string.duplicate)
                        .setDescriptionId(R.string.duplicate_container_description)
                        .setIconId(R.drawable.ic_content_copy)
                        .setClickTask(() -> {

                        })
                        .build()
        );

        // Container -> Delete
        sourceList.add(
                new CommonPropertyModel.Builder()
                        .setTitleId(R.string.delete)
                        .setDescriptionId(R.string.delete_container_description)
                        .setIconId(R.drawable.ic_delete_forever)
                        .setClickTask(() -> {
                            ConfirmByInputDialogFragment dialogFragment =
                                    new ConfirmByInputDialogFragment(result -> {
                                        if (!result)
                                            return;

                                        ContainerManager.getInstance().deleteContainer(container);
                                        requireActivity().getOnBackPressedDispatcher().onBackPressed();
                                    });
                            dialogFragment.setArguments(
                                    new ConfirmByInputDialogFragmentArgs.Builder(
                                            R.string.delete,
                                            R.string.delete_container_warning_description,
                                            containerName.getValue()
                                    ).build().toBundle()
                            );
                            dialogFragment.show(getParentFragmentManager(), null);
                        })
                        .build()
        );

//        // 动态更新列表的方式
//        sourceList.add(
//                new ContainerPropertyModel.Builder()
//                        .setTitleId(R.string.registry)
//                        .setDescriptionId(R.string.container_registry_description)
//                        .setIconId(R.drawable.ic_registry)
//                        .setClickTask(new Runnable() {
//                            final int position = sourceList.size();
//
//                            @Override
//                            public void run() {
//                                adapter.notifyItemChanged(position);
//                            }
//                        })
//                        .build()
//        );
    }
}
