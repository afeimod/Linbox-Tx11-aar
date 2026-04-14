package com.winfusion.feature.input.overlay.popupwindow.ui;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.winfusion.R;
import com.winfusion.databinding.DialogKeyboardSelectorBinding;
import com.winfusion.databinding.ListItemBindingBinding;
import com.winfusion.feature.input.key.StandardAction;
import com.winfusion.feature.input.key.StandardButton;
import com.winfusion.feature.input.key.StandardItem;
import com.winfusion.feature.input.key.StandardKey;
import com.winfusion.feature.input.overlay.bind.Binding;
import com.winfusion.utils.NoFilterArrayAdapter;
import com.winfusion.utils.UiUtils;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class BindingUiHandler {

    private static final Map<StandardItem, LocalStandardItem> LocalStandardItemPool = new ConcurrentHashMap<>();

    public void update(@NonNull ListItemBindingBinding binding, @NonNull BindingProvider provider) {
        Context context = binding.getRoot().getContext();
        Binding keyBinding = provider.getBinding();

        if (keyBinding != null) {
            binding.autoTextType.setText(UiUtils.getEntryByValue(context, keyBinding.getType().name(),
                    R.array.binding_type_entries, R.array.binding_type_values));
            binding.autoTextValue.setText(getLocalStandardItem(context, keyBinding.getItem()).toString());
            updateValueAdapter(binding, keyBinding.getType());
            binding.buttonKeyboard.setVisibility(keyBinding.getType() == Binding.Type.Keyboard ? VISIBLE : GONE);
        } else {
            binding.autoTextType.setText("");
            binding.autoTextValue.setText("");
            binding.autoTextValue.setAdapter(null);
            binding.buttonKeyboard.setVisibility(GONE);
        }

        binding.autoTextType.setAdapter(new NoFilterArrayAdapter<>(
                context,
                android.R.layout.simple_dropdown_item_1line,
                context.getResources().getStringArray(R.array.binding_type_entries)
        ));
        binding.autoTextType.setOnItemClickListener((parent, view, position, id) -> {
            String typeName = context.getResources().getStringArray(R.array.binding_type_values)[position];
            if (typeName.equals("None")) {
                binding.autoTextValue.setText("");
                binding.autoTextValue.setAdapter(null);
                binding.buttonKeyboard.setVisibility(GONE);
                provider.updateBinding(null);
            } else {
                Binding.Type type = Binding.Type.valueOf(typeName);
                updateValueAdapter(binding, type);
                binding.autoTextValue.setText(binding.autoTextValue.getAdapter().getItem(0).toString());
                provider.updateBinding(new Binding(
                        Binding.Type.valueOf(context.getResources().getStringArray(R.array.binding_type_values)[position]),
                        ((LocalStandardItem) (binding.autoTextValue.getAdapter().getItem(0))).item));

                binding.buttonKeyboard.setVisibility(type == Binding.Type.Keyboard ? VISIBLE : GONE);
            }
        });
        binding.autoTextValue.setOnItemClickListener((parent, view, position, id) ->
                provider.updateBinding(new Binding(
                        Objects.requireNonNull(provider.getBinding()).getType(),
                        ((LocalStandardItem) (binding.autoTextValue.getAdapter().getItem(position))).item)));

        binding.buttonKeyboard.setOnClickListener(v -> {
            DialogKeyboardSelectorBinding b = DialogKeyboardSelectorBinding.inflate(LayoutInflater.from(context));
            b.keyboardSelector.setSelectedKey((StandardKey) provider.getBinding().getItem());
            Dialog dialog = new MaterialAlertDialogBuilder(context)
                    .setView(b.getRoot())
                    .setOnDismissListener(dialog1 -> {
                        StandardKey key = b.keyboardSelector.getSelectedKey();
                        if (key == null)
                            key = StandardKey.None;
                        provider.updateBinding(new Binding(Binding.Type.Keyboard, key));
                        binding.autoTextValue.setText(getLocalStandardItem(context, key).i18n);
                    })
                    .create();
            Objects.requireNonNull(dialog.getWindow()).setLayout(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            dialog.show();
        });
    }

    private void updateValueAdapter(@NonNull ListItemBindingBinding binding, @NonNull Binding.Type type) {
        Context context = binding.getRoot().getContext();
        binding.autoTextValue.setAdapter(new NoFilterArrayAdapter<>(
                context,
                android.R.layout.simple_dropdown_item_1line,
                getEntriesByType(context, type)
        ));
    }

    @NonNull
    private Object[] getEntriesByType(@NonNull Context context, @NonNull Binding.Type type) {
        return switch (type) {
            case Keyboard -> buildKeyboardEntries(context);
            case MouseButton -> buildMouseButtonEntries(context);
            case MouseAction -> buildMouseActionEntries(context);
            case ControllerButton -> buildControllerButtonEntries(context);
            case ControllerAction -> buildControllerActionEntries(context);
        };
    }

    @NonNull
    private Object[] buildKeyboardEntries(@NonNull Context context) {
        StandardKey[] keys = StandardKey.values();
        LocalStandardItem[] items = new LocalStandardItem[keys.length];
        for (int i = 0; i < keys.length; i++)
            items[i] = getLocalStandardItem(context, keys[i]);
        return items;
    }

    @NonNull
    private Object[] buildMouseButtonEntries(@NonNull Context context) {
        return new LocalStandardItem[]{
                getLocalStandardItem(context, StandardButton.BtnLeft),
                getLocalStandardItem(context, StandardButton.BtnRight),
                getLocalStandardItem(context, StandardButton.BtnMiddle)
        };
    }

    @NonNull
    private Object[] buildMouseActionEntries(@NonNull Context context) {
        return new LocalStandardItem[]{
                getLocalStandardItem(context, StandardAction.MouseMoveUp),
                getLocalStandardItem(context, StandardAction.MouseMoveDown),
                getLocalStandardItem(context, StandardAction.MouseMoveLeft),
                getLocalStandardItem(context, StandardAction.MouseMoveRight),
                getLocalStandardItem(context, StandardAction.MouseScrollUp),
                getLocalStandardItem(context, StandardAction.MouseScrollDown)
        };
    }

    @NonNull
    private Object[] buildControllerButtonEntries(@NonNull Context context) {
        return new LocalStandardItem[]{
                getLocalStandardItem(context, StandardButton.BtnA),
                getLocalStandardItem(context, StandardButton.BtnB),
                getLocalStandardItem(context, StandardButton.BtnX),
                getLocalStandardItem(context, StandardButton.BtnY),
                getLocalStandardItem(context, StandardButton.BtnL1),
                getLocalStandardItem(context, StandardButton.BtnR1),
                getLocalStandardItem(context, StandardButton.BtnL2),
                getLocalStandardItem(context, StandardButton.BtnR2),
                getLocalStandardItem(context, StandardButton.BtnL3),
                getLocalStandardItem(context, StandardButton.BtnR3),
                getLocalStandardItem(context, StandardButton.BtnDPadUp),
                getLocalStandardItem(context, StandardButton.BtnDPadDown),
                getLocalStandardItem(context, StandardButton.BtnDPadLeft),
                getLocalStandardItem(context, StandardButton.BtnDPadRight),
                getLocalStandardItem(context, StandardButton.BtnSelect),
                getLocalStandardItem(context, StandardButton.BtnStart)
        };
    }

    @NonNull
    private Object[] buildControllerActionEntries(@NonNull Context context) {
        return new LocalStandardItem[]{
                getLocalStandardItem(context, StandardAction.LeftThumbUp),
                getLocalStandardItem(context, StandardAction.LeftThumbDown),
                getLocalStandardItem(context, StandardAction.LeftThumbLeft),
                getLocalStandardItem(context, StandardAction.LeftThumbRight),
                getLocalStandardItem(context, StandardAction.RightThumbUp),
                getLocalStandardItem(context, StandardAction.RightThumbDown),
                getLocalStandardItem(context, StandardAction.RightThumbLeft),
                getLocalStandardItem(context, StandardAction.RightThumbRight)
        };
    }

    @NonNull
    private LocalStandardItem getLocalStandardItem(@NonNull Context context, @NonNull StandardItem item) {
        LocalStandardItem i18nItem = LocalStandardItemPool.get(item);
        if (i18nItem != null)
            return i18nItem;

        String i18nText = item.getResId() == 0 ? item.getSymbol() : context.getString(item.getResId());
        i18nItem = new LocalStandardItem(item, i18nText);
        LocalStandardItemPool.put(item, i18nItem);

        return i18nItem;
    }

    public interface BindingProvider {

        @Nullable
        Binding getBinding();

        void updateBinding(@Nullable Binding binding);
    }

    private record LocalStandardItem(StandardItem item, String i18n) {

        private LocalStandardItem(@NonNull StandardItem item, @NonNull String i18n) {
            this.item = item;
            this.i18n = i18n;
        }

        @NonNull
        @Override
        public String toString() {
            return i18n;
        }
    }
}
