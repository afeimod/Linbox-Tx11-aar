package com.winfusion.feature.setting.model.action;

import androidx.annotation.NonNull;
import androidx.navigation.NavDirections;

/**
 * 适用于目标为 {@link NavDirections} 的动作类。
 * 用于使用 Nav 打开一个 Fragment。
 */
public class DirectionAction extends BaseAction {

    private final NavDirections directions;

    /**
     * 构造函数。
     *
     * @param directions 导航对象。
     */
    public DirectionAction(@NonNull NavDirections directions) {
        this.directions = directions;
    }

    /**
     * 获取导航对象。
     *
     * @return 导航对象
     */
    @NonNull
    public NavDirections getDirections() {
        return directions;
    }
}
