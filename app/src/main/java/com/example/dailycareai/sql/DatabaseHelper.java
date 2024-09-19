package com.example.dailycareai.sql;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.dailycareai.session.SessionModel;
import com.example.dailycareai.ui.checkup.AICameraModel;
import com.example.dailycareai.ui.checkup.FaceDiagnostic;
import com.example.dailycareai.ui.menu.AccountModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "dailycareai.db";
    private static final int DATABASE_VERSION = 1;

    //variables for Account Table
    private static final String TABLE_Account = "ACCOUNT";
    private static final String COLUMN_ACCOUNT_ID = "AccountId";
    private static final String COLUMN_FIRST_NAME = "FirstName";
    private static final String COLUMN_LAST_NAME = "LastName";
    private static final String COLUMN_EMAIL = "Email";
    private static final String COLUMN_PASSWORD = "Password";
    private static final String COLUMN_ENABLE = "Enable";
    private static final String COLUMN_SAVE_PWD = "SavePwd";

    //variables for Session Table
    private static final String TABLE_Session = "SESSION";
    private static final String COLUMN_SESSION_ID = "SessionId";
    private static final String COLUMN_TIME_BEGIN = "TimeBegin";
    private static final String COLUMN_TIME_END = "TimeEnd";
    private static final String COLUMN_IS_ON = "IsON";

    //variables for Face Diagnostic Parameters Table
    private static final String TABLE_Diagnostic_Parameters = "DIAGNOSTIC_PARAMETERS";
    private static final String COLUMN_DIAG_PARAMETER_ID = "DiagnosticParameterId";
    private static final String COLUMN_DIAG_PARAMETER_DESCRIPTION = "DiagnosticParameterDescription";

    //variables for Face Diagnostic Table
    private static final String TABLE_Diagnostic = "DIAGNOSTIC";
    private static final String COLUMN_DIAG_ID = "DiagnosticId";
    private static final String COLUMN_DROWSINESS = "Drowsiness";
    private static final String COLUMN_STABLE_HEAD_POSITION = "stableHeadPosition";
    private static final String COLUMN_REGULAR_BLINKING = "regularBlinking";
    private static final String COLUMN_SMILING = "Smiling";
    private static final String COLUMN_OVERALL_DIAGNOSTIC = "overallDiagnostic";
    private static final String COLUMN_DATE_DIAGNOSTIC = "dateDiagnostic";
    private static final String COLUMN_REPORT_DIAGNOSTIC = "reportDiagnostic";

    //variables for Camera and Face Detection Control.
    private static final String TABLE_Face_Detection = "FACE_DETECTION";
    private static final String COLUMN_FACE_DETECTION_ID = "FaceDetectionId";
    private static final String COLUMN_FACE_DETECTION_STATUS = "FaceDetectionStatus"; //success or fail
    private static final String COLUMN_FACE_DETECTION_CONTROL = "FaceDetectionControl"; // open/close
    private static final String TABLE_Face_Detection_Parameter = "FACE_DETECTION_PARAMETERS";
    private static final String COLUMN_CAMERA_DETECTION_ID = "CameraDetectionId";
    private static final String COLUMN_CAMERA_DETECTION_DESCRIPTION = "CameraDetectionDescription";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null,  DATABASE_VERSION);
    }

    private final int ENABLE = 1;
    private final int DISABLE = 0;

    @Override
    public void onCreate(SQLiteDatabase db) {

        //Create user account table.
        String CREATE_TABLE_USER_ACCOUNT = "CREATE TABLE " + TABLE_Account + "("
                + COLUMN_ACCOUNT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_FIRST_NAME + " TEXT,"
                + COLUMN_LAST_NAME + " TEXT,"
                + COLUMN_EMAIL + " TEXT,"
                + COLUMN_PASSWORD + " TEXT,"
                + COLUMN_ENABLE + " INTEGER,"
                + COLUMN_SAVE_PWD + " INTEGER"
                + ");";

        db.execSQL(CREATE_TABLE_USER_ACCOUNT);

        //Create Session Table
        String CREATE_TABLE_SESSION = "CREATE TABLE " + TABLE_Session + "("
                + COLUMN_SESSION_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_TIME_BEGIN + " TEXT,"
                + COLUMN_TIME_END + " TEXT,"
                + COLUMN_IS_ON + " INTEGER,"
                + COLUMN_ACCOUNT_ID + " INTEGER,"
                + "FOREIGN KEY (" + COLUMN_ACCOUNT_ID + ") REFERENCES "
                    + TABLE_Account + "(" + COLUMN_ACCOUNT_ID + ")"
                + ");";

        db.execSQL(CREATE_TABLE_SESSION);

        //Create Face Diagnostic Parameters Table
        String CREATE_TABLE_DIAGNOSTIC_PARAMETERS = "CREATE TABLE " + TABLE_Diagnostic_Parameters + "("
                + COLUMN_DIAG_PARAMETER_ID + " INTEGER PRIMARY KEY,"
                + COLUMN_DIAG_PARAMETER_DESCRIPTION + " TEXT,"
                + COLUMN_REPORT_DIAGNOSTIC + " TEXT"
                + ");";

        db.execSQL(CREATE_TABLE_DIAGNOSTIC_PARAMETERS);

        //Create Face Diagnostic Table
        String CREATE_TABLE_DIAGNOSTIC = "CREATE TABLE " + TABLE_Diagnostic + "("
                + COLUMN_DIAG_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_DROWSINESS + " INTEGER,"
                + COLUMN_STABLE_HEAD_POSITION + " INTEGER,"
                + COLUMN_REGULAR_BLINKING + " INTEGER,"
                + COLUMN_SMILING + " INTEGER,"
                + COLUMN_OVERALL_DIAGNOSTIC + " INTEGER,"
                + COLUMN_DATE_DIAGNOSTIC + " TEXT,"
                + COLUMN_ACCOUNT_ID + " INTEGER,"
                + "FOREIGN KEY (" + COLUMN_ACCOUNT_ID + ") REFERENCES "
                + TABLE_Account + "(" + COLUMN_ACCOUNT_ID + "),"
                + "FOREIGN KEY (" + COLUMN_DROWSINESS + ") REFERENCES "
                + TABLE_Diagnostic_Parameters + "(" + COLUMN_DIAG_PARAMETER_ID + "),"
                + "FOREIGN KEY (" + COLUMN_STABLE_HEAD_POSITION + ") REFERENCES "
                + TABLE_Diagnostic_Parameters + "(" + COLUMN_DIAG_PARAMETER_ID + "),"
                + "FOREIGN KEY (" + COLUMN_REGULAR_BLINKING + ") REFERENCES "
                + TABLE_Diagnostic_Parameters + "(" + COLUMN_DIAG_PARAMETER_ID + "),"
                + "FOREIGN KEY (" + COLUMN_SMILING + ") REFERENCES "
                + TABLE_Diagnostic_Parameters + "(" + COLUMN_DIAG_PARAMETER_ID + "),"
                + "FOREIGN KEY (" + COLUMN_OVERALL_DIAGNOSTIC + ") REFERENCES "
                + TABLE_Diagnostic_Parameters + "(" + COLUMN_DIAG_PARAMETER_ID + ")"
                + ");";

        db.execSQL(CREATE_TABLE_DIAGNOSTIC);

        //Create TABLE_Face_Detection_Parameter
        String CREATE_TABLE_FACE_DETECTION_PARAMETERS = "CREATE TABLE " + TABLE_Face_Detection_Parameter + "("
                + COLUMN_CAMERA_DETECTION_ID + " INTEGER PRIMARY KEY,"
                + COLUMN_CAMERA_DETECTION_DESCRIPTION + " TEXT"
                + ");";

        db.execSQL(CREATE_TABLE_FACE_DETECTION_PARAMETERS);

        //Create TABLE_Face_Detection
        String CREATE_TABLE_FACE_DETECTION = "CREATE TABLE " + TABLE_Face_Detection + "("
                + COLUMN_FACE_DETECTION_ID + " INTEGER PRIMARY KEY,"
                + COLUMN_FACE_DETECTION_STATUS + " INTEGER,"
                + COLUMN_FACE_DETECTION_CONTROL + " INTEGER,"
                + "FOREIGN KEY (" + COLUMN_FACE_DETECTION_STATUS + ") REFERENCES "
                + TABLE_Face_Detection_Parameter + "(" + COLUMN_CAMERA_DETECTION_ID + ")"
                + ");";

        db.execSQL(CREATE_TABLE_FACE_DETECTION);

        //populate Face Diagnostic Parameters Table with initial data.
        String INSERT_INTO_DIAGNOSTIC_PARAMETERS = "INSERT INTO " + TABLE_Diagnostic_Parameters
                + " (" + COLUMN_DIAG_PARAMETER_ID + ", "
                        + COLUMN_DIAG_PARAMETER_DESCRIPTION + ", "
                        + COLUMN_REPORT_DIAGNOSTIC + ") "
                + "VALUES "
                    + "(100, 'Awake', 'Embrace the dawn with a heart filled with optimism and" +
                " a mind brimming with possibilities. Your awake face is a testament to your resilience" +
                " and strength. Let the morning light ignite your spirit and guide you towards a day" +
                " filled with purpose and joy. Believe in yourself, for every sunrise brings new" +
                " opportunities to shine brightly. You have got this!'),"
                    + "(101, 'Slightly Sleepy', 'Even in moments of sleepiness, your inner strength" +
                " shines through. Embrace this gentle transition with kindness towards yourself." +
                " As the day unfolds, allow yourself to awaken gradually, knowing that each step forward" +
                " is a testament to your resilience. Let the warmth of positivity envelop you, guiding you" +
                " towards a day filled with promise and possibility. You are capable of greatness, even" +
                " in moments of sleepy-eyed wonder. Embrace the journey ahead with a smile, knowing" +
                " that you are exactly where you need to be.'),"
                    + "(102, 'Extremely Sleepy', 'Extreme sleepiness warrants immediate attention to ensure safety and well-being." +
                " Avoid engaging in activities requiring alertness, especially driving or operating machinery." +
                " Prioritize rest by finding a safe, quiet space to nap if possible. Hydrate adequately and consider" +
                " consuming a light snack to boost energy levels. Evaluate potential causes such as sleep disorders or medication" +
                " side effects and seek professional guidance if necessary. Remember, your health is paramount; listen to your body is" +
                " signals and prioritize recovery. With proper care and support, you will overcome extreme sleepiness and regain vitality.'),"
                    + "(103, 'Sleeping', 'Be safe and enjoy your nap!'),"
                    + "(1000, 'Awake', 'Embrace the dawn with a heart filled with optimism and" +
                " a mind brimming with possibilities. Your awake face is a testament to your resilience" +
                " and strength. Let the morning light ignite your spirit and guide you towards a day" +
                " filled with purpose and joy. Believe in yourself, for every sunrise brings new" +
                " opportunities to shine brightly. You have got this!'),"
                    + "(1001, 'Vibrant', 'Radiate your vibrant energy and let it light up the world around you!" +
                " Your zest for life is contagious, inspiring those around you to embrace each moment with enthusiasm." +
                " Keep shining brightly and embracing every opportunity with open arms. Your positivity is a beacon of hope," +
                " illuminating even the darkest of days. Embrace your vibrancy and continue to spread joy wherever you go!'),"
                    + "(1002, 'Lively', 'Your lively spirit is like a breath of fresh air, infusing every moment with boundless energy and joy!" +
                " Embrace your vivaciousness and let it propel you towards new adventures and experiences. Your zest for life" +
                " is truly inspiring, uplifting everyone fortunate enough to be in your presence. Keep dancing through life with" +
                " that infectious enthusiasm, lighting up the world with your vibrant personality. Your liveliness is a gift to" +
                " cherish and share with the world!'),"
                    + "(1003, 'Attentive', 'Your attentive demeanor speaks volumes about your dedication and caring nature." +
                " In a world filled with distractions, your focused presence is a beacon of reliability and trustworthiness." +
                " Your keen attention to detail ensures nothing slips through the cracks, making you an invaluable asset to any team" +
                " or relationship. Embrace your attentiveness as a superpower, guiding you to success and fulfillment. Your mindfulness" +
                " and awareness enrich every interaction and endeavor. Keep shining brightly with your attentive spirit, knowing that your" +
                " presence makes a positive difference in the lives of those around you.'),"
                    + "(1010, 'Slightly Sleepy', 'Even in moments of sleepiness, your inner strength" +
                " shines through. Embrace this gentle transition with kindness towards yourself." +
                " As the day unfolds, allow yourself to awaken gradually, knowing that each step forward" +
                " is a testament to your resilience. Let the warmth of positivity envelop you, guiding you" +
                " towards a day filled with promise and possibility. You are capable of greatness, even" +
                " in moments of sleepy-eyed wonder. Embrace the journey ahead with a smile, knowing" +
                " that you are exactly where you need to be.'),"
                    + "(1011, 'Serene', 'Amidst life is whirlwind, your serene presence is a tranquil oasis, calming storms and soothing souls." +
                " Your inner peace radiates outward, touching hearts and inspiring tranquility in others. Embrace the stillness within, for it is" +
                " a source of strength and clarity. Your serenity is a gift to cherish and share, bringing harmony to chaotic moments and clarity" +
                " to cloudy thoughts. In your calm demeanor lies great power and resilience. Keep nurturing your inner serenity, for it is a" +
                " beacon of light in a world often shrouded in chaos. You are a peaceful force, guiding others towards inner harmony.'),"
                    + "(1012, 'Steady', 'In the ebb and flow of life, your steady presence is a rock-solid foundation upon which others can rely. Your unwavering" +
                " commitment and steadfast determination inspire confidence and trust in those around you. Like a steady beacon in the night," +
                " you guide others through challenges with grace and resilience. Embrace your constancy as a source of strength, knowing that your" +
                " steadfastness brings stability to turbulent times. Your consistency is a testament to your character and integrity. Keep moving" +
                " forward with steady resolve, for your steadfastness lights the path to success and inspires others to follow.'),"
                    + "(1013, 'Scattered', 'Amidst the chaos, your scattered brilliance shines through, illuminating pathways yet to be discovered. Embrace the diversity" +
                " of your thoughts and ideas, for they hold the seeds of innovation and creativity. In your scattered moments lies untapped potential" +
                " waiting to be harnessed. Embrace the journey of exploration and discovery, knowing that each scattered thought is a stepping stone" +
                " towards clarity and insight. Your scattered brilliance is a unique gift, guiding you towards new horizons and inspiring others to see" +
                " the beauty in chaos. Embrace your scattered nature with open arms, for within it lies the spark of brilliance.'),"
                    + "(1020, 'Exhausted', 'Prioritize self-care amidst exhaustion. Rest is essential for rejuvenation. Listen to your body is signals" +
                " and honor its need for recovery. Seek support from loved ones or healthcare professionals if needed. Remember, self-care is not selfish;" +
                " it is necessary for overall well-being. Practice relaxation techniques, maintain a balanced diet, and engage in gentle exercise when possible." +
                " Pace yourself and set realistic goals. Remember, you are not alone in your exhaustion; reach out for help when necessary. Your health and well-being matter." +
                " Take small steps towards self-care, and with time, you will regain your vitality and strength.'),"
                    + "(1021, 'Drowsy', 'Combat drowsiness with caution. If possible, avoid activities requiring focus or coordination until alertness returns." +
                " Prioritize safety by refraining from driving or operating heavy machinery. Ensure surroundings are well-lit and free of hazards to minimize risks." +
                " Hydrate regularly and consider a brief, revitalizing walk if feasible. If drowsiness persists, evaluate underlying causes such as inadequate sleep or" +
                " medication side effects. Consult a healthcare professional for personalized advice. Remember, your well-being is paramount; listen to your body is" +
                " signals and prioritize rest. With patience and proper care, you will overcome drowsiness and reclaim vitality.'),"
                    + "(1022, 'Tired', 'Address tiredness with self-compassion and proactive steps. Prioritize quality sleep by establishing a consistent bedtime routine" +
                " and creating a comfortable sleep environment. Incorporate regular physical activity and nourishing foods into your daily routine to boost energy levels." +
                " Practice stress management techniques such as mindfulness or deep breathing to alleviate fatigue. Take breaks when needed and avoid overextending yourself." +
                " Consider consulting a healthcare professional if tiredness persists despite lifestyle adjustments. Remember, prioritizing your well-being is key to maintaining" +
                " vitality and resilience. With self-care and support, you will overcome tiredness and embrace a renewed sense of vitality.'),"
                    + "(1023, 'Tired', 'Address tiredness with self-compassion and proactive steps. Prioritize quality sleep by establishing a consistent bedtime routine" +
                " and creating a comfortable sleep environment. Incorporate regular physical activity and nourishing foods into your daily routine to boost energy levels." +
                " Practice stress management techniques such as mindfulness or deep breathing to alleviate fatigue. Take breaks when needed and avoid overextending yourself." +
                " Consider consulting a healthcare professional if tiredness persists despite lifestyle adjustments. Remember, prioritizing your well-being is key to maintaining" +
                " vitality and resilience. With self-care and support, you will overcome tiredness and embrace a renewed sense of vitality.'),"
                    + "(1030, 'Sleeping', 'Be safe and enjoy your nap!')"
                    + ";";

        db.execSQL(INSERT_INTO_DIAGNOSTIC_PARAMETERS);

        //populate Face Diagnostic Parameters Table with initial data.
        String INSERT_INTO_FACE_DETECTION_PARAMETERS = "INSERT INTO " + TABLE_Face_Detection_Parameter
                + " (" + COLUMN_CAMERA_DETECTION_ID + ", " + COLUMN_CAMERA_DETECTION_DESCRIPTION + ") "
                + "VALUES "
                + "(100, 'Success'),"
                + "(101, 'Image_Fail'),"
                + "(102, 'Unknow_Face'),"
                + "(103, 'Empty')"
                + ";";

        db.execSQL(INSERT_INTO_FACE_DETECTION_PARAMETERS);

        //populate TABLE_Face_Detection Table with initial data.
        String INSERT_INTO_FACE_DETECTION = "INSERT INTO " + TABLE_Face_Detection
                + " (" + COLUMN_FACE_DETECTION_ID
                + ", " + COLUMN_FACE_DETECTION_STATUS
                + ", " + COLUMN_FACE_DETECTION_CONTROL + ") "
                + "VALUES "
                + "(100, 103, 0)"   //100: default id; 103: empty; 0: closed
                + ";";

        db.execSQL(INSERT_INTO_FACE_DETECTION);

        /*
        //creating initial account.
        String INSERT_INTO_ACCOUNT = "INSERT INTO " + TABLE_Account
                + " (" + COLUMN_FIRST_NAME
                + ", " + COLUMN_LAST_NAME
                + ", " + COLUMN_EMAIL
                + ", " + COLUMN_PASSWORD
                + ", " + COLUMN_ENABLE
                + ", " + COLUMN_SAVE_PWD + ") "
                + "VALUES "
                + "('Abbasi', 'Reza', 'reza@reza.com', 'reza', 1, 1)"
                + ";";

        db.execSQL(INSERT_INTO_ACCOUNT);
         */
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        // Drop older table if exist
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_Account);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_Session);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_Diagnostic_Parameters);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_Diagnostic);

        //Recreate tables
        onCreate(db);
    }

    public boolean CreateAccount(AccountModel account) {

        try{
            //Get the data repository in write mode.
            SQLiteDatabase db = this.getWritableDatabase();

            //Create a new map of values.
            ContentValues cValues = new ContentValues();
            cValues.put(COLUMN_FIRST_NAME, account.getFirstName());
            cValues.put(COLUMN_LAST_NAME, account.getLastName());
            cValues.put(COLUMN_EMAIL, account.getEmail());
            cValues.put(COLUMN_PASSWORD, account.getPassword());
            cValues.put(COLUMN_ENABLE, ENABLE);
            cValues.put(COLUMN_SAVE_PWD, DISABLE);

            // Insert row and return the primary key
            long rowId = db.insert(TABLE_Account, null, cValues);

            //close database
            db.close();

            if (rowId == -1) {

                return false;
            }

            return true;
        }
        catch (SQLiteException e) {
            e.printStackTrace();
        }

        return false;
    }

    public AccountModel GetAccount(String email){

        //Get instance of Account Model to store data.
        AccountModel accountModel = null;

        try{
            //Get the data repository in read mode.
            SQLiteDatabase db = this.getReadableDatabase();

            String queryString = "SELECT * FROM " + TABLE_Account +
                    " WHERE " + COLUMN_EMAIL + " = '" + email + "';";

            Cursor cursor = db.rawQuery(queryString, null);

            if(cursor.moveToFirst()){

                accountModel = new AccountModel();
                accountModel.setAccountId(cursor.getInt(0));
                accountModel.setFirstName(cursor.getString(1));
                accountModel.setLastName(cursor.getString(2));
                accountModel.setEmail(cursor.getString(3));
                accountModel.setPassword(cursor.getString(4));
                accountModel.setAccountEnable(cursor.getInt(5));
            }
            cursor.close();
            db.close();
        }
        catch (SQLiteException e) {
            e.printStackTrace();
        }

        return accountModel;
    }

    public AccountModel GetAccountById(int accountId){

        //Get instance of Account Model to store data.
        AccountModel accountModel = null;

        try{
            //Get the data repository in read mode.
            SQLiteDatabase db = this.getReadableDatabase();

            String queryString = "SELECT * FROM " + TABLE_Account +
                    " WHERE " + COLUMN_ACCOUNT_ID + " = '" + accountId + "';";

            Cursor cursor = db.rawQuery(queryString, null);

            if(cursor.moveToFirst()){

                accountModel = new AccountModel();
                accountModel.setAccountId(cursor.getInt(0));
                accountModel.setFirstName(cursor.getString(1));
                accountModel.setLastName(cursor.getString(2));
                accountModel.setEmail(cursor.getString(3));
                accountModel.setPassword(cursor.getString(4));
                accountModel.setAccountEnable(cursor.getInt(5));
                accountModel.setSavePassword(cursor.getInt(6));
            }
            cursor.close();
            db.close();
        }
        catch (SQLiteException e) {
            e.printStackTrace();
        }

        return accountModel;
    }

    public boolean UpdateAccount(AccountModel account) {

        try {
            //Get the Data Repository in write mode
            SQLiteDatabase db = this.getWritableDatabase();

            //Create a new map of values.
            ContentValues cValues = new ContentValues();

            cValues.put(COLUMN_FIRST_NAME, account.getFirstName());
            cValues.put(COLUMN_LAST_NAME, account.getLastName());
            cValues.put(COLUMN_EMAIL, account.getEmail());
            cValues.put(COLUMN_PASSWORD, account.getPassword());
            cValues.put(COLUMN_ENABLE, ENABLE);
            cValues.put(COLUMN_SAVE_PWD, DISABLE);

            //update record in the database
            String selectAccount = COLUMN_ACCOUNT_ID + " = ?";
            String[] selectionArgs = { String.valueOf(account.getAccountId()) };

            //Execute.
            int count = db.update(
                    TABLE_Account,
                    cValues,
                    selectAccount,
                    selectionArgs);

            //close database
            db.close();

            if(count > 0){
                return true;
            }
            return false;
        }
        catch (SQLiteException e) {

            e.printStackTrace();
        }

        return false;
    }

    public boolean SaveAccountPassword(int accountId) {

        try {
            //Get the Data Repository in write mode
            SQLiteDatabase db = this.getWritableDatabase();

            //Create a new map of values.
            ContentValues cValues = new ContentValues();

            cValues.put(COLUMN_SAVE_PWD, ENABLE);

            //update record in the database
            String selectAccount = COLUMN_ACCOUNT_ID + " = ?";
            String[] selectionArgs = { String.valueOf(accountId) };

            //Execute.
            int count = db.update(
                    TABLE_Account,
                    cValues,
                    selectAccount,
                    selectionArgs);

            //close database
            db.close();

            if(count > 0){
                return true;
            }
            return false;
        }
        catch (SQLiteException e) {

            e.printStackTrace();
        }

        return false;
    }

    //Start new session.
    public boolean StartSession(int accountId) {

        //Set date format.
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss MM/dd/yyyy", Locale.US);

        try{
            //Get the data repository in write mode.
            SQLiteDatabase db = this.getWritableDatabase();

            //Create a new map of values.
            ContentValues cValues = new ContentValues();
            cValues.put(COLUMN_TIME_BEGIN, dateFormat.format(new Date()));
            cValues.put(COLUMN_IS_ON, ENABLE);
            cValues.put(COLUMN_ACCOUNT_ID, accountId);

            //Insert row and return the primary key
            long rowId = db.insert(TABLE_Session,null, cValues);

            //close database
            db.close();

            if(rowId == -1){

                return false;
            }

            return true;
        }
        catch (SQLiteException e) {
            e.printStackTrace();
        }

        return false;
    }

    //Finish all sessions.
    public boolean EndAllSessions(){

        //Set date format.
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss MM/dd/yyyy", Locale.US);

        try {
            //Get the data repository in write mode.
            SQLiteDatabase db = this.getWritableDatabase();

            // Update database
            String queryString = "UPDATE " + TABLE_Session +
                    " SET "
                    + COLUMN_TIME_END + " = '" + dateFormat.format(new Date()) + "', "
                    + COLUMN_IS_ON + " = " + DISABLE
                    + " WHERE " + COLUMN_IS_ON + " = " + ENABLE + ";";

            db.execSQL(queryString);
            db.close();

            return true;
        }
        catch (SQLiteException e) {
            e.printStackTrace();
        }
        return false;
    }

    public SessionModel GetActiveSession(){

        SessionModel sessionModel = null;

        try{
            //Get the data repository in read mode.
            SQLiteDatabase db = this.getReadableDatabase();

            String queryString = "SELECT * FROM "
                    + TABLE_Session +
                    " WHERE " + COLUMN_IS_ON + " = " + ENABLE + ";";

            Cursor cursor = db.rawQuery(queryString, null);

            if(cursor.moveToFirst()){

                sessionModel = new SessionModel();
                sessionModel.setSessionId(cursor.getInt(0));
                sessionModel.setTimeBegin(cursor.getString(1));
                sessionModel.setTimeEnd(cursor.getString(2));
                sessionModel.setSessionOn(cursor.getInt(3));
                sessionModel.setAccountId(cursor.getInt(4));
            }
            cursor.close();
            db.close();
        }
        catch (SQLiteException e) {
            e.printStackTrace();
        }

        return sessionModel;
    }

    public SessionModel GetLatestSession(){

        SessionModel sessionModel = null;

        try{
            //Get the data repository in read mode.
            SQLiteDatabase db = this.getReadableDatabase();

            String queryString = "SELECT * " + " FROM " + TABLE_Session
                    + " ORDER BY " + COLUMN_TIME_END + " DESC"
                    + " LIMIT 1";

            Cursor cursor = db.rawQuery(queryString, null);

            if(cursor.moveToFirst()){

                sessionModel = new SessionModel();
                sessionModel.setSessionId(cursor.getInt(0));
                sessionModel.setTimeBegin(cursor.getString(1));
                sessionModel.setTimeEnd(cursor.getString(2));
                sessionModel.setSessionOn(cursor.getInt(3));
                sessionModel.setAccountId(cursor.getInt(4));
            }
            cursor.close();
            db.close();
        }
        catch (SQLiteException e) {
            e.printStackTrace();
        }

        return sessionModel;
    }

    public String GetLogin(String email){

        String _email = null;

        try{
            //Get the data repository in read mode.
            SQLiteDatabase db = this.getReadableDatabase();

            String queryString = "SELECT " + COLUMN_EMAIL +
                    " FROM " + TABLE_Account +
                    " WHERE " + COLUMN_EMAIL + " = '" + email + "';";

            Cursor cursor = db.rawQuery(queryString, null);

            if(cursor.moveToFirst()){

                _email = cursor.getString(0);
            }

            cursor.close();
            db.close();

            return _email;
        }
        catch (SQLiteException e) {
            e.printStackTrace();
        }
        return _email;
    }

    public boolean AddDiagnostic(FaceDiagnostic diagnostic) {

        try{
            //Get the data repository in write mode.
            SQLiteDatabase db = this.getWritableDatabase();

            //Create a new map of values.
            ContentValues cValues = new ContentValues();
            cValues.put(COLUMN_DROWSINESS, diagnostic.getDrowsiness());
            cValues.put(COLUMN_STABLE_HEAD_POSITION, diagnostic.getStableHeadPosition());
            cValues.put(COLUMN_REGULAR_BLINKING, diagnostic.getRegularBlinking());
            cValues.put(COLUMN_SMILING, diagnostic.getSmiling());
            cValues.put(COLUMN_OVERALL_DIAGNOSTIC, diagnostic.getOverallDiagnostic());
            cValues.put(COLUMN_DATE_DIAGNOSTIC, diagnostic.getDateDiagnostic());
            cValues.put(COLUMN_ACCOUNT_ID, diagnostic.getAccountId());

            // Insert row and return the primary key
            long rowId = db.insert(TABLE_Diagnostic, null, cValues);

            //close database
            db.close();

            if (rowId == -1) {

                return false;
            }

            return true;
        }
        catch (SQLiteException e) {
            e.printStackTrace();
        }

        return false;
    }

    public List<FaceDiagnostic> GetDiagnosticsByAccountId(int accountId) {

        List<FaceDiagnostic> listDiagnostics = null;

        try{
            //
            SQLiteDatabase db = this.getReadableDatabase();

            String queryString = "SELECT * FROM "
                    + TABLE_Diagnostic + " AS D INNER JOIN "
                    + TABLE_Diagnostic_Parameters + " AS DP "
                    + "ON D." + COLUMN_OVERALL_DIAGNOSTIC
                    + " = DP." + COLUMN_DIAG_PARAMETER_ID
                    + " WHERE D." + COLUMN_ACCOUNT_ID + " = " + accountId
                    + " ORDER BY D." + COLUMN_DIAG_ID + " DESC"
                    + ";";

            Cursor cursor = db.rawQuery(queryString, null);

            if(cursor.moveToFirst()){

                listDiagnostics = new ArrayList<>();

                do {

                    FaceDiagnostic faceDiagnostic = new FaceDiagnostic();

                    faceDiagnostic.setDiagnosticId(cursor.getInt(0));
                    faceDiagnostic.setDrowsiness(cursor.getInt(1));
                    faceDiagnostic.setStableHeadPosition(cursor.getInt(2));
                    faceDiagnostic.setRegularBlinking(cursor.getInt(3));
                    faceDiagnostic.setSmiling(cursor.getInt(4));
                    faceDiagnostic.setOverallDiagnostic(cursor.getInt(5));
                    faceDiagnostic.setDateDiagnostic(cursor.getString(6));
                    faceDiagnostic.setAccountId(cursor.getInt(7));
                    faceDiagnostic.setDrowsinessDescription(cursor.getString(9));
                    faceDiagnostic.setDiagnosticDescription(cursor.getString(10));

                    listDiagnostics.add(faceDiagnostic);
                }
                while(cursor.moveToNext());
            }

            cursor.close();
            db.close();

            return listDiagnostics;
        }
        catch (SQLiteException e) {
            e.printStackTrace();
        }

        return listDiagnostics;
    }

    public FaceDiagnostic GetLatestDiagnosticByAccountId(int accountId) {

        FaceDiagnostic faceDiagnostic = null;

        try{
            //
            SQLiteDatabase db = this.getReadableDatabase();

            String queryString = "SELECT * FROM "
                    + TABLE_Diagnostic + " AS D INNER JOIN "
                    + TABLE_Diagnostic_Parameters + " AS DP "
                    + "ON D." + COLUMN_OVERALL_DIAGNOSTIC
                    + " = DP." + COLUMN_DIAG_PARAMETER_ID
                    + " WHERE D." + COLUMN_ACCOUNT_ID + " = " + accountId
                    + " ORDER BY D." + COLUMN_DIAG_ID + " DESC"
                    + " LIMIT 1";

            Cursor cursor = db.rawQuery(queryString, null);

            if(cursor.moveToFirst()){

                faceDiagnostic = new FaceDiagnostic();

                faceDiagnostic.setDiagnosticId(cursor.getInt(0));
                faceDiagnostic.setDrowsiness(cursor.getInt(1));
                faceDiagnostic.setStableHeadPosition(cursor.getInt(2));
                faceDiagnostic.setRegularBlinking(cursor.getInt(3));
                faceDiagnostic.setSmiling(cursor.getInt(4));
                faceDiagnostic.setOverallDiagnostic(cursor.getInt(5));
                faceDiagnostic.setDateDiagnostic(cursor.getString(6));
                faceDiagnostic.setAccountId(cursor.getInt(7));
                faceDiagnostic.setDrowsinessDescription(cursor.getString(9));
                faceDiagnostic.setDiagnosticDescription(cursor.getString(10));
            }

            cursor.close();
            db.close();

            return faceDiagnostic;
        }
        catch (SQLiteException e) {
            e.printStackTrace();
        }

        return faceDiagnostic;
    }

    public FaceDiagnostic GetDiagnosticById(int diagnosticId) {

        FaceDiagnostic faceDiagnostic = null;

        try{
            //
            SQLiteDatabase db = this.getReadableDatabase();

            String queryString = "SELECT * FROM "
                    + TABLE_Diagnostic + " AS D INNER JOIN "
                    + TABLE_Diagnostic_Parameters + " AS DP "
                    + "ON D." + COLUMN_OVERALL_DIAGNOSTIC
                    + " = DP." + COLUMN_DIAG_PARAMETER_ID
                    + " WHERE " + COLUMN_DIAG_ID + " = " + diagnosticId + ";";

            Cursor cursor = db.rawQuery(queryString, null);

            if(cursor.moveToFirst()){

                faceDiagnostic = new FaceDiagnostic();

                faceDiagnostic.setDiagnosticId(cursor.getInt(0));
                faceDiagnostic.setDrowsiness(cursor.getInt(1));
                faceDiagnostic.setStableHeadPosition(cursor.getInt(2));
                faceDiagnostic.setRegularBlinking(cursor.getInt(3));
                faceDiagnostic.setSmiling(cursor.getInt(4));
                faceDiagnostic.setOverallDiagnostic(cursor.getInt(5));
                faceDiagnostic.setDateDiagnostic(cursor.getString(6));
                faceDiagnostic.setAccountId(cursor.getInt(7));
                faceDiagnostic.setDrowsinessDescription(cursor.getString(9));
                faceDiagnostic.setDiagnosticDescription(cursor.getString(10));
            }

            cursor.close();
            db.close();

            return faceDiagnostic;
        }
        catch (SQLiteException e) {
            e.printStackTrace();
        }

        return faceDiagnostic;
    }

    public boolean SetAICameraFaceDetection(AICameraModel aiCameraModel) {

        try {
            //Get the Data Repository in write mode
            SQLiteDatabase db = this.getWritableDatabase();

            //Create a new map of values.
            ContentValues cValues = new ContentValues();

            cValues.put(COLUMN_FACE_DETECTION_STATUS, aiCameraModel.getFaceDetectionStatus());
            cValues.put(COLUMN_FACE_DETECTION_CONTROL, aiCameraModel.getFaceDetectionControl());

            //update record in the database
            String updateAICamera = COLUMN_FACE_DETECTION_ID + " = ?";
            String[] selectionArgs = { String.valueOf(aiCameraModel.getFaceDetectionId()) };

            //Execute.
            int count = db.update(
                    TABLE_Face_Detection,
                    cValues,
                    updateAICamera,
                    selectionArgs);

            //close database
            db.close();

            if(count > 0){
                return true;
            }
            return false;
        }
        catch (SQLiteException e) {

            e.printStackTrace();
        }

        return false;
    }

    public AICameraModel GetAICameraFaceDetection() {

        //use join to get description

        AICameraModel aiCameraModel = null;

        try{
            //Get the data repository in read mode.
            SQLiteDatabase db = this.getReadableDatabase();

            String queryString = "SELECT F." + COLUMN_FACE_DETECTION_ID + ", "
                    + " F." + COLUMN_FACE_DETECTION_STATUS + ", "
                    + " F." + COLUMN_FACE_DETECTION_CONTROL + ", "
                    + " FP." + COLUMN_CAMERA_DETECTION_DESCRIPTION
                    + " FROM " + TABLE_Face_Detection + " AS F INNER JOIN "
                    + TABLE_Face_Detection_Parameter + " AS FP"
                    + " ON F." + COLUMN_FACE_DETECTION_STATUS + " = FP."
                    + COLUMN_CAMERA_DETECTION_ID + ";";

            Cursor cursor = db.rawQuery(queryString, null);

            if(cursor.moveToFirst()){

                aiCameraModel = new AICameraModel();

                aiCameraModel.setFaceDetectionId(cursor.getInt(0));
                aiCameraModel.setFaceDetectionStatus(cursor.getInt(1));
                aiCameraModel.setFaceDetectionControl(cursor.getInt(2));
                aiCameraModel.setFaceDetectionDescription(cursor.getString(3));
            }

            cursor.close();
            db.close();

            return aiCameraModel;
        }
        catch (SQLiteException e) {
            e.printStackTrace();
        }
        return aiCameraModel;
    }

}
