package com.winfusion.feature.input.overlay.bind;

import androidx.annotation.NonNull;

import com.winfusion.feature.input.key.StandardItem;

public class Binding {

    public enum Type {
        Keyboard,
        MouseButton,
        MouseAction,
        ControllerButton,
        ControllerAction
    }

    private final Type type;
    private final StandardItem item;
    private final float value; // for MouseAction and ControllerAction TODO: ControllerAction not implemented yet

    public Binding(@NonNull Type type, @NonNull StandardItem item) {
        this.type = type;
        this.item = item;
        value = 0;
    }

    public Binding(@NonNull Type type, @NonNull StandardItem item, float value) {
        this.type = type;
        this.item = item;
        this.value = value;
    }

    @NonNull
    public Type getType() {
        return type;
    }

    @NonNull
    public StandardItem getItem() {
        return item;
    }

    public float getValue() {
        return value;
    }
}
