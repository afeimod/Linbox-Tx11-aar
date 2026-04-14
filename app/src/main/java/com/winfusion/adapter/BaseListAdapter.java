package com.winfusion.adapter;

import android.annotation.SuppressLint;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewbinding.ViewBinding;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * A base adapter class for managing a list of items in a RecyclerView. This class provides methods
 * to add, replace, and remove items from the list, as well as notify the RecyclerView about changes.
 *
 * @param <T>  The type of item in the list.
 * @param <VH> The ViewHolder type associated with the RecyclerView.
 */
public abstract class BaseListAdapter<T, VH extends BaseListAdapter.BaseViewHolder<T>>
        extends RecyclerView.Adapter<VH> {

    private ArrayList<T> sourceList;

    /**
     * Constructor to initialize the adapter with an optional initial list of items.
     *
     * @param sourceList The initial list of items. If null, an empty list is used.
     */
    public BaseListAdapter(@Nullable ArrayList<T> sourceList) {
        if (sourceList == null)
            this.sourceList = new ArrayList<>();
        else
            this.sourceList = new ArrayList<>(sourceList);
    }

    /**
     * Adds an item to the list and notifies the RecyclerView about the change. This method adds the
     * item to the end of the list.
     *
     * @param item The item to be added to the list.
     */
    public void addItem(@NonNull T item) {
        addItem(item, null);
    }

    /**
     * Adds an item to the list at a specified position and notifies the RecyclerView about the change.
     *
     * @param item The item to be added to the list.
     * @param pos  The position where the item will be added. If null, the item is added at the end.
     */
    public void addItem(@NonNull T item, Integer pos) {
        addItem(item, pos, null);
    }

    /**
     * Adds an item to the list at a specified position or to the end of the list, and notifies the
     * RecyclerView about the change. Optionally, a callback can be provided to be called after the
     * item is added.
     *
     * @param item     The item to be added to the list.
     * @param pos      The position where the item will be added. If null, the item is added at the end.
     * @param callback A callback that will be invoked with the updated position after the item is added.
     */
    public void addItem(@NonNull T item, Integer pos, @Nullable Consumer<Integer> callback) {
        int updatedPosition;
        ArrayList<T> newList = new ArrayList<>(sourceList);

        if (pos == null) {
            newList.add(item);
            updatedPosition = newList.size() - 1;
        } else {
            newList.add(pos, item);
            updatedPosition = pos;
        }

        sourceList = newList;
        notifyItemInserted(updatedPosition);

        if (callback != null)
            callback.accept(updatedPosition);
    }

    /**
     * Replaces an item at a specified position with a new item and notifies the RecyclerView about the change.
     *
     * @param item The item to replace the existing item at the specified position.
     * @param pos  The position of the item to be replaced.
     */
    public void replaceItem(@NonNull T item, int pos) {
        replaceItem(item, pos, null);
    }

    /**
     * Replaces an item at a specified position with a new item and notifies the RecyclerView about the change.
     * Optionally, a callback can be provided to be called after the item is replaced.
     *
     * @param item     The item to replace the existing item at the specified position.
     * @param pos      The position of the item to be replaced.
     * @param callback A callback that will be invoked with the position of the replaced item.
     */
    public void replaceItem(@NonNull T item, int pos, @Nullable Consumer<Integer> callback) {
        ArrayList<T> newList = new ArrayList<>(sourceList);

        newList.set(pos, item);
        sourceList = newList;
        notifyItemChanged(pos);

        if (callback != null)
            callback.accept(pos);
    }

    /**
     * Removes an item at a specified position and notifies the RecyclerView about the change.
     *
     * @param pos The position of the item to be removed.
     */
    public void removeItem(int pos) {
        removeItem(pos, null);
    }

    /**
     * Removes an item at a specified position and notifies the RecyclerView about the change.
     * Optionally, a callback can be provided to be called after the item is removed.
     *
     * @param pos      The position of the item to be removed.
     * @param callback A callback that will be invoked with the position of the removed item.
     */
    public void removeItem(int pos, @Nullable Consumer<Integer> callback) {
        ArrayList<T> newList = new ArrayList<>(sourceList);

        newList.remove(pos);
        sourceList = newList;
        notifyItemRemoved(pos);

        if (callback != null)
            callback.accept(pos);
    }

    /**
     * Replaces the entire list with a new list and notifies the RecyclerView about the changes.
     * This method invalidates the current data set and requires the adapter to update the entire list.
     *
     * @param list The new list to replace the current list. If null, an empty list will be used.
     */
    @SuppressLint("NotifyDataSetChanged")
    public void replaceList(@Nullable List<T> list) {
        if (list == null)
            sourceList = new ArrayList<>();
        else
            sourceList = new ArrayList<>(list);

        notifyDataSetChanged();
    }

    /**
     * Returns a new list that is a copy of the current list. This method is used to provide a read-only
     * view of the list.
     *
     * @return A new ArrayList containing the items in the current list.
     */
    @NonNull
    public ArrayList<T> getCurrentList() {
        return sourceList;
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        holder.bind(sourceList.get(position));
    }

    @Override
    public int getItemCount() {
        return sourceList.size();
    }

    /**
     * A generic abstract ViewHolder class for RecyclerView items.
     * <p>
     * It extends RecyclerView.ViewHolder and is designed to work with ViewBinding for easier access
     * to the views in the item layout.
     *
     * @param <M> The type of data that this ViewHolder will bind to.
     */
    public static abstract class BaseViewHolder<M> extends RecyclerView.ViewHolder {

        /**
         * Constructor for the BaseViewHolder.
         *
         * @param binding The ViewBinding instance that holds the references to views.
         */
        public BaseViewHolder(@NonNull ViewBinding binding) {
            super(binding.getRoot());
        }

        /**
         * Abstract method to bind the given data to the views in the ViewHolder.
         *
         * @param model The data model to bind to the ViewHolder.
         */
        public abstract void bind(@NonNull M model);
    }
}
