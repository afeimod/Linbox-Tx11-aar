package com.winfusion.feature.input.overlay.gesture;

import androidx.annotation.NonNull;

import java.util.function.Consumer;

public class FingerHandler {

    private enum State {
        IDLE,
        BLOCK,
        ONE_FINGER_DOWN,
        ONE_FINGER_MOVE,
        ONE_FINGER_MOVE_BLOCK,
        ONE_FINGER_MOVE_BLOCK_FINGER2_DOWN,
        TWO_FINGER_DOWN,
        TWO_FINGER_MOVE_SCROLL,
        TWO_FINGER_MOVE_SCROLL_BLOCK,
        TWO_FINGER_MOVE_DRAG,
        TWO_FINGER_MOVE_DRAG_BLOCK
    }

    public static final int DefaultMoveTriggerTime = 150; // ms
    public static final int DefaultMoveTriggerDistance = 10; // px
    public static final int DefaultTwoFingerDragTriggerDistance = 300; // px

    private final Consumer<Gesture> callback;
    private State state = State.IDLE;
    private int fingerCount = 0;
    private Finger downFinger1;
    private Finger downFinger2;
    private Finger lastFinger1;
    private Finger lastFinger2;
    private int moveTriggerTime = DefaultMoveTriggerTime; // move: actionTime - downTime > clickTriggerTime (ms)
    private int moveTriggerDistance = DefaultMoveTriggerDistance; // move: distance > clickTriggerDistance (px)
    private int twoFingerDragTriggerDistance = DefaultTwoFingerDragTriggerDistance; // distance <= twoFingerDragDistance ? scroll : drag

    public FingerHandler(@NonNull Consumer<Gesture> callback) {
        this.callback = callback;
    }

    public void setMoveTriggerTime(int time) {
        moveTriggerTime = time;
    }

    public void setMoveTriggerDistance(int distance) {
        moveTriggerDistance = distance;
    }

    public void setTwoFingerDragTriggerDistance(int distance) {
        twoFingerDragTriggerDistance = distance;
    }

    public void handleFinger(@NonNull Finger finger) {
        switch (state) {
            case IDLE -> processIdle(finger);
            case BLOCK -> processBlock(finger);
            case ONE_FINGER_DOWN -> processOneFingerDown(finger);
            case ONE_FINGER_MOVE -> processOneFingerMove(finger);
            case ONE_FINGER_MOVE_BLOCK -> processOneFingerMoveBlock(finger);
            case ONE_FINGER_MOVE_BLOCK_FINGER2_DOWN -> processOneFingerMoveBlockFinger2Down(finger);
            case TWO_FINGER_DOWN -> processTwoFingerDown(finger);
            case TWO_FINGER_MOVE_SCROLL -> processTwoFingerMoveScroll(finger);
            case TWO_FINGER_MOVE_SCROLL_BLOCK -> processTwoFingerMoveScrollBlock(finger);
            case TWO_FINGER_MOVE_DRAG -> processTwoFingerMoveDrag(finger);
            case TWO_FINGER_MOVE_DRAG_BLOCK -> processTwoFingerMoveDragBlock(finger);
            default -> throw new IllegalStateException("Unhandled state: " + state.name());
        }
    }

    private void processIdle(@NonNull Finger finger) {
        if (finger.getAction() == Finger.Action.Down) {
            clearFingerCache();
            fingerCount = 0;

            fingerCount++;
            downFinger1 = lastFinger1 = finger;
            state = State.ONE_FINGER_DOWN;
        }
    }

    private void processBlock(@NonNull Finger finger) {
        if (finger.getAction() == Finger.Action.Up) {
            fingerCount--;
            if (fingerCount == 0) {
                clearFingerCache();
                state = State.IDLE;
            }
        }
    }

    private void processOneFingerDown(@NonNull Finger finger) {
        if (finger.getAction() == Finger.Action.Down) {
            fingerCount++;
            lastFinger2 = downFinger2 = finger;
            state = State.TWO_FINGER_DOWN;
        } else if (finger.getAction() == Finger.Action.Move) {
            state = State.ONE_FINGER_MOVE;
            lastFinger1 = finger;
        } else if (finger.getAction() == Finger.Action.Up) {
            fingerCount--;
            callback.accept(oneFingerClick(Gesture.Stage.START));
            callback.accept(oneFingerClick(Gesture.Stage.FINISH));
            state = State.IDLE;
        }
    }

