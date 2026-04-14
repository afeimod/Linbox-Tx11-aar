package com.winfusion.adapter;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;

import com.winfusion.adapter.common.ItemCallback;
import com.winfusion.adapter.common.SelectableListAdapter;
import com.winfusion.databinding.CardOverlayProfileBinding;
import com.winfusion.model.OverlayProfileModel;

import java.util.Objects;

public class OverlayProfileAdapter extends SelectableListAdapter<OverlayProfileModel,
        OverlayProfileAdapter.OverlayProfileViewHolder> {

    public OverlayProfileAdapter(@NonNull ItemCallback itemCallback) {
        super(new DiffCallback(), itemCallback);
    }

    @NonNull
    @Override
    public OverlayProfileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        CardOverlayProfileBinding binding = CardOverlayProfileBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);

        return new OverlayProfileViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull OverlayProfileViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    protected static class DiffCallback extends DiffUtil.ItemCallback<OverlayProfileModel> {

        @Override
        public boolean areItemsTheSame(@NonNull OverlayProfileModel oldItem,
                                       @NonNull OverlayProfileModel newItem) {

            boolean a = Objects.equals(oldItem.getOverlay().getFileName(),
                    newItem.getOverlay().getFileName());
            boolean b = Objects.equals(oldItem.getOverlay().getProfile().getName(),
                    newItem.getOverlay().getProfile().getName());
            return a && b;
        }

        @Override
        public boolean areContentsTheSame(@NonNull OverlayProfileModel oldItem,
                                          @NonNull OverlayProfileModel newItem) {

            return Objects.equals(oldItem.getOverlay().getProfile().getName(),
                    newItem.getOverlay().getProfile().getName());
        }
    }

    public class OverlayProfileViewHolder extends SelectableListAdapter.ViewHolder<OverlayProfileModel,
            CardOverlayProfileBinding> {

        private boolean fromUser = true;

        public OverlayProfileViewHolder(@NonNull CardOverlayProfileBinding binding) {
            super(binding, OverlayProfileAdapter.this.itemCallback);
        }

        @Override
        protected void setup(@NonNull OverlayProfileModel model) {
            binding.getRoot().setOnClickListener(v -> binding.radioSelect.performClick());
            binding.getRoot().setOnLongClickListener(v -> itemCallback.onLongClick(getAdapterPosition()));
            binding.radioSelect.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (fromUser && isChecked)
                    internalSetSelectedPosition(getAdapterPosition());
            });
        }

        @Override
        public void update(@NonNull OverlayProfileModel model) {
            binding.textTitle.setText(model.getOverlay().getProfile().getName());
            binding.radioSelect.setVisibility(model.isSelectable() ? VISIBLE : GONE);
            fromUser = false;
            binding.radioSelect.setChecked(getSelectedPosition() == getAdapterPosition());
            fromUser = true;
        }
    }
}
