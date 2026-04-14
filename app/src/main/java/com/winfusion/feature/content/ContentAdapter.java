package com.winfusion.feature.content;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;

import com.winfusion.R;
import com.winfusion.adapter.common.ItemCallback;
import com.winfusion.adapter.common.SelectableListAdapter;
import com.winfusion.databinding.CardContentBinding;
import com.winfusion.feature.content.model.BaseContentModel;
import com.winfusion.feature.content.model.SoundfontModel;
import com.winfusion.feature.content.model.WfpModel;

public class ContentAdapter extends SelectableListAdapter<BaseContentModel, ContentAdapter.ContentViewHolder> {

    protected ContentAdapter(@NonNull ItemCallback itemCallback) {
        super(new DiffCallback(), itemCallback);
    }

    @NonNull
    @Override
    public ContentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        CardContentBinding binding = CardContentBinding.inflate(LayoutInflater.from(parent.getContext()),
                parent, false);

        return new ContentViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ContentViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    protected static class DiffCallback extends DiffUtil.ItemCallback<BaseContentModel> {

        @Override
        public boolean areItemsTheSame(@NonNull BaseContentModel oldItem,
                                       @NonNull BaseContentModel newItem) {

            return oldItem == newItem;
        }

        @SuppressLint("DiffUtilEquals")
        @Override
        public boolean areContentsTheSame(@NonNull BaseContentModel oldItem,
                                          @NonNull BaseContentModel newItem) {

            return oldItem == newItem;
        }
    }

    public class ContentViewHolder extends SelectableListAdapter.ViewHolder<BaseContentModel, CardContentBinding> {

        private boolean fromUser = true;

        public ContentViewHolder(@NonNull CardContentBinding binding) {
            super(binding, ContentAdapter.this.itemCallback);
        }

        @Override
        protected void setup(@NonNull BaseContentModel model) {
            binding.getRoot().setOnClickListener(v -> binding.radioSelect.performClick());
            binding.getRoot().setOnLongClickListener(v -> itemCallback.onLongClick(getAdapterPosition()));
            binding.radioSelect.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (fromUser && isChecked)
                    internalSetSelectedPosition(getAdapterPosition());
            });
        }

        @Override
        protected void update(@NonNull BaseContentModel model) {
            binding.textTitle.setText(model.getTitle());
            updateSelectStatus(model);
            updateComment(model);
            updateLabel(model);
            updateIcon(model);
        }

        private void updateSelectStatus(@NonNull BaseContentModel model) {
            binding.radioSelect.setVisibility(model.isSelectable() ? VISIBLE : GONE);
            fromUser = false;
            binding.radioSelect.setChecked(getSelectedPosition() == getAdapterPosition());
            fromUser = true;
        }

        private void updateComment(@NonNull BaseContentModel model) {
            String description = null;

            if (model instanceof WfpModel wfpModel)
                description = wfpModel.getWfp().getComment();
            else if (model instanceof SoundfontModel soundfontModel)
                description = soundfontModel.getInfo().getName();

            if (description != null)
                binding.textDescription.setText(description);
        }

        private void updateIcon(@NonNull BaseContentModel model) {
            int iconId = 0;

            if (model instanceof WfpModel wfpModel) {
                iconId = switch (wfpModel.getWfp().getWfpType()) {
                    case Wine -> R.drawable.ic_winehq;
                    case Box64 -> R.drawable.ic_box64;
                    case DXVK, VKD3D, WineD3D -> R.drawable.ic_build;
                    case MesaTurnip, MesaVenus, MesaWrapper, MesaZink, MesaVirGL ->
                            R.drawable.ic_mesa;
                    case Unknown -> 0;
                };
            } else if (model instanceof SoundfontModel soundfontModel) {
                iconId = R.drawable.ic_piano;
            }

            binding.imageIcon.setImageResource(iconId);
        }

        private void updateLabel(@NonNull BaseContentModel model) {
            if (model instanceof WfpModel wfpModel)
                binding.textLabel.setText(wfpModel.getWfp().getWfpType().name());
            else if (model instanceof SoundfontModel)
                binding.textLabel.setText(R.string.soundfont);
        }
    }
}
