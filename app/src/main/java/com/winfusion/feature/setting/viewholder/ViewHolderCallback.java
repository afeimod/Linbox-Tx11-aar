package com.winfusion.feature.setting.viewholder;

public interface ViewHolderCallback {

    boolean onItemLongClick(int position);

    void onItemClick(int position);

    void onItemClearButtonClick(int position);
}
