package com.winfusion.application;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class WinfusionApplication extends Application {

    @SuppressLint("StaticFieldLeak")
    private static WinfusionApplication instance;

    private ThreadPoolExecutor mThreadPoolExecutor;
    private final CopyOnWriteArraySet<Activity> mActivities = new CopyOnWriteArraySet<>();
    private Activity currentActivity = null;

    public static WinfusionApplication getInstance() {
        return instance;
    }

    public WinfusionApplication() {
        super();
        instance = this;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        registerActivityLifecycleCallbacks(mActivityLifecycleCallbacks);
        mThreadPoolExecutor = new ThreadPoolExecutor(4, 16, 10,
                TimeUnit.SECONDS, new LinkedBlockingQueue<>(), new ThreadPoolExecutor.CallerRunsPolicy());
    }

    private final ActivityLifecycleCallbacks mActivityLifecycleCallbacks = new ActivityLifecycleCallbacks() {
        @Override
        public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {
            mActivities.add(activity);
        }

        @Override
        public void onActivityStarted(@NonNull Activity activity) {

        }

        @Override
        public void onActivityResumed(@NonNull Activity activity) {
            currentActivity = activity;
        }

        @Override
        public void onActivityPaused(@NonNull Activity activity) {
            currentActivity = null;
        }

        @Override
        public void onActivityStopped(@NonNull Activity activity) {

        }

        @Override
        public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {

        }

        @Override
        public void onActivityDestroyed(@NonNull Activity activity) {
            mActivities.remove(activity);
        }
    };

    @NonNull
    public ExecutorService getExecutor() {
        return mThreadPoolExecutor;
    }

    public void finishAllActivities() {
        for (Activity activity : mActivities) {
            if (!activity.isFinishing())
                activity.finish();
        }
    }

    @Nullable
    public synchronized Activity requireCurrentActivity() {
        return currentActivity;
    }
}

