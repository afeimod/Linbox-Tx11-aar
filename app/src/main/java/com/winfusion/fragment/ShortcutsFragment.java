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
import com.winfusion.adapter.ShortcutAdapter;
import com.winfusion.databinding.FragmentShortcutsBinding;
import com.winfusion.feature.manager.Shortcut;
import com.winfusion.feature.manager.ShortcutManager;
import com.winfusion.model.MainActivityViewModel;
import com.winfusion.model.ShortcutModel;

import java.util.ArrayList;

public class ShortcutsFragment extends Fragment {

    private FragmentShortcutsBinding binding;
    private ShortcutAdapter shortcutAdapter;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentShortcutsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        MainActivityViewModel.updateRootViewToFitNavigationBar(root);
        MainActivityViewModel.setShowNavigation(requireActivity(), true);

        setupShortcutList();
        binding.swipeRefresh.setOnRefreshListener(() -> {
            updateList();
            binding.swipeRefresh.setRefreshing(false);
        });

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
        shortcutAdapter = null;
    }

    private void setupShortcutList() {
        GridLayoutManager gridLayoutManager = new GridLayoutManager(requireContext(),
                getResources().getInteger(R.integer.shortcut_list_grid_columns));
        binding.listShortcuts.setLayoutManager(gridLayoutManager);
        shortcutAdapter = new ShortcutAdapter(new ArrayList<>(), new ShortcutAdapter.ShortcutCallback() {

            @Override
            public void startShortcut(@NonNull ShortcutModel model) {
                // TODO: 启动快捷方式
            }

            @Override
            public void editShortcut(@NonNull ShortcutModel model) {
                ShortcutsFragmentDirections.ActionToShortcutProperties action =
                        ShortcutsFragmentDirections.actionToShortcutProperties(
                                model.getShortcut().getUUID());
                NavHostFragment.findNavController(ShortcutsFragment.this).navigate(action);
            }

            @Override
            public void requireIcon(@NonNull ShortcutModel model) {
                // TODO: 请求图标
            }
        });

        binding.listShortcuts.setAdapter(shortcutAdapter);
    }

    private void updateList() {
        ShortcutManager.getInstance().refreshShortcuts();
        ArrayList<ShortcutModel> models = new ArrayList<>();
        for (Shortcut shortcut : ShortcutManager.getInstance().getShortcuts())
            models.add(new ShortcutModel(shortcut));

        shortcutAdapter.replaceList(models);
        if (models.isEmpty()) {
            binding.textNotice.setVisibility(VISIBLE);
            binding.listShortcuts.setVisibility(GONE);
        } else {
            binding.textNotice.setVisibility(GONE);
            binding.listShortcuts.setVisibility(VISIBLE);
        }
    }
}
