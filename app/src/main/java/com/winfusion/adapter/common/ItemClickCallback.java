package com.winfusion.adapter.common;

public abstract class ItemClickCallback implements ItemCallback {

    public abstract void onClick(int position);

    public abstract boolean onLongClick(int position);

    public void onSelected(int position) {
        // do nothing
    }
}
