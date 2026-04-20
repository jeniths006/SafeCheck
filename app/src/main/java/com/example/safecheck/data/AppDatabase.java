package com.example.safecheck.data;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.safecheck.data.dao.SafetyDao;
import com.example.safecheck.data.entity.Defect;
import com.example.safecheck.data.entity.SafetyCheck;

@Database(entities = {SafetyCheck.class, Defect.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract SafetyDao safetyDao();

    private static volatile AppDatabase INSTANCE;

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "safecheck_db")
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
