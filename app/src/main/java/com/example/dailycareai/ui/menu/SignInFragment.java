package com.example.dailycareai.ui.menu;

import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import com.example.dailycareai.R;
import com.example.dailycareai.databinding.FragmentSignInBinding;
import com.example.dailycareai.session.SessionManager;
import com.example.dailycareai.session.SessionModel;
import com.example.dailycareai.sql.DatabaseHelper;
import com.example.dailycareai.ui.home.HomeFragment;

public class SignInFragment extends Fragment {

    public FragmentSignInBinding binding;
    private final int ENABLE = 1;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        binding = FragmentSignInBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();

        binding.btnSignin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AccountManager accountManager = new AccountManager(
                        getContext(),
                        binding.txtEmail.getText().toString().trim(),
                        binding.txtPassword.getText().toString().trim(),
                        binding.switchSavePwd.isChecked());

                if(!accountManager.SignInAccount()) {

                    Toast.makeText(getActivity(), "Fail to sign in. Please, check your credentials.", Toast.LENGTH_SHORT).show();
                }

                //Open Home Fragment
                SetActiveFragment(new HomeFragment());
            }
        });

        binding.btnAskSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                SetActiveFragment(new SignUpFragment());
            }
        });

        //check if password is set as saved.
        CheckSavedPassword();

        return view;
    }

    //if checked, fill out form.
    private void CheckSavedPassword() {

        //Instantiating database
        DatabaseHelper databaseHelper = new DatabaseHelper(getContext());

        //Instantiating session manager to get the latest session.
        SessionModel sessionModel = databaseHelper.GetLatestSession();

        if(sessionModel != null) {

            //Instantiating account model to get latest account.
            AccountModel accountModel = databaseHelper.GetAccountById
                    (sessionModel.getAccountId());

            databaseHelper.close();

            if(accountModel.isSavePassword() == ENABLE) {

                binding.txtEmail.setText(accountModel.getEmail());
                binding.txtPassword.setText(accountModel.getPassword());
                binding.switchSavePwd.setChecked(true);
            }
        }
        else {
            databaseHelper.close();
        }
    }

    private void SetActiveFragment(Fragment fragment) {

        try {

            FragmentManager manager = getActivity().getSupportFragmentManager();
            FragmentTransaction transaction = manager.beginTransaction();
            transaction.replace(R.id.nav_host_fragment_activity_main, fragment);
            transaction.commit();
        }
        catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onDestroyView() {

        super.onDestroyView();
    }
}