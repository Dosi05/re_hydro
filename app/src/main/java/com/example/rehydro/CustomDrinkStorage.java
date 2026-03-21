package com.example.rehydro;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CustomDrinkStorage {

    private static final String PREF_NAME  = "custom_drinks";
    private static final String KEY_DRINKS = "drinks";
    private static final Gson gson = new Gson();

    // ── Preset drinks bundled with the app ────────────────────────────────────
    private static final List<CustomDrink> PRESETS = Arrays.asList(
            new CustomDrink("Beer",       5.0,  330),
            new CustomDrink("Wine",      13.0,  150),
            new CustomDrink("Spirits",   40.0,   50),
            new CustomDrink("Cider",      4.5,  330),
            new CustomDrink("Champagne", 12.0,  150),
            new CustomDrink("Prosecco",  11.0,  150),
            new CustomDrink("Lager",      4.0,  330),
            new CustomDrink("Stout",      4.2,  440),
            new CustomDrink("Gin",       37.5,   50),
            new CustomDrink("Vodka",     40.0,   50),
            new CustomDrink("Whiskey",   40.0,   50),
            new CustomDrink("Rum",       40.0,   50),
            new CustomDrink("Tequila",   38.0,   50),
            new CustomDrink("Cocktail",  10.0,  200)
    );

    // ── Get all drinks — presets + user saved ─────────────────────────────────
    public static List<CustomDrink> getAllDrinks(Context context) {
        List<CustomDrink> all = new ArrayList<>(PRESETS);
        all.addAll(getUserDrinks(context));
        return all;
    }

    // ── Get only user saved drinks ────────────────────────────────────────────
    public static List<CustomDrink> getUserDrinks(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(
                PREF_NAME, Context.MODE_PRIVATE);
        String json = prefs.getString(KEY_DRINKS, null);
        if (json == null) return new ArrayList<>();

        Type type = new TypeToken<List<CustomDrink>>(){}.getType();
        return gson.fromJson(json, type);
    }

    // ── Save a new custom drink ───────────────────────────────────────────────
    public static void saveDrink(Context context, CustomDrink drink) {
        List<CustomDrink> existing = getUserDrinks(context);

        // Avoid duplicates — if same name exists update it
        for (int i = 0; i < existing.size(); i++) {
            if (existing.get(i).name.equalsIgnoreCase(drink.name)) {
                existing.set(i, drink);
                writeToPrefs(context, existing);
                return;
            }
        }

        existing.add(drink);
        writeToPrefs(context, existing);
    }

    // ── Search drinks by name ─────────────────────────────────────────────────
    public static List<CustomDrink> search(Context context, String query) {
        if (query == null || query.trim().isEmpty()) {
            return getAllDrinks(context);
        }

        String lower = query.toLowerCase().trim();
        List<CustomDrink> results = new ArrayList<>();

        for (CustomDrink d : getAllDrinks(context)) {
            if (d.name.toLowerCase().contains(lower)) {
                results.add(d);
            }
        }
        return results;
    }

    // ── Check if name already exists ──────────────────────────────────────────
    public static boolean exists(Context context, String name) {
        for (CustomDrink d : getAllDrinks(context)) {
            if (d.name.equalsIgnoreCase(name.trim())) return true;
        }
        return false;
    }

    // ── Private ───────────────────────────────────────────────────────────────
    private static void writeToPrefs(Context context, List<CustomDrink> drinks) {
        SharedPreferences prefs = context.getSharedPreferences(
                PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit()
                .putString(KEY_DRINKS, gson.toJson(drinks))
                .apply();
    }
}