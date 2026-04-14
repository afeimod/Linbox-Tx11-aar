package com.winfusion.feature.setting.adapter;

import static com.winfusion.feature.setting.model.Constants.FLAG_NON_RESETTABLE;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.viewbinding.ViewBinding;

import com.winfusion.databinding.ListItemSettingsCommonBinding;
import com.winfusion.databinding.ListItemSettingsLabelBinding;
import com.winfusion.feature.setting.Config;
import com.winfusion.feature.setting.dialog.AutoTextDialogFragment;
import com.winfusion.feature.setting.dialog.EditTextDialogFragment;
import com.winfusion.feature.setting.dialog.RestoreDefaultDialogFragment;
import com.winfusion.feature.setting.dialog.SingleChoiceDialogFragment;
import com.winfusion.feature.setting.dialog.SliderDialogFragment;
import com.winfusion.feature.setting.model.ActionModel;
import com.winfusion.feature.setting.model.AutoTextModel;
import com.winfusion.feature.setting.model.BaseModel;
import com.winfusion.feature.setting.model.GroupModel;
import com.winfusion.feature.setting.model.LabelModel;
import com.winfusion.feature.setting.model.SavableModel;
import com.winfusion.feature.setting.model.common.Resettable;
import com.winfusion.feature.setting.model.SingleChoiceModel;
import com.winfusion.feature.setting.model.SliderModel;
import com.winfusion.feature.setting.model.SwitchLinkModel;
import com.winfusion.feature.setting.model.SwitchModel;
import com.winfusion.feature.setting.model.TextModel;
import com.winfusion.feature.setting.model.action.ActivityAction;
import com.winfusion.feature.setting.model.action.BaseAction;
import com.winfusion.feature.setting.model.action.DirectionAction;
import com.winfusion.feature.setting.model.action.FragmentAction;
import com.winfusion.feature.setting.viewholder.ActionViewHolder;
import com.winfusion.feature.setting.viewholder.BaseViewHolder;
import com.winfusion.feature.setting.viewholder.CommonSavableViewHolder;
import com.winfusion.feature.setting.viewholder.CommonViewHolder;
import com.winfusion.feature.setting.viewholder.LabelViewHolder;
import com.winfusion.feature.setting.viewholder.SwitchViewHolder;
import com.winfusion.feature.setting.viewholder.ViewHolderCallback;

