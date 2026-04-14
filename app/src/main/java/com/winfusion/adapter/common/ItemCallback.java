package com.winfusion.adapter.common;

public interface ItemCallback {

    void onClick(int position);

    boolean onLongClick(int position);

    void onSelected(int position);
}
