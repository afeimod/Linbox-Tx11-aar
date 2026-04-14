package com.winfusion.core.eventbus;

import androidx.annotation.NonNull;

import java.util.HashSet;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * 事件总线类，提供订阅者机制。
 */
public class EventBus {

    private static final BaseEvent POISON_PILL_EVENT = new BaseEvent() {
        // PoisonPillEvent
    };

    private final LinkedBlockingDeque<BaseEvent> mQueue = new LinkedBlockingDeque<>();
    private final WeakHashMap<ISubscriber, Set<Class<? extends BaseEvent>>> mMap = new WeakHashMap<>();
    private final Object lock = new Object();
    private CompletableFuture<Void> mFuture;

    /**
     * 注册订阅者。
     *
     * @param subscriber 订阅者对象
     * @param eventTypes 订阅的事件类型
     * @return 如果成功则返回 true，否则返回 false
     */
    @SafeVarargs
    public final boolean register(@NonNull ISubscriber subscriber,
                                  @NonNull Class<? extends BaseEvent>... eventTypes) {

        synchronized (lock) {
            if (eventTypes.length == 0)
                return false;

            var eventTypeSets = mMap.computeIfAbsent(subscriber, k -> new HashSet<>());
            boolean ret = true;

            for (Class<? extends BaseEvent> eventType : eventTypes) {
                if (!eventTypeSets.add(eventType))
                    ret = false;
            }
            return ret;
        }
    }

    /**
     * 反注册订阅者。
     *
     * @param subscriber 订阅者对象
     * @return 如果成功则返回 true，否则返回 false
     */
    public boolean unregister(@NonNull ISubscriber subscriber) {
        synchronized (lock) {
            return mMap.remove(subscriber) != null;
        }
    }

    /**
     * 反注册订阅者。
     *
     * @param subscriber 订阅者对象
     * @param eventTypes 反注册的事件类型
     * @return 如果成功则返回 true，否则返回 false
     */
    @SafeVarargs
    public final boolean unregister(@NonNull ISubscriber subscriber, @NonNull Class<? extends BaseEvent>... eventTypes) {
        var eventTypeSets = mMap.get(subscriber);
        boolean ret = true;

        if (eventTypeSets == null || eventTypes.length == 0)
            return false;

        synchronized (lock) {
            for (Class<? extends BaseEvent> eventType : eventTypes) {
                if (!eventTypeSets.remove(eventType))
                    ret = false;
            }

            if (eventTypeSets.isEmpty())
                mMap.remove(subscriber);
        }

        return ret;
    }

    /**
     * 发布一个事件。
     *
     * @param event 事件
     * @return 如果成功则返回 true，否则返回 false
     */
    public boolean post(@NonNull BaseEvent event) {
        try {
            if (event.getPriority() == EventPriority.HIGH)
                mQueue.putFirst(event);
            else
                mQueue.put(event);
        } catch (InterruptedException e) {
            return false;
        }
        return true;
    }

    /**
     * 停止事件总线。
     * 向事件总线发送终止事件。
     */
    public void stop() {
        try {
            mQueue.put(POISON_PILL_EVENT);
        } catch (InterruptedException e) {
            synchronized (lock) {
                internalDestroy();
            }
        }
    }

    /**
     * 启动事件总线
     */
    public void start() {
        mFuture = startEventBusFuture();
    }

    private void dispatchEvent(@NonNull BaseEvent event) {
        ISubscriber subscriber;
        Set<Class<? extends BaseEvent>> sets;
        boolean ret;

        for (var entry : mMap.entrySet()) {
            subscriber = entry.getKey();
            sets = entry.getValue();

            if (sets.contains(event.getClass())) {
                ret = subscriber.onEvent(event);
                if (ret) {
                    event.handled();
                    if (event.isConsumeOnce())
                        break;
                }
            }
        }
    }

    private void internalDestroy() {
        mQueue.clear();
        mMap.clear();
        mFuture = null;
    }

    @NonNull
    private CompletableFuture<Void> startEventBusFuture() {
        return CompletableFuture.runAsync(() -> {
            BaseEvent currentEvent;
            while (true) {
                try {
                    currentEvent = mQueue.take();
                } catch (InterruptedException e) {
                    break;
                }
                synchronized (lock) {
                    if (currentEvent == POISON_PILL_EVENT) {
                        internalDestroy();
                        break;
                    }

                    dispatchEvent(currentEvent);
                    // 这里显式的将当前事件置为 null
                    // 因为 take() 会将当前线程阻塞，有可能阻止它的内存回收
                    currentEvent = null;
                }
            }
        });
    }
}
