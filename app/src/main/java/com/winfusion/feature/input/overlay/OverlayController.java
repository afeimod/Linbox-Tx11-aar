package com.winfusion.feature.input.overlay;

import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.winfusion.R;
import com.winfusion.feature.input.event.ControllerAxisEvent;
import com.winfusion.feature.input.event.ControllerButtonEvent;
import com.winfusion.feature.input.event.KeyboardEvent;
import com.winfusion.feature.input.event.MouseButtonEvent;
import com.winfusion.feature.input.event.MousePointerEvent;
import com.winfusion.feature.input.event.MouseScrollEvent;
import com.winfusion.feature.input.interfaces.InputInterface;
import com.winfusion.feature.input.overlay.popupwindow.OverlayToolbarPopupWindow;
import com.winfusion.feature.input.overlay.popupwindow.WidgetEditorPopupWindow;
import com.winfusion.feature.input.overlay.utils.OverlayBuilder;
import com.winfusion.feature.input.overlay.widget.BaseWidget;
import com.winfusion.feature.input.overlay.widget.ButtonWidget;
import com.winfusion.feature.input.overlay.widget.DPadWidget;
import com.winfusion.feature.input.overlay.widget.ThumbStickWidget;
import com.winfusion.feature.input.overlay.widget.TouchpadWidget;
import com.winfusion.feature.input.overlay.widget.WidgetProvider;
import com.winfusion.feature.input.overlay.widget.WidgetType;
import com.winfusion.utils.UiUtils;

import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.Collections;

/**
 * 覆盖层控制器，提供从外部控制覆盖层的功能。
 */
public class OverlayController implements WidgetProvider {

    private static final InputInterface DEFAULT_INPUT_INTERFACE = new EmptyInputInterface();

    private final View overlayView;
    private InputInterface inputInterface;
    private Paint boundsPaint;
    private int baseWidth = 0;
    private int baseHeight = 0;
    private int baseSize = 0;
    private Status status = Status.Control;
    private OverlayProfile profile = new OverlayProfile();
    private Collection<BaseWidget<?>> widgets = Collections.emptyList();
    private final TouchpadWidget touchpadWidget;
    private WeakReference<BaseWidget<?>> selectedWidgetReference = new WeakReference<>(null);
    private OverlayToolbarPopupWindow toolbar;
    private WidgetEditorPopupWindow editor;
    private boolean widgetChanged = false;

    public OverlayController(@NonNull View overlayView) {
        this.overlayView = overlayView;
        touchpadWidget = new TouchpadWidget(this, new TouchpadWidget.TouchpadConfig());
        setupBoundsPaint();
        setupToolbar();
        setupEditor();
    }

    @Override
    public int getBaseWidth() {
        return baseWidth;
    }

    @Override
    public int getBaseHeight() {
        return baseHeight;
    }

    @Override
    public int getBaseSize() {
        return baseSize;
    }

    @NonNull
    @Override
    public Status getStatus() {
        return status;
    }

    @NonNull
    @Override
    public Paint getBoundsPaint() {
        return boundsPaint;
    }

    @NonNull
    @Override
    public InputInterface getInputInterface() {
        return inputInterface == null ? DEFAULT_INPUT_INTERFACE : inputInterface;
    }

    @Override
    public void setWidgetChanged() {
        widgetChanged = true;
    }

    @Override
    public void setSelectedWidget(@Nullable BaseWidget<?> widget) {
        BaseWidget<?> lastSelectedWidget = selectedWidgetReference.get();
        if (lastSelectedWidget != null)
            lastSelectedWidget.setSelected(false);

        if (widget != null)
            widget.setSelected(true);

        if (status == Status.Edit)
            updateEditor(widget);

        selectedWidgetReference = new WeakReference<>(widget);
    }

    @Nullable
    @Override
    public BaseWidget<?> getSelectedWidget() {
        return selectedWidgetReference.get();
    }

    /**
     * 更新屏幕大小，单位为像素。
     *
     * @param width  屏幕宽度
     * @param height 屏幕高度
     */
    public void updateScreenSize(int width, int height) {
        baseWidth = width;
        baseHeight = height;
        baseSize = Math.min(height, width);
        notifyWidgetsConfigUpdated();
        overlayView.invalidate();
    }

    /**
     * 更新状态。
     *
     * @param status 状态
     */
    public void updateStatus(@NonNull Status status) {
        this.status = status;
        if (status == Status.Edit)
            toolbar.showAtLastLocation(overlayView, 0, 0);
        else if (status == Status.Control) {
            toolbar.dismiss();
            editor.dismiss();
        } else if (status == Status.Preview) {
            toolbar.showAtLastLocation(overlayView, 0, 0);
            editor.dismiss();
        }
    }

    /**
     * 设定覆盖层配置文件。
     *
     * @param profile 配置文件对象
     */
    public void setProfile(@NonNull OverlayProfile profile) {
        this.profile = profile;
        toolbar.setProfileName(profile.getName());
        widgets = OverlayBuilder.buildWidgets(profile, this);
        overlayView.invalidate();
    }

    /**
     * 从当前状态生成覆盖层配置文件。
     *
     * @return 配置文件对象
     */
    @NonNull
    public OverlayProfile toProfile() {
        OverlayProfile p = new OverlayProfile();
        p.setConfigs(OverlayBuilder.buildConfigs(widgets));
        p.setName(profile.getName());
        return p;
    }

