package com.example.rehydro.data.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "custom_drink")
public class CustomDrinkEntity {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public String name;
    public double abvPercent;
    public int defaultVolumeMl;
}