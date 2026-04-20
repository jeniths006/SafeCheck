package com.example.safecheck.ui;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.safecheck.R;
import com.example.safecheck.data.entity.Defect;
import com.example.safecheck.data.entity.SafetyCheck;
import com.example.safecheck.ui.viewmodel.SafetyViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Calendar;
import java.util.Locale;

public class AddCheckActivity extends AppCompatActivity {

    private SafetyViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_check);

        TextInputEditText etVehicle = findViewById(R.id.etVehicle);
        TextInputEditText etDriver = findViewById(R.id.etDriver);
        TextInputEditText etDate = findViewById(R.id.etDate);
        TextInputEditText etDefect = findViewById(R.id.etDefect);
        MaterialButtonToggleGroup toggleSeverity = findViewById(R.id.toggleSeverity);

        MaterialButton btnAddDefect = findViewById(R.id.btnAddDefect);
        MaterialButton btnSave = findViewById(R.id.btnSave);

        LinearLayout defectList = findViewById(R.id.defectList);

        viewModel = new ViewModelProvider(this).get(SafetyViewModel.class);

        // 🔹 ENABLE BACK BUTTON
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // 🔹 DATE PICKER
        etDate.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            new DatePickerDialog(this, (view, year, month, day) -> {
                String date = String.format(Locale.getDefault(), "%02d/%02d/%d", day, month + 1, year);
                etDate.setText(date);
                viewModel.dateInput.setValue(date);
            }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
        });

        // 🔹 VIEWMODEL -> UI (RESTORE STATE ON ROTATION)
        etVehicle.setText(viewModel.vehicleRegInput.getValue());
        etDriver.setText(viewModel.driverNameInput.getValue());
        etDate.setText(viewModel.dateInput.getValue());
        etDefect.setText(viewModel.defectDescriptionInput.getValue());
        if ("High".equals(viewModel.defectSeverityInput.getValue())) {
            toggleSeverity.check(R.id.btnHigh);
        } else {
            toggleSeverity.check(R.id.btnLow);
        }

        // 🔹 UI -> VIEWMODEL (SAVE STATE AS USER TYPES)
        etVehicle.addTextChangedListener(new SimpleTextWatcher(s -> viewModel.vehicleRegInput.setValue(s)));
        etDriver.addTextChangedListener(new SimpleTextWatcher(s -> viewModel.driverNameInput.setValue(s)));
        etDefect.addTextChangedListener(new SimpleTextWatcher(s -> viewModel.defectDescriptionInput.setValue(s)));
        
        toggleSeverity.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                viewModel.defectSeverityInput.setValue(checkedId == R.id.btnHigh ? "High" : "Low");
            }
        });

        // 🔹 OBSERVE PENDING DEFECTS
        viewModel.getPendingDefects().observe(this, defects -> {
            defectList.removeAllViews();
            for (Defect d : defects) {
                TextView tv = new TextView(this);
                tv.setText(String.format("[%s] %s", d.severity, d.description));
                tv.setPadding(0, 8, 0, 8);
                tv.setTextSize(16);
                defectList.addView(tv);
            }
        });

        btnAddDefect.setOnClickListener(v -> {
            String text = etDefect.getText().toString().trim();
            if (text.isEmpty()) {
                Toast.makeText(this, "Please enter defect description", Toast.LENGTH_SHORT).show();
                return;
            }
            viewModel.addPendingDefect();
            etDefect.setText(""); // ViewModel reset handled inside addPendingDefect
        });

        btnSave.setOnClickListener(v -> {
            String vehicleReg = viewModel.vehicleRegInput.getValue();
            
            // 🔹 2.3 INPUT VALIDATION
            if (vehicleReg == null || vehicleReg.trim().isEmpty()) {
                Toast.makeText(this, "Please enter vehicle details", Toast.LENGTH_SHORT).show();
                etVehicle.setError("Registration Required");
                return;
            }

            SafetyCheck check = new SafetyCheck();
            check.vehicleRegistration = vehicleReg;
            check.driverName = viewModel.driverNameInput.getValue();
            check.date = viewModel.dateInput.getValue();
            
            // 🔹 DYNAMIC STATUS
            boolean hasDefects = viewModel.getPendingDefects().getValue() != null && 
                                !viewModel.getPendingDefects().getValue().isEmpty();
            check.overallStatus = hasDefects ? "Fail" : "Pass";

            viewModel.insert(check, viewModel.getPendingDefects().getValue());

            Toast.makeText(this, "Safety check saved successfully", Toast.LENGTH_SHORT).show();
            viewModel.clearForm();
            finish();
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    // Helper class to reduce boilerplate
    private static class SimpleTextWatcher implements TextWatcher {
        private final java.util.function.Consumer<String> consumer;
        public SimpleTextWatcher(java.util.function.Consumer<String> consumer) { this.consumer = consumer; }
        @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override public void onTextChanged(CharSequence s, int start, int before, int count) { consumer.accept(s.toString()); }
        @Override public void afterTextChanged(Editable s) {}
    }
}
