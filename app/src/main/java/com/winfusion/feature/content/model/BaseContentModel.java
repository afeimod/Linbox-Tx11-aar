package com.winfusion.feature.content.model;

import androidx.annotation.Nullable;

public abstract class BaseContentModel {

    private String title;
    private boolean enabled = true;
    private String disabledReason;
    private boolean builtin = false;
    private boolean selectable = false;

    @Nullable
    public String getTitle() {
        return title;
    }

    public boolean isEnabled() {
        return enabled;
    }

    @Nullable
    public String getDisabledReason() {
        return disabledReason;
    }

    public boolean isBuiltin() {
        return builtin;
    }

    public boolean isSelectable() {
        return selectable;
    }

    public void setTitle(@Nullable String title) {
        this.title = title;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void setDisabledReason(@Nullable String disabledReason) {
        this.disabledReason = disabledReason;
    }

    public void setBuiltin(boolean builtin) {
        this.builtin = builtin;
    }

    public void setSelectable(boolean selectable) {
        this.selectable = selectable;
    }
}
