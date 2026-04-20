package com.example.safecheck.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.safecheck.R;
import com.example.safecheck.data.entity.SafetyCheck;
import com.example.safecheck.data.entity.SafetyCheckWithDefects;

import java.util.ArrayList;
import java.util.List;

public class SafetyAdapter extends RecyclerView.Adapter<SafetyAdapter.ViewHolder> {

    private List<SafetyCheckWithDefects> list = new ArrayList<>();
    private OnClickListener listener;

    public interface OnClickListener {
        void onClick(SafetyCheck check);
    }

    public SafetyAdapter(OnClickListener listener) {
        this.listener = listener;
    }

    public void setChecks(List<SafetyCheckWithDefects> checks) {
        this.list = checks;
        notifyDataSetChanged();
    }

    public List<SafetyCheckWithDefects> getCurrentList() {
        return list;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView title, date, count;
        ImageView statusIcon;
        View statusStripe;

        ViewHolder(View v) {
            super(v);
            title = v.findViewById(R.id.tvTitle);
            date = v.findViewById(R.id.tvDate);
            count = v.findViewById(R.id.tvCount);
            statusIcon = v.findViewById(R.id.ivStatus);
            statusStripe = v.findViewById(R.id.viewStatusStripe);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_row, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        SafetyCheckWithDefects data = list.get(position);

        holder.title.setText(data.safetyCheck.vehicleRegistration);
        holder.date.setText(data.safetyCheck.date);
        
        int defectCount = data.defects.size();
        holder.count.setText(defectCount + (defectCount == 1 ? " Defect" : " Defects"));

        boolean isPass = "Pass".equalsIgnoreCase(data.safetyCheck.overallStatus);
        int statusColor = ContextCompat.getColor(holder.itemView.getContext(), 
                isPass ? R.color.status_pass : R.color.status_fail);
        
        if (holder.statusStripe != null) {
            holder.statusStripe.setBackgroundColor(statusColor);
        }

        holder.itemView.setOnClickListener(v ->
                listener.onClick(data.safetyCheck)
        );
    }

    @Override
    public int getItemCount() {
        return list == null ? 0 : list.size();
    }
}