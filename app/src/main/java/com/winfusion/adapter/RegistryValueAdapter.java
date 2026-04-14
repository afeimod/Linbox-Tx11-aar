package com.winfusion.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;

import com.winfusion.R;
import com.winfusion.adapter.common.ClickableListAdapter;
import com.winfusion.adapter.common.ItemCallback;
import com.winfusion.core.registry.data.DataType;
import com.winfusion.core.registry.data.RegistryData;
import com.winfusion.core.registry.exporter.HexUtils;
import com.winfusion.databinding.ListItemRegistryValueBinding;
import com.winfusion.model.RegistryValueModel;

import java.util.Objects;

public class RegistryValueAdapter
        extends ClickableListAdapter<RegistryValueModel, RegistryValueAdapter.RegistryValueViewHolder> {

    private static final int MAX_FLAT_DATA_LENGTH = 48;
    private static final String FLAT_DATA_DELIMITER = " ";
    private static final String FLAT_DATA_ELLIPSIS = "...";

    public RegistryValueAdapter(@NonNull ItemCallback itemCallback) {
        super(new DiffCallback(), itemCallback);
    }

    @NonNull
    @Override
    public RegistryValueViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ListItemRegistryValueBinding binding = ListItemRegistryValueBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);

        return new RegistryValueViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull RegistryValueViewHolder holder, int position) {
        holder.bind(getItem(position));
    }


    private static class DiffCallback extends DiffUtil.ItemCallback<RegistryValueModel> {

        @Override
        public boolean areItemsTheSame(@NonNull RegistryValueModel oldItem,
                                       @NonNull RegistryValueModel newItem) {

            return Objects.equals(oldItem.getValueName(), newItem.getValueName());
        }

        @Override
        public boolean areContentsTheSame(@NonNull RegistryValueModel oldItem,
                                          @NonNull RegistryValueModel newItem) {

            return Objects.equals(oldItem.getValueName(), newItem.getValueName()) &&
                    Objects.equals(oldItem.getValueData(), newItem.getValueData());
        }
    }

    public class RegistryValueViewHolder extends ClickableListAdapter.ViewHolder<RegistryValueModel,
            ListItemRegistryValueBinding> {

        public RegistryValueViewHolder(@NonNull ListItemRegistryValueBinding binding) {

            super(binding, RegistryValueAdapter.this.itemCallback);
        }

        @Override
        protected void setup(@NonNull RegistryValueModel model) {
            binding.getRoot().setOnClickListener(v ->
                    itemCallback.onClick(getAdapterPosition()));

            binding.getRoot().setOnLongClickListener(v ->
                    itemCallback.onLongClick(getAdapterPosition()));
        }

        @Override
        protected void update(@NonNull RegistryValueModel model) {
            RegistryData data = model.getValueData();
            int iconId;
            String typeName;
            String flatData;
            if (data != null) {
                DataType type = data.getDataType();
                switch (type) {
                    case REG_NONE:
                        iconId = R.drawable.ic_file_bin;
                        typeName = "NONE";
                        flatData = bytesToFlat(data.getAsNone().toBytes());
                        break;
                    case REG_SZ:
                        iconId = R.drawable.ic_file_txt;
                        typeName = "SZ";
                        flatData = stringToFlat(data.getAsString().getString());
                        break;
                    case REG_EXPAND_SZ:
                        iconId = R.drawable.ic_file_txt;
                        typeName = "EXPAND_SZ";
                        flatData = stringToFlat(data.getAsExpandString().getString());
                        break;
                    case REG_BINARY:
                        iconId = R.drawable.ic_file_bin;
                        typeName = "BINARY";
                        flatData = bytesToFlat(data.getAsBinary().toBytes());
                        break;
                    case REG_DWORD:
                        iconId = R.drawable.ic_file_num;
                        typeName = "DWORD";
                        flatData = Integer.toUnsignedString(data.getAsDword().toSignedInt());
                        break;
                    case REG_DWORD_BIG_ENDIAN:
                        iconId = R.drawable.ic_file_num;
                        typeName = "DWORD_BE";
                        flatData = stringToFlat(String.valueOf(data.getAsDwordBigEndian().toUnsignedLong()));
                        break;
                    case REG_LINK:
                        iconId = R.drawable.ic_file_bin;
                        typeName = "LINK";
                        flatData = bytesToFlat(data.getAsLink().toBytes());
                        break;
                    case REG_MULTI_SZ:
                        iconId = R.drawable.ic_file_txt;
                        typeName = "MULTI_SZ";
                        flatData = stringArrayToFlat(data.getAsMultiString().getStringArray());
                        break;
                    case REG_RESOURCE_LIST:
                        iconId = R.drawable.ic_file_bin;
                        typeName = "RES_LIST";
                        flatData = bytesToFlat(data.getAsResourceList().toBytes());
                        break;
                    case REG_FULL_RESOURCE_DESCRIPTOR:
                        iconId = R.drawable.ic_file_bin;
                        typeName = "RES_DESC";
                        flatData = bytesToFlat(data.getAsFullResourceDescriptor().toBytes());
                        break;
                    case REG_RESOURCE_REQUIREMENTS_LIST:
                        iconId = R.drawable.ic_file_bin;
                        typeName = "RES_REQ";
                        flatData = bytesToFlat(data.getAsResourceRequirementsList().toBytes());
                        break;
                    case REG_QWORD:
                        iconId = R.drawable.ic_file_num;
                        typeName = "QWORD";
                        flatData = stringToFlat(Long.toUnsignedString(data.getAsQword().toSignedLong()));
                        break;
                    case REG_RAW:
                        char[] chars = new char[8];
                        iconId = R.drawable.ic_file_bin;
                        HexUtils.intToHexCharsN(data.getAsRaw().getId(), chars);
                        typeName = String.valueOf(chars);
                        flatData = bytesToFlat(data.getAsRaw().toBytes());
                        break;
                    default:
                        throw new IllegalArgumentException("Unsupported data type: " + type);
                }
            } else {
                iconId = R.drawable.ic_file_txt;
                typeName = "SZ";
                flatData = model.getFlatData();
            }

            binding.imageValueIcon.setImageResource(iconId);
            binding.textValueType.setText(typeName);
            binding.textValueName.setText(model.getValueName());
            binding.textValueName.setSelected(true);
            binding.textValueData.setText(flatData);
            binding.textValueData.setSelected(true);
        }

        @NonNull
        private String bytesToFlat(@NonNull byte[] bytes) {
            char[] chars = new char[2];
            int length = MAX_FLAT_DATA_LENGTH / 2;
            StringBuilder builder = new StringBuilder();

            for (int i = 0; i < bytes.length; i++) {
                HexUtils.byteToHexChars(bytes[i], chars);
                builder.append(chars[0]);
                builder.append(chars[1]);
                if (i >= length) {
                    builder.append("...");
                    break;
                }
                if (i != bytes.length - 1)
                    builder.append(FLAT_DATA_DELIMITER);
            }

            return builder.toString();
        }

        @NonNull
        private String stringToFlat(@NonNull String s) {
            return s.length() >= MAX_FLAT_DATA_LENGTH ?
                    s.substring(0, MAX_FLAT_DATA_LENGTH) + FLAT_DATA_ELLIPSIS : s;
        }

        @NonNull
        private String stringArrayToFlat(@NonNull String[] array) {
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < array.length; i++) {
                builder.append(array[i]);
                if (builder.length() > MAX_FLAT_DATA_LENGTH)
                    break;
                if (i != array.length - 1)
                    builder.append(FLAT_DATA_DELIMITER);
            }
            return stringToFlat(builder.toString());
        }
    }
}
