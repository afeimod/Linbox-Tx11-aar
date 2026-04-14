package com.winfusion.feature.setting.viewholder;

import androidx.annotation.NonNull;

import com.winfusion.databinding.ListItemSettingsLabelBinding;
import com.winfusion.feature.setting.model.LabelModel;

public class LabelViewHolder extends BaseViewHolder<LabelModel, ListItemSettingsLabelBinding>{

    public LabelViewHolder(@NonNull ListItemSettingsLabelBinding binding,
                           @NonNull ViewHolderCallback viewHolderCallback) {

        super(binding, viewHolderCallback);
    }

    @Override
    public void bind(@NonNull LabelModel model) {
        binding.textLabel.setText(model.getLabelId());
        binding.textLabel.setSelected(true);
    }
}
