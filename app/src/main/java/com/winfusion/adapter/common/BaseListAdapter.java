package com.winfusion.adapter.common;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewbinding.ViewBinding;

/**
 * 一个基于 {@link ListAdapter} 的列表适配器，适用于使用 {@link ViewBinding} 的情况。
 *
 * @param <T> 数据模板类
 * @param <H> ViewHolder 类，必须继承于 {@link BaseListAdapter.ViewHolder}
 */
public abstract class BaseListAdapter<T, H extends BaseListAdapter.ViewHolder<T, ? extends ViewBinding>>
        extends ListAdapter<T, H> {

    protected BaseListAdapter(@NonNull DiffUtil.ItemCallback<T> diffCallback) {
        super(diffCallback);
    }

    /**
     * ViewHolder的基类，适用于使用 {@link ViewBinding} 的情况。
     *
     * @param <T> 数据模板类
     * @param <V> 视图绑定类，必须实现 {@link ViewBinding} 接口
     */
    protected abstract static class ViewHolder<T, V extends ViewBinding> extends RecyclerView.ViewHolder {

        protected final V binding;
        private boolean hasSetup = false;

        public ViewHolder(@NonNull V binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        /**
         * 调用该方法来将模板对象的数据显示在 View 上。
         * 将会根据当前的ViewHolder的状态选择性的调用 {@link #setup} 和 {@link #update}，
         * 以避免视图复用时发生
         * 一般在适配器的 {@link #onBindViewHolder} 中调用。
         *
         * @param model 数据模板对象
         */
        public void bind(@NonNull T model) {
            if (!hasSetup) {
                setup(model);
                hasSetup = true;
            }
            update(model);
        }

        /**
         * 在数据绑定时进行必要的初始化操作，比如设置监听器等。
         *
         * @param model 数据模板对象
         */
        protected abstract void setup(@NonNull T model);

        /**
         * 在数据更新时进行必要的更新操作，比如更新文本内容等。
         *
         * @param model 数据模板对象
         */
        protected abstract void update(@NonNull T model);
    }
}
