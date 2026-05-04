package com.example.rehydro.data.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "session_drink_item")
public class SessionDrinkItemEntity {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public int sessionId;
    public String drinkName;
    public double abvPercent;
    public int volumeMl;
    public boolean isWater;
}