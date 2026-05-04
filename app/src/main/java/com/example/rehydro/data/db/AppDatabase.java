package com.example.rehydro.data.db;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.example.rehydro.data.entity.CustomDrinkEntity;
import com.example.rehydro.data.entity.LocationEntry;
import com.example.rehydro.data.entity.SessionDrinkItemEntity;
import com.example.rehydro.data.entity.SessionLogEntity;

@Database(
        entities = {
                SessionLogEntity.class,
                SessionDrinkItemEntity.class,
                CustomDrinkEntity.class,
                LocationEntry.class
        },
        version = 4,
        exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {

    private static AppDatabase instance;

    private static final Migration MIGRATION_3_4 = new Migration(3, 4) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("DROP TABLE IF EXISTS photo_entry");
        }
    };

    public abstract SessionDao sessionDao();
    public abstract CustomDrinkDao customDrinkDao();
    public abstract LocationDao locationDao();

    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class,
                            "rehydro_db"
                    )
                    .addMigrations(MIGRATION_3_4)
                    .build();
        }
        return instance;
    }
}