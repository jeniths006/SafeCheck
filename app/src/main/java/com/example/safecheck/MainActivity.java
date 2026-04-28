package com.example.safecheck;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.safecheck.data.entity.SafetyCheckWithDefects;
import com.example.safecheck.ui.AddCheckActivity;
import com.example.safecheck.ui.DetailActivity;
import com.example.safecheck.ui.adapter.SafetyAdapter;
import com.example.safecheck.ui.viewmodel.SafetyViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.snackbar.Snackbar;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private SafetyViewModel viewModel;
    private SafetyAdapter adapter;
    private LinearLayout emptyView;
    private TextView tvTotalCount;
    private TextView tvFailCount;
    private TextView tvComplianceLabel;
    private LinearProgressIndicator progressCompliance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 🔹 UI ELEMENTS
        FloatingActionButton btnAdd = findViewById(R.id.btnAdd);
        RecyclerView recycler = findViewById(R.id.recyclerView);
        emptyView = findViewById(R.id.emptyView);
        tvTotalCount = findViewById(R.id.tvTotalCount);
        tvFailCount = findViewById(R.id.tvFailCount);
        tvComplianceLabel = findViewById(R.id.tvComplianceLabel);
        progressCompliance = findViewById(R.id.progressCompliance);

        // 🔹 ADAPTER SETUP
        adapter = new SafetyAdapter(check -> {
            Intent intent = new Intent(this, DetailActivity.class);
            intent.putExtra("checkId", check.checkId);
            startActivity(intent);
        });

        recycler.setLayoutManager(new LinearLayoutManager(this));
        recycler.setAdapter(adapter);

        // 🔹 VIEWMODEL SETUP
        viewModel = new ViewModelProvider(this).get(SafetyViewModel.class);

        // 🔹 OBSERVE DATA
        viewModel.getAllChecksWithDefects().observe(this, checks -> {
            adapter.setChecks(checks);

            int total = checks == null ? 0 : checks.size();
            int failCount = 0;
            if (checks != null) {
                for (SafetyCheckWithDefects check : checks) {
                    if ("Fail".equalsIgnoreCase(check.safetyCheck.overallStatus)) {
                        failCount++;
                    }
                }
            }
            tvTotalCount.setText(String.valueOf(total));
            tvFailCount.setText(String.valueOf(failCount));
            int passCount = Math.max(0, total - failCount);
            int compliance = total == 0 ? 100 : (int) ((passCount * 100f) / total);
            tvComplianceLabel.setText("Compliance score: " + compliance + "%");
            progressCompliance.setProgress(compliance);
            progressCompliance.setIndicatorColor(ContextCompat.getColor(this,
                    compliance >= 80 ? R.color.status_pass : R.color.status_fail));
            
            // Toggle empty state visibility
            if (checks == null || checks.isEmpty()) {
                emptyView.setVisibility(View.VISIBLE);
                recycler.setVisibility(View.GONE);
            } else {
                emptyView.setVisibility(View.GONE);
                recycler.setVisibility(View.VISIBLE);
            }
        });

        // 🔹 NAVIGATION
        btnAdd.setOnClickListener(v -> {
            startActivity(new Intent(this, AddCheckActivity.class));
        });

        // 🔹 SWIPE TO DELETE (Requirement 2.3)
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getBindingAdapterPosition();
                List<SafetyCheckWithDefects> currentList = adapter.getCurrentList();
                
                if (currentList != null && position < currentList.size()) {
                    SafetyCheckWithDefects toDelete = currentList.get(position);
                    
                    // Show confirmation/undo with Snackbar
                    viewModel.delete(toDelete.safetyCheck);
                    
                    Snackbar.make(recycler, "Safety Check for " + toDelete.safetyCheck.vehicleRegistration + " deleted", Snackbar.LENGTH_LONG)
                            .setAction("UNDO", v -> {
                                viewModel.insert(toDelete.safetyCheck, toDelete.defects);
                            })
                            .show();
                }
            }
        }).attachToRecyclerView(recycler);
    }
}