import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class SettingAdapter
        extends ListAdapter<BaseModel, BaseViewHolder<? extends BaseModel, ? extends ViewBinding>> {

    private static final int VIEW_TYPE_COMMON = 1;
    private static final int VIEW_TYPE_COMMON_SAVABLE = 2;
    private static final int VIEW_TYPE_SWITCH = 3;
    private static final int VIEW_TYPE_LABEL = 4;
    private static final int VIEW_TYPE_ACTION = 5;

    private final SettingAdapterAgent agent;
    private final Deque<BackModel> backStack;
    private final Map<String, BaseModel> modelMap;
    private String currentPageKey;
    private ViewHolderCallback viewHolderCallback;

    public SettingAdapter(@NonNull Map<String, BaseModel> modelMap, @NonNull String rootPageKey,
                          @NonNull SettingAdapterAgent agent) {

        super(new DiffCallback());
        this.agent = agent;
        backStack = agent.getBackStack();
        this.modelMap = modelMap;

        updateModels();
        setupViewHolderCallback();

        if (backStack.isEmpty())
            setPageAndPush(rootPageKey);
        else
            peekAndSetPage();
    }


    @SuppressWarnings("unchecked")
    @Override
    public void onBindViewHolder(@NonNull BaseViewHolder holder, int position) {
        holder.bind(getCurrentList().get(position));
    }

    @NonNull
    @Override
    public BaseViewHolder<? extends BaseModel, ? extends ViewBinding> onCreateViewHolder(
            @NonNull ViewGroup parent, int viewType) {

        if (viewType == VIEW_TYPE_COMMON) {
            return new CommonViewHolder<>(createCommonBinding(parent), viewHolderCallback);
        } else if (viewType == VIEW_TYPE_COMMON_SAVABLE)
            return new CommonSavableViewHolder(createCommonBinding(parent), viewHolderCallback);
        else if (viewType == VIEW_TYPE_SWITCH)
            return new SwitchViewHolder(createCommonBinding(parent), viewHolderCallback);
        else if (viewType == VIEW_TYPE_LABEL)
            return new LabelViewHolder(createLabelBinding(parent), viewHolderCallback);
        else if (viewType == VIEW_TYPE_ACTION)
            return new ActionViewHolder(createCommonBinding(parent), viewHolderCallback);
        throw new IllegalArgumentException("Unsupported ViewType: " + viewType);
    }

    @Override
    public int getItemViewType(int position) {
        BaseModel model = getCurrentList().get(position);
        if (model instanceof GroupModel) {
            return VIEW_TYPE_COMMON;
        } else if (model instanceof SingleChoiceModel || model instanceof SliderModel ||
                model instanceof TextModel || model instanceof AutoTextModel) {
            return VIEW_TYPE_COMMON_SAVABLE;
        } else if (model instanceof SwitchModel || model instanceof SwitchLinkModel) {
            return VIEW_TYPE_SWITCH;
        } else if (model instanceof LabelModel) {
            return VIEW_TYPE_LABEL;
        } else if (model instanceof ActionModel) {
            return VIEW_TYPE_ACTION;
        }
        throw new IllegalArgumentException("Unsupported model: " + model);
    }

    /**
     * 当按下返回键时，调用该方法。
     *
     * @return 如果返回栈不为空，则弹出页面并切换到该页面，返回 true，否则直接返回 false
     */
    public boolean onBackPressed() {
        if (backStack.size() <= 1)
            return false;

        popAndSetPage();
        return true;
    }

    private void setupViewHolderCallback() {
        viewHolderCallback = new ViewHolderCallback() {
            @Override
            public boolean onItemLongClick(int position) {
                List<BaseModel> list = getCurrentList();
                if (position < 0 || position >= list.size())
                    return false;

                BaseModel model = list.get(position);
                if (!model.hasFlag(FLAG_NON_RESETTABLE) && model instanceof Resettable) {
                    agent.showDialogFragment(new RestoreDefaultDialogFragment(model,
                            () -> notifyItemChanged(position)));
                    return true;
                }
                return false;
            }

            @Override
            public void onItemClick(int position) {
                List<BaseModel> list = getCurrentList();
                if (position < 0 || position >= list.size())
                    return;

                BaseModel model = list.get(position);
                if (model instanceof AutoTextModel autoTextModel) {
                    agent.showDialogFragment(new AutoTextDialogFragment(autoTextModel,
                            () -> notifyItemChanged(position)));
                } else if (model instanceof TextModel textModel) {
                    agent.showDialogFragment(new EditTextDialogFragment(textModel,
                            () -> notifyItemChanged(position)));
                } else if (model instanceof SliderModel sliderModel) {
                    agent.showDialogFragment(new SliderDialogFragment(sliderModel,
                            () -> notifyItemChanged(position)));
                } else if (model instanceof SingleChoiceModel singleChoiceModel) {
                    agent.showDialogFragment(new SingleChoiceDialogFragment(singleChoiceModel,
                            () -> notifyItemChanged(position)));
                } else if (model instanceof GroupModel groupModel) {
                    setPageAndPush(groupModel.getLabelKey());
                } else if (model instanceof ActionModel actionModel) {
                    for (BaseAction action : actionModel.getActions()) {
                        if (action instanceof ActivityAction activityAction) {
                            agent.toActivity(activityAction.getActivityClass(),
                                    activityAction.getBundle(), position);
                        } else if (action instanceof FragmentAction fragmentAction) {
                            agent.toFragment(fragmentAction.getId(), fragmentAction.getBundle(),
                                    position);
                        } else if (action instanceof DirectionAction directionAction) {
                            agent.toDirection(directionAction.getDirections(), position);
                        } else {
                            throw new IllegalArgumentException("Unsupported action: " + action);
                        }

                        replaceTopPage();
                    }
                } else {
                    throw new IllegalArgumentException("Unsupported clickable item: " + model);
                }
            }

            @Override
            public void onItemClearButtonClick(int position) {
                List<BaseModel> list = getCurrentList();
                if (position < 0 || position >= list.size())
                    return;

                SavableModel savableModel = getSavableModel(position, list);
                Config.SourceType sourceType = savableModel.getCurrentSourceType();
                if (sourceType != null) {
                    savableModel.getConfig().get(savableModel.getCurrentSourceType())
                            .remove(savableModel.getDataKey());
                }

                notifyItemChanged(position);
            }
        };
    }

    @NonNull
    private static SavableModel getSavableModel(int position, List<BaseModel> list) {
        BaseModel model = list.get(position);
        SavableModel savableModel;
        if (model instanceof SavableModel) {
            savableModel = (SavableModel) model;
        } else if (model instanceof ActionModel actionModel) {
            savableModel = actionModel.getSubModel();
            if (savableModel == null)
                throw new IllegalStateException("Sub model is null.");
        } else {
            throw new UnsupportedOperationException("Model cannot be cleared: " + model);
        }
        return savableModel;
    }

    private void setPageAndPush(@NonNull String key) {
        setPage(key, null);
        BackModel backModel = new BackModel(currentPageKey, agent.getViewPosition());
        backStack.push(backModel);
    }

    private void popAndSetPage() {
        backStack.pop();
        BackModel backModel = backStack.peek();
        if (backModel == null)
            throw new IllegalStateException("Back stack is empty.");
        setPage(backModel.key, () -> agent.setViewPosition(backModel.position));
    }

    private void peekAndSetPage() {
        BackModel backModel = backStack.peek();
        if (backModel == null)
            return;
        setPage(backModel.key, () -> agent.setViewPosition(backModel.position));
    }

    private void replaceTopPage() {
        backStack.pop();
        backStack.push(new BackModel(currentPageKey, agent.getViewPosition()));
    }

    private void setPage(@NonNull String key, @Nullable Runnable commitCallback) {
        currentPageKey = key;
        GroupModel model = (GroupModel) getModel(currentPageKey);
        ArrayList<BaseModel> models = new ArrayList<>();
        for (String childKey : model.getChildrenKeys())
            models.add(getModel(childKey));
        submitList(models, commitCallback);
        agent.updateTitle(model.getTitleId());
    }

    private void updateModels() {
        for (Map.Entry<String, BaseModel> entry : modelMap.entrySet()) {
            if (entry.getValue() instanceof SwitchLinkModel switchLinkModel)
                updateSwitchLinkedModel(switchLinkModel);
        }
    }

    private void updateSwitchLinkedModel(@NonNull SwitchLinkModel model) {
        boolean on = model.getValue().getAsBool();
        for (String key : model.getHideWhenOnKeys())
            getModel(key).setHide(on);
        for (String key : model.getHideWhenOffKeys())
            getModel(key).setHide(!on);
    }

    @NonNull
    private ListItemSettingsCommonBinding createCommonBinding(@NonNull ViewGroup parent) {
        return ListItemSettingsCommonBinding.inflate(LayoutInflater.from(parent.getContext()),
                parent, false);
    }

    @NonNull
    private ListItemSettingsLabelBinding createLabelBinding(@NonNull ViewGroup parent) {
        return ListItemSettingsLabelBinding.inflate(LayoutInflater.from(parent.getContext()),
                parent, false);
    }

    @NonNull
    private BaseModel getModel(@NonNull String key) {
        BaseModel model = modelMap.get(key);
        if (model == null)
            throw new IllegalStateException("Model not found: " + key);
        return model;
    }

    private static class DiffCallback extends DiffUtil.ItemCallback<BaseModel> {

        @Override
        public boolean areItemsTheSame(@NonNull BaseModel oldItem, @NonNull BaseModel newItem) {
            return Objects.equals(oldItem.getLabelKey(), newItem.getLabelKey());
        }

        @Override
        public boolean areContentsTheSame(@NonNull BaseModel oldItem, @NonNull BaseModel newItem) {
            return Objects.equals(oldItem.getLabelKey(), newItem.getLabelKey());
        }
    }

    public record BackModel(String key, SettingAdapterAgent.ViewPosition position) {

    }
}
