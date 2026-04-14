package com.winfusion.core.wfp;

import androidx.annotation.NonNull;

import java.util.Objects;

public enum WfpType {

    Wine("wine"),
    Box64("box64"),

    // directx wrapper
    DXVK("dxvk"),
    VKD3D("vkd3d"),
    WineD3D("wined3d"),

    // vulkan
    MesaTurnip("mesa_turnip"),
    MesaVenus("mesa_venus"),
    MesaWrapper("mesa_wrapper"),

    // opengl
    MesaZink("mesa_zink"),
    MesaVirGL("mesa_virgl"),

    Unknown("");

    private final String id;

    WfpType(@NonNull String id) {
        this.id = id;
    }

    @NonNull
    public String getId() {
        return id;
    }

    @NonNull
    public static WfpType fromId(@NonNull String id) {
        WfpType[] types = WfpType.values();
        for (WfpType type : types) {
            if (Objects.equals(type.id, id))
                return type;
        }
        return Unknown;
    }
}
