package com.example.safecheck.ui.viewmodel;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.safecheck.data.entity.Defect;
import com.example.safecheck.data.entity.SafetyCheck;
import com.example.safecheck.data.entity.SafetyCheckWithDefects;
import com.example.safecheck.data.repository.SafetyRepository;

import java.util.ArrayList;
import java.util.List;

public class SafetyViewModel extends AndroidViewModel {

    private SafetyRepository repo;


    public MutableLiveData<String> vehicleRegInput = new MutableLiveData<>("");
    public MutableLiveData<String> driverNameInput = new MutableLiveData<>("");
    public MutableLiveData<String> dateInput = new MutableLiveData<>("");
    public MutableLiveData<String> defectDescriptionInput = new MutableLiveData<>("");
    public MutableLiveData<String> defectSeverityInput = new MutableLiveData<>("Low");

    private MutableLiveData<List<Defect>> pendingDefects = new MutableLiveData<>(new ArrayList<>());

    public SafetyViewModel(Application app) {
        super(app);
        repo = new SafetyRepository(app);
    }

    public LiveData<List<Defect>> getPendingDefects() {
        return pendingDefects;
    }

    public void addPendingDefect() {
        String desc = defectDescriptionInput.getValue();
        String sev = defectSeverityInput.getValue();

        if (desc == null || desc.trim().isEmpty()) return;
        
        List<Defect> current = pendingDefects.getValue();
        List<Defect> newList = new ArrayList<>();
        if (current != null) {
            newList.addAll(current);
        }
        
        Defect d = new Defect();
        d.description = desc;
        d.severity = sev != null ? sev : "Low";
        
        newList.add(d);
        pendingDefects.setValue(newList);
        
        // Reset input fields
        defectDescriptionInput.setValue("");
        defectSeverityInput.setValue("Low");
    }

    public void clearForm() {
        vehicleRegInput.setValue("");
        driverNameInput.setValue("");
        dateInput.setValue("");
        defectDescriptionInput.setValue("");
        defectSeverityInput.setValue("Low");
        pendingDefects.setValue(new ArrayList<>());
    }

    public LiveData<List<SafetyCheckWithDefects>> getAllChecks() {
        return repo.getAllChecks();
    }

    public LiveData<SafetyCheckWithDefects> getCheck(int id) {
        return repo.getCheck(id);
    }

    public void insert(SafetyCheck check, List<Defect> defects) {
        repo.insert(check, defects);
    }

    public void delete(SafetyCheck check) {
        repo.delete(check);
    }

    public LiveData<List<SafetyCheckWithDefects>> getAllChecksWithDefects() {
        return repo.getAllChecksWithDefects();
    }
}
