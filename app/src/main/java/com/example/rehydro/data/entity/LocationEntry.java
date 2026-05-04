package com.example.rehydro.data.entity;

import androidx.annotation.Nullable;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "location_entry")
public class LocationEntry {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public int sessionId;
    public double latitude;
    public double longitude;
    public long timestamp;

    @Nullable
    public String photoPath;

    public boolean isPending;
}