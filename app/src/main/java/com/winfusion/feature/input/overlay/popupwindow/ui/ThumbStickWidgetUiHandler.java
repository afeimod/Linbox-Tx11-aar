package com.winfusion.feature.input.overlay.popupwindow.ui;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.content.Context;
import android.view.LayoutInflater;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.winfusion.R;
import com.winfusion.databinding.LayoutStickWidgetEditorBinding;
import com.winfusion.feature.input.overlay.bind.Binding;
import com.winfusion.feature.input.overlay.widget.ThumbStickWidget;
import com.winfusion.utils.NoFilterArrayAdapter;
import com.winfusion.utils.UiUtils;

import java.util.Arrays;

public class ThumbStickWidgetUiHandler extends UiHandler<LayoutStickWidgetEditorBinding> {

    private final BindingUiHandler bindingUiHandler = new BindingUiHandler();
    private boolean fromUser = true;

    public ThumbStickWidgetUiHandler(@NonNull Context context, @NonNull Runnable onConfigUpdatedCallback) {
        super(context, onConfigUpdatedCallback);
    }

    @Override
    protected LayoutStickWidgetEditorBinding onCreateView(@NonNull Context context) {
        LayoutStickWidgetEditorBinding b = LayoutStickWidgetEditorBinding.inflate(
                LayoutInflater.from(context));
        setupUi(b);
        return b;
    }

    @Override
    protected void onWidgetChanged() {
        updateUi();
    }

    private void setupUi(@NonNull LayoutStickWidgetEditorBinding binding) {
        Context context = binding.getRoot().getContext();

        binding.autoTextMode.setAdapter(new NoFilterArrayAdapter<>(
                context,
                android.R.layout.simple_dropdown_item_1line,
                context.getResources().getStringArray(R.array.thumb_stick_widget_binding_mode_entries)
        ));

        binding.autoTextMode.setOnItemClickListener((parent, view, position, id) -> {
            String modeName = context.getResources().getStringArray(
                    R.array.thumb_stick_widget_binding_mode_values)[position];
            ThumbStickWidget.Mode mode = ThumbStickWidget.Mode.valueOf(modeName);
            if (mode == ThumbStickWidget.Mode.Mapping) {
                binding.groupDirBinding.setVisibility(VISIBLE);
                binding.groupInputInvert.setVisibility(GONE);
            } else {
                binding.groupDirBinding.setVisibility(GONE);
                binding.groupInputInvert.setVisibility(VISIBLE);
                Arrays.fill(getWidget().getConfig().bindings, null);
            }
            getWidget().getConfig().mode = mode;
            callConfigUpdate();
        });

        binding.switch8way.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!fromUser)
                return;
            getWidget().getConfig().enable8Way = isChecked;
        });

        binding.switchInvertX.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!fromUser)
                return;
            getWidget().getConfig().invertX = isChecked;
        });

        binding.switchInvertY.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!fromUser)
                return;
            getWidget().getConfig().invertY = isChecked;
        });
    }

    private void updateUi() {
        ThumbStickWidget.Config config = getWidget().getConfig();
        fromUser = false;
        binding.autoTextMode.setText(UiUtils.getEntryByValue(
                binding.getRoot().getContext(),
                config.mode.name(),
                R.array.thumb_stick_widget_binding_mode_entries,
                R.array.thumb_stick_widget_binding_mode_values)
        );
        binding.groupDirBinding.setVisibility(config.mode == ThumbStickWidget.Mode.Mapping ?
                VISIBLE : GONE);
        binding.groupInputInvert.setVisibility(config.mode == ThumbStickWidget.Mode.Mapping ?
                GONE : VISIBLE);
        bindingUiHandler.update(binding.bindUp, buildBindingProvider(0));
        bindingUiHandler.update(binding.bindDown, buildBindingProvider(1));
        bindingUiHandler.update(binding.bindLeft, buildBindingProvider(2));
        bindingUiHandler.update(binding.bindRight, buildBindingProvider(3));
        binding.switch8way.setChecked(config.enable8Way);
        binding.switchInvertX.setChecked(config.invertX);
        binding.switchInvertY.setChecked(config.invertY);
        fromUser = true;
    }

    private BindingUiHandler.BindingProvider buildBindingProvider(int index) {
        return new BindingUiHandler.BindingProvider() {
            @Nullable
            @Override
            public Binding getBinding() {
                return getWidget().getConfig().bindings[index];
            }

            @Override
            public void updateBinding(@Nullable Binding binding) {
                getWidget().getConfig().bindings[index] = binding;
                callConfigUpdate();
            }
        };
    }

    @NonNull
    private ThumbStickWidget getWidget() {
        return (ThumbStickWidget) super.widget;
    }
}
