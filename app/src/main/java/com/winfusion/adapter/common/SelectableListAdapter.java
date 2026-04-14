package com.winfusion.adapter.common;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.viewbinding.ViewBinding;

public abstract class SelectableListAdapter<T,H extends SelectableListAdapter.ViewHolder<T, ? extends ViewBinding>>
        extends ClickableListAdapter<T, H>  {

    protected int selectedPosition = -1;

    protected SelectableListAdapter(@NonNull DiffUtil.ItemCallback<T> diffCallback,
                                    @NonNull ItemCallback itemCallback) {

        super(diffCallback, itemCallback);
    }

    public void setSelectedPosition(int selectedPosition) {
        int lastSelectedPosition = this.selectedPosition;
        this.selectedPosition = selectedPosition;
        notifySelectedItem(lastSelectedPosition);
        notifySelectedItem(selectedPosition);
    }

    public int getSelectedPosition() {
        return selectedPosition;
    }

    // called by view holder
    protected void internalSetSelectedPosition(int selectedPosition) {
        setSelectedPosition(selectedPosition);
        itemCallback.onSelected(selectedPosition);
    }

    private void notifySelectedItem(int position) {
        if (position >= 0 && position < getCurrentList().size())
            notifyItemChanged(position);
    }

    public static abstract class ViewHolder<T, V extends ViewBinding>
            extends ClickableListAdapter.ViewHolder<T, V> {

        public ViewHolder(@NonNull V binding, @NonNull ItemCallback itemCallback) {
            super(binding, itemCallback);
        }
    }
}
