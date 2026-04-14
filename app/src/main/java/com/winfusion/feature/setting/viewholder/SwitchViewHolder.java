package com.winfusion.feature.setting.viewholder;

import static android.view.View.VISIBLE;

import androidx.annotation.NonNull;

import com.winfusion.databinding.ListItemSettingsCommonBinding;
import com.winfusion.feature.setting.model.SwitchModel;

public class SwitchViewHolder extends CommonViewHolder<SwitchModel>{

    private boolean fromUser = true;

    public SwitchViewHolder(@NonNull ListItemSettingsCommonBinding binding,
                            @NonNull ViewHolderCallback viewHolderCallback) {

        super(binding, viewHolderCallback);
    }

    @Override
    public void bind(@NonNull SwitchModel model) {
        super.bind(model);
        binding.switchWidget.setVisibility(VISIBLE);
        fromUser = false;
        binding.switchWidget.setChecked(model.getValue().getAsBool());
        binding.switchWidget.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (fromUser)
                model.setValue(model.getElementCreator().create(isChecked));
        });
        fromUser = true;
        binding.getRoot().setOnClickListener(v -> {
            binding.switchWidget.performClick();
            viewHolderCallback.onItemClick(getAdapterPosition());
        });
    }
}
