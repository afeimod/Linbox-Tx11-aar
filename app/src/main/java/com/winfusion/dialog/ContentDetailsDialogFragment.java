package com.winfusion.dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textview.MaterialTextView;
import com.winfusion.R;
import com.winfusion.core.soundfont.SoundFontInfo;
import com.winfusion.core.wfp.Wfp;
import com.winfusion.databinding.DialogContentDetailsBinding;
import com.winfusion.utils.TextUtils;
import com.winfusion.utils.UiUtils;

import java.util.Map;

public class ContentDetailsDialogFragment extends DialogFragment {

    private DialogContentDetailsBinding binding;
    private ContentDetailsDialogFragmentArgs args;
    private SoundFontInfo sfi = null;
    private Wfp wfp = null;
    private Runnable positiveCallback = null;
    private Runnable negativeCallback = null;

    public ContentDetailsDialogFragment() {
        super();
    }

    public ContentDetailsDialogFragment(@NonNull SoundFontInfo sfi, @Nullable Runnable positiveCallback,
                                        @Nullable Runnable negativeCallback) {

        this.sfi = sfi;
        this.positiveCallback = positiveCallback;
        this.negativeCallback = negativeCallback;
    }

    public ContentDetailsDialogFragment(@NonNull Wfp wfp, @Nullable Runnable positiveCallback,
                                        @Nullable Runnable negativeCallback) {

        this.wfp = wfp;
        this.positiveCallback = positiveCallback;
        this.negativeCallback = negativeCallback;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        args = ContentDetailsDialogFragmentArgs.fromBundle(getArguments());

        if (sfi == null && wfp == null)
            dismissAllowingStateLoss();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        binding = DialogContentDetailsBinding.inflate(getLayoutInflater());

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.details)
                .setView(binding.getRoot());

        int positiveBtn = args.getPositiveBtn() == -1 ? android.R.string.ok : args.getPositiveBtn();
        int negativeBtn = args.getNegativeBtn() == -1 ? android.R.string.cancel : args.getNegativeBtn();

        builder.setPositiveButton(positiveBtn, (dialog, which) -> {
            if (positiveCallback != null)
                positiveCallback.run();
        });

        if (negativeCallback != null) {
            builder.setNegativeButton(negativeBtn, (dialog, which) -> negativeCallback.run());
            builder.setCancelable(false);
            setCancelable(false);
        }

        return builder.create();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        if (sfi != null)
            setupSoundfontUi();
        else if (wfp != null)
            setupWfpUi();

        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        sfi = null;
        wfp = null;
    }

    private void setupSoundfontUi() {
        addRow(R.string.name, sfi.getName());
        addRow(R.string.sound_engine, sfi.getSoundEngine());
        addRow(R.string.version, version(sfi.getVersionMajor(), sfi.getVersionMinor()));
        addRow(R.string.rom_name, sfi.getRomName());
        addRow(R.string.rom_version, version(sfi.getROMVersionMajor(), sfi.getROMVersionMinor()));
        addRow(R.string.creation_date, sfi.getCreationDate());
        addRow(R.string.engineer, sfi.getEngineer());
        addRow(R.string.product, sfi.getProduct());
        addRow(R.string.copyright, sfi.getCopyright());
        addRow(R.string.comment, sfi.getComment());
        addRow(R.string.software, sfi.getSoftware());
    }

    private void setupWfpUi() {
        addRow(R.string.type, wfp.getWfpType().name());
        addRow(R.string.name, wfp.getName());
        addRow(R.string.author, wfp.getAuthor());
        addRow(R.string.package_copyright, wfp.getPackageCopyright());
        addRow(R.string.package_license, wfp.getPackageLicense());
        addRow(R.string.library_copyright, wfp.getLibraryCopyright());
        addRow(R.string.library_license, wfp.getLibraryLicense());
        addRow(R.string.comment, wfp.getComment());
        addRow(R.string.details, wfp.getDetails());
        addRow(R.string.property, buildPropertyStringFromMap(wfp.getProperty()));
    }

    @Nullable
    private String buildPropertyStringFromMap(@Nullable Map<String, String> map) {
        if (map == null)
            return null;

        StringBuilder builder = new StringBuilder();
        for (Map.Entry<String, String> entry : map.entrySet())
            builder.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");

        return builder.toString();
    }

    @Nullable
    private String version(@NonNull int... array) {
        try {
            StringBuilder builder = new StringBuilder();
            for (int i : array)
                builder.append(i).append(".");
            builder.deleteCharAt(builder.length() - 1);
            return builder.toString();
        } catch (Exception e) {
            return null;
        }
    }

    private void addRow(@StringRes int resId, @Nullable String str) {
        addRow(getString(resId), str);
    }

    private void addRow(@NonNull String str1, @Nullable String str2) {
        int margin = UiUtils.dpToPx(requireContext(), 4f);
        int paddingStart = UiUtils.dpToPx(requireContext(), 16f);

        TableRow.LayoutParams layoutParams1 = new TableRow.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        layoutParams1.setMargins(margin, 0, margin, 0);
        MaterialTextView view1 = new MaterialTextView(requireContext(), null,
                com.google.android.material.R.attr.textAppearanceTitleMedium);
        view1.setLayoutParams(layoutParams1);
        view1.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
        view1.setText(str1);

        TableRow.LayoutParams layoutParams2 = new TableRow.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        layoutParams2.setMargins(0, margin, 0, margin);
        MaterialTextView view2 = new MaterialTextView(requireContext(), null,
                com.google.android.material.R.attr.textAppearanceBodyMedium);
        view2.setLayoutParams(layoutParams2);
        view2.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
        if (getResources().getConfiguration().getLayoutDirection() == View.LAYOUT_DIRECTION_LTR)
            view2.setPadding(paddingStart, 0, 0, 0);
        else
            view2.setPadding(0, 0, paddingStart, 0);
        if (str2 == null)
            view2.setText(R.string.null_data);
        else
            view2.setText(str2);

        TableLayout.LayoutParams tableLayoutParams = new TableLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        TableRow row1 = new TableRow(requireContext());
        tableLayoutParams.setMargins(0, margin, 0, margin);
        row1.setLayoutParams(tableLayoutParams);
        row1.addView(view1);

        TableRow row2 = new TableRow(requireContext());
        row2.setLayoutParams(tableLayoutParams);
        row2.addView(view2);
        row2.setOnLongClickListener(v -> {
            TextUtils.copyTextToClipboard(requireContext(), view2.getText().toString());
            return true;
        });

        binding.tableDetails.addView(row1);
        binding.tableDetails.addView(row2);
    }
}
