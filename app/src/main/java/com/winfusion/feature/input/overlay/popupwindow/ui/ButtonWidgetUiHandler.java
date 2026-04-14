package com.winfusion.feature.input.overlay.popupwindow.ui;

import static android.view.View.GONE;

import android.content.Context;
import android.view.LayoutInflater;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.winfusion.R;
import com.winfusion.databinding.DialogEditTextBinding;
import com.winfusion.databinding.LayoutButtonWidgetEditorBinding;
import com.winfusion.feature.input.overlay.bind.Binding;
import com.winfusion.feature.input.overlay.widget.ButtonWidget;
import com.winfusion.utils.NoFilterArrayAdapter;
import com.winfusion.utils.UiUtils;

public class ButtonWidgetUiHandler extends UiHandler<LayoutButtonWidgetEditorBinding> {

    private boolean fromUser = true;
    private final BindingUiHandler bindingUiHandler = new BindingUiHandler();

    public ButtonWidgetUiHandler(@NonNull Context context, @NonNull Runnable onConfigUpdatedCallback) {
        super(context, onConfigUpdatedCallback);
    }

    @Override
    protected LayoutButtonWidgetEditorBinding onCreateView(@NonNull Context context) {
        LayoutButtonWidgetEditorBinding b = LayoutButtonWidgetEditorBinding.inflate(
                LayoutInflater.from(context));
        setupUi(b);
        return b;
    }

    @Override
    protected void onWidgetChanged() {
        updateUi();
    }

    private void setupUi(@NonNull LayoutButtonWidgetEditorBinding binding) {
        Context context = binding.getRoot().getContext();

        binding.autoTextShape.setAdapter(new NoFilterArrayAdapter<>(
                context,
                android.R.layout.simple_dropdown_item_1line,
                context.getResources().getStringArray(R.array.button_widget_shape_values)
        ));

        binding.autoTextShape.setOnItemClickListener((parent, view, position, id) -> {
            String shapeName = context.getResources().getStringArray(R.array.button_widget_shape_values)[position];
            getWidget().getConfig().shape = ButtonWidget.Shape.valueOf(shapeName);
            callConfigUpdate();
        });

        binding.switchToggle.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!fromUser)
                return;

            ButtonWidget widget = getWidget();
            widget.getConfig().toggleSwitch = isChecked;
            callConfigUpdate();
        });

        binding.editText.setOnClickListener(v -> {
            ButtonWidget widget = getWidget();
            DialogEditTextBinding b2 = DialogEditTextBinding.inflate(LayoutInflater.from(context));
            b2.buttonCopy.setVisibility(GONE);
            b2.editText.setText(widget.getConfig().text);
            b2.editText.requestFocus();
            new MaterialAlertDialogBuilder(context)
                    .setTitle(R.string.text)
                    .setPositiveButton(android.R.string.ok, ((dialog, which) -> {
                        CharSequence text = b2.editText.getText();
                        if (text == null)
                            return;
                        binding.editText.setText(text.toString());
                        widget.getConfig().text = text.toString();
                        callConfigUpdate();
                    }))
                    .setNegativeButton(android.R.string.cancel, null)
                    .setView(b2.getRoot())
                    .create()
                    .show();
        });
    }

    private void updateUi() {
        ButtonWidget.Config config = getWidget().getConfig();
        fromUser = false;
        binding.autoTextShape.setText(UiUtils.getEntryByValue(
                binding.getRoot().getContext(),
                config.shape.name(),
                R.array.button_widget_shape_entries,
                R.array.button_widget_shape_values)
        );
        binding.switchToggle.setChecked(config.toggleSwitch);
        binding.editText.setText(config.text);
        bindingUiHandler.update(binding.bind1, buildBindingProvider(0));
        bindingUiHandler.update(binding.bind2, buildBindingProvider(1));
        bindingUiHandler.update(binding.bind3, buildBindingProvider(2));
        bindingUiHandler.update(binding.bind4, buildBindingProvider(3));
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
    private ButtonWidget getWidget() {
        return (ButtonWidget) super.widget;
    }
}
