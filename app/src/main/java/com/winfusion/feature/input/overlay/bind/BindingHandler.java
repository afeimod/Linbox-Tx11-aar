package com.winfusion.feature.input.overlay.bind;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;

import com.winfusion.feature.input.interfaces.InputInterface;
import com.winfusion.feature.input.event.KeyState;
import com.winfusion.feature.input.event.KeyboardEvent;
import com.winfusion.feature.input.event.MouseButtonEvent;
import com.winfusion.feature.input.event.MousePointerEvent;
import com.winfusion.feature.input.key.StandardAction;
import com.winfusion.feature.input.key.StandardButton;
import com.winfusion.feature.input.key.StandardKey;

public class BindingHandler {

    private static final int MinMouseMoveInterval = 20; // ms
    private static final int MaxMouseMoverInterval = 60; // ms
    private static final int MouseMoveStep = 1; // px

    private final InputInterface inputInterface;
    private Handler mainLoopHandler;
    private Binding binding;
    private Runnable task;

    public BindingHandler(@NonNull InputInterface inputInterface) {
        this.inputInterface = inputInterface;
    }

    public boolean isRunning() {
        return binding != null;
    }

    public void start(@NonNull Binding binding) {
        if (isRunning())
            throw new IllegalStateException("Binding handler is running already.");

        this.binding = binding;
        handleBinding(true);
    }

    public void stop() {
        if (binding == null)
            return;

        handleBinding(false);
        binding = null;
    }

    private void handleBinding(boolean start) {
        switch (binding.getType()) {
            case Keyboard -> handleKeyboardBinding(start);
            case MouseButton -> handleMouseButtonBinding(start);
            case MouseAction -> handleMouseActionBinding(start);
            case ControllerButton -> handleControllerButtonBinding(start);
            case ControllerAction -> handleControllerActionBinding(start);
        }
    }

    private void handleKeyboardBinding(boolean start) {
        StandardKey key = (StandardKey) binding.getItem();
        inputInterface.onKeyboardEvent(new KeyboardEvent(start ? KeyState.Pressed :
                KeyState.Released, key));
    }

    private void handleMouseButtonBinding(boolean start) {
        StandardButton button = (StandardButton) binding.getItem();
        inputInterface.onMouseButtonEvent(new MouseButtonEvent(start ? KeyState.Pressed :
                KeyState.Released, button));
    }

    private void handleMouseActionBinding(boolean start) {
        if (start) {
            StandardAction action = (StandardAction) binding.getItem();
            float relX, relY;

            if (action == StandardAction.MouseMoveUp) {
                relX = 0;
                relY = -MouseMoveStep;
            } else if (action == StandardAction.MouseMoveDown) {
                relX = 0;
                relY = MouseMoveStep;
            } else if (action == StandardAction.MouseMoveLeft) {
                relX = -MouseMoveStep;
                relY = 0;
            } else if (action == StandardAction.MouseMoveRight) {
                relX = MouseMoveStep;
                relY = 0;
            } else
                throw new IllegalArgumentException("Unsupported action: " + action.name());

            int interval = (int) ((BindingHandler.MaxMouseMoverInterval - BindingHandler.MinMouseMoveInterval) *
                    (1 - binding.getValue()) + BindingHandler.MinMouseMoveInterval);

            task = new Runnable() {
                @Override
                public void run() {
                    inputInterface.onMousePointerEvent(new MousePointerEvent(MousePointerEvent.Type.Relative,
                            relX, relY));
                    getMainLoopHandler().postDelayed(this, interval);
                }
            };

            getMainLoopHandler().postDelayed(task, interval);
        } else {
            getMainLoopHandler().removeCallbacks(task);
        }
    }

    private void handleControllerButtonBinding(boolean start) {

    }

    private void handleControllerActionBinding(boolean start) {

    }

    @NonNull
    private Handler getMainLoopHandler() {
        if (mainLoopHandler == null)
            mainLoopHandler = new Handler(Looper.getMainLooper());
        return mainLoopHandler;
    }
}
