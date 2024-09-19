package com.example.dailycareai.ui.menu;

import android.content.Context;

import com.example.dailycareai.session.SessionManager;
import com.example.dailycareai.session.SessionModel;
import com.example.dailycareai.sql.DatabaseHelper;

public class AccountManager {

    AccountModel accountModel;
    private String email;
    private String password;
    private boolean savePassword;
    private final int ENABLE = 1;
    private final int DISABLE =0;
    Context context;

    public AccountManager(Context _context, AccountModel accountModel) {

        this.accountModel = accountModel;
        this.context = _context;
    }

    public AccountManager(Context _context, String _email, String _password, boolean _savePassword) {

        this.context = _context;
        this.email = _email;
        this.password = _password;
        this.savePassword = _savePassword;
    }

    //method to create new account.
    public boolean CreateAccount() {

        //Instantiating DatabaseHelper Class.
        DatabaseHelper databaseHelper = new DatabaseHelper(context);

        //check if email already exists.
        String email = databaseHelper.GetLogin(accountModel.getEmail());

        if(email == null) {

            //Submit create account. Return true or false.
            if(databaseHelper.CreateAccount(accountModel)) {

                databaseHelper.close();
                return true;
            }

            databaseHelper.close();
            return false;
        }

        databaseHelper.close();
        return false;
    }

    public boolean UpdateAccount() {

        //Instantiating DatabaseHelper Class.
        DatabaseHelper databaseHelper = new DatabaseHelper(context);

        if(databaseHelper.UpdateAccount(accountModel)) {

            databaseHelper.close();
            return true;
        }
        else {

            databaseHelper.close();
            return false;
        }
    }

    public boolean SignInAccount() {

        if(ValidateSignForm()) {

            //Instantiating Account Model.
            AccountModel accountModel;

            //Instantiating DatabaseHelper Class.
            DatabaseHelper databaseHelper = new DatabaseHelper(context);
            accountModel = databaseHelper.GetAccount(email);
            databaseHelper.close();

            if(accountModel != null && password.equals(accountModel.getPassword())
                    && email.equals(accountModel.getEmail())) {

                //Open new user's session.
                SessionManager sessionManager = new SessionManager(context);
                if(sessionManager.StartSession(accountModel.getAccountId())) {

                    //Instantiating DatabaseHelper Class to update save password
                    databaseHelper = new DatabaseHelper(context);
                    if(savePassword) {
                        accountModel.setSavePassword(ENABLE);
                        databaseHelper.SaveAccountPassword(accountModel.getAccountId());
                    }
                    else {
                        accountModel.setSavePassword(DISABLE);
                        databaseHelper.SaveAccountPassword(accountModel.getAccountId());
                    }
                    databaseHelper.close();

                    return true;
                }
                return false;
            }
            return false;
        }
        return false;
    }

    public boolean ValidateSignForm() {

        email = email.trim();
        password = password.trim();

        if(!email.isEmpty() && !password.isEmpty()) {

            return true;
        }
        return false;
    }

    public AccountModel CheckSavedPassword() {

        //Instantiating database
        DatabaseHelper databaseHelper = new DatabaseHelper(context);

        SessionModel sessionModel = databaseHelper.GetLatestSession();

        //Instantiating account model and filling it out with latest session account.
        AccountModel accountModel = databaseHelper.GetAccountById
                (sessionModel.getAccountId());

        databaseHelper.close();

        if(accountModel != null && accountModel.isSavePassword() == ENABLE) {

            return accountModel;
        }

        return null;
    }
}
