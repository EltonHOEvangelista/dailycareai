package com.example.dailycareai.ui.home;
/*
Student Name: Elton Henrique de Oliveira Evangelista
Student ID: 300371029
Course: CSIS 4175-050 – Mobile Application Development II
Instructor: Reza, Abbasi
April 08, 2024
 */

import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.dailycareai.R;
import com.example.dailycareai.databinding.FragmentHomeBinding;
import com.example.dailycareai.session.SessionManager;
import com.example.dailycareai.session.SessionModel;
import com.example.dailycareai.sql.DatabaseHelper;
import com.example.dailycareai.ui.checkup.FaceDiagnostic;
import com.example.dailycareai.ui.menu.AccountFragment;
import com.example.dailycareai.ui.menu.AccountManager;
import com.example.dailycareai.ui.menu.AccountModel;
import com.example.dailycareai.ui.menu.SignInFragment;
import com.example.dailycareai.ui.menu.SignUpFragment;

public class HomeFragment extends Fragment implements MenuProvider {

    FragmentHomeBinding binding;
    Menu menu;
    //controller variables.
    private final int ENABLE = 1;
    private final int DISABLE = 0;
    private final int SIGNED_IN = 1;
    private final int SIGNED_OUT = 0;

    //variable to handle diagnostic id (coming from AICamera/Bundle).
    private int diagnosticId;
    FaceDiagnostic faceDiagnostic;
    Handler handler = null;
    Runnable runnable;
    int index = 0;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        //get diagnostic id from Bundle (filled it out on AICamera Class(Fragment Checkup)).
        Bundle bundle = getArguments();

        if (bundle != null) {

            if(bundle.getInt("Diagnostic_Result") != 0) {

                binding.cardViewMessage.setVisibility(View.GONE);
                binding.layoutDiagnostic.setVisibility(View.VISIBLE);
                binding.scrollViewText.setVisibility(View.VISIBLE);

                //get diagnostic id from AICamera Class\Bundle.
                diagnosticId = bundle.getInt("Diagnostic_Result");

                //call method to display the result.
                DisplayDiagnostic();
            }
            else {

                binding.cardViewMessage.setVisibility(View.VISIBLE);
                binding.layoutDiagnostic.setVisibility(View.GONE);
                binding.scrollViewText.setVisibility(View.GONE);

                //call method to display message.
                String result = "Please reattempt the task, ensuring the camera is directed" +
                        "towards your face for optimal results.";

                DisplayMessage(result);
            }
        }
        else {

            binding.cardViewMessage.setVisibility(View.VISIBLE);
            binding.layoutDiagnostic.setVisibility(View.GONE);
            binding.scrollViewText.setVisibility(View.GONE);

            String message = "Hi, Click checkup for a quick health check!";

            DisplayMessage(message);
        }

