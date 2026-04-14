package com.winfusion.adapter;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.winfusion.databinding.CardHomeSettingBinding;
import com.winfusion.dialog.WrappedDialogFragment;
import com.winfusion.model.HomeSettingModel;

import java.util.ArrayList;

public class HomeSettingAdapter extends BaseListAdapter<HomeSettingModel,
        HomeSettingAdapter.HomeSettingViewHolder> {

    private final static float DISABLED_VIEW_ALPHA = 0.5f;

    public HomeSettingAdapter(@Nullable ArrayList<HomeSettingModel> sourceList) {
        super(sourceList);
    }

    @NonNull
    @Override
    public HomeSettingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        CardHomeSettingBinding binding = CardHomeSettingBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new HomeSettingViewHolder(binding);
    }

    public static class HomeSettingViewHolder extends BaseListAdapter.BaseViewHolder<HomeSettingModel> {

        private final CardHomeSettingBinding binding;

        public HomeSettingViewHolder(@NonNull CardHomeSettingBinding binding) {

            super(binding);
            this.binding = binding;

        }

        @Override
        public void bind(@NonNull HomeSettingModel model) {
            binding.textTitle.setText(model.getTitleId());
            binding.textDescription.setText(model.getDescriptionId());
            binding.imageIcon.setImageResource(model.getIconId());

            if (!model.isEnabled()) {
                binding.textTitle.setAlpha(DISABLED_VIEW_ALPHA);
                binding.textDescription.setAlpha(DISABLED_VIEW_ALPHA);
                binding.imageIcon.setAlpha(DISABLED_VIEW_ALPHA);
            }

            String details = model.getDetailsSupplier().get(model);
            if (details.isEmpty())
                binding.textDetails.setVisibility(View.GONE);
            else
                binding.textDetails.setVisibility(View.VISIBLE);
            binding.textDetails.setText(details);
            binding.textDetails.setSelected(true);

            binding.getRoot().setOnClickListener(v -> HomeSettingViewHolder.this.onClick(model));
        }

        private void onClick(HomeSettingModel model) {
            if (model.isEnabled())
                model.getClickTask().run();
            else if (model.getDisabledReasonId() != 0)
                showDisableReasonDialog(model);
        }

        private void showDisableReasonDialog(@NonNull HomeSettingModel model) {
            Context context = binding.getRoot().getContext();

            Dialog dialog = new MaterialAlertDialogBuilder(context)
                    .setMessage(model.getDisabledReasonId())
                    .setPositiveButton(android.R.string.ok, null)
                    .create();

            if (context instanceof FragmentActivity)
                new WrappedDialogFragment(dialog).show(
                        ((FragmentActivity) context).getSupportFragmentManager(), null);
            else
                dialog.show();
        }
    }
}
