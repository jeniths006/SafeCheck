package com.example.safecheck.data.entity;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

@Entity(foreignKeys = @ForeignKey(
        entity = SafetyCheck.class,
        parentColumns = "checkId",
        childColumns = "checkId",
        onDelete = ForeignKey.CASCADE

))
public class Defect {

    @PrimaryKey(autoGenerate = true)
    public int defectId;

    public int checkId;
    public String description;
    public String severity;
}
