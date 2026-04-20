package com.example.safecheck.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;

import com.example.safecheck.data.entity.Defect;
import com.example.safecheck.data.entity.SafetyCheck;
import com.example.safecheck.data.entity.SafetyCheckWithDefects;

import java.util.List;

@Dao
public interface SafetyDao {

    @Insert
    long insertSafetyCheck(SafetyCheck check);

    @Insert
    void insertDefect(Defect defect);

    @Transaction
    default void insertCheckWithDefects(SafetyCheck check, List<Defect> defects) {
        long id = insertSafetyCheck(check);
        if (defects != null) {
            for (Defect defect : defects) {
                defect.checkId = (int) id;
                insertDefect(defect);
            }
        }
    }

    @Query("SELECT * FROM SafetyCheck")
    LiveData<List<SafetyCheck>> getAllChecks();

    @Transaction
    @Query("SELECT * FROM SafetyCheck WHERE checkId = :id")
    LiveData<SafetyCheckWithDefects> getCheckWithDefects(int id);

    @Delete
    void deleteCheck(SafetyCheck check);

    @Transaction
    @Query("SELECT * FROM SafetyCheck")
    LiveData<List<SafetyCheckWithDefects>> getAllChecksWithDefects();
}
