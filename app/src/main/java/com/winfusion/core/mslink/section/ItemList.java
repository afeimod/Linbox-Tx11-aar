package com.winfusion.core.mslink.section;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

/**
 * A generic list container that provides utility methods for managing items.
 *
 * @param <T> The type of elements stored in this list.
 */
public class ItemList<T> {

    private final ArrayList<T> list;

    public ItemList() {
        list = new ArrayList<>();
    }

    /**
     * Retrieves a copy of the internal list of items.
     *
     * @return A new {@link ArrayList} containing all items in this list.
     */
    @NonNull
    public ArrayList<T> getItemIDList() {
        return new ArrayList<>(list);
    }

    /**
     * Removes an item at the specified position in the list.
     *
     * @param pos The position of the item to remove.
     */
    public void removeItem(int pos) {
        list.remove(pos);
    }

    /**
     * Removes the first occurrence of the specified item from the list.
     *
     * @param item The item to remove.
     */
    public void removeItem(@NonNull T item) {
        list.remove(item);
    }

    /**
     * Removes all items from the list.
     */
    public void removeAll() {
        list.clear();
    }

    /**
     * Adds a single item to the list.
     *
     * @param item The item to add.
     */
    public void addItem(@NonNull T item) {
        list.add(item);
    }

    /**
     * Adds all items from the specified collection to the list.
     *
     * @param items The collection of items to add.
     */
    public void addItem(@NonNull List<T> items) {
        list.addAll(items);
    }

    /**
     * Updates the item at the specified position in the list.
     *
     * @param pos  The position of the item to replace.
     * @param item The new item to set at the specified position.
     */
    public void setItemAt(int pos, @NonNull T item) {
        list.set(pos, item);
    }
}
