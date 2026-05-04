package com.example.rehydro.data.prefs;

import android.content.Context;
import android.content.SharedPreferences;

public class UserPrefs {
    private static final String PREF_NAME = "user_profile";
    private final SharedPreferences prefs;

    public UserPrefs(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void saveProfile(String name, float weightKg,
                            int heightCm, int age, String sex) {
        prefs.edit()
                .putString("name", name)
                .putFloat("weight", weightKg)
                .putInt("height", heightCm)
                .putInt("age", age)
                .putString("sex", sex)
                .apply();
    }

    public String getName()   { return prefs.getString("name", ""); }
    public float getWeight()  { return prefs.getFloat("weight", 70f); }
    public int getHeight()    { return prefs.getInt("height", 170); }
    public int getAge()       { return prefs.getInt("age", 25); }
    public String getSex()    { return prefs.getString("sex", "M"); }
    public boolean isComplete() { return !getName().isEmpty(); }
}