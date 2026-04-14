package com.winfusion.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.winfusion.databinding.CardSimpleOutlinedBinding;
import com.winfusion.model.CommonPropertyModel;

import java.util.ArrayList;

public class CommonPropertyAdapter extends BaseListAdapter<CommonPropertyModel,
        CommonPropertyAdapter.ContainerPropertyViewHolder> {

    public CommonPropertyAdapter(@Nullable ArrayList<CommonPropertyModel> sourceList) {
        super(sourceList);
    }

    @NonNull
    @Override
    public ContainerPropertyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        CardSimpleOutlinedBinding binding = CardSimpleOutlinedBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ContainerPropertyViewHolder(binding);
    }

    public static class ContainerPropertyViewHolder
            extends BaseListAdapter.BaseViewHolder<CommonPropertyModel> {

        private final CardSimpleOutlinedBinding binding;

        public ContainerPropertyViewHolder(@NonNull CardSimpleOutlinedBinding binding) {
            super(binding);
            this.binding = binding;
        }

        @Override
        public void bind(@NonNull CommonPropertyModel model) {
            binding.textTitle.setText(model.getTitleId());
            binding.textDescription.setText(model.getDescriptionId());
            binding.imageIcon.setImageResource(model.getIconId());

            String details = model.getDetailsSupplier().get(model);
            if (details.isEmpty())
                binding.textDetails.setVisibility(View.GONE);
            else
                binding.textDetails.setVisibility(View.VISIBLE);
            binding.textDetails.setText(details);
            binding.textDetails.setSelected(true);

            binding.getRoot().setOnClickListener(v -> model.getClickTask().run());
        }
    }
}
