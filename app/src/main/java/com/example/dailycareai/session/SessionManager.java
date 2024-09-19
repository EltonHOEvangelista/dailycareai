package com.example.dailycareai.session;

import android.content.Context;

import com.example.dailycareai.sql.DatabaseHelper;
import com.example.dailycareai.ui.menu.AccountModel;

public class SessionManager {

    Context context;

    //default constructor get context as parameter.
    public SessionManager(Context context) {
        this.context = context;
    }

    public boolean StartSession(int accountId) {

        //Instantiating DatabaseHelper Class.
        DatabaseHelper databaseHelper = new DatabaseHelper(context);

        //close previous sessions and start new user's session.
        if(databaseHelper.EndAllSessions() &&
                databaseHelper.StartSession(accountId)) {

            databaseHelper.close();
            return true;
        }

        databaseHelper.close();
        return false;
    }

    public boolean StartSessionByEmail(String email) {

        //Instantiating DatabaseHelper Class.
        DatabaseHelper databaseHelper = new DatabaseHelper(context);

        AccountModel accountModel = databaseHelper.GetAccount(email);

        //close previous sessions and start new user's session.
        if(databaseHelper.EndAllSessions() &&
                databaseHelper.StartSession(accountModel.getAccountId())) {

            databaseHelper.close();
            return true;
        }

        databaseHelper.close();
        return false;
    }

    public SessionModel GetActiveSession() {

        //Instantiating DatabaseHelper Class.
        DatabaseHelper databaseHelper = new DatabaseHelper(context);
        SessionModel sessionModel = databaseHelper.GetActiveSession();
        databaseHelper.close();

        return sessionModel;
    }

    public SessionModel GetLatestSession() {

        //Instantiating DatabaseHelper Class.
        DatabaseHelper databaseHelper = new DatabaseHelper(context);

        SessionModel sessionModel = databaseHelper.GetLatestSession();

        databaseHelper.close();

        return sessionModel;
    }

    public boolean EndAllSessions() {

        //Instantiating DatabaseHelper Class.
        DatabaseHelper databaseHelper = new DatabaseHelper(context);

        //close previous sessions and start new user's session.
        if(databaseHelper.EndAllSessions()) {

            databaseHelper.close();
            return true;
        }

        databaseHelper.close();
        return false;
    }
}
