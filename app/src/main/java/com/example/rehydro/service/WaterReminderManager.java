package com.example.rehydro.service;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

public class WaterReminderManager {

    private static final long INTERVAL_MS = 30 * 60 * 1000L; // 30 минути
    private static final long MAX_IDLE_MS = 60 * 60 * 1000L; // 1 час

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Context context;

    private Runnable reminderRunnable;
    private boolean isRunning = false;

    public interface HydrationProvider {
        int getWaterNeededMl();
        int getWaterConsumedMl();
        long getLastDrinkTimestamp();
    }

    private HydrationProvider hydrationProvider;

    public WaterReminderManager(Context context) {
        this.context = context;
        NotificationHelper.createNotificationChannel(context);
    }

    public void setHydrationProvider(HydrationProvider provider) {
        this.hydrationProvider = provider;
    }

    public void start() {
        if (isRunning) return;
        isRunning = true;

        reminderRunnable = new Runnable() {
            @Override
            public void run() {
                checkAndNotify();
                handler.postDelayed(this, INTERVAL_MS);
            }
        };

        handler.postDelayed(reminderRunnable, INTERVAL_MS);
    }

    public void stop() {
        isRunning = false;
        if (reminderRunnable != null) {
            handler.removeCallbacks(reminderRunnable);
            reminderRunnable = null;
        }
        NotificationHelper.cancelWaterReminder(context);
    }

    private void checkAndNotify() {
        if (hydrationProvider == null) return;

        int needed     = hydrationProvider.getWaterNeededMl();
        int consumed   = hydrationProvider.getWaterConsumedMl();
        long lastDrink = hydrationProvider.getLastDrinkTimestamp();

        if (needed == 0) return;

        float hydrationPercent = (consumed / (float) needed) * 100f;
        if (hydrationPercent >= 80f) return;

        if (lastDrink != 0) {
            long idleMs = System.currentTimeMillis() - lastDrink;
            if (idleMs > MAX_IDLE_MS) return;
        }

        NotificationHelper.sendWaterReminder(context);
    }
}