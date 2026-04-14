package com.winfusion.feature.content.model;

import androidx.annotation.NonNull;

import com.winfusion.core.wfp.Wfp;

public class WfpModel extends BaseContentModel {

    private final Wfp wfp;

    public WfpModel(@NonNull Wfp wfp) {
        this.wfp = wfp;
    }

    @NonNull
    public Wfp getWfp() {
        return wfp;
    }
}
