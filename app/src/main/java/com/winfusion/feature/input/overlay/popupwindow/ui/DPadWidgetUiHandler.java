package com.winfusion.feature.input.overlay.popupwindow.ui;

import android.content.Context;
import android.view.LayoutInflater;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.winfusion.databinding.LayoutDpadWidgetEditorBinding;
import com.winfusion.feature.input.overlay.bind.Binding;
import com.winfusion.feature.input.overlay.widget.DPadWidget;

public class DPadWidgetUiHandler extends UiHandler<LayoutDpadWidgetEditorBinding> {

    private final BindingUiHandler bindingUiHandler = new BindingUiHandler();
    private boolean fromUser = true;

    public DPadWidgetUiHandler(@NonNull Context context, @NonNull Runnable onConfigUpdatedCallback) {
        super(context, onConfigUpdatedCallback);
    }

    @Override
    protected LayoutDpadWidgetEditorBinding onCreateView(@NonNull Context context) {
        LayoutDpadWidgetEditorBinding b = LayoutDpadWidgetEditorBinding.inflate(
                LayoutInflater.from(context));
        setupUi(b);
        return b;
    }

    @Override
    protected void onWidgetChanged() {
        updateUi();
    }

    private void setupUi(@NonNull LayoutDpadWidgetEditorBinding binding) {
        binding.switch8way.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!fromUser)
                return;
            getWidget().getConfig().enable8Way = isChecked;
        });
    }

    private void updateUi() {
        fromUser = false;
        binding.switch8way.setChecked(getWidget().getConfig().enable8Way);
        bindingUiHandler.update(binding.bindUp, buildBindingProvider(0));
        bindingUiHandler.update(binding.bindDown, buildBindingProvider(1));
        bindingUiHandler.update(binding.bindLeft, buildBindingProvider(2));
        bindingUiHandler.update(binding.bindRight, buildBindingProvider(3));
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
    private DPadWidget getWidget() {
        return (DPadWidget) super.widget;
    }
}
