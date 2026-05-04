package com.example.rehydro.data.db;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.rehydro.data.entity.SessionDrinkItemEntity;
import com.example.rehydro.data.entity.SessionLogEntity;

import java.util.List;

@Dao
public interface SessionDao {

    @Insert
    long insertSession(SessionLogEntity session);

    @Insert
    void insertDrinkItem(SessionDrinkItemEntity item);

    @Query("SELECT * FROM session_log ORDER BY startTimestamp DESC")
    List<SessionLogEntity> getAllSessions();

    @Query("SELECT * FROM session_drink_item WHERE sessionId = :sessionId")
    List<SessionDrinkItemEntity> getDrinksForSession(int sessionId);

    @Query("DELETE FROM session_log")
    void deleteAllSessions();

    @Query("SELECT * FROM session_log WHERE id = :sessionId")
    SessionLogEntity getSessionById(int sessionId);
}