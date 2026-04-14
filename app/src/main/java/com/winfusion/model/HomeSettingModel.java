package com.winfusion.model;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;

import java.util.Objects;

/**
 * A model representing a setting option with various properties.
 */
public class HomeSettingModel {

    /**
     * Default Value for missing resource IDs
     */
    public static final int EMPTY_ID = 0;

    private final int titleId;          // Resource ID for the title
    private final int descriptionId;    // Resource ID for the description
    private final int iconId;           // Resource ID for the icon
    private final boolean enabled;      // Whether the option is enabled
    private final int disabledTitleId;  // Resource ID for the title when option is disabled
    private final int disabledReasonId; // Resource ID for the disabled reason
    private final Runnable clickTask;   // The task to be executed when clicked
    private final DetailsSupplier detailsSupplier;

    private HomeSettingModel(@StringRes int titleId, @StringRes int descriptionId,
                             @DrawableRes int iconId, boolean enabled, @StringRes int disabledTitleId,
                             @StringRes int disabledReasonId, @NonNull Runnable clickTask,
                             @NonNull DetailsSupplier detailsSupplier) {

        this.titleId = titleId;
        this.descriptionId = descriptionId;
        this.iconId = iconId;
        this.enabled = enabled;
        this.disabledTitleId = disabledTitleId;
        this.disabledReasonId = disabledReasonId;
        this.clickTask = clickTask;
        this.detailsSupplier = detailsSupplier;
    }

    @StringRes
    public int getTitleId() {
        return titleId;
    }

    @StringRes
    public int getDescriptionId() {
        return descriptionId;
    }

    @DrawableRes
    public int getIconId() {
        return iconId;
    }

    public boolean isEnabled() {
        return enabled;
    }

    @StringRes
    public int getDisabledTitleId() {
        return disabledTitleId;
    }

    @StringRes
    public int getDisabledReasonId() {
        return disabledReasonId;
    }

    @NonNull
    public Runnable getClickTask() {
        return clickTask;
    }

    @NonNull
    public DetailsSupplier getDetailsSupplier() {
        return detailsSupplier;
    }

    public interface DetailsSupplier {

        @NonNull
        String get(@NonNull HomeSettingModel model);
    }

    public static class Builder {

        private static final Runnable EMPTY_RUNNABLE = () -> {
            // empty runnable
        };

        private static final DetailsSupplier EMPTY_DETAILS_SUPPLIER = new DetailsSupplier() {

            @NonNull
            @Override
            public String get(@NonNull HomeSettingModel model) {
                return "";
            }
        };

        private int titleId = EMPTY_ID;
        private int descriptionId = EMPTY_ID;
        private int iconId = EMPTY_ID;
        private boolean enabled = true;
        private int disabledTitleId = EMPTY_ID;
        private int disabledReasonId = EMPTY_ID;
        private Runnable clickTask = EMPTY_RUNNABLE;
        private DetailsSupplier detailsSupplier = EMPTY_DETAILS_SUPPLIER;

        @NonNull
        public Builder setTitleId(@StringRes int titleId) {
            this.titleId = titleId;
            return this;
        }

        @NonNull
        public Builder setDescriptionId(@StringRes int descriptionId) {
            this.descriptionId = descriptionId;
            return this;
        }

        @NonNull
        public Builder setIconId(@DrawableRes int iconId) {
            this.iconId = iconId;
            return this;
        }

        @NonNull
        public Builder setEnabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        @NonNull
        public Builder setDisabledTitleId(@StringRes int disabledTitleId) {
            this.disabledTitleId = disabledTitleId;
            return this;
        }

        @NonNull
        public Builder setDisabledReasonId(@StringRes int disabledReasonId) {
            this.disabledReasonId = disabledReasonId;
            return this;
        }

        @NonNull
        public Builder setClickTask(@Nullable Runnable clickTask) {
            this.clickTask = Objects.requireNonNullElse(clickTask, EMPTY_RUNNABLE);
            return this;
        }

        @NonNull
        public Builder setDetailsSupplier(@Nullable DetailsSupplier detailsSupplier) {
            this.detailsSupplier = Objects.requireNonNullElse(detailsSupplier, EMPTY_DETAILS_SUPPLIER);
            return this;
        }

        @NonNull
        public HomeSettingModel build() {
            return new HomeSettingModel(
                    titleId, descriptionId, iconId,
                    enabled, disabledTitleId, disabledReasonId,
                    clickTask, detailsSupplier
            );
        }
    }
}