    /**
     * 通知全部控件配置发生更新。
     */
    public void notifyWidgetsConfigUpdated() {
        for (BaseWidget<?> widget : widgets)
            widget.notifyConfigUpdated();
    }

    /**
     * 获取全部控件。
     *
     * @return 全部控件的集合
     */
    public Collection<BaseWidget<?>> getWidgets() {
        return widgets;
    }

    /**
     * 获取触摸板控件。
     *
     * @return 触摸板控件
     */
    public TouchpadWidget getTouchpad() {
        return touchpadWidget;
    }

    /**
     * 设定输入接口。
     *
     * @param inputInterface 输入接口对象
     */
    public void setInputInterface(@Nullable InputInterface inputInterface) {
        this.inputInterface = inputInterface;
    }

    /**
     * 判断控件是否发生了更改。
     *
     * @return 如果发生更改则返回 true，否则返回 false
     */
    public boolean isWidgetChanged() {
        return widgetChanged;
    }

    /**
     * 销毁控制器。
     */
    public void destroy() {
        toolbar.destroy();
        editor.destroy();
    }

    private void setupBoundsPaint() {
        boundsPaint = new Paint();
        boundsPaint.setColor(Color.RED);
        boundsPaint.setStyle(Paint.Style.STROKE);
        boundsPaint.setStrokeWidth(UiUtils.dpToPx(overlayView.getContext(), 1));
    }

    private void setupToolbar() {
        toolbar = new OverlayToolbarPopupWindow(overlayView.getContext());
        toolbar.setToolbarCallback(new OverlayToolbarPopupWindow.Callback() {

            @Override
            public void onCreatingWidget() {
                BaseWidget<?> widget = new ButtonWidget(OverlayController.this,
                        new ButtonWidget.Config());
                widget.getConfig().normalizedX = 0.5f;
                widget.getConfig().normalizedY = 0.5f;
                widgets.add(widget);
                setSelectedWidget(widget);
                overlayView.invalidate();
            }

            @Override
            public void onDeletingSelectedWidget() {
                BaseWidget<?> widget = selectedWidgetReference.get();
                if (widget == null)
                    return;

                new MaterialAlertDialogBuilder(overlayView.getContext())
                        .setTitle(R.string.delete)
                        .setMessage(R.string.delete_widget_description)
                        .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                            setSelectedWidget(null);
                            widgets.remove(widget);
                            overlayView.invalidate();
                        })
                        .setNegativeButton(android.R.string.cancel, null)
                        .create()
                        .show();
            }

            @Override
            public void onStatusUpdate(@NonNull Status status) {
                updateStatus(status);
                overlayView.invalidate();
            }
        });
    }

    private void setupEditor() {
        editor = new WidgetEditorPopupWindow(overlayView.getContext());
        editor.setEditorCallback(new WidgetEditorPopupWindow.Callback() {
            @Override
            public void onWidgetConfigUpdated() {
                overlayView.invalidate();
            }

            @Override
            public void onWidgetTypeChanged(@NonNull WidgetType type) {
                BaseWidget<?> selectedWidget = selectedWidgetReference.get();
                if (selectedWidget == null)
                    throw new IllegalStateException("Selected widget must not be null");

                BaseWidget<?> newWidget;
                if (type == WidgetType.Button) {
                    ButtonWidget.Config config = new ButtonWidget.Config();
                    config.set(selectedWidget.getConfig());
                    newWidget = new ButtonWidget(OverlayController.this, config);
                } else if (type == WidgetType.DPad) {
                    DPadWidget.Config config = new DPadWidget.Config();
                    config.set(selectedWidget.getConfig());
                    newWidget = new DPadWidget(OverlayController.this, config);
                } else if (type == WidgetType.ThumbStick) {
                    ThumbStickWidget.Config config = new ThumbStickWidget.Config();
                    config.set(selectedWidget.getConfig());
                    newWidget = new ThumbStickWidget(OverlayController.this, config);
                } else {
                    throw new IllegalArgumentException("Unsupported widget: " + selectedWidget.getClass());
                }

                widgets.remove(selectedWidget);
                widgets.add(newWidget);
                setSelectedWidget(newWidget);
                overlayView.invalidate();
            }
        });
    }

    private void updateEditor(@Nullable BaseWidget<?> widget) {
        if (widget == null)
            editor.dismiss();
        else
            editor.showAtLastLocation(overlayView, Short.MAX_VALUE, 0);
        editor.setWidget(widget);
    }

    private static class EmptyInputInterface implements InputInterface {

        @Override
        public void onControllerAxisEvent(@NonNull ControllerAxisEvent event) {
            // do nothing
        }

        @Override
        public void onControllerButtonEvent(@NonNull ControllerButtonEvent event) {
            // do nothing
        }

        @Override
        public void onKeyboardEvent(@NonNull KeyboardEvent event) {
            // do nothing
        }

        @Override
        public void onMousePointerEvent(@NonNull MousePointerEvent event) {
            // do nothing
        }

        @Override
        public void onMouseButtonEvent(@NonNull MouseButtonEvent event) {
            // do nothing
        }

        @Override
        public void onMouseScrollEvent(@NonNull MouseScrollEvent event) {
            // do nothing
        }
    }
}
