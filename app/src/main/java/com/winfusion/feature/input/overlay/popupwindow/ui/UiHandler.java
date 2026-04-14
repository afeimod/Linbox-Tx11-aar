package com.winfusion.feature.input.overlay.popupwindow.ui;

import android.content.Context;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.viewbinding.ViewBinding;

import com.winfusion.feature.input.overlay.widget.BaseWidget;

public abstract class UiHandler<T extends ViewBinding> {

    protected final Runnable onConfigUpdatedCallback;
    protected T binding;
    protected BaseWidget<?> widget;

    public UiHandler(@NonNull Context context, @NonNull Runnable onConfigUpdatedCallback) {
        binding = onCreateView(context);
        this.onConfigUpdatedCallback = onConfigUpdatedCallback;
    }

    public View getView() {
        return binding.getRoot();
    }

    public void destroy() {
        binding = null;
    }

    public void update(@NonNull BaseWidget<?> widget) {
        this.widget = widget;
        onWidgetChanged();
    }

    protected abstract T onCreateView(@NonNull Context context);

    protected abstract void onWidgetChanged();

    protected void callConfigUpdate() {
        onConfigUpdatedCallback.run();
    }
}
