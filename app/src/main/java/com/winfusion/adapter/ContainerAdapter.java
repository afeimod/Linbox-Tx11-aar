package com.winfusion.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.winfusion.R;
import com.winfusion.databinding.CardContainerBinding;
import com.winfusion.model.ContainerModel;

import java.util.ArrayList;

public class ContainerAdapter extends BaseListAdapter<ContainerModel, ContainerAdapter.ContainerViewHolder> {

    private final ContainerCallback containerCallback;

    public ContainerAdapter(@Nullable ArrayList<ContainerModel> sourceList,
                            @NonNull ContainerCallback containerCallback) {

        super(sourceList);
        this.containerCallback = containerCallback;
    }

    @NonNull
    @Override
    public ContainerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        CardContainerBinding binding = CardContainerBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ContainerViewHolder(binding, containerCallback);
    }

    public static class ContainerViewHolder extends BaseListAdapter.BaseViewHolder<ContainerModel> {

        private final CardContainerBinding binding;
        private final ContainerCallback containerCallback;

        public ContainerViewHolder(@NonNull CardContainerBinding binding,
                                   @NonNull ContainerCallback containerCallback) {

            super(binding);
            this.binding = binding;
            this.containerCallback = containerCallback;
        }

        @Override
        public void bind(@NonNull ContainerModel model) {
            binding.textTitle.setText(model.getName());
            binding.textTitle.setSelected(true);

            binding.imageIcon.setImageResource(R.drawable.ic_settings_filled);

            binding.cardMain.setOnClickListener(v -> containerCallback.startContainer(model));

            binding.cardMain.setOnLongClickListener(v -> {
                containerCallback.editContainer(model);
                return true;
            });
        }
    }

    public interface ContainerCallback {

        void startContainer(@NonNull ContainerModel model);

        void editContainer(@NonNull ContainerModel model);
    }
}
