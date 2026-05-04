package com.example.rehydro.model;

public class DrinkEntry {
    public String name;
    public double abvPercent;
    public int volumeMl;
    public boolean isWater;

    public DrinkEntry(String name, double abvPercent, int volumeMl, boolean isWater) {
        this.name = name;
        this.abvPercent = abvPercent;
        this.volumeMl = volumeMl;
        this.isWater = isWater;
    }
}