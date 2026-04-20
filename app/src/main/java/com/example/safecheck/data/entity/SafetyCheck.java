package com.example.safecheck.data.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class SafetyCheck {

    @PrimaryKey(autoGenerate = true)
    public int checkId;

    public String date;
    public String vehicleRegistration;
    public String driverName;
    public String overallStatus;
}
