package com.winfusion.model;

import android.content.Context;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;

import com.winfusion.R;

public class MainActivityViewModel extends ViewModel {

    private final MutableLiveData<Boolean> showNavigation;

    public MainActivityViewModel() {
        showNavigation = new MutableLiveData<>();
        showNavigation.setValue(true);
    }

    public MutableLiveData<Boolean> getShowNavigationLiveData() {
        return showNavigation;
    }

    public void setShowNavigation(boolean show) {
        showNavigation.setValue(show);
    }

    public static void setShowNavigation(@NonNull ViewModelStoreOwner owner, boolean show) {
        MainActivityViewModel mainActivityViewModel = new ViewModelProvider(owner)
                .get(MainActivityViewModel.class);
        mainActivityViewModel.setShowNavigation(show);
    }

    public static void updateRootViewToFitNavigationBar(@NonNull View rootView) {

        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) rootView.getLayoutParams();
        Context context = rootView.getContext();
        boolean defaultLayout = context.getResources().getBoolean(R.bool.default_layout);
        boolean w600dpLayout = context.getResources().getBoolean(R.bool.w600dp_layout);
        int margin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 72,
                context.getResources().getDisplayMetrics());

        if (defaultLayout) {
            params.setMargins(params.leftMargin, params.topMargin, params.rightMargin,
                    params.bottomMargin + margin);
        } else if (w600dpLayout) {
            params.setMargins(params.leftMargin + margin, params.topMargin, params.rightMargin,
                    params.bottomMargin);
        }

        rootView.post(() -> rootView.setLayoutParams(params));
    }
}
