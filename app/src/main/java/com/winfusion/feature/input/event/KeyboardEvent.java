package com.winfusion.feature.input.event;

import androidx.annotation.NonNull;

import com.winfusion.feature.input.key.StandardKey;

public class KeyboardEvent extends AbstractInputEvent {

    private final KeyState state;
    private final StandardKey key;

    public KeyboardEvent(@NonNull KeyState state, @NonNull StandardKey key) {
        this.state = state;
        this.key = key;
    }

    @NonNull
    public KeyState getState() {
        return state;
    }

    @NonNull
    public StandardKey getKey() {
        return key;
    }

    @NonNull
    @Override
    public String toString() {
        return "[KeyboardEvent] state:" + state.name() + " key:" + key.name();
    }
}
