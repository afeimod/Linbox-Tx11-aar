package com.winfusion.model;

import androidx.annotation.NonNull;

import com.winfusion.feature.manager.Overlay;

public class OverlayProfileModel {

    private final Overlay overlay;
    private boolean selectable;

    public OverlayProfileModel(@NonNull Overlay overlay) {
        this.overlay = overlay;
    }

    @NonNull
    public Overlay getOverlay() {
        return overlay;
    }

    public boolean isSelectable() {
        return selectable;
    }

    public void setSelectable(boolean selectable) {
        this.selectable = selectable;
    }
}
