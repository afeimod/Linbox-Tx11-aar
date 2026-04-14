package com.winfusion.adapter.common;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.viewbinding.ViewBinding;

public abstract class ClickableListAdapter<T, H extends ClickableListAdapter.ViewHolder<T, ? extends ViewBinding>>
        extends BaseListAdapter<T, H> {

    protected final ItemCallback itemCallback;

    protected ClickableListAdapter(@NonNull DiffUtil.ItemCallback<T> diffCallback,
                                   @NonNull ItemCallback itemCallback) {

        super(diffCallback);
        this.itemCallback = itemCallback;
    }

    public abstract static class ViewHolder<T, V extends ViewBinding> extends BaseListAdapter.ViewHolder<T, V> {

        protected final ItemCallback itemCallback;

        public ViewHolder(@NonNull V binding, @NonNull ItemCallback itemCallback) {
            super(binding);
            this.itemCallback = itemCallback;
        }
    }
}
