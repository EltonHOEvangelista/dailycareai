package com.example.dailycareai.ui.healthchecks;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dailycareai.databinding.CardHealthCheckBinding;
import com.example.dailycareai.ui.checkup.FaceDiagnostic;

import java.util.List;

public class HealthChecksAdapter extends RecyclerView.Adapter<HealthChecksAdapter.HealthCheckHolder> {

    List<FaceDiagnostic> healthCheckList;
    Context context;

    public HealthChecksAdapter(List<FaceDiagnostic> healthCheckList, Context context) {
        this.healthCheckList = healthCheckList;
        this.context = context;
    }

    @NonNull
    @Override
    public HealthCheckHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        CardHealthCheckBinding binding = CardHealthCheckBinding.inflate(inflater, parent, false);
        HealthCheckHolder holder = new HealthCheckHolder(binding.getRoot(), binding);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull HealthCheckHolder holder, int position) {

        holder.holderBinding.txtResult.setText(healthCheckList.get(position).getDrowsinessDescription());
        holder.holderBinding.txtCardDate.setText(healthCheckList.get(position).getDateDiagnostic());

        if(healthCheckList.get(position).getStableHeadPosition() == 1) {
            holder.holderBinding.switchHeadPosition.setChecked(true);
        }
        else {
            holder.holderBinding.switchHeadPosition.setChecked(false);
        }

        if(healthCheckList.get(position).getRegularBlinking() == 1) {
            holder.holderBinding.switchBlinking.setChecked(true);
        }
        else {
            holder.holderBinding.switchBlinking.setChecked(false);
        }

        if(healthCheckList.get(position).getSmiling() == 1) {
            holder.holderBinding.switchSmiling.setChecked(true);
        }
        else {
            holder.holderBinding.switchSmiling.setChecked(false);
        }
    }

    @Override
    public int getItemCount() {

        return healthCheckList.size();
    }

    public class HealthCheckHolder extends RecyclerView.ViewHolder {

        CardHealthCheckBinding holderBinding;

        public HealthCheckHolder(@NonNull View itemView, CardHealthCheckBinding holderBinding) {
            super(itemView);

            this.holderBinding = holderBinding;

            this.holderBinding.getRoot().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    //show checkup detail.
                }
            });
        }
    }
}