    private void processOneFingerMove(@NonNull Finger finger) {
        if (finger.getAction() == Finger.Action.Down) {
            fingerCount++;
            lastFinger2 = downFinger2 = finger;
            state = State.TWO_FINGER_DOWN;
        } else if (finger.getAction() == Finger.Action.Move) {
            if (fingerDistance(finger, downFinger1) > moveTriggerDistance ||
                    finger.getActionTime() - downFinger1.getActionTime() > moveTriggerTime) {
                state = State.ONE_FINGER_MOVE_BLOCK;
                callback.accept(oneFingerDrag(Gesture.Stage.START, finger));
                lastFinger1 = finger;
            }
        } else if (finger.getAction() == Finger.Action.Up) {
            if (finger.getActionTime() - downFinger1.getActionTime() <= moveTriggerTime) {
                fingerCount--;
                callback.accept(oneFingerClick(Gesture.Stage.START));
                callback.accept(oneFingerClick(Gesture.Stage.FINISH));
                state = State.IDLE;
            }
        }
    }

    private void processOneFingerMoveBlock(@NonNull Finger finger) {
        if (finger.getAction() == Finger.Action.Down) {
            fingerCount++;
            downFinger2 = lastFinger2 = finger;
            state = State.ONE_FINGER_MOVE_BLOCK_FINGER2_DOWN;
        } else if (finger.getAction() == Finger.Action.Move) {
            callback.accept(oneFingerDrag(Gesture.Stage.RUNNING, finger));
            lastFinger1 = finger;
        } else if (finger.getAction() == Finger.Action.Up) {
            fingerCount--;
            callback.accept(oneFingerDrag(Gesture.Stage.FINISH, finger));
            state = State.IDLE;
        }
    }

    private void processOneFingerMoveBlockFinger2Down(@NonNull Finger finger) {
        if (finger.getId() == downFinger1.getId()) {
            if (finger.getAction() == Finger.Action.Move) {
                callback.accept(oneFingerDrag(Gesture.Stage.RUNNING, finger));
                lastFinger1 = finger;
            } else if (finger.getAction() == Finger.Action.Up) {
                fingerCount--;
                callback.accept(oneFingerDrag(Gesture.Stage.FINISH, finger));
                state = State.BLOCK;
            }
        } else if (finger.getId() == downFinger2.getId()) {
            if (finger.getAction() == Finger.Action.Up) {
                fingerCount--;
                callback.accept(twoFingerClick(Gesture.Stage.START));
                callback.accept(twoFingerClick(Gesture.Stage.FINISH));
                downFinger2 = lastFinger2 = null;
                state = State.ONE_FINGER_MOVE_BLOCK;
            }
        }
        // TODO: handle finger_3 here
    }

    private void processTwoFingerDown(@NonNull Finger finger) {
        if (finger.getAction() == Finger.Action.Up) {
            fingerCount--;
            state = State.BLOCK;
        } else if (finger.getAction() == Finger.Action.Move) {
            if (finger.getId() == downFinger1.getId())
                lastFinger1 = finger;
            else
                lastFinger2 = finger;

            if (fingerDistance(downFinger1, downFinger2) > twoFingerDragTriggerDistance)
                state = State.TWO_FINGER_MOVE_DRAG;
            else
                state = State.TWO_FINGER_MOVE_SCROLL;
        }
        // TODO: handle finger_3 here
    }

    private void processTwoFingerMoveScroll(@NonNull Finger finger) {
        if (finger.getAction() == Finger.Action.Up) {
            fingerCount--;
            state = State.BLOCK;
        } else if (finger.getAction() == Finger.Action.Move) {
            Finger lastFinger;

            if (finger.getId() == downFinger1.getId()) {
                lastFinger = lastFinger1;
                lastFinger1 = finger;
            } else {
                lastFinger = lastFinger2;
                lastFinger2 = finger;
            }

            callback.accept(twoFingerVerticalScroll(Gesture.Stage.START, finger, lastFinger));
            callback.accept(twoFingerHorizontalScroll(Gesture.Stage.START, finger, lastFinger));
            state = State.TWO_FINGER_MOVE_SCROLL_BLOCK;
        }
        // TODO: handle finger_3 here
    }

