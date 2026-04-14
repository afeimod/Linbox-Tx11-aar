package com.winfusion.feature.input.event;

public abstract class AbstractControllerEvent extends AbstractInputEvent {

    public static final int WinfusionVirtualControllerId = 0;

    protected final int id;

    public AbstractControllerEvent(int id) {
        this.id = id;
    }

    public AbstractControllerEvent() {
        this.id = WinfusionVirtualControllerId;
    }

    public int getId() {
        return id;
    }
}
