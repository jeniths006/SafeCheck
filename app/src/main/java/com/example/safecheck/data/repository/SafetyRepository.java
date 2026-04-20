package com.example.safecheck.data.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.room.Room;

import com.example.safecheck.data.AppDatabase;
import com.example.safecheck.data.dao.SafetyDao;
import com.example.safecheck.data.entity.Defect;
import com.example.safecheck.data.entity.SafetyCheck;
import com.example.safecheck.data.entity.SafetyCheckWithDefects;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SafetyRepository {

    private SafetyDao dao;
    private ExecutorService executor;

    public SafetyRepository(Context context) {
        AppDatabase db = AppDatabase.getDatabase(context);
        dao = db.safetyDao();
        executor = Executors.newSingleThreadExecutor();
    }

    // ✅ FIXED: this is now the main LiveData list your ViewModel uses
    public LiveData<List<SafetyCheckWithDefects>> getAllChecks() {
        return dao.getAllChecksWithDefects();
    }

    public LiveData<SafetyCheckWithDefects> getCheck(int id) {
        return dao.getCheckWithDefects(id);
    }

    public void insert(SafetyCheck check, List<Defect> defects) {
        executor.execute(() -> dao.insertCheckWithDefects(check, defects));
    }

    public void delete(SafetyCheck check) {
        executor.execute(() -> dao.deleteCheck(check));
    }

    public LiveData<List<SafetyCheckWithDefects>> getAllChecksWithDefects() {
        return dao.getAllChecksWithDefects();
    }
}