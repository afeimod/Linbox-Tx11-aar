package com.winfusion.fragment;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.GridLayoutManager;

import com.winfusion.R;
import com.winfusion.adapter.ContainerAdapter;
import com.winfusion.databinding.FragmentContainersBinding;
import com.winfusion.dialog.EditTextDialogFragment;
import com.winfusion.dialog.EditTextDialogFragmentArgs;
import com.winfusion.feature.manager.Container;
import com.winfusion.feature.manager.ContainerManager;
import com.winfusion.model.ContainerModel;
import com.winfusion.model.MainActivityViewModel;
import com.winfusion.utils.UiUtils;

import java.util.ArrayList;

public class ContainersFragment extends Fragment {

    private FragmentContainersBinding binding;
    private ContainerAdapter containerAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        binding = FragmentContainersBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        MainActivityViewModel.updateRootViewToFitNavigationBar(root);
        MainActivityViewModel.setShowNavigation(requireActivity(), true);

        setupContainerList();
        binding.swipeRefresh.setOnRefreshListener(() -> {
            updateList();
            binding.swipeRefresh.setRefreshing(false);
        });
        binding.buttonCreate.setOnClickListener(v -> showCreateContainerDialog());

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        updateList();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        containerAdapter = null;
    }

    private void setupContainerList() {
        GridLayoutManager gridLayoutManager = new GridLayoutManager(requireContext(),
                getResources().getInteger(R.integer.container_list_grid_columns));
        binding.listContainers.setLayoutManager(gridLayoutManager);
        containerAdapter = new ContainerAdapter(new ArrayList<>(), new ContainerAdapter.ContainerCallback() {
            @Override
            public void startContainer(@NonNull ContainerModel model) {
                // TODO: 启动当前容器
            }

            @Override
            public void editContainer(@NonNull ContainerModel model) {
                ContainersFragmentDirections.ActionToContainerProperties action =
                        ContainersFragmentDirections.actionToContainerProperties(
                                model.getContainer().getUUID());

                NavHostFragment.findNavController(ContainersFragment.this).navigate(action);
            }
        });
        binding.listContainers.setAdapter(containerAdapter);
        UiUtils.hideExtendedFloatingActionButtonOnScroll(binding.listContainers, binding.buttonCreate);
    }

    private void updateList() {
        ContainerManager.getInstance().refreshContainers();
        ArrayList<ContainerModel> models = new ArrayList<>();
        for (Container container : ContainerManager.getInstance().getContainers())
            models.add(new ContainerModel(container));

        containerAdapter.replaceList(models);
        if (models.isEmpty()) {
            binding.textNotice.setVisibility(VISIBLE);
            binding.listContainers.setVisibility(GONE);
        } else {
            binding.textNotice.setVisibility(GONE);
            binding.listContainers.setVisibility(VISIBLE);
        }
    }

    private void showCreateContainerDialog() {
        EditTextDialogFragment dialogFragment = new EditTextDialogFragment(
                result -> {
                    binding.swipeRefresh.setRefreshing(true);
                    ContainerManager.getInstance().createContainer(result);
                    updateList();
                    binding.swipeRefresh.setRefreshing(false);
                }, null);

        dialogFragment.setArguments(new EditTextDialogFragmentArgs.Builder(R.string.container_name,
                getString(R.string.untitled_container)).build().toBundle());

        dialogFragment.show(getParentFragmentManager(), null);
    }
}
