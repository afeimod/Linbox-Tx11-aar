package com.winfusion.dialog;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.app.Dialog;
import android.os.Bundle;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.winfusion.R;
import com.winfusion.core.registry.data.BaseHexData;
import com.winfusion.core.registry.data.BaseStringData;
import com.winfusion.core.registry.data.BinaryData;
import com.winfusion.core.registry.data.DataType;
import com.winfusion.core.registry.data.DoubleWordBigEndianData;
import com.winfusion.core.registry.data.DoubleWordData;
import com.winfusion.core.registry.data.ExpandStringData;
import com.winfusion.core.registry.data.FullResourceDescriptorData;
import com.winfusion.core.registry.data.LinkData;
import com.winfusion.core.registry.data.MultiStringData;
import com.winfusion.core.registry.data.NoneData;
import com.winfusion.core.registry.data.QuadWordData;
import com.winfusion.core.registry.data.RawData;
import com.winfusion.core.registry.data.RegistryData;
import com.winfusion.core.registry.data.ResourceListData;
import com.winfusion.core.registry.data.ResourceRequirementsListData;
import com.winfusion.core.registry.data.StringData;
import com.winfusion.databinding.DialogEditRegistryValueBinding;
import com.winfusion.utils.NoFilterArrayAdapter;
import com.winfusion.utils.SimpleTextWatcher;
import com.winfusion.utils.TextChecker;
import com.winfusion.utils.TextUtils;

import java.math.BigInteger;
import java.util.Objects;
import java.util.Set;
import java.util.StringJoiner;
import java.util.function.Consumer;
import java.util.regex.Pattern;

public class EditRegistryValueDialogFragment extends DialogFragment {

    private final Consumer<RegistryValue> callback;
    private final RegistryValue originalValue;
    private final Set<String> unavailableNames;
    private final Pattern hexPattern = Pattern.compile("^0[xX][0-9A-Fa-f]+$");
    private DialogEditRegistryValueBinding binding;
    private DataType currentType = DataType.REG_SZ;
    private TextCheckerBuilder.Base currentBase = TextCheckerBuilder.Base.Dec;
    private TextChecker currentValueTextChecker;
    private TextChecker nameTextChecker;

    /**
     * 包含名称、默认值状态等信息的注册表值类。
     *
     * @param name      名称
     * @param data      数据
     * @param isDefault 是否为默认值
     */
    public record RegistryValue(String name, RegistryData data, boolean isDefault) {

    }

    public EditRegistryValueDialogFragment() {
        callback = null;
        originalValue = null;
        unavailableNames = null;
    }

