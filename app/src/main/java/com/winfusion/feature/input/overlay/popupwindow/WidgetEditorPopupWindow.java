package com.winfusion.feature.input.overlay.popupwindow;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.winfusion.R;
import com.winfusion.databinding.LayoutWidgetEditorBinding;
import com.winfusion.feature.input.overlay.popupwindow.ui.ButtonWidgetUiHandler;
import com.winfusion.feature.input.overlay.popupwindow.ui.DPadWidgetUiHandler;
import com.winfusion.feature.input.overlay.popupwindow.ui.ThumbStickWidgetUiHandler;
import com.winfusion.feature.input.overlay.popupwindow.ui.UiHandler;
import com.winfusion.feature.input.overlay.widget.BaseWidget;
import com.winfusion.feature.input.overlay.widget.ButtonWidget;
import com.winfusion.feature.input.overlay.widget.DPadWidget;
import com.winfusion.feature.input.overlay.widget.ThumbStickWidget;
import com.winfusion.feature.input.overlay.widget.WidgetType;
import com.winfusion.utils.NoFilterArrayAdapter;
import com.winfusion.utils.UiUtils;

public class WidgetEditorPopupWindow extends FloatHeaderPopupWindow {

    private static final int DefaultAlpha = (int) (255 * 0.8f);

    private LayoutWidgetEditorBinding binding;
    private BaseWidget<?> widget;
    private Callback callback;
    private UiHandler<?> buttonUiHandler;
    private UiHandler<?> dpadUiHandler;
    private UiHandler<?> thumbStickUiHandler;
    private boolean fromUser = true;

    public WidgetEditorPopupWindow(@NonNull Context context) {
        super(context);
        setExpandBtnEnabled(true);
        setCloseBtnEnabled(true);
        setAlpha(DefaultAlpha);
        setOutsideTouchable(false);
        setTitle(R.string.widget_editor);
        setupUiHandlers();
    }

    public void setWidget(@Nullable BaseWidget<?> widget) {
        this.widget = widget;
        if (widget != null)
            updateUi();
    }

    public void setEditorCallback(@Nullable Callback callback) {
        this.callback = callback;
    }

    @SuppressLint("SetTextI18n")
    @NonNull
    @Override
    protected View onCreateView() {
        binding = LayoutWidgetEditorBinding.inflate(LayoutInflater.from(getContext()));

        binding.autoTextType.setAdapter(new NoFilterArrayAdapter<>(
                getContext(),
                android.R.layout.simple_dropdown_item_1line,
                getContext().getResources().getStringArray(R.array.overlay_widget_type_entries)
        ));
        binding.autoTextType.setOnItemClickListener((parent, view, position, id) -> {
            String typeName = getContext().getResources().getStringArray(R.array.overlay_widget_type_values)[position];
            WidgetType type = WidgetType.valueOf(typeName);
            changeWidgetType(type);
        });

        binding.sliderScale.addOnChangeListener((slider, value, fromUser) -> {
            if (widget == null || !fromUser)
                return;
            widget.getConfig().scale = value / 100;
            binding.textScale.setText((int) value + "%");
            updateWidget();
        });

        binding.sliderOpacity.addOnChangeListener((slider, value, fromUser) -> {
            if (widget == null || !fromUser)
                return;
            widget.getConfig().opacity = value / 100;
            binding.textOpacity.setText((int) value + "%");
            updateWidget();
        });

        binding.switchHide.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!fromUser)
                return;
            widget.getConfig().hide = isChecked;
            updateWidget();
        });

        return binding.getRoot();
    }

    @Override
    protected void onDestroyView() {
        binding = null;
    }

    private void setupUiHandlers() {
        buttonUiHandler = new ButtonWidgetUiHandler(getContext(), this::updateWidget);
        dpadUiHandler = new DPadWidgetUiHandler(getContext(), this::updateWidget);
        thumbStickUiHandler = new ThumbStickWidgetUiHandler(getContext(), this::updateWidget);
    }

    @SuppressLint("SetTextI18n")
    private void updateUi() {
        if (binding == null)
            return;

        BaseWidget.Config config = widget.getConfig();

        fromUser = false;
        binding.autoTextType.setText(UiUtils.getEntryByValue(getContext(), getWidgetType().name(),
                R.array.overlay_widget_type_entries, R.array.overlay_widget_type_values));

        float scaleValue = Math.clamp(config.scale * 100,
                binding.sliderScale.getValueFrom(), binding.sliderScale.getValueTo());
        binding.sliderScale.setValue(scaleValue);
        binding.textScale.setText((int) scaleValue + "%");

        float opacityValue = Math.clamp(config.opacity * 100,
                binding.sliderOpacity.getValueFrom(), binding.sliderOpacity.getValueTo());
        binding.sliderOpacity.setValue(opacityValue);
        binding.textOpacity.setText((int) opacityValue + "%");

        binding.switchHide.setChecked(config.hide);
        fromUser = true;

        changeUiHandler(getWidgetType());
    }

    private void updateWidget() {
        if (widget != null)
            widget.notifyConfigUpdated();
        if (callback != null)
            callback.onWidgetConfigUpdated();
    }

    private void changeWidgetType(@NonNull WidgetType type) {
        if (callback != null)
            callback.onWidgetTypeChanged(type);

        changeUiHandler(type);
    }

    @NonNull
    private WidgetType getWidgetType() {
        if (widget instanceof ButtonWidget)
            return WidgetType.Button;
        else if (widget instanceof DPadWidget)
            return WidgetType.DPad;
        else if (widget instanceof ThumbStickWidget)
            return WidgetType.ThumbStick;

        throw new IllegalArgumentException("Unsupported widget: " + widget.getClass());
    }

    private void changeUiHandler(@NonNull WidgetType type) {
        binding.container.removeAllViews();
        UiHandler<?> handler;

        if (type == WidgetType.Button)
            handler = buttonUiHandler;
        else if (type == WidgetType.DPad)
            handler = dpadUiHandler;
        else if (type == WidgetType.ThumbStick)
            handler = thumbStickUiHandler;
        else
            return;

        handler.update(widget);
        binding.container.addView(handler.getView());
    }

    public interface Callback {

        void onWidgetConfigUpdated();

        void onWidgetTypeChanged(@NonNull WidgetType type);
    }
}
