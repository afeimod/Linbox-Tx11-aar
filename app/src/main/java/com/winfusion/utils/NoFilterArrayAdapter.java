package com.winfusion.utils;

import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.Filter;

import androidx.annotation.NonNull;

import java.util.List;

/**
 * 一个没有过滤功能的数组适配器。
 * 一般用于 {@link android.text.AutoText} 从而将其仅作为下拉栏使用而禁用过滤功能。
 *
 * @param <T> 适配器的元素类型
 */
public class NoFilterArrayAdapter<T> extends ArrayAdapter<T> {

    public NoFilterArrayAdapter(@NonNull Context context, int resource) {
        super(context, resource);
    }

    public NoFilterArrayAdapter(@NonNull Context context, int resource, int textViewResourceId) {
        super(context, resource, textViewResourceId);
    }

    public NoFilterArrayAdapter(@NonNull Context context, int resource, @NonNull T[] objects) {
        super(context, resource, objects);
    }

    public NoFilterArrayAdapter(@NonNull Context context, int resource, int textViewResourceId,
                                @NonNull T[] objects) {

        super(context, resource, textViewResourceId, objects);
    }

    public NoFilterArrayAdapter(@NonNull Context context, int resource, @NonNull List<T> objects) {
        super(context, resource, objects);
    }

    public NoFilterArrayAdapter(@NonNull Context context, int resource, int textViewResourceId,
                                @NonNull List<T> objects) {

        super(context, resource, textViewResourceId, objects);
    }

    @NonNull
    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                return null;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {

            }
        };
    }
}
