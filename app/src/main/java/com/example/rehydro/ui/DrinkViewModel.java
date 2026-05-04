package com.example.rehydro.ui;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.rehydro.data.prefs.SessionPrefs;
import com.example.rehydro.data.prefs.UserPrefs;
import com.example.rehydro.model.DrinkEntry;
import com.example.rehydro.util.HydrationCalculator;

import java.util.ArrayList;
import java.util.List;

public class DrinkViewModel extends AndroidViewModel {

    private final MutableLiveData<List<DrinkEntry>> drinks =
            new MutableLiveData<>(new ArrayList<>());

    private final MutableLiveData<Integer> waterConsumedMl =
            new MutableLiveData<>(0);

    private final MutableLiveData<Integer> waterNeededMl =
            new MutableLiveData<>(0);

    private final MutableLiveData<Float> drunkenness =
            new MutableLiveData<>(0f);

    private final MutableLiveData<Float> bac =
            new MutableLiveData<>(0f);

    private final MutableLiveData<Boolean> sessionSaved =
            new MutableLiveData<>(false);

    private long sessionStartTime    = 0;
    private long lastDrinkTimestamp  = 0; // timestamp на последно добавената напитка

    public DrinkViewModel(@NonNull Application application) {
        super(application);
        restoreSession();
    }


    public LiveData<List<DrinkEntry>> getDrinks()      { return drinks; }
    public LiveData<Integer> getWaterConsumedMl()      { return waterConsumedMl; }
    public LiveData<Integer> getWaterNeededMl()        { return waterNeededMl; }
    public LiveData<Float> getDrunkenness()            { return drunkenness; }
    public LiveData<Float> getBac()                    { return bac; }
    public LiveData<Boolean> getSessionSaved()         { return sessionSaved; }
    public long getSessionStartTime()                  { return sessionStartTime; }
    public long getLastDrinkTimestamp()                { return lastDrinkTimestamp; }


    public void addDrink(DrinkEntry entry, float weightKg, String sex) {
        if (sessionStartTime == 0) {
            sessionStartTime = System.currentTimeMillis();
        }
        lastDrinkTimestamp = System.currentTimeMillis();

        List<DrinkEntry> list = new ArrayList<>(drinks.getValue());
        list.add(entry);
        drinks.setValue(list);
        recalculate(list, weightKg, sex);
        persistSession(list);
    }

    public void addWater(int ml, float weightKg, String sex) {
        lastDrinkTimestamp = System.currentTimeMillis();

        DrinkEntry water = new DrinkEntry("Water", 0, ml, true);
        List<DrinkEntry> list = new ArrayList<>(drinks.getValue());
        list.add(water);
        drinks.setValue(list);
        int consumed = waterConsumedMl.getValue() + ml;
        waterConsumedMl.setValue(consumed);
        recalculate(list, weightKg, sex);
        persistSession(list);
    }

    public void removeDrink(int index, float weightKg, String sex) {
        List<DrinkEntry> list = new ArrayList<>(drinks.getValue());
        if (index < 0 || index >= list.size()) return;

        DrinkEntry removed = list.remove(index);
        if (removed.isWater) {
            int current = waterConsumedMl.getValue();
            waterConsumedMl.setValue(
                    Math.max(0, current - removed.volumeMl));
        }
        drinks.setValue(list);
        recalculate(list, weightKg, sex);
        persistSession(list);
    }

    public void clearSession() {
        drinks.setValue(new ArrayList<>());
        waterConsumedMl.setValue(0);
        waterNeededMl.setValue(0);
        drunkenness.setValue(0f);
        bac.setValue(0f);
        sessionStartTime   = 0;
        lastDrinkTimestamp = 0;
        SessionPrefs.clear(getApplication());
    }

    public void notifySessionSaved() {
        sessionSaved.setValue(true);
    }

    public void resetSessionSaved() {
        sessionSaved.setValue(false);
    }

    public boolean hasActiveSession() {
        List<DrinkEntry> list = drinks.getValue();
        return list != null && !list.isEmpty();
    }


    private void restoreSession() {
        if (!SessionPrefs.hasActiveSession(getApplication())) return;

        List<DrinkEntry> savedDrinks =
                SessionPrefs.getDrinks(getApplication());
        int savedWater =
                SessionPrefs.getWaterConsumed(getApplication());
        long savedStart =
                SessionPrefs.getStartTime(getApplication());

        sessionStartTime   = savedStart;
        lastDrinkTimestamp = savedStart; // при restore използваме start time
        drinks.setValue(savedDrinks);
        waterConsumedMl.setValue(savedWater);

        UserPrefs prefs = new UserPrefs(getApplication());
        recalculate(savedDrinks, prefs.getWeight(), prefs.getSex());
    }


    private void persistSession(List<DrinkEntry> list) {
        SessionPrefs.saveActiveSession(
                getApplication(),
                list,
                waterConsumedMl.getValue() != null
                        ? waterConsumedMl.getValue() : 0,
                sessionStartTime
        );
    }

    private void recalculate(List<DrinkEntry> list,
                             float weightKg, String sex) {
        int needed  = HydrationCalculator.calculateWaterNeededMl(list);
        float level = HydrationCalculator.getDrunkenessLevel(list, weightKg, sex);
        float bacVal = HydrationCalculator.calculateBAC(list, weightKg, sex);

        waterNeededMl.setValue(needed);
        drunkenness.setValue(level);
        bac.setValue(bacVal);
    }
}