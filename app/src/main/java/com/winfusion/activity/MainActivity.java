package com.winfusion.activity;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.ViewPropertyAnimator;
import android.view.animation.PathInterpolator;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.navigation.NavigationBarView;
import com.winfusion.R;
import com.winfusion.databinding.ActivityMainBinding;
import com.winfusion.model.MainActivityViewModel;
import com.winfusion.utils.UiUtils;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());

        setContentView(binding.getRoot());
        UiUtils.setActivityNotFullscreen(this);
        setupNavController();
        setupShowNavigationControl();

        binding.navView.post(() -> showNavigation(true, false));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }

    private void setupNavController() {
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.fragment_container);

        if (navHostFragment != null) {
            NavigationUI.setupWithNavController((NavigationBarView) binding.navView,
                    navHostFragment.getNavController());
        }
    }

    private void setupShowNavigationControl() {
        MainActivityViewModel mainActivityViewModel = new ViewModelProvider(this)
                .get(MainActivityViewModel.class);
        mainActivityViewModel.getShowNavigationLiveData()
                .observe(this, aBoolean -> showNavigation(aBoolean, true));
    }

    private void showNavigation(boolean visible, boolean animated) {
        int visibility = visible ? VISIBLE : GONE;

        if (visibility == binding.navView.getVisibility())
            return;

        if (!animated) {
            binding.navView.setVisibility(visibility);
            return;
        }

        boolean defaultLayout = getResources().getBoolean(R.bool.default_layout);
        boolean w600dpLayout = getResources().getBoolean(R.bool.w600dp_layout);
        ViewPropertyAnimator animator = binding.navView.animate();
        int duration = getResources().getInteger(android.R.integer.config_shortAnimTime);

        if (visible) {
            binding.navView.setVisibility(visibility);
            animator.setDuration(duration).setInterpolator(new PathInterpolator(0.05f, 0.7f, 0.1f, 1f));

            if (defaultLayout) {
                binding.navView.setTranslationY(binding.navView.getHeight() * 2f);
                animator.translationY(0f);
            } else if (w600dpLayout) {
                if (binding.getRoot().getLayoutDirection() == View.LAYOUT_DIRECTION_LTR)
                    binding.navView.setTranslationX(binding.navView.getWidth() * -2f);
                else
                    binding.navView.setTranslationX(binding.navView.getWidth() * 2f);
                animator.translationX(0f);
            }
        } else {
            animator.setDuration(duration).setInterpolator(new PathInterpolator(0.3f, 0f, 0.8f, 0.15f));

            if (defaultLayout) {
                animator.translationY(binding.navView.getHeight() * 2f);
            } else if (w600dpLayout) {
                if (binding.getRoot().getLayoutDirection() == View.LAYOUT_DIRECTION_LTR)
                    animator.translationX(binding.navView.getWidth() * -2f);
                else
                    animator.translationX(binding.navView.getWidth() * 2f);
            }
        }

        animator
                .withStartAction(() -> setNavViewEnabled(false))
                .withEndAction(() -> {
                    if (!visible)
                        binding.navView.setVisibility(visibility);
                    setNavViewEnabled(true);
                })
                .start();
    }

    private void setNavViewEnabled(boolean enabled) {
        NavigationBarView navigationBarView = (NavigationBarView) binding.navView;
        Menu menu = navigationBarView.getMenu();
        int size = menu.size();

        for (int i = 0; i < size; i++)
            menu.getItem(i).setEnabled(enabled);
    }
}
