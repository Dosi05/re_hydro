package com.example.rehydro.data.db;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.rehydro.data.entity.LocationEntry;

import java.util.List;

@Dao
public interface LocationDao {

    @Insert
    long insertLocation(LocationEntry entry);

    @Update
    void updateLocation(LocationEntry entry);

    @Query("SELECT * FROM location_entry WHERE id = :id")
    LocationEntry getLocationById(int id);

    @Query("SELECT * FROM location_entry WHERE sessionId = :sessionId ORDER BY timestamp ASC")
    List<LocationEntry> getLocationsForSession(int sessionId);

    @Query("SELECT * FROM location_entry WHERE sessionId = :sessionId ORDER BY timestamp DESC LIMIT 1")
    LocationEntry getLastLocationForSession(int sessionId);

    @Query("SELECT * FROM location_entry WHERE isPending = 1 ORDER BY timestamp ASC")
    List<LocationEntry> getPendingLocations();

    @Query("DELETE FROM location_entry WHERE sessionId = :sessionId")
    void deleteLocationsForSession(int sessionId);

    @Query("DELETE FROM location_entry WHERE isPending = 1")
    void deletePendingLocations();
}