    private void processTwoFingerMoveScrollBlock(@NonNull Finger finger) {
        if (finger.getAction() == Finger.Action.Up) {
            fingerCount--;
            callback.accept(twoFingerVerticalScroll(Gesture.Stage.FINISH, finger, finger));
            callback.accept(twoFingerHorizontalScroll(Gesture.Stage.FINISH, finger, finger));
            state = State.BLOCK;
        } else if (finger.getAction() == Finger.Action.Move) {
            Finger lastFinger;

            if (finger.getId() == downFinger1.getId()) {
                lastFinger = lastFinger1;
                lastFinger1 = finger;
            } else {
                lastFinger = lastFinger2;
                lastFinger2 = finger;
            }

            callback.accept(twoFingerVerticalScroll(Gesture.Stage.RUNNING, finger, lastFinger));
            callback.accept(twoFingerHorizontalScroll(Gesture.Stage.RUNNING, finger, lastFinger));
        }
        // TODO: handle finger_3 here
    }

    private void processTwoFingerMoveDrag(@NonNull Finger finger) {
        if (finger.getAction() == Finger.Action.Up) {
            fingerCount--;
            state = State.BLOCK;
        } else if (finger.getAction() == Finger.Action.Move) {
            if (finger.getId() == downFinger1.getId())
                lastFinger1 = finger;
            else
                lastFinger2 = finger;

            callback.accept(twoFingerDrag(Gesture.Stage.START, lastFinger1, lastFinger2));
            state = State.TWO_FINGER_MOVE_DRAG_BLOCK;
        }
        // TODO: handle finger_3 here
    }

    private void processTwoFingerMoveDragBlock(@NonNull Finger finger) {
        if (finger.getAction() == Finger.Action.Up) {
            fingerCount--;
            callback.accept(twoFingerDrag(Gesture.Stage.FINISH, lastFinger1, lastFinger2));
            state = State.BLOCK;
        } else if (finger.getAction() == Finger.Action.Move) {
            if (finger.getId() == downFinger1.getId())
                lastFinger1 = finger;
            else
                lastFinger2 = finger;

            callback.accept(twoFingerDrag(Gesture.Stage.RUNNING, lastFinger1, lastFinger2));
        }
        // TODO: handle finger_3 here
    }

    private void clearFingerCache() {
        downFinger1 = null;
        downFinger2 = null;
        lastFinger1 = null;
        lastFinger2 = null;
    }

    @NonNull
    private Gesture oneFingerClick(@NonNull Gesture.Stage stage) {
        return new Gesture(Gesture.Type.ONE_FINGER_CLICK, stage, Gesture.Direction.NONE, 0, 0);
    }

    @NonNull
    private Gesture oneFingerDrag(@NonNull Gesture.Stage stage, @NonNull Finger finger) {
        return new Gesture(Gesture.Type.ONE_FINGER_DRAG, stage, Gesture.Direction.NONE,
                finger.getPoint().x, finger.getPoint().y);
    }

    @NonNull
    private Gesture twoFingerClick(@NonNull Gesture.Stage stage) {
        return new Gesture(Gesture.Type.TWO_FINGERS_CLICK, stage, Gesture.Direction.NONE, 0, 0);
    }

    @NonNull
    private Gesture twoFingerVerticalScroll(@NonNull Gesture.Stage stage, @NonNull Finger lastFinger,
                                            @NonNull Finger finger) {

        return new Gesture(Gesture.Type.TWO_FINGERS_SCROLL, stage, Gesture.Direction.VERTICAL, 0,
                finger.getPoint().y - lastFinger.getPoint().y);
    }

    @NonNull
    private Gesture twoFingerHorizontalScroll(@NonNull Gesture.Stage stage, @NonNull Finger lastFinger,
                                              @NonNull Finger finger) {

        return new Gesture(Gesture.Type.TWO_FINGERS_SCROLL, stage, Gesture.Direction.HORIZONTAL,
                finger.getPoint().x - lastFinger.getPoint().x, 0);
    }

    @NonNull
    private Gesture twoFingerDrag(@NonNull Gesture.Stage stage, @NonNull Finger finger1,
                                  @NonNull Finger finger2) {

        return new Gesture(Gesture.Type.TWO_FINGERS_DRAG, stage, Gesture.Direction.NONE,
                finger1.getPoint().x + finger2.getPoint().x / 2,
                finger1.getPoint().y + finger2.getPoint().y / 2);
    }

    private double fingerDistance(@NonNull Finger finger1, @NonNull Finger finger2) {
        return Math.hypot(finger1.getPoint().x - finger2.getPoint().x,
                finger1.getPoint().y - finger2.getPoint().y);
    }
}
