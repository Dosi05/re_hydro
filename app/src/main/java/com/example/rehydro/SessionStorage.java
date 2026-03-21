package com.example.rehydro;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class SessionStorage {

    private static final String PREF_NAME   = "session_storage";
    private static final String KEY_SESSIONS = "sessions";
    private static final Gson gson = new Gson();

    public static void saveSession(Context context, SessionLog session) {
        List<SessionLog> sessions = getSessions(context);

        // Assign a simple incremental ID
        session.id = sessions.size() + 1;
        sessions.add(0, session); // newest first

        SharedPreferences prefs = context.getSharedPreferences(
                PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit()
                .putString(KEY_SESSIONS, gson.toJson(sessions))
                .apply();
    }

    public static List<SessionLog> getSessions(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(
                PREF_NAME, Context.MODE_PRIVATE);
        String json = prefs.getString(KEY_SESSIONS, null);

        if (json == null) return new ArrayList<>();

        Type type = new TypeToken<List<SessionLog>>(){}.getType();
        return gson.fromJson(json, type);
    }

    public static SessionLog getSessionById(Context context, int id) {
        for (SessionLog s : getSessions(context)) {
            if (s.id == id) return s;
        }
        return null;
    }
}