        return view;
    }

    private void DisplayMessage(String message) {

        //
        handler = new Handler();

        runnable = new Runnable() {
            @Override
            public void run() {

                try {

                    binding.txtDailyCareAi.setText(message.subSequence(0, index++));

                    if (index <= message.length()) {
                        // Delay between each character.
                        handler.postDelayed(this, 30);
                    }
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };

        handler.postDelayed(runnable, 30);
    }

    private void DisplayDiagnostic() {

        try {

            //Instantiating database class.
            DatabaseHelper databaseHelper = new DatabaseHelper(getContext());

            //retrieving diagnostic.
            faceDiagnostic = databaseHelper.GetDiagnosticById(diagnosticId);

            //close database.
            databaseHelper.close();

            String smile;
            if(faceDiagnostic.getSmiling() == 1){
                smile = "smiling";
            }
            else{
                smile = "no smile";
            }

            String blinking;
            if(faceDiagnostic.getRegularBlinking() != 0) {
                blinking = "regular blinking";
            }
            else {
                blinking = "irregular blinking";
            }

            String head;
            if(faceDiagnostic.getStableHeadPosition() != 0) {
                head = "stable head position";
            }
            else{
                head = "unstable head position";
            }

            handler = new Handler();

            runnable = new Runnable() {
                @Override
                public void run() {

                    String result = "The diagnostic is ready!\n" +
                            "You look " + faceDiagnostic.getDrowsinessDescription() +
                            "\n Your have : " + blinking + "," +
                            "\n" + head + " and" +
                            "\n" + smile;

                    binding.txtDiagnostic.setText(result.subSequence(0, index++));

                    if (index <= result.length()) {
                        // Delay between each character.
                        handler.postDelayed(this, 30);
                    }
                    else {

                        DisplayReport();
                    }
                }
            };

            handler.postDelayed(runnable, 30);
        }
        catch (SQLiteException e) {
            e.printStackTrace();
        }
    }

    private void DisplayReport() {

        index = 0;

        runnable = new Runnable() {
            @Override
            public void run() {

                binding.txtDiagnosticReport.setText(faceDiagnostic.getDiagnosticDescription()
                        .subSequence(0, index++));

                if (index <= faceDiagnostic.getDiagnosticDescription().length()) {
                    // Delay between each character.
                    handler.postDelayed(this, 30);
                }
            }
        };

        handler.postDelayed(runnable, 30);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);
        getActivity().addMenuProvider(this, getViewLifecycleOwner(), getLifecycle().getCurrentState());
    }

    @Override
    public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {

        menuInflater.inflate(R.menu.toolbar_menu, menu);

        //giving class access to menu.
        this.menu = menu;

        //call method to setup menu item according to login status.
        MenuManager();
    }

    @Override
    public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {

        if(menuItem.getItemId() == R.id.menu_signin) {

            SetActiveFragment(new SignInFragment());
            return true;
        }
        else if (menuItem.getItemId() == R.id.menu_signup) {

            SetActiveFragment(new SignUpFragment());
            return true;
        }
        else if (menuItem.getItemId() == R.id.menu_myAccount) {


            SetActiveFragment(new AccountFragment());
            return true;
        }
        else if (menuItem.getItemId() == R.id.menu_signout) {
            //sign out

            //Instantiating database
            DatabaseHelper databaseHelper = new DatabaseHelper(getContext());

            //Instantiating session manager and get the active session.
            SessionManager sessionManager = new SessionManager(getContext());
            SessionModel sessionModel = sessionManager.GetActiveSession();

            //Instantiating account model to update the database.
            AccountModel accountModel = databaseHelper.GetAccountById
                    (sessionModel.getAccountId());

            //update save password as disable.
            accountModel.setSavePassword(DISABLE);
            databaseHelper.UpdateAccount(accountModel);

            databaseHelper.close();

            if(sessionManager.EndAllSessions()) {

                SetActiveFragment(new HomeFragment());
                return true;
            }
            return false;
        }
        return false;
    }

    //Setup menu item according to login status.
    public void MenuManager() {

        //Check existing opened session.
        DatabaseHelper databaseHelper = new DatabaseHelper(getContext());

        SessionModel sessionModel = databaseHelper.GetActiveSession();

        //active session.
        if(sessionModel != null) {

            databaseHelper.close();

            //call method to setup menu item.
            SetupMenuItem(SIGNED_IN);
        }
        //empty active session.
        else {
            //check whether the latest account session is set as savePassword or not.

            sessionModel = databaseHelper.GetLatestSession();

            if(sessionModel != null) {

                //Instantiating account model and filling it out with latest session account.
                AccountModel accountModel = databaseHelper.GetAccountById
                        (sessionModel.getAccountId());

                databaseHelper.close();

                if(accountModel.isSavePassword() == ENABLE) {

                    //if yes, sign in
                    AccountManager accountManager = new AccountManager(
                            getContext(),
                            accountModel.getEmail(),
                            accountModel.getPassword(),
                            true);

                    if(accountManager.SignInAccount()) {

                        //call method to setup menu item.
                        SetupMenuItem(SIGNED_IN);
                    }
                    else {

                        //call method to setup menu item.
                        SetupMenuItem(SIGNED_OUT);

                        Toast.makeText(getActivity(), "Fail to sign in. Please, check your credentials.",
                                Toast.LENGTH_SHORT).show();
                    }
                }
                else {

                    //call method to setup menu item.
                    SetupMenuItem(SIGNED_OUT);
                }
            }
            else{

                databaseHelper.close();

                //call method to setup menu item.
                SetupMenuItem(SIGNED_OUT);
            }
        }
    }

    //method to setup menu item.
    public void SetupMenuItem(int status) {

        //Instantiating Menu Item.
        MenuItem menuItem;

        switch (status) {

            case SIGNED_IN:

                //activate sign out and my account.
                menuItem = menu.findItem(R.id.menu_myAccount);
                menuItem.setEnabled(true);
                menuItem.setVisible(true);
                menuItem = menu.findItem(R.id.menu_signout);
                menuItem.setEnabled(true);
                menuItem.setVisible(true);

                //deactivate sign in and sign up
                menuItem = menu.findItem(R.id.menu_signin);
                menuItem.setEnabled(false);
                menuItem.setVisible(false);
                menuItem = menu.findItem(R.id.menu_signup);
                menuItem.setEnabled(false);
                menuItem.setVisible(false);

                break;

            case SIGNED_OUT:

                //deactivate sign out and my account.
                menuItem = menu.findItem(R.id.menu_myAccount);
                menuItem.setEnabled(false);
                menuItem.setVisible(false);
                menuItem = menu.findItem(R.id.menu_signout);
                menuItem.setEnabled(false);
                menuItem.setVisible(false);

                //activate sign in and sign up
                menuItem = menu.findItem(R.id.menu_signin);
                menuItem.setEnabled(true);
                menuItem.setVisible(true);
                menuItem = menu.findItem(R.id.menu_signup);
                menuItem.setEnabled(true);

                break;
        }
    }

    //method to setup fragment
    private void SetActiveFragment(Fragment fragment) {

        FragmentManager manager = getActivity().getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.replace(R.id.nav_host_fragment_activity_main, fragment);
        transaction.commit();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if(handler != null) {

            handler.removeCallbacks(runnable);
        }

        getActivity().removeMenuProvider(this);

        binding = null;
    }
}