package com.winfusion.feature.setting.viewholder;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.winfusion.feature.setting.model.Constants.FLAG_SHOW_DESCRIPTION;
import static com.winfusion.feature.setting.model.Constants.FLAG_SHOW_DETAILS;
import static com.winfusion.feature.setting.model.Constants.FLAG_SHOW_ICON;
import static com.winfusion.feature.setting.model.Constants.FLAG_SHOW_TITLE;
import static com.winfusion.feature.setting.model.Constants.INVALID_RES_ID;

import androidx.annotation.NonNull;

import com.winfusion.R;
import com.winfusion.databinding.ListItemSettingsCommonBinding;
import com.winfusion.feature.setting.model.BaseModel;

public class CommonViewHolder<T extends BaseModel>
        extends BaseViewHolder<T, ListItemSettingsCommonBinding> {

    public CommonViewHolder(@NonNull ListItemSettingsCommonBinding binding,
                            @NonNull ViewHolderCallback viewHolderCallback) {

        super(binding, viewHolderCallback);
    }

    @Override
    public void bind(@NonNull T model) {
        if (model.hasFlag(FLAG_SHOW_ICON) && model.getIconId() != INVALID_RES_ID) {
            binding.imageSettingIcon.setImageResource(model.getIconId());
            binding.imageSettingIcon.setVisibility(VISIBLE);
        } else {
            binding.imageSettingIcon.setVisibility(GONE);
        }

        if (model.hasFlag(FLAG_SHOW_TITLE) && model.getTitleId() != INVALID_RES_ID) {
            binding.textSettingTitle.setText(model.getTitleId());
            binding.textSettingTitle.setVisibility(VISIBLE);
        } else {
            binding.textSettingTitle.setVisibility(GONE);
        }

        if (model.hasFlag(FLAG_SHOW_DESCRIPTION) &&
                model.getDescriptionId() != INVALID_RES_ID) {
            binding.textSettingDescription.setText(model.getDescriptionId());
            binding.textSettingDescription.setVisibility(VISIBLE);
        } else {
            binding.textSettingDescription.setVisibility(GONE);
        }

        if (model.hasFlag(FLAG_SHOW_DETAILS))
            binding.textSettingDetails.setVisibility(VISIBLE);
        else
            binding.textSettingDetails.setVisibility(GONE);

        binding.switchWidget.setVisibility(GONE);
        binding.switchWidget.setClickable(false);

        binding.buttonClear.setVisibility(GONE);
        binding.buttonClear.setText(R.string.use_container_setting);

        binding.getRoot().setOnLongClickListener(v ->
                viewHolderCallback.onItemLongClick(getAdapterPosition()));
        binding.getRoot().setOnClickListener(v ->
                viewHolderCallback.onItemClick(getAdapterPosition()));
    }
}
