package com.winfusion.adapter;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.winfusion.R;
import com.winfusion.databinding.CardShortcutBinding;
import com.winfusion.model.ShortcutModel;

import java.util.ArrayList;

public class ShortcutAdapter extends BaseListAdapter<ShortcutModel, ShortcutAdapter.ShortcutViewHolder> {

    private final ShortcutCallback shortcutCallback;

    public ShortcutAdapter(@Nullable ArrayList<ShortcutModel> sourceList,
                           @NonNull ShortcutCallback shortcutCallback) {

        super(sourceList);
        this.shortcutCallback = shortcutCallback;
    }

    @NonNull
    @Override
    public ShortcutViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        CardShortcutBinding binding = CardShortcutBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);

        return new ShortcutViewHolder(binding);
    }

    public class ShortcutViewHolder extends BaseListAdapter.BaseViewHolder<ShortcutModel> {

        private final CardShortcutBinding binding;

        public ShortcutViewHolder(@NonNull CardShortcutBinding binding) {

            super(binding);
            this.binding = binding;
        }

        @SuppressLint("ResourceType")
        @Override
        public void bind(@NonNull ShortcutModel model) {
            binding.textTitle.setText(model.getShortcutName());
            binding.textTitle.setSelected(true);
            binding.textDescription.setText(model.getContainerName());

            // TODO: 实现图标加载
            binding.imageIcon.setImageResource(R.drawable.ic_settings_filled);

//            Bitmap icon = ShortcutManager.getInstance().getIconLoader().getIcon(model.getShortcutUUID());
//            if (icon == null) {
//                binding.imageIcon.setImageResource(R.drawable.ic_settings_filled);
//                shortcutCallback.requireIcon(model);
//            } else {
//                binding.imageIcon.setImageBitmap(icon);
//            }

            binding.cardMain.setOnClickListener(v -> shortcutCallback.startShortcut(model));

            binding.cardMain.setOnLongClickListener(v -> {
                shortcutCallback.editShortcut(model);
                return true;
            });
        }
    }

    public interface ShortcutCallback {

        void startShortcut(@NonNull ShortcutModel model);

        void editShortcut(@NonNull ShortcutModel model);

        void requireIcon(@NonNull ShortcutModel model);
    }
}
