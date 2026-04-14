package com.winfusion.feature.input.overlay.popupwindow;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.PopupWindow;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.winfusion.R;
import com.winfusion.databinding.PopupFloatContainerBinding;

import java.util.Objects;

public abstract class FloatHeaderPopupWindow extends PopupWindow {

    private final Context context;
    private boolean expanded = true;
    private PopupFloatContainerBinding binding;
    private View primaryView;
    private String titleStr;
    private int titleId;
    private int downX;
    private int downY;
    private boolean expandBtnEnabled = true;
    private boolean closeBtnEnabled = true;
    private int alpha = 0;
    private int lastX;
    private int lastY;

    public FloatHeaderPopupWindow(@NonNull Context context) {
        super(context);
        setTouchInterceptor(null);
        setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        this.context = context;
    }

    public void destroy() {
        dismiss();
        onDestroyView();
        setContentView(null);
        binding = null;
        primaryView = null;
    }

    public void showAtLastLocation(View parent, int defaultX, int defaultY) {
        if (lastX == 0 && lastY == 0)
            this.showAtLocation(parent, Gravity.NO_GRAVITY, defaultX, defaultY);
        else
            this.showAtLocation(parent, Gravity.NO_GRAVITY, lastX, lastY);
    }

    @Override
    public void showAtLocation(View parent, int gravity, int x, int y) {
        lastX = x;
        lastY = y;

        if (isShowing()) {
            update(x, y, -1, -1);
            return;
        }

        if (binding == null)
            setupUi();

        super.showAtLocation(parent, gravity, x, y);
    }

    @Override
    public void showAsDropDown(View anchor, int offsetX, int offsetY, int gravity) {
        if (isShowing())
            return;

        if (binding == null)
            setupUi();

        super.showAsDropDown(anchor, offsetX, offsetY, gravity);
    }

    public void setTitle(@Nullable String title) {
        titleStr = title;
        if (binding != null)
            binding.textTitle.setText(title);
    }

    public void setTitle(int titleId) {
        this.titleId = titleId;
        if (binding != null)
            binding.textTitle.setText(titleId);
    }

    public void setExpandBtnEnabled(boolean enabled) {
        expandBtnEnabled = enabled;
        expanded = true;
        if (binding != null)
            binding.buttonExpand.setVisibility(GONE);
        if (primaryView != null)
            primaryView.setVisibility(VISIBLE);
    }

    public void setCloseBtnEnabled(boolean enabled) {
        closeBtnEnabled = enabled;
        if (binding != null)
            binding.buttonClose.setVisibility(GONE);
    }

    @NonNull
    protected abstract View onCreateView();

    protected abstract void onDestroyView();

    @NonNull
    protected Context getContext() {
        return context;
    }

    public void setAlpha(int alpha) {
        this.alpha = Math.clamp(alpha, 0, 255);
        if (binding != null) {
            binding.header.getBackground().setAlpha(this.alpha);
            binding.header.invalidate();
            primaryView.getBackground().setAlpha(this.alpha);
            primaryView.invalidate();
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private final View.OnTouchListener touchToDragListener = (v, event) -> {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            downX = (int) event.getX();
            downY = (int) event.getY();
        } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
            lastX = (int) event.getRawX() - downX;
            lastY = (int) event.getRawY() - downY;
            update(lastX, lastY, -1, -1, true);
        }
        return true;
    };

    @SuppressLint("ClickableViewAccessibility")
    private void setupUi() {
        binding = PopupFloatContainerBinding.inflate(LayoutInflater.from(context));

        if (titleId != 0)
            binding.textTitle.setText(titleId);
        else
            binding.textTitle.setText(Objects.requireNonNullElse(titleStr, ""));

        binding.header.setOnTouchListener(touchToDragListener);
        binding.header.getBackground().setAlpha(alpha);

        binding.buttonExpand.setOnClickListener(v -> {
            expanded = !expanded;
            if (expanded) {
                binding.buttonExpand.setImageResource(R.drawable.ic_expand_less);
                binding.header.getLayoutParams().width = MATCH_PARENT;
                binding.header.setBackgroundResource(R.drawable.bg_float_popup_header_expand);
                binding.header.getBackground().setAlpha(alpha);
                binding.container.setVisibility(VISIBLE);
            } else {
                binding.buttonExpand.setImageResource(R.drawable.ic_expand_more);
                binding.header.getLayoutParams().width = primaryView.getWidth();
                binding.header.setBackgroundResource(R.drawable.bg_float_popup_header);
                binding.header.getBackground().setAlpha(alpha);
                binding.container.setVisibility(GONE);
            }
        });
        binding.buttonExpand.setVisibility(expandBtnEnabled ? VISIBLE : GONE);

        binding.buttonClose.setOnClickListener(v -> dismiss());
        binding.buttonClose.setVisibility(closeBtnEnabled ? VISIBLE : GONE);

        primaryView = onCreateView();
        primaryView.getBackground().setAlpha(alpha);
        binding.container.addView(primaryView);

        setContentView(binding.getRoot());
    }
}
