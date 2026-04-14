package com.winfusion.feature.input.overlay.utils;

import androidx.annotation.NonNull;

import com.winfusion.feature.input.overlay.OverlayProfile;
import com.winfusion.feature.input.overlay.widget.BaseWidget;
import com.winfusion.feature.input.overlay.widget.ButtonWidget;
import com.winfusion.feature.input.overlay.widget.DPadWidget;
import com.winfusion.feature.input.overlay.widget.ThumbStickWidget;
import com.winfusion.feature.input.overlay.widget.WidgetProvider;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * 覆盖层构建器，帮助构建控件或控件的配置信息。
 */
public final class OverlayBuilder {

    private OverlayBuilder() {

    }

    /**
     * 从控件配置信息构建控件。
     *
     * @param profile  覆盖层配置文件对象
     * @param provider 控件提供器接口
     * @return 控件的集合
     */
    @NonNull
    public static List<BaseWidget<?>> buildWidgets(@NonNull OverlayProfile profile,
                                                   @NonNull WidgetProvider provider) {

        ArrayList<BaseWidget<?>> widgets = new ArrayList<>();
        Collection<BaseWidget.Config> configs = profile.getConfigs();
        for (BaseWidget.Config config : configs)
            widgets.add(buildWidget(config, provider));
        return widgets;
    }

    /**
     * 从控件构建控件配置信息。
     *
     * @param widgets 控件的集合
     * @return 控件配置信息的集合
     */
    @NonNull
    public static Collection<BaseWidget.Config> buildConfigs(
            @NonNull Collection<BaseWidget<?>> widgets) {

        ArrayList<BaseWidget.Config> configs = new ArrayList<>();
        for (BaseWidget<?> widget : widgets)
            configs.add(widget.getConfig());
        return configs;
    }

    private static BaseWidget<?> buildWidget(@NonNull BaseWidget.Config config,
                                             @NonNull WidgetProvider provider) {

        if (config instanceof ButtonWidget.Config buttonConfig)
            return new ButtonWidget(provider, buttonConfig);
        else if (config instanceof DPadWidget.Config dpadConfig)
            return new DPadWidget(provider, dpadConfig);
        else if (config instanceof ThumbStickWidget.Config thumbStickConfig)
            return new ThumbStickWidget(provider, thumbStickConfig);
        else
            throw new IllegalArgumentException("Unsupported widget: " + config.getClass());
    }
}
