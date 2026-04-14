package com.winfusion.dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.winfusion.databinding.DialogMenuBinding;
import com.winfusion.databinding.ListItemMenuBinding;
import com.winfusion.model.MenuModel;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class MenuDialogFragment extends DialogFragment {

    private DialogMenuBinding binding;
    private MenuDialogFragmentArgs args;
    private final List<MenuModel> models;

    public MenuDialogFragment() {
        models = null;
    }

    public MenuDialogFragment(@NonNull List<MenuModel> models) {
        this.models = models;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        args = MenuDialogFragmentArgs.fromBundle(getArguments());

        if (models == null)
            dismissAllowingStateLoss();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        binding = DialogMenuBinding.inflate(getLayoutInflater());

        return new MaterialAlertDialogBuilder(requireContext())
                .setTitle(args.getTitleId())
                .setView(binding.getRoot())
                .setNegativeButton(android.R.string.cancel, null)
                .create();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        MenuAdapter adapter = new MenuAdapter(i -> {
            dismiss();
            Runnable runnable = Objects.requireNonNull(models).get(i).getRunnable();
            if (runnable != null)
                runnable.run();
        });

        binding.listMenu.setLayoutManager(new LinearLayoutManager(requireContext()));

        binding.listMenu.setAdapter(adapter);

        adapter.submitList(models);

        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private static class MenuAdapter extends ListAdapter<MenuModel, MenuAdapter.MenuViewHolder> {

        private final Consumer<Integer> itemViewClickCallback;

        public MenuAdapter(@NonNull Consumer<Integer> itemViewClickCallback) {
            super(new DiffCallback());

            this.itemViewClickCallback = itemViewClickCallback;
        }

        @NonNull
        @Override
        public MenuViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ListItemMenuBinding binding = ListItemMenuBinding.inflate(
                    LayoutInflater.from(parent.getContext()), parent, false);

            return new MenuViewHolder(binding, itemViewClickCallback);
        }

        @Override
        public void onBindViewHolder(@NonNull MenuViewHolder holder, int position) {
            holder.bind(getItem(position));
        }

        public static class DiffCallback extends DiffUtil.ItemCallback<MenuModel> {

            @Override
            public boolean areItemsTheSame(@NonNull MenuModel oldItem, @NonNull MenuModel newItem) {
                return false;
            }

            @Override
            public boolean areContentsTheSame(@NonNull MenuModel oldItem, @NonNull MenuModel newItem) {
                return false;
            }
        }

        private static class MenuViewHolder extends RecyclerView.ViewHolder {

            private final ListItemMenuBinding binding;
            private final Consumer<Integer> callback;

            public MenuViewHolder(@NonNull ListItemMenuBinding binding, @NonNull Consumer<Integer> callback) {
                super(binding.getRoot());
                this.binding = binding;
                this.callback = callback;
            }

            public void bind(@NonNull MenuModel model) {
                binding.imageIcon.setImageResource(model.getIconId());
                binding.textTitle.setText(model.getTitleId());
                binding.getRoot().setEnabled(model.isEnabled());
                binding.getRoot().setOnClickListener(v -> callback.accept(getAdapterPosition()));
            }
        }
    }
}
