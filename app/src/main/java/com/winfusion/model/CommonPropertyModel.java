package com.winfusion.model;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;

import java.util.Objects;

public class CommonPropertyModel {

    private final static int EMPTY_ID = 0;

    private final int titleId;
    private final int descriptionId;
    private final int iconId;
    private final Runnable clickTask;
    private final DetailsSupplier detailsSupplier;

    private CommonPropertyModel(@StringRes int titleId, @StringRes int descriptionId,
                                @DrawableRes int iconId, @NonNull Runnable clickTask,
                                @NonNull DetailsSupplier detailsSupplier) {

        this.titleId = titleId;
        this.descriptionId = descriptionId;
        this.iconId = iconId;
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
        String get(@NonNull CommonPropertyModel model);
    }

    public static class Builder {

        private static final Runnable EMPTY_RUNNABLE = () -> {
            // empty runnable
        };

        private static final DetailsSupplier EMPTY_DETAILS_SUPPLIER = model -> "";

        private int titleId = EMPTY_ID;
        private int descriptionId = EMPTY_ID;
        private int iconId = EMPTY_ID;
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
        public CommonPropertyModel build() {
            return new CommonPropertyModel(titleId, descriptionId, iconId, clickTask,
                    detailsSupplier);
        }
    }
}
