package com.example.rehydro.data.db;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.example.rehydro.model.DrinkEntry;
import com.example.rehydro.data.entity.LocationEntry;
import com.example.rehydro.data.entity.SessionDrinkItemEntity;
import com.example.rehydro.data.entity.SessionLogEntity;
import com.example.rehydro.data.entity.CustomDrinkEntity;

import java.util.List;

public class DatabaseHelper {

    private static final String TAG = "REHYDRO";

    public static void saveSession(Context context,
                                   SessionLogEntity session,
                                   List<DrinkEntry> drinks,
                                   OnSessionSavedCallback callback) {
        new Thread(() -> {
            AppDatabase db = AppDatabase.getInstance(context);
            long sessionId = db.sessionDao().insertSession(session);

            for (DrinkEntry d : drinks) {
                SessionDrinkItemEntity item =
                        new SessionDrinkItemEntity();
                item.sessionId  = (int) sessionId;
                item.drinkName  = d.name;
                item.abvPercent = d.abvPercent;
                item.volumeMl   = d.volumeMl;
                item.isWater    = d.isWater;
                db.sessionDao().insertDrinkItem(item);
            }

            if (callback != null) {
                new Handler(Looper.getMainLooper())
                        .post(() -> callback.onSaved((int) sessionId));
            }
        }).start();
    }

    public static void getAllSessions(Context context,
                                      OnSessionsLoadedCallback callback) {
        new Thread(() -> {
            List<SessionLogEntity> sessions =
                    AppDatabase.getInstance(context)
                            .sessionDao()
                            .getAllSessions();

            new Handler(Looper.getMainLooper())
                    .post(() -> callback.onLoaded(sessions));
        }).start();
    }

    public static void getDrinksForSession(Context context,
                                           int sessionId,
                                           OnDrinksLoadedCallback callback) {
        new Thread(() -> {
            List<SessionDrinkItemEntity> items =
                    AppDatabase.getInstance(context)
                            .sessionDao()
                            .getDrinksForSession(sessionId);

            new Handler(Looper.getMainLooper())
                    .post(() -> callback.onLoaded(items));
        }).start();
    }

    public static void saveCustomDrink(Context context,
                                       CustomDrinkEntity drink,
                                       Runnable onComplete) {
        new Thread(() -> {
            Log.d(TAG, "DB insert starting for: " + drink.name);
            AppDatabase.getInstance(context)
                    .customDrinkDao()
                    .insert(drink);
            Log.d(TAG, "DB insert done for: " + drink.name);

            if (onComplete != null) {
                new Handler(Looper.getMainLooper()).post(onComplete);
            }
        }).start();
    }

    public static void saveCustomDrink(Context context,
                                       CustomDrinkEntity drink) {
        saveCustomDrink(context, drink, null);
    }

    public static void getAllCustomDrinks(Context context,
                                          OnCustomDrinksLoadedCallback callback) {
        new Thread(() -> {
            List<CustomDrinkEntity> drinks =
                    AppDatabase.getInstance(context)
                            .customDrinkDao()
                            .getAll();

            new Handler(Looper.getMainLooper())
                    .post(() -> callback.onLoaded(drinks));
        }).start();
    }

    public static void searchCustomDrinks(Context context,
                                          String query,
                                          OnCustomDrinksLoadedCallback callback) {
        new Thread(() -> {
            String wildcardQuery = "%" + query + "%";
            List<CustomDrinkEntity> drinks =
                    AppDatabase.getInstance(context)
                            .customDrinkDao()
                            .search(wildcardQuery);

            new Handler(Looper.getMainLooper())
                    .post(() -> callback.onLoaded(drinks));
        }).start();
    }

    public static void saveLocation(Context context,
                                    LocationEntry entry,
                                    OnLocationSavedCallback callback) {
        new Thread(() -> {
            long id = AppDatabase.getInstance(context)
                    .locationDao()
                    .insertLocation(entry);

            if (callback != null) {
                new Handler(Looper.getMainLooper())
                        .post(() -> callback.onSaved((int) id));
            }
        }).start();
    }

    public static void getLocationsForSession(Context context,
                                              int sessionId,
                                              OnLocationsLoadedCallback callback) {
        new Thread(() -> {
            List<LocationEntry> locations =
                    AppDatabase.getInstance(context)
                            .locationDao()
                            .getLocationsForSession(sessionId);

            new Handler(Looper.getMainLooper())
                    .post(() -> callback.onLoaded(locations));
        }).start();
    }

    public interface OnSessionSavedCallback {
        void onSaved(int sessionId);
    }

    public interface OnSessionsLoadedCallback {
        void onLoaded(List<SessionLogEntity> sessions);
    }

    public interface OnDrinksLoadedCallback {
        void onLoaded(List<SessionDrinkItemEntity> drinks);
    }

    public interface OnCustomDrinksLoadedCallback {
        void onLoaded(List<CustomDrinkEntity> drinks);
    }

    public interface OnLocationSavedCallback {
        void onSaved(int locationId);
    }

    public interface OnLocationsLoadedCallback {
        void onLoaded(List<LocationEntry> locations);
    }
}