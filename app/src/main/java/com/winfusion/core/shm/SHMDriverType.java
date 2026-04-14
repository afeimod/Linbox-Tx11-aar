package com.winfusion.core.shm;

public enum SHMDriverType {

    MIDISynth(1),
    ALSARCM(2),
    GameController(3),
    TaskManager(4),
    Unknown(-1);

    private final int value;

    SHMDriverType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static SHMDriverType fromValue(int value) {
        SHMDriverType[] types = SHMDriverType.values();
        for (SHMDriverType t : types) {
            if (t.getValue() == value)
                return t;
        }
        return Unknown;
    }
}