    public EditRegistryValueDialogFragment(@NonNull Consumer<RegistryValue> callback,
                                           @NonNull RegistryValue originalValue,
                                           @NonNull Set<String> unavailableNames) {

        this.callback = callback;
        this.originalValue = originalValue;
        this.unavailableNames = unavailableNames;

        if (originalValue.data != null) {
            currentType = originalValue.data.getDataType();
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (callback == null)
            dismissAllowingStateLoss();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        binding = DialogEditRegistryValueBinding.inflate(getLayoutInflater());
        return new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.edit_registry_value)
                .setView(binding.getRoot())
                .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                    if (binding.textLayoutValue.isErrorEnabled() || binding.textLayoutName.isErrorEnabled())
                        return;
                    callback.accept(toRegistryValue());
                })
                .setNegativeButton(android.R.string.cancel, null)
                .create();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setupTypeText();
        setupNameText();
        setupValueText();
        setupBaseButtons();
        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void setupTypeText() {
        NoFilterArrayAdapter<String> adapter = new NoFilterArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                getResources().getStringArray(R.array.registry_value_type_entries)
        );
        binding.autoTextType.setAdapter(adapter);
        binding.autoTextType.setOnItemClickListener((parent, view, position, id) -> {
            String typeName = getResources().getStringArray(R.array.registry_value_type_values)[position];
            currentType = DataType.valueOf(typeName);
            updateValueTextChecker();
            updateBaseButtonsGroup();
            forceRefreshEditText(binding.editValue);
        });

        int pos = TextUtils.tryGetStringPosInArrayRes(requireContext(),
                R.array.registry_value_type_values, currentType.name());
        if (pos == -1) {
            binding.autoTextType.setText(currentType.name());
            updateValueTextChecker();
            updateBaseButtonsGroup();
            forceRefreshEditText(binding.editValue);
        } else {
            binding.autoTextType.getOnItemClickListener().onItemClick(null, null, pos, 0);
            binding.autoTextType.setText(getResources().getStringArray(R.array.registry_value_type_entries)[pos],
                    false);
        }
    }

    private void setupNameText() {
        nameTextChecker = new TextChecker() {
            private int tipsId;

            @Override
            public boolean check(@NonNull String textStr) {
                if (unavailableNames.contains(textStr.toLowerCase())) {
                    tipsId = R.string.name_duplicate_description;
                    return false;
                }
                if (textStr.isEmpty()) {
                    tipsId = R.string.name_invalid_description;
                    return false;
                }
                return true;
            }

            @Override
            public int getTipsId() {
                return tipsId;
            }
        };
        binding.editName.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                if (nameTextChecker.check(s.toString())) {
                    binding.textLayoutName.setErrorEnabled(false);
                } else {
                    binding.textLayoutName.setError(getResources().getString(nameTextChecker.getTipsId()));
                    binding.textLayoutName.setErrorEnabled(true);
                }
            }
        });
        if (originalValue.isDefault)
            binding.textLayoutName.setVisibility(GONE);
        else
            binding.editName.setText(originalValue.name);
    }

    private void setupValueText() {
        binding.editValue.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                if (currentValueTextChecker.check(s.toString())) {
                    binding.textLayoutValue.setErrorEnabled(false);
                } else {
                    binding.textLayoutValue.setError(getResources().getString(currentValueTextChecker.getTipsId()));
                    binding.textLayoutValue.setErrorEnabled(true);
                }
            }
        });
        if (originalValue.data != null)
            binding.editValue.setText(dataToString(originalValue.data));
    }

    private void setupBaseButtons() {
        binding.groupNumberBase.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (!isChecked)
                return;

            TextCheckerBuilder.Base base;
            CharSequence text = binding.editValue.getText();
            if (text == null)
                return;
            String newText = text.toString();

            if (checkedId == R.id.button_decimal) {
                base = TextCheckerBuilder.Base.Dec;
                newText = hex2UnsignedDec(newText);
            } else if (checkedId == R.id.button_hex) {
                base = TextCheckerBuilder.Base.Hex;
                newText = unsignedDec2Hex(newText);
            } else {
                throw new IllegalArgumentException("Unknown checked id: " + checkedId);
            }

            currentBase = base;
            updateValueTextChecker();
            binding.editValue.setText(newText);
        });
    }

    private void updateValueTextChecker() {
        currentValueTextChecker = TextCheckerBuilder.build(currentType, currentBase);
    }

    private void updateBaseButtonsGroup() {
        if (currentType == DataType.REG_DWORD || currentType == DataType.REG_DWORD_BIG_ENDIAN ||
                currentType == DataType.REG_QWORD)
            binding.groupNumberBase.setVisibility(VISIBLE);
        else
            binding.groupNumberBase.setVisibility(GONE);
    }

    private void forceRefreshEditText(@NonNull EditText editText) {
        Editable editable = editText.getText();
        if (editable != null)
            editText.setText(editable);
    }

    @NonNull
    private String dataToString(@NonNull RegistryData data) {
        return switch (data.getDataType()) {
            case REG_NONE, REG_BINARY, REG_LINK, REG_RESOURCE_LIST, REG_FULL_RESOURCE_DESCRIPTOR,
                 REG_RESOURCE_REQUIREMENTS_LIST, REG_RAW -> {
                BaseHexData d = (BaseHexData) data;
                StringJoiner joiner = new StringJoiner(" ");
                for (byte b : d.toBytes())
                    joiner.add(String.format("%02x", b & 0xFF));
                yield joiner.toString();
            }
            case REG_SZ, REG_EXPAND_SZ -> ((BaseStringData) data).getString();
            case REG_DWORD -> Long.toString(data.getAsDword().toUnsignedLong());
            case REG_DWORD_BIG_ENDIAN -> Long.toString(data.getAsDwordBigEndian().toUnsignedLong());
            case REG_MULTI_SZ -> {
                StringJoiner joiner = new StringJoiner("\n");
                for (String s : data.getAsMultiString().getStringArray())
                    joiner.add(s);
                yield joiner.toString();
            }
            case REG_QWORD -> data.getAsQword().toBigInteger().toString(10);
        };
    }

    @NonNull
    private RegistryValue toRegistryValue() {
        String value = Objects.requireNonNull(binding.editValue.getText()).toString();
        String name = Objects.requireNonNull(binding.editName.getText()).toString();

        RegistryData data = switch (currentType) {
            case REG_NONE -> new NoneData(string2Bytes(value));
            case REG_SZ -> new StringData(value);
            case REG_EXPAND_SZ -> new ExpandStringData(value);
            case REG_BINARY -> new BinaryData(string2Bytes(value));
            case REG_DWORD -> new DoubleWordData(string2Integer(value, currentBase).longValue());
            case REG_DWORD_BIG_ENDIAN ->
                    new DoubleWordBigEndianData(string2Integer(value, currentBase).longValue());
            case REG_LINK -> new LinkData(string2Bytes(value));
            case REG_MULTI_SZ -> new MultiStringData(value.split("\n"));
            case REG_RESOURCE_LIST -> new ResourceListData(string2Bytes(value));
            case REG_FULL_RESOURCE_DESCRIPTOR ->
                    new FullResourceDescriptorData(string2Bytes(value));
            case REG_RESOURCE_REQUIREMENTS_LIST ->
                    new ResourceRequirementsListData(string2Bytes(value));
            case REG_QWORD -> new QuadWordData(string2Integer(value, currentBase));
            case REG_RAW -> new RawData(originalValue.data.getAsRaw().getId(), string2Bytes(value));
        };

        return new RegistryValue(name, data, originalValue.isDefault);
    }

    @NonNull
    private byte[] string2Bytes(@NonNull String str) {
        String[] strings = str.split(" ");
        byte[] bytes = new byte[strings.length];
        for (int i = 0; i < strings.length; i++)
            bytes[i] = Byte.parseByte(strings[i], 16);
        return bytes;
    }

    @NonNull
    private BigInteger string2Integer(@NonNull String s, @NonNull TextCheckerBuilder.Base base) {
        return new BigInteger(s, switch (base) {
            case Dec -> 10;
            case Hex -> 16;
        });
    }

    @NonNull
    private String hex2UnsignedDec(@NonNull String hex) {
        BigInteger value;
        if (!hexPattern.matcher(hex).matches())
            return hex;
        try {
            value = new BigInteger(hex.substring(2), 16);
        } catch (NumberFormatException e) {
            return hex;
        }
        return value.toString();
    }

    @NonNull
    private String unsignedDec2Hex(@NonNull String dec) {
        BigInteger value;
        try {
            value = new BigInteger(dec);
        } catch (NumberFormatException e) {
            return dec;
        }
        return "0x" + value.toString(16);
    }

    private final static class TextCheckerBuilder {

        private static final BigInteger DWORD_MAX = new BigInteger("FFFFFFFF", 16);
        private static final BigInteger QWORD_MAX = new BigInteger("FFFFFFFFFFFFFFFF", 16);

        public enum Base {
            Dec,
            Hex
        }

        @NonNull
        public static TextChecker build(@NonNull DataType type, @NonNull Base base) {
            return switch (type) {
                case REG_NONE, REG_BINARY, REG_LINK, REG_RESOURCE_LIST,
                     REG_FULL_RESOURCE_DESCRIPTOR, REG_RESOURCE_REQUIREMENTS_LIST, REG_RAW ->
                        buildBinaryChecker();
                case REG_SZ, REG_EXPAND_SZ -> buildStringChecker();
                case REG_DWORD, REG_DWORD_BIG_ENDIAN -> buildDwordChecker(base);
                case REG_MULTI_SZ -> buildMultiStringChecker();
                case REG_QWORD -> buildQwordChecker(base);
            };
        }

        @NonNull
        private static TextChecker buildBinaryChecker() {
            return new TextChecker() {
                private final Pattern pattern = Pattern.compile("^[0-9A-Fa-f]{2}$");

                @Override
                public boolean check(@NonNull String textStr) {
                    if (textStr.isEmpty())
                        return true;

                    String[] bytes = textStr.split(" ");
                    for (String s : bytes) {
                        if (!pattern.matcher(s).matches())
                            return false;
                    }

                    return true;
                }

                @Override
                public int getTipsId() {
                    return R.string.invalid_input_binary_description;
                }
            };
        }

        @NonNull
        private static TextChecker buildStringChecker() {
            return new TextChecker() {
                @Override
                public boolean check(@NonNull String textStr) {
                    return !textStr.contains("\n");
                }

                @Override
                public int getTipsId() {
                    return R.string.invalid_input_string_description;
                }
            };
        }

        @NonNull
        private static TextChecker buildDwordChecker(Base base) {
            return switch (base) {
                case Dec -> buildDwordDecChecker();
                case Hex -> buildDwordHexChecker();
            };
        }

        @NonNull
        private static TextChecker buildDwordDecChecker() {
            return new TextChecker() {
                private int tipsId;

                @Override
                public boolean check(@NonNull String textStr) {
                    BigInteger value;
                    try {
                        value = new BigInteger(textStr);
                    } catch (NumberFormatException e) {
                        tipsId = R.string.invalid_input_decimal_description;
                        return false;
                    }

                    if (value.compareTo(BigInteger.ZERO) < 0 || value.compareTo(DWORD_MAX) > 0) {
                        tipsId = R.string.dword_decimal_range_description;
                        return false;
                    }

                    return true;
                }

                @Override
                public int getTipsId() {
                    return tipsId;
                }
            };
        }

        @NonNull
        private static TextChecker buildDwordHexChecker() {
            return new TextChecker() {
                private int tipsId;

                @Override
                public boolean check(@NonNull String textStr) {
                    if (!textStr.matches("^0[xX][0-9A-Fa-f]+$")) {
                        tipsId = R.string.invalid_input_hexadecimal_description;
                        return false;
                    }

                    BigInteger value;
                    try {
                        value = new BigInteger(textStr.substring(2), 16);
                    } catch (NumberFormatException e) {
                        tipsId = R.string.invalid_input_hexadecimal_description;
                        return false;
                    }

                    if (value.compareTo(BigInteger.ZERO) < 0 || value.compareTo(DWORD_MAX) > 0) {
                        tipsId = R.string.dword_hexadecimal_range_description;
                        return false;
                    }

                    return true;
                }

                @Override
                public int getTipsId() {
                    return tipsId;
                }
            };
        }

        @NonNull
        private static TextChecker buildMultiStringChecker() {
            return new TextChecker() {
                @Override
                public boolean check(@NonNull String textStr) {
                    String[] strings = textStr.split("\n");
                    long lfCount = textStr.chars().filter(c -> c == '\n').count();
                    return strings.length == lfCount;
                }

                @Override
                public int getTipsId() {
                    return R.string.invalid_input_multi_string_description;
                }
            };
        }

        @NonNull
        private static TextChecker buildQwordChecker(Base base) {
            return switch (base) {
                case Dec -> buildQwordDecChecker();
                case Hex -> buildQwordHexChecker();
            };
        }

        @NonNull
        private static TextChecker buildQwordDecChecker() {
            return new TextChecker() {
                private int tipsId;

                @Override
                public boolean check(@NonNull String textStr) {
                    BigInteger value;
                    try {
                        value = new BigInteger(textStr);
                    } catch (NumberFormatException e) {
                        tipsId = R.string.invalid_input_decimal_description;
                        return false;
                    }

                    if (value.compareTo(BigInteger.ZERO) < 0 || value.compareTo(QWORD_MAX) > 0) {
                        tipsId = R.string.qword_decimal_range_description;
                        return false;
                    }

                    return true;
                }

                @Override
                public int getTipsId() {
                    return tipsId;
                }
            };
        }

        @NonNull
        private static TextChecker buildQwordHexChecker() {
            return new TextChecker() {
                private int tipsId;

                @Override
                public boolean check(@NonNull String textStr) {
                    if (!textStr.matches("^0[xX][0-9A-Fa-f]+$")) {
                        tipsId = R.string.invalid_input_hexadecimal_description;
                        return false;
                    }

                    BigInteger value;
                    try {
                        value = new BigInteger(textStr.substring(2), 16);
                    } catch (NumberFormatException e) {
                        tipsId = R.string.invalid_input_hexadecimal_description;
                        return false;
                    }

                    if (value.compareTo(BigInteger.ZERO) < 0 || value.compareTo(QWORD_MAX) > 0) {
                        tipsId = R.string.qword_hexadecimal_range_description;
                        return false;
                    }

                    return true;
                }

                @Override
                public int getTipsId() {
                    return tipsId;
                }
            };
        }
    }
}
