package com.example.rehydro.model;

public class CustomDrink {
    public String name;
    public double abvPercent;
    public int defaultVolumeMl;

    public CustomDrink(String name, double abvPercent, int defaultVolumeMl) {
        this.name           = name;
        this.abvPercent     = abvPercent;
        this.defaultVolumeMl = defaultVolumeMl;
    }
}