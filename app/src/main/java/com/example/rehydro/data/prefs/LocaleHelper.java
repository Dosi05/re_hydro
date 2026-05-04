package com.example.rehydro.data.prefs;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import java.util.Locale;

public class LocaleHelper {

    private static final String PREF_NAME = "language_prefs";
    private static final String KEY_LANG  = "selected_language";

    public static void setLocale(Context context, String langCode) {
        saveLanguage(context, langCode);
        applyLocale(context, langCode);
    }

    public static Context applyLocale(Context context, String langCode) {
        Locale locale = new Locale(langCode);
        Locale.setDefault(locale);
        Configuration config = new Configuration(
                context.getResources().getConfiguration());
        config.setLocale(locale);
        return context.createConfigurationContext(config);
    }

    public static String getLanguage(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(
                PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_LANG, "en");
    }

    private static void saveLanguage(Context context, String langCode) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .edit()
                .putString(KEY_LANG, langCode)
                .apply();
    }
}