package com.example.safecheck.ui;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.safecheck.R;
import com.example.safecheck.data.entity.Defect;
import com.example.safecheck.ui.viewmodel.SafetyViewModel;

public class DetailActivity extends AppCompatActivity {

    private SafetyViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        // 🔹 GET ID FROM INTENT
        int id = getIntent().getIntExtra("checkId", -1);

        // 🔹 INIT VIEWMODEL
        viewModel = new ViewModelProvider(this).get(SafetyViewModel.class);

        // 🔹 ENABLE BACK BUTTON
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // 🔹 FIND VIEWS
        TextView tvVehicle = findViewById(R.id.tvVehicle);
        TextView tvStatus = findViewById(R.id.tvStatus);
        TextView tvStatusPill = findViewById(R.id.tvStatusPill);
        TextView tvDriver = findViewById(R.id.tvDriver);
        TextView tvDate = findViewById(R.id.tvDate);
        TextView tvDefects = findViewById(R.id.tvDefects);
        Button btnEmail = findViewById(R.id.btnEmail);
        Button btnDelete = findViewById(R.id.btnDelete);


        viewModel.getCheck(id).observe(this, data -> {

            if (data == null) return;

            // VEHICLE
            tvVehicle.setText(data.safetyCheck.vehicleRegistration);

            // STATUS + COUNT
            tvStatus.setText(
                    "Status: " + data.safetyCheck.overallStatus +
                            " (" + data.defects.size() + " defects)"
            );
            tvDriver.setText("Driver: " + (data.safetyCheck.driverName == null || data.safetyCheck.driverName.isEmpty()
                    ? "Not provided"
                    : data.safetyCheck.driverName));
            tvDate.setText("Date: " + (data.safetyCheck.date == null || data.safetyCheck.date.isEmpty()
                    ? "Not provided"
                    : data.safetyCheck.date));

            boolean isPass = "Pass".equalsIgnoreCase(data.safetyCheck.overallStatus);
            int statusColor = ContextCompat.getColor(this, isPass ? R.color.status_pass : R.color.status_fail);
            int statusBgColor = ContextCompat.getColor(this, isPass ? R.color.status_pill_bg : R.color.status_pill_bg_fail);
            tvStatusPill.setText(isPass ? "Pass" : "Fail");
            tvStatusPill.setTextColor(statusColor);
            Drawable statusPill = tvStatusPill.getBackground().mutate();
            statusPill.setTint(statusBgColor);


            StringBuilder defectsText = new StringBuilder();

            for (Defect d : data.defects) {
                defectsText.append("- ")
                        .append(d.description)
                        .append(" (")
                        .append(d.severity)
                        .append(")\n");
            }

            if (data.defects.isEmpty()) {
                defectsText.append("No defects");
            }

            tvDefects.setText(defectsText.toString());


            btnEmail.setOnClickListener(v -> {
                String subject = "Safety Defect Report: " + data.safetyCheck.vehicleRegistration;
                
                StringBuilder body = new StringBuilder();
                body.append("Vehicle: ").append(data.safetyCheck.vehicleRegistration).append("\n");
                body.append("Driver: ").append(data.safetyCheck.driverName).append("\n");
                body.append("Date: ").append(data.safetyCheck.date).append("\n");
                body.append("Status: ").append(data.safetyCheck.overallStatus).append("\n\n");
                body.append("Defects Found:\n");

                if (data.defects.isEmpty()) {
                    body.append("- No defects reported.");
                } else {
                    for (Defect d : data.defects) {
                        body.append("- ").append(d.description).append(" (").append(d.severity).append(")\n");
                    }
                }

                Intent intent = new Intent(Intent.ACTION_SENDTO);
                intent.setData(android.net.Uri.parse("mailto:")); // ensures only email apps handle this
                intent.putExtra(Intent.EXTRA_SUBJECT, subject);
                intent.putExtra(Intent.EXTRA_TEXT, body.toString());
                
                try {
                    startActivity(intent);
                } catch (android.content.ActivityNotFoundException ex) {
                    android.widget.Toast.makeText(this, "No email app installed", android.widget.Toast.LENGTH_SHORT).show();
                }
            });


            btnDelete.setOnClickListener(v -> {
                viewModel.delete(data.safetyCheck);
                finish();
            });
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
