package com.example.dailycareai;
/*
Student Name: Elton Henrique de Oliveira Evangelista
Student ID: 300371029
Course: CSIS 4175-050 – Mobile Application Development II
Instructor: Reza, Abbasi
April 08, 2024
 */

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.dailycareai.session.SessionManager;
import com.example.dailycareai.ui.checkup.AICamera;
import com.example.dailycareai.ui.healthchecks.HealthChecksFragment;
import com.example.dailycareai.ui.home.HomeFragment;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.dailycareai.databinding.ActivityMainBinding;
import com.example.dailycareai.ui.menu.SignInFragment;
import com.example.dailycareai.ui.videoanalysis.VideoAnalysis;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        //setup clickListener to Bottom Navigation Bar
        binding.bottomNavView.setOnItemSelectedListener(item -> {

            if(item.getItemId() == R.id.navigation_home) {

                SetActiveFragment(new HomeFragment());
            }
            //Sign in required to perform face analysis.
            else if (item.getItemId() == R.id.navigation_checkup) {

                //verify active session.
                if(CheckActiveSession()){

                    SetActiveFragment(new AICamera());
                }
                else {

                    Toast.makeText(this, "Please, sign in to get face analysis.", Toast.LENGTH_SHORT).show();
                    SetActiveFragment(new SignInFragment());
                }
            }
            //Sign in required to perform face analysis.
            else if(item.getItemId() == R.id.navigation_health_checks) {

                if(CheckActiveSession()){

                    SetActiveFragment(new HealthChecksFragment());
                }
                else {

                    Toast.makeText(this, "Please, sign in to get face analysis.", Toast.LENGTH_SHORT).show();
                    SetActiveFragment(new SignInFragment());
                }
            }
            //Sign in required to perform face analysis.
            else if(item.getItemId() == R.id.navigation_video_analysis) {

                if(CheckActiveSession()){

                    SetActiveFragment(new VideoAnalysis());
                }
                else {

                    Toast.makeText(this, "Please, sign in to perform video analysis.", Toast.LENGTH_SHORT).show();
                    SetActiveFragment(new SignInFragment());
                }
            }

            return true;
        });

        //call method to close previous sessions on the app.
        SessionManager sessionManager = new SessionManager(this);
        sessionManager.EndAllSessions();
    }

    //method to verify active session. Sign in required to perform face analysis.
    private boolean CheckActiveSession() {

        SessionManager sessionManager = new SessionManager(this);

        if(sessionManager.GetActiveSession() != null) {

            return true;
        }
        return false;
    }

    //method to setup fragment
    private void SetActiveFragment(Fragment fragment) {

        FragmentManager manager = this.getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.replace(R.id.nav_host_fragment_activity_main, fragment);
        transaction.commit();
    }
}