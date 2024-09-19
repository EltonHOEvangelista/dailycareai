package com.example.dailycareai.ui.menu;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.dailycareai.R;
import com.example.dailycareai.databinding.FragmentAccountBinding;
import com.example.dailycareai.session.SessionModel;
import com.example.dailycareai.sql.DatabaseHelper;
import com.example.dailycareai.ui.home.HomeFragment;

public class AccountFragment extends Fragment {

    FragmentAccountBinding binding;

    //Instantiating Account Model.
    AccountModel accountModel;

    public AccountFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentAccountBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();

        LoadAccountForm();

        binding.btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(ValidateForm()) {

                    //Instantiating Account Manager
                    AccountManager accountManager = new AccountManager(getContext(), accountModel);

                    //call method to update account.
                    if(accountManager.UpdateAccount()) {

                        Toast.makeText(getActivity(), "Account successfully updated.", Toast.LENGTH_SHORT).show();
                        SetActiveFragment(new HomeFragment());
                    }
                    else {

                        Toast.makeText(getActivity(), "Fail to update account.", Toast.LENGTH_SHORT).show();
                    }
                }
                else {

                    Toast.makeText(getActivity(), "Invalid data. Verify form fields.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        return view;
    }

    //method to load Account form.
    private void LoadAccountForm() {

        DatabaseHelper databaseHelper = new DatabaseHelper(getContext());

        SessionModel sessionModel = databaseHelper.GetActiveSession();

        accountModel = databaseHelper.GetAccountById(sessionModel.getAccountId());

        binding.txtAccFirstName.setText(accountModel.getFirstName());
        binding.txtAccLastName.setText(accountModel.getLastName());
        binding.txtAccEmail.setText(accountModel.getEmail());
        binding.txtAccPassword.setText(accountModel.getPassword());
        binding.txtAccConfirmPassword.setText(accountModel.getPassword());
    }

    private boolean ValidateForm() {

        //fill out account model with form data.
        accountModel.setFirstName(binding.txtAccFirstName.getText().toString().trim());
        accountModel.setLastName(binding.txtAccLastName.getText().toString().trim());
        accountModel.setEmail(binding.txtAccEmail.getText().toString().trim());
        accountModel.setPassword(binding.txtAccPassword.getText().toString().trim());
        accountModel.setConfirmPassword(binding.txtAccConfirmPassword.getText().toString().trim());

        if(!accountModel.getFirstName().isEmpty() && !accountModel.getLastName().isEmpty()
                && !accountModel.getEmail().isEmpty() && !accountModel.getPassword().isEmpty()
                && !accountModel.getConfirmPassword().isEmpty()) {

            //validate password and confirm password. At least 4 digits.
            if(accountModel.getPassword().equals(accountModel.getConfirmPassword())
                    && accountModel.getPassword().length() > 3) {

                return true;
            }
            return false;
        }
        return false;
    }

    private void SetActiveFragment(Fragment fragment) {

        FragmentManager manager = getActivity().getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.replace(R.id.nav_host_fragment_activity_main, fragment);
        transaction.commit();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

}