package com.winfusion.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;

import com.winfusion.adapter.common.ClickableListAdapter;
import com.winfusion.adapter.common.ItemCallback;
import com.winfusion.databinding.ListItemRegistryItemBinding;
import com.winfusion.model.RegistryItemModel;

import java.util.Objects;

public class RegistryItemAdapter
        extends ClickableListAdapter<RegistryItemModel, RegistryItemAdapter.RegistryItemViewHolder> {

    public RegistryItemAdapter(@NonNull ItemCallback itemCallback) {
        super(new DiffCallback(), itemCallback);
    }

    @NonNull
    @Override
    public RegistryItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ListItemRegistryItemBinding binding = ListItemRegistryItemBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);

        return new RegistryItemViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull RegistryItemViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    protected static class DiffCallback extends DiffUtil.ItemCallback<RegistryItemModel> {

        @Override
        public boolean areItemsTheSame(@NonNull RegistryItemModel oldItem,
                                       @NonNull RegistryItemModel newItem) {

            return Objects.equals(oldItem.getItemKey(), newItem.getItemKey());
        }

        @Override
        public boolean areContentsTheSame(@NonNull RegistryItemModel oldItem,
                                          @NonNull RegistryItemModel newItem) {

            return Objects.equals(oldItem.getItemKey(), newItem.getItemKey()) &&
                    Objects.equals(oldItem.getItemName(), newItem.getItemName()) &&
                    Objects.equals(oldItem.getAction(), newItem.getAction());
        }
    }

    public class RegistryItemViewHolder extends ClickableListAdapter.ViewHolder<RegistryItemModel,
            ListItemRegistryItemBinding> {

        public RegistryItemViewHolder(@NonNull ListItemRegistryItemBinding binding) {

            super(binding, RegistryItemAdapter.this.itemCallback);
        }

        @Override
        protected void setup(@NonNull RegistryItemModel model) {
            binding.getRoot().setOnClickListener(v ->
                    itemCallback.onClick(getAdapterPosition()));

            binding.getRoot().setOnLongClickListener(v ->
                    itemCallback.onLongClick(getAdapterPosition()));
        }

        @Override
        protected void update(@NonNull RegistryItemModel model) {
            binding.textItemName.setText(model.getItemName());
            binding.textItemName.setSelected(true);
        }
    }
}
