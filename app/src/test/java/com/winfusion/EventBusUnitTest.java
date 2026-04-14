package com.winfusion;

import com.winfusion.core.eventbus.BaseEvent;
import com.winfusion.core.eventbus.EventBus;
import com.winfusion.core.eventbus.EventPriority;
import com.winfusion.core.eventbus.ISubscriber;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Objects;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class EventBusUnitTest {

    private static final long POST_WAIT_TIME = 100;

    private EventBus mEventBus;

    @Before
    public void setUp() {
        mEventBus = null;

        try {
            mEventBus = new EventBus();
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }

        mEventBus.start();
    }

    @After
    public void tearDown() {
        mEventBus.stop();
    }

    @Test
    public void test_eventbus_register() {
        ISubscriber subscriber1 = new MySubscriber1();
        ISubscriber subscriber2 = new MySubscriber2();
        ISubscriber subscriber3 = new MySubscriber3();

        // Test register event
        Assert.assertTrue(mEventBus.register(subscriber1, MyEvent1.class));
        Assert.assertTrue(mEventBus.register(subscriber2, MyEvent2.class));
        Assert.assertTrue(mEventBus.register(subscriber3, MyEvent1.class, MyEvent2.class));

        // Test register with duplicated event
        Assert.assertFalse(mEventBus.register(subscriber1, MyEvent1.class));
        Assert.assertFalse(mEventBus.register(subscriber3, MyEvent2.class));

        // Test unregister with unsubscribed event
        Assert.assertFalse(mEventBus.unregister(subscriber1, MyEvent2.class));

        // Test unregister event
        Assert.assertTrue(mEventBus.unregister(subscriber1, MyEvent1.class));

        // Test unregister with unregistered object
        Assert.assertFalse(mEventBus.unregister(subscriber1));

        // Test unregister one event
        Assert.assertTrue(mEventBus.unregister(subscriber3, MyEvent1.class));

        // Test unregister events
        Assert.assertTrue(mEventBus.register(subscriber3, MyEvent1.class));
        Assert.assertTrue(mEventBus.unregister(subscriber3, MyEvent1.class, MyEvent2.class));
    }

    @Test
    public void test_eventbus_post() throws InterruptedException {
        String msg1 = "event1";
        String msg2 = "event2";
        String msg3 = "event3";
        String msg4 = "event4";
        BaseEvent event1 = new MyEvent1();
        BaseEvent event2 = new MyEvent2();
        BaseEvent event3 = new MyEvent1();
        BaseEvent event4 = new MyEvent1();
        MySubscriber1 subscriber1 = new MySubscriber1();
        MySubscriber2 subscriber2 = new MySubscriber2();
        MySubscriber3 subscriber3 = new MySubscriber3();

        event1.setMessage(msg1);
        event2.setMessage(msg2);
        event2.setConsumeOnce(true);
        event3.setMessage(msg3);
        event3.setPriority(EventPriority.NORMAL);
        event4.setMessage(msg4);
        event4.setPriority(EventPriority.HIGH);

        mEventBus.register(subscriber1, MyEvent1.class);
        mEventBus.register(subscriber2, MyEvent2.class);
        mEventBus.register(subscriber3, MyEvent1.class, MyEvent2.class);

        // Test post event1, which is a default event
        Assert.assertTrue(mEventBus.post(event1));
        Thread.sleep(POST_WAIT_TIME);
        Assert.assertEquals(2, event1.getHandledCounts());
        Assert.assertEquals(msg1, subscriber1.msg);
        Assert.assertNull(subscriber2.msg);
        Assert.assertEquals(msg1, subscriber3.msg1);

        // Test post event2, which will be only consumed once
        Assert.assertTrue(mEventBus.post(event2));
        Thread.sleep(POST_WAIT_TIME);
        Assert.assertEquals(1, event2.getHandledCounts());
        Assert.assertNotEquals(subscriber2.msg, subscriber3.msg2);
        Assert.assertTrue(
                Objects.equals(subscriber2.msg, msg2) || Objects.equals(subscriber3.msg2, msg2)
        );

        // Test Event Priority
        mEventBus.post(event3);
        mEventBus.post(event4);

        Thread.sleep(POST_WAIT_TIME);
        Assert.assertEquals(2, event3.getHandledCounts());
        Assert.assertEquals(2, event4.getHandledCounts());
        Assert.assertEquals(msg3, subscriber1.msg);
        Assert.assertEquals(msg3, subscriber3.msg1);
    }

    private static class MyEvent1 extends BaseEvent {

    }

    private static class MyEvent2 extends BaseEvent {

    }

    private static class MySubscriber1 implements ISubscriber {
        public String msg = null;

        @Override
        public boolean onEvent(BaseEvent event) {
            Class<?> eventType = event.getClass();
            if (eventType == MyEvent1.class) {
                msg = event.getMessage();
                return true;
            }
            return false;
        }
    }

    private static class MySubscriber2 implements ISubscriber {
        public String msg = null;

        @Override
        public boolean onEvent(BaseEvent event) {
            Class<?> eventType = event.getClass();
            if (eventType == MyEvent2.class) {
                msg = event.getMessage();
                return true;
            }
            return false;
        }
    }

    private static class MySubscriber3 implements ISubscriber {
        public String msg1 = null;
        public String msg2 = null;

        @Override
        public boolean onEvent(BaseEvent event) {
            Class<?> eventType = event.getClass();
            if (eventType == MyEvent1.class) {
                msg1 = event.getMessage();
                return true;
            } else if (eventType == MyEvent2.class) {
                msg2 = event.getMessage();
                return true;
            }
            return false;
        }
    }
}
