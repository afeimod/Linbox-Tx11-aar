package com.winfusion.feature.setting;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavDirections;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.winfusion.R;
import com.winfusion.databinding.FragmentSettingsBinding;
import com.winfusion.feature.manager.Container;
import com.winfusion.feature.manager.ContainerManager;
import com.winfusion.feature.manager.Shortcut;
import com.winfusion.feature.manager.ShortcutManager;
import com.winfusion.feature.setting.adapter.SettingAdapter;
import com.winfusion.feature.setting.adapter.SettingAdapterAgent;
import com.winfusion.feature.setting.key.SettingWrapper;
import com.winfusion.feature.setting.provider.SettingsProvider;
import com.winfusion.utils.LaunchMode;

import java.util.Deque;
import java.util.LinkedList;

public abstract class SettingsFragment extends Fragment {

    private FragmentSettingsBinding binding;
    private SettingsFragmentViewModel viewModel;
    private SettingAdapter adapter;
    private SettingAdapterAgent agent;
    private LinearLayoutManager layoutManager;
    protected Container container;
    protected Shortcut shortcut;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(SettingsFragmentViewModel.class);
        if (viewModel.backStack == null)
            viewModel.backStack = new LinkedList<>();
        switch (getMode()) {
            case Container ->
                    container = ContainerManager.getInstance().getContainerByUUID(getUUID());
            case Shortcut -> shortcut = ShortcutManager.getInstance().getShortcutByUUID(getUUID());
            case Standalone -> {
                // do nothing
            }
        }
        setupAgent();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        setupSettingList();
        setupBackPress();
        setupAppbar();
        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        int position = viewModel.lastItemPosition;
        if (position >= 0 && position < adapter.getItemCount())
            adapter.notifyItemChanged(position);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        adapter = null;
        layoutManager = null;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (isSaveOnDetached()) {
            switch (getMode()) {
                case Container -> container.saveConfig();
                case Shortcut -> shortcut.saveConfig();
                case Standalone -> {
                    // do nothing
                }
            }
        }
        viewModel.lastItemPosition = 0;
        viewModel.backStack = null;
    }

    protected Config getConfig() {
        return switch (getMode()) {
            case Container -> container.getConfig();
            case Shortcut -> shortcut.getConfig();
            case Standalone -> throw new IllegalArgumentException("Not support standalone.");
        };
    }

    @NonNull
    protected abstract SettingsProvider getProvider();

    @NonNull
    protected abstract String getUUID();

    @NonNull
    protected abstract LaunchMode getMode();

    protected abstract boolean isSaveOnDetached();

    private void setupBackPress() {
        requireActivity().getOnBackPressedDispatcher().addCallback(
                getViewLifecycleOwner(),
                new OnBackPressedCallback(true) {

                    @Override
                    public void handleOnBackPressed() {
                        if (!adapter.onBackPressed()) {
                            setEnabled(false);
                            NavHostFragment
                                    .findNavController(SettingsFragment.this)
                                    .popBackStack();
                        }
                    }
                }
        );
    }

    private void setupAgent() {
        agent = new SettingAdapterAgent() {
            @NonNull
            @Override
            public Deque<SettingAdapter.BackModel> getBackStack() {
                return viewModel.backStack;
            }

            @NonNull
            @Override
            public ViewPosition getViewPosition() {
                int position = layoutManager.findFirstVisibleItemPosition();
                View firstVisbleView = layoutManager.findViewByPosition(position);
                return new ViewPosition(position, firstVisbleView == null ? 0 : firstVisbleView.getTop());
            }

            @Override
            public void setViewPosition(@NonNull ViewPosition position) {
                layoutManager.scrollToPositionWithOffset(position.position(), position.offset());
            }

            @Override
            public void showDialogFragment(@NonNull DialogFragment dialog) {
                dialog.show(getParentFragmentManager(), null);
            }

            @Override
            public void toActivity(@NonNull Class<? extends Activity> activityClass,
                                   @NonNull Bundle bundle, int adapterPosition) {

                viewModel.lastItemPosition = adapterPosition;
                Intent intent = new Intent(requireActivity(), activityClass);
                intent.putExtras(bundle);
                startActivity(intent);
            }

            @Override
            public void toDirection(@NonNull NavDirections directions, int adapterPosition) {
                NavHostFragment.findNavController(SettingsFragment.this)
                        .navigate(directions);
            }

            @Override
            public void toFragment(int fragmentId, @Nullable Bundle bundle, int adapterPosition) {
                NavHostFragment.findNavController(SettingsFragment.this)
                        .navigate(fragmentId, bundle);
            }

            @Override
            public void updateTitle(int titleId) {
                binding.layoutAppbar.textTitle.setText(titleId);
            }
        };
    }

    private void setupSettingList() {
        layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        binding.listSettings.setLayoutManager(layoutManager);
        adapter = new SettingAdapter(getProvider().models(), getProvider().root().getLabelKey(), agent);
        binding.listSettings.setAdapter(adapter);
    }

    private void setupAppbar() {
        String description = switch (getMode()) {
            case Container -> {
                if (container == null)
                    throw new IllegalArgumentException("Invalid container: " + getUUID());
                yield new SettingWrapper(container.getConfig()).getContainerInfoName();
            }
            case Shortcut -> {
                if (shortcut == null)
                    throw new IllegalArgumentException("Invalid shortcut: " + getUUID());
                yield new SettingWrapper(shortcut.getConfig()).getShortcutInfoName();

            }
            case Standalone -> "";
        };

        binding.layoutAppbar.toolbar.setOnClickListener(v ->
                requireActivity().getOnBackPressedDispatcher().onBackPressed());
        binding.layoutAppbar.textDescription.setText(description);
    }

    protected static class SettingsFragmentViewModel extends ViewModel {

        private Deque<SettingAdapter.BackModel> backStack;
        private int lastItemPosition;
    }
}
