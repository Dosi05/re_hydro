package com.example.rehydro.ui;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.example.rehydro.service.NotificationHelper;
import com.example.rehydro.R;
import com.example.rehydro.data.prefs.UserPrefs;
import com.example.rehydro.service.WaterReminderManager;
import com.example.rehydro.data.prefs.LocaleHelper;
import com.example.rehydro.service.LocationTrackingService;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 100;

    private int currentSessionId = -1;

    private WaterReminderManager waterReminderManager;
    private DrinkViewModel viewModel;

    public static MainActivity instance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        instance = this;

        UserPrefs userPrefs = new UserPrefs(this);
        if (!userPrefs.isComplete()) {
            startActivity(new Intent(this, OnboardingActivity.class));
            finish();
            return;
        }

        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        setContentView(R.layout.activity_main);

        viewModel = new ViewModelProvider(this).get(DrinkViewModel.class);
        waterReminderManager = new WaterReminderManager(this);

        NotificationHelper.createNotificationChannel(this);
        NotificationHelper.createLocationNotificationChannel(this);

        setupNavBar();
        requestPermissions();
    }

    private void setupNavBar() {
        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);
        ViewCompat.setOnApplyWindowInsetsListener(bottomNav, (view, insets) -> {
            int gestureBarHeight = insets.getInsets(
                    WindowInsetsCompat.Type.navigationBars()).bottom;

            androidx.constraintlayout.widget.ConstraintLayout.LayoutParams params =
                    (androidx.constraintlayout.widget.ConstraintLayout.LayoutParams)
                            view.getLayoutParams();

            params.bottomMargin = gestureBarHeight +
                    (int)(24 * getResources().getDisplayMetrics().density);
            view.setLayoutParams(params);
            return insets;
        });

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);

        NavController navController = navHostFragment.getNavController();
        NavigationUI.setupWithNavController(bottomNav, navController);

        navController.addOnDestinationChangedListener(
                (controller, destination, arguments) -> {
                    if (destination.getId() == R.id.sessionDetailFragment) {
                        bottomNav.setVisibility(View.GONE);
                    } else {
                        bottomNav.setVisibility(View.VISIBLE);
                    }
                });
    }

    private void requestPermissions() {
        String[] permissions = {
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.POST_NOTIFICATIONS
        };

        boolean allGranted = true;
        for (String perm : permissions) {
            if (ActivityCompat.checkSelfPermission(this, perm)
                    != PackageManager.PERMISSION_GRANTED) {
                allGranted = false;
                break;
            }
        }

        if (allGranted) {
            startLocationService();
        } else {
            ActivityCompat.requestPermissions(
                    this, permissions, PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions,
                                           int[] grantResults) {
        super.onRequestPermissionsResult(
                requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                startLocationService();
            }
        }
    }

    public void startLocationService() {
        try {
            Intent serviceIntent =
                    new Intent(this, LocationTrackingService.class);
            startForegroundService(serviceIntent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stopLocationService() {
        try {
            Intent serviceIntent =
                    new Intent(this, LocationTrackingService.class);
            stopService(serviceIntent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void startWaterReminder() {
        if (waterReminderManager != null) {
            waterReminderManager.start();
        }
    }

    public void stopWaterReminder() {
        if (waterReminderManager != null) {
            waterReminderManager.stop();
        }
    }

    public void updateServiceWaterData() {
        Integer needed   = viewModel.getWaterNeededMl().getValue();
        Integer consumed = viewModel.getWaterConsumedMl().getValue();
        LocationTrackingService.waterNeededMl      = needed   != null ? needed   : 0;
        LocationTrackingService.waterConsumedMl    = consumed != null ? consumed : 0;
        LocationTrackingService.lastDrinkTimestamp = viewModel.getLastDrinkTimestamp();
    }

    public void setCurrentSessionId(int sessionId) {
        this.currentSessionId = sessionId;
        LocationTrackingService.currentSessionId = sessionId;
    }

    public int getCurrentSessionId() {
        return currentSessionId;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        instance = null;
        LocationTrackingService.locationChangeListener = null;
        if (waterReminderManager != null) {
            waterReminderManager.stop();
        }
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        String lang = LocaleHelper.getLanguage(newBase);
        super.attachBaseContext(LocaleHelper.applyLocale(newBase, lang));
    }
}