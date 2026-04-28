package com.example.safecheck.ui;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.safecheck.R;
import com.example.safecheck.data.entity.Defect;
import com.example.safecheck.data.entity.SafetyCheck;
import com.example.safecheck.ui.viewmodel.SafetyViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.card.MaterialCardView;
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

        // 🔹 SET DEFAULT DATE IF EMPTY
        if (viewModel.dateInput.getValue() == null || viewModel.dateInput.getValue().isEmpty()) {
            Calendar c = Calendar.getInstance();
            String currentDate = String.format(Locale.getDefault(), "%02d/%02d/%d", 
                    c.get(Calendar.DAY_OF_MONTH), c.get(Calendar.MONTH) + 1, c.get(Calendar.YEAR));
            viewModel.dateInput.setValue(currentDate);
        }

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
        viewModel.vehicleRegInput.observe(this, s -> {
            if (!s.equals(String.valueOf(etVehicle.getText()))) {
                etVehicle.setText(s);
            }
        });
        viewModel.driverNameInput.observe(this, s -> {
            if (!s.equals(String.valueOf(etDriver.getText()))) {
                etDriver.setText(s);
            }
        });
        viewModel.dateInput.observe(this, s -> {
            if (!s.equals(String.valueOf(etDate.getText()))) {
                etDate.setText(s);
            }
        });
        viewModel.defectDescriptionInput.observe(this, s -> {
            if (!s.equals(String.valueOf(etDefect.getText()))) {
                etDefect.setText(s);
            }
        });

        viewModel.defectSeverityInput.observe(this, s -> {
            if ("High".equals(s)) {
                toggleSeverity.check(R.id.btnHigh);
            } else {
                toggleSeverity.check(R.id.btnLow);
            }
        });

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
                MaterialCardView defectCard = new MaterialCardView(this);
                defectCard.setRadius(14f);
                defectCard.setCardElevation(0f);
                defectCard.setStrokeWidth(1);
                defectCard.setStrokeColor(ContextCompat.getColor(this, R.color.surface_border));
                defectCard.setCardBackgroundColor(ContextCompat.getColor(this, R.color.surface));

                LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
                cardParams.bottomMargin = 12;
                defectCard.setLayoutParams(cardParams);

                LinearLayout cardContent = new LinearLayout(this);
                cardContent.setOrientation(LinearLayout.VERTICAL);
                cardContent.setPadding(20, 16, 20, 16);

                TextView severityBadge = new TextView(this);
                boolean isHigh = "High".equalsIgnoreCase(d.severity);
                severityBadge.setText(isHigh ? "HIGH SEVERITY" : "LOW SEVERITY");
                severityBadge.setTextSize(11f);
                severityBadge.setTextColor(ContextCompat.getColor(this, isHigh ? R.color.status_fail : R.color.status_pass));
                severityBadge.setPadding(16, 6, 16, 6);
                severityBadge.setBackgroundResource(R.drawable.bg_status_pill);
                severityBadge.getBackground().setTint(ContextCompat.getColor(this,
                        isHigh ? R.color.status_pill_bg_fail : R.color.status_pill_bg
                ));

                TextView description = new TextView(this);
                description.setText(d.description);
                description.setTextSize(15f);
                description.setTextColor(ContextCompat.getColor(this, R.color.on_surface));
                description.setPadding(0, 10, 0, 0);

                cardContent.addView(severityBadge);
                cardContent.addView(description);
                defectCard.addView(cardContent);
                defectList.addView(defectCard);
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
            String driverName = viewModel.driverNameInput.getValue();
            String date = viewModel.dateInput.getValue();
            
            // 🔹 INPUT VALIDATION
            if (vehicleReg == null || vehicleReg.trim().isEmpty()) {
                Toast.makeText(this, "Please enter vehicle registration", Toast.LENGTH_SHORT).show();
                etVehicle.setError("Registration Required");
                return;
            }

            if (driverName == null || driverName.trim().isEmpty()) {
                Toast.makeText(this, "Please enter driver name", Toast.LENGTH_SHORT).show();
                etDriver.setError("Driver Name Required");
                return;
            }

            if (date == null || date.trim().isEmpty()) {
                Toast.makeText(this, "Please select a date", Toast.LENGTH_SHORT).show();
                etDate.setError("Date Required");
                return;
            }

            SafetyCheck check = new SafetyCheck();
            check.vehicleRegistration = vehicleReg;
            check.driverName = driverName;
            check.date = date;
            
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
        getOnBackPressedDispatcher().onBackPressed();
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
