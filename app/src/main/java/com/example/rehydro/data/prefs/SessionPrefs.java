package com.example.rehydro.data.prefs;

import android.content.Context;

import com.example.rehydro.model.DrinkEntry;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class SessionPrefs {

    private static final String PREF_NAME       = "active_session";
    private static final String KEY_DRINKS      = "drinks";
    private static final String KEY_WATER       = "water_consumed";
    private static final String KEY_START_TIME  = "start_time";
    private static final Gson gson              = new Gson();

    public static void saveActiveSession(Context context,
                                         List<DrinkEntry> drinks,
                                         int waterConsumed,
                                         long startTime) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .edit()
                .putString(KEY_DRINKS, gson.toJson(drinks))
                .putInt(KEY_WATER, waterConsumed)
                .putLong(KEY_START_TIME, startTime)
                .apply();
    }

    public static List<DrinkEntry> getDrinks(Context context) {
        String json = context.getSharedPreferences(
                        PREF_NAME, Context.MODE_PRIVATE)
                .getString(KEY_DRINKS, null);
        if (json == null) return new ArrayList<>();
        Type type = new TypeToken<List<DrinkEntry>>(){}.getType();
        return gson.fromJson(json, type);
    }

    public static int getWaterConsumed(Context context) {
        return context.getSharedPreferences(
                        PREF_NAME, Context.MODE_PRIVATE)
                .getInt(KEY_WATER, 0);
    }

    public static long getStartTime(Context context) {
        return context.getSharedPreferences(
                        PREF_NAME, Context.MODE_PRIVATE)
                .getLong(KEY_START_TIME, 0);
    }

    public static boolean hasActiveSession(Context context) {
        List<DrinkEntry> drinks = getDrinks(context);
        return drinks != null && !drinks.isEmpty();
    }

    public static void clear(Context context) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .edit()
                .clear()
                .apply();
    }
}