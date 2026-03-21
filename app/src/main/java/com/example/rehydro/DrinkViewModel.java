package com.example.rehydro;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import java.util.ArrayList;
import java.util.List;

public class DrinkViewModel extends ViewModel {

    // ── LiveData ──────────────────────────────────────────────────────────────
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

    private long sessionStartTime = 0;

    // ── Getters ───────────────────────────────────────────────────────────────

    public LiveData<List<DrinkEntry>> getDrinks()       { return drinks; }
    public LiveData<Integer> getWaterConsumedMl()       { return waterConsumedMl; }
    public LiveData<Integer> getWaterNeededMl()         { return waterNeededMl; }
    public LiveData<Float> getDrunkenness()             { return drunkenness; }
    public LiveData<Float> getBac()                     { return bac; }
    public LiveData<Boolean> getSessionSaved()          { return sessionSaved; }
    public long getSessionStartTime()                   { return sessionStartTime; }

    // ── Actions ───────────────────────────────────────────────────────────────

    public void addDrink(DrinkEntry entry, float weightKg, String sex) {
        if (sessionStartTime == 0) {
            sessionStartTime = System.currentTimeMillis();
        }
        List<DrinkEntry> list = new ArrayList<>(drinks.getValue());
        list.add(entry);
        drinks.setValue(list);
        recalculate(list, weightKg, sex);
    }

    public void addWater(int ml, float weightKg, String sex) {
        DrinkEntry water = new DrinkEntry("Water", 0, ml, true);
        List<DrinkEntry> list = new ArrayList<>(drinks.getValue());
        list.add(water);
        drinks.setValue(list);
        waterConsumedMl.setValue(waterConsumedMl.getValue() + ml);
        recalculate(list, weightKg, sex);
    }

    public void removeDrink(int index, float weightKg, String sex) {
        List<DrinkEntry> list = new ArrayList<>(drinks.getValue());
        if (index < 0 || index >= list.size()) return;

        DrinkEntry removed = list.remove(index);
        if (removed.isWater) {
            int current = waterConsumedMl.getValue();
            waterConsumedMl.setValue(Math.max(0, current - removed.volumeMl));
        }
        drinks.setValue(list);
        recalculate(list, weightKg, sex);
    }

    public void clearSession() {
        drinks.setValue(new ArrayList<>());
        waterConsumedMl.setValue(0);
        waterNeededMl.setValue(0);
        drunkenness.setValue(0f);
        bac.setValue(0f);
        sessionStartTime = 0;
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

    // ── Private ───────────────────────────────────────────────────────────────

    private void recalculate(List<DrinkEntry> list, float weightKg, String sex) {
        int needed    = HydrationCalculator.calculateWaterNeededMl(list);
        float level   = HydrationCalculator.getDrunkenessLevel(list, weightKg, sex);
        float bacVal  = HydrationCalculator.calculateBAC(list, weightKg, sex);

        waterNeededMl.setValue(needed);
        drunkenness.setValue(level);
        bac.setValue(bacVal);
    }
}