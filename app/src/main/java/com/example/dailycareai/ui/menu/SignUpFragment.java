package com.example.dailycareai.ui.menu;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.dailycareai.R;
import com.example.dailycareai.databinding.FragmentSignUpBinding;
import com.example.dailycareai.session.SessionManager;
import com.example.dailycareai.ui.home.HomeFragment;

public class SignUpFragment extends Fragment {

    FragmentSignUpBinding binding;
    AccountModel accountModel = new AccountModel();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        binding = FragmentSignUpBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();

        binding.btnSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //validate form
                if(ValidateForm()) {

                    //call method to create account.
                    AccountManager accountManager = new AccountManager(getContext(), accountModel);

                    if(accountManager.CreateAccount()) {

                        Toast.makeText(getActivity(), "Thanks for signing up!", Toast.LENGTH_SHORT).show();

                        //start new session.
                        SessionManager sessionManager = new SessionManager(getContext());

                        if(sessionManager.EndAllSessions()) {

                            if(sessionManager.StartSessionByEmail(accountModel.getEmail())) {

                                //Open Home Fragment
                                SetActiveFragment(new HomeFragment());
                            }
                            else {

                                Toast.makeText(getActivity(), "Fail to open new session", Toast.LENGTH_SHORT).show();
                                //Open Home Fragment
                                SetActiveFragment(new SignInFragment());
                            }
                        }
                    }
                    else {

                        //account already exists.
                        Toast.makeText(getActivity(), "Account already exists", Toast.LENGTH_SHORT).show();
                        //Open SignIn Fragment
                        SetActiveFragment(new SignInFragment());
                    }
                }
                else {
                    Toast.makeText(getActivity(), "Invalid data. Verify form fields.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        return view;
    }

    private boolean ValidateForm() {

        //fill out account model with form data.
        accountModel.setFirstName(binding.txtFirstName.getText().toString().trim());
        accountModel.setLastName(binding.txtLastName.getText().toString().trim());
        accountModel.setEmail(binding.txtEmail.getText().toString().trim());
        accountModel.setPassword(binding.txtPassword.getText().toString().trim());
        accountModel.setConfirmPassword(binding.txtConfirmPassword.getText().toString().trim());

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