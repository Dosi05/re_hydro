package com.example.rehydro.data.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "session_log")
public class SessionLogEntity {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public long startTimestamp;
    public long endTimestamp;
    public int waterNeededMl;
    public int waterConsumedMl;
    public float hydrationPercent;
    public int drinkCount;
}