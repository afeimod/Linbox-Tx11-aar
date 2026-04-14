package com.winfusion.feature.setting.viewholder;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewbinding.ViewBinding;

import com.winfusion.feature.setting.model.BaseModel;

public abstract class BaseViewHolder<T extends BaseModel, V extends ViewBinding>
        extends RecyclerView.ViewHolder {

    protected final V binding;
    protected final ViewHolderCallback viewHolderCallback;

    public BaseViewHolder(@NonNull V binding, @NonNull ViewHolderCallback viewHolderCallback) {
        super(binding.getRoot());
        this.binding = binding;
        this.viewHolderCallback = viewHolderCallback;
    }

    public abstract void bind(@NonNull T model);
}
