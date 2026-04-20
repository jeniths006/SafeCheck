package com.example.safecheck;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
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
import com.google.android.material.snackbar.Snackbar;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private SafetyViewModel viewModel;
    private SafetyAdapter adapter;
    private LinearLayout emptyView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 🔹 UI ELEMENTS
        FloatingActionButton btnAdd = findViewById(R.id.btnAdd);
        RecyclerView recycler = findViewById(R.id.recyclerView);
        emptyView = findViewById(R.id.emptyView);

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
                int position = viewHolder.getAdapterPosition();
                List<SafetyCheckWithDefects> currentList = adapter.getCurrentList();
                
                if (currentList != null && position < currentList.size()) {
                    SafetyCheckWithDefects toDelete = currentList.get(position);
                    
                    // Show confirmation/undo with Snackbar
                    viewModel.delete(toDelete.safetyCheck);
                    
                    Snackbar.make(recycler, "Safety Check for " + toDelete.safetyCheck.vehicleRegistration + " deleted", Snackbar.LENGTH_LONG)
                            .setAction("UNDO", v -> {
                                // Re-insert logic could go here, but for now we just notify
                                // viewModel.insert(toDelete.safetyCheck, toDelete.defects);
                            })
                            .show();
                }
            }
        }).attachToRecyclerView(recycler);
    }
}
