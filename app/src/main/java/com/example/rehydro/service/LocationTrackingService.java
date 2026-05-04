package com.example.rehydro.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.example.rehydro.R;
import com.example.rehydro.data.db.AppDatabase;
import com.example.rehydro.data.entity.LocationEntry;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

public class LocationTrackingService extends Service {

    private static final String CHANNEL_ID   = "location_tracking_channel";
    private static final int    NOTIF_ID     = 1001;
    private static final float  MIN_DISTANCE = 50f;
    private static final long   INTERVAL_MS  = 5 * 60 * 1000L;

    private static final long WATER_INTERVAL_MS = 30 * 60 * 1000L;
    private static final long MAX_IDLE_MS       = 60 * 60 * 1000L;
    private final Handler waterHandler = new Handler(Looper.getMainLooper());
    private Runnable waterRunnable;

    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private Location lastSavedLocation;

    public static int currentSessionId = -1;
    public static LocationChangeListener locationChangeListener;

    public static int waterNeededMl    = 0;
    public static int waterConsumedMl  = 0;
    public static long lastDrinkTimestamp = 0;

    public interface LocationChangeListener {
        void onLocationChanged(double latitude, double longitude);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        createNotificationChannel();
        startForeground(NOTIF_ID, buildNotification());

        fusedLocationClient =
                LocationServices.getFusedLocationProviderClient(this);
        startLocationUpdates();
        startWaterReminder();

        return START_STICKY;
    }

    private void startLocationUpdates() {
        if (locationCallback != null) return;

        LocationRequest locationRequest = new LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY, INTERVAL_MS)
                .setMinUpdateDistanceMeters(MIN_DISTANCE)
                .build();

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult result) {
                if (result == null) return;
                Location location = result.getLastLocation();
                if (location == null) return;
                handleNewLocation(location);
            }
        };

        try {
            fusedLocationClient.requestLocationUpdates(
                    locationRequest,
                    locationCallback,
                    Looper.getMainLooper()
            );
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    private void handleNewLocation(Location location) {
        if (currentSessionId == -1) return;

        if (lastSavedLocation != null) {
            float distance = lastSavedLocation.distanceTo(location);
            if (distance < MIN_DISTANCE) return;
        }

        lastSavedLocation = location;

        LocationEntry entry = new LocationEntry();
        entry.sessionId  = 0;
        entry.latitude   = location.getLatitude();
        entry.longitude  = location.getLongitude();
        entry.timestamp  = System.currentTimeMillis();
        entry.photoPath  = null;
        entry.isPending  = true;

        new Thread(() ->
                AppDatabase.getInstance(getApplicationContext())
                        .locationDao()
                        .insertLocation(entry)
        ).start();

        NotificationHelper.sendLocationAlert(getApplicationContext());

        if (locationChangeListener != null) {
            locationChangeListener.onLocationChanged(
                    location.getLatitude(),
                    location.getLongitude()
            );
        }
    }


    private void startWaterReminder() {
        if (waterRunnable != null) return;

        waterRunnable = new Runnable() {
            @Override
            public void run() {
                checkAndNotifyWater();
                waterHandler.postDelayed(this, WATER_INTERVAL_MS);
            }
        };
        waterHandler.postDelayed(waterRunnable, WATER_INTERVAL_MS);
    }

    private void checkAndNotifyWater() {
        if (currentSessionId == -1) return;
        if (waterNeededMl == 0) return;

        float hydrationPercent = (waterConsumedMl / (float) waterNeededMl) * 100f;
        if (hydrationPercent >= 80f) return;

        if (lastDrinkTimestamp != 0) {
            long idleMs = System.currentTimeMillis() - lastDrinkTimestamp;
            if (idleMs > MAX_IDLE_MS) return;
        }

        NotificationHelper.sendWaterReminder(getApplicationContext());
    }


    private void createNotificationChannel() {
        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "Location Tracking",
                NotificationManager.IMPORTANCE_LOW
        );
        channel.setDescription("Tracks your route during active session");
        NotificationManager manager =
                getSystemService(NotificationManager.class);
        if (manager != null) {
            manager.createNotificationChannel(channel);
        }
    }

    private Notification buildNotification() {
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Re:Hydro")
                .setContentText("Tracking your route")
                .setSmallIcon(R.drawable.ic_arrow_back)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setSilent(true)
                .build();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (fusedLocationClient != null && locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
        if (waterRunnable != null) {
            waterHandler.removeCallbacks(waterRunnable);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}