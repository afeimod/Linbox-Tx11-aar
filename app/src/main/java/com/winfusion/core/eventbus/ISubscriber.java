package com.winfusion.core.eventbus;

public interface ISubscriber {

    boolean onEvent(BaseEvent event);
}
