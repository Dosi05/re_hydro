package com.example.rehydro.data.db;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.rehydro.data.entity.CustomDrinkEntity;

import java.util.List;

@Dao
public interface CustomDrinkDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(CustomDrinkEntity drink);

    @Query("SELECT * FROM custom_drink ORDER BY name ASC")
    List<CustomDrinkEntity> getAll();

    @Query("SELECT * FROM custom_drink WHERE LOWER(name) LIKE LOWER(:query) ORDER BY name ASC")
    List<CustomDrinkEntity> search(String query);

    @Query("DELETE FROM custom_drink WHERE name = :name")
    void deleteByName(String name);
}