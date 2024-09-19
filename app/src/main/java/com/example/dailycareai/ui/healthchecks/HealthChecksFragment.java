package com.example.dailycareai.ui.healthchecks;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.dailycareai.databinding.FragmentHealthChecksBinding;
import com.example.dailycareai.session.SessionManager;
import com.example.dailycareai.session.SessionModel;
import com.example.dailycareai.sql.DatabaseHelper;
import com.example.dailycareai.ui.checkup.FaceDiagnostic;
import com.example.dailycareai.ui.menu.AccountModel;

import java.util.ArrayList;
import java.util.List;

public class HealthChecksFragment extends Fragment {

    FragmentHealthChecksBinding binding;
    List<FaceDiagnostic> healthCheckList = new ArrayList<>();


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentHealthChecksBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        //retrieve data from database
        LoadHealthChecks();

        return view;
    }

    //method to retrieve data from database
    private void LoadHealthChecks() {

        //retrieving list of health checks by account id.
        DatabaseHelper databaseHelper = new DatabaseHelper(getContext());

        SessionModel sessionModel = databaseHelper.GetActiveSession();

        if(sessionModel != null) {

            healthCheckList =  databaseHelper.GetDiagnosticsByAccountId(sessionModel.getAccountId());

            //close database.
            databaseHelper.close();

            if(healthCheckList != null) {

                //load recyclerView
                LoadHealthChecksOnRecyclerView();
            }
            else {

                Toast.makeText(getContext(), "Empty list. Please, get you first health check.", Toast.LENGTH_SHORT).show();
            }
        }
        else {

            Toast.makeText(getContext(), "Please, sign in to get face analysis.", Toast.LENGTH_SHORT).show();

            //close database.
            databaseHelper.close();
        }
    }

    private void LoadHealthChecksOnRecyclerView() {

        HealthChecksAdapter healthChecksAdapter = new HealthChecksAdapter(healthCheckList, getContext());
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        binding.recyclerViewDailyCareAI.setAdapter(healthChecksAdapter);
        binding.recyclerViewDailyCareAI.setLayoutManager(layoutManager);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}