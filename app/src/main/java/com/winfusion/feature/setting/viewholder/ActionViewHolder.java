package com.winfusion.feature.setting.viewholder;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import static com.winfusion.feature.setting.model.Constants.FLAG_SHOW_DETAILS;

import android.view.View;

import androidx.annotation.NonNull;

import com.winfusion.databinding.ListItemSettingsCommonBinding;
import com.winfusion.feature.setting.model.ActionModel;
import com.winfusion.feature.setting.model.SavableModel;
import com.winfusion.feature.setting.model.SubModel;

public class ActionViewHolder extends CommonViewHolder<ActionModel> {

    public ActionViewHolder(@NonNull ListItemSettingsCommonBinding binding,
                            @NonNull ViewHolderCallback viewHolderCallback) {

        super(binding, viewHolderCallback);
    }

    @Override
    public void bind(@NonNull ActionModel model) {
        super.bind(model);
        if (model.hasFlag(FLAG_SHOW_DETAILS) && model.getSubModel() != null) {
            binding.textSettingDetails.setVisibility(VISIBLE);
            binding.textSettingDetails.setText(
                    model.getDetailsFormatter().getFormattedValue(model.getSubModel().getValue()));
        } else {
            binding.textSettingDescription.setVisibility(GONE);
        }
        SavableModel subModel = model.getSubModel();
        if (subModel != null) {
            binding.buttonClear.setVisibility(subModel.canFollowed() ? VISIBLE : GONE);
            binding.buttonClear.setOnClickListener(v ->
                    viewHolderCallback.onItemClearButtonClick(getAdapterPosition()));
        }
    }
}
