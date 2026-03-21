package com.example.rehydro;

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