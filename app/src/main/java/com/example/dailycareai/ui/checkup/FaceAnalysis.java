package com.example.dailycareai.ui.checkup;

import android.content.Context;
import android.util.Log;
import androidx.annotation.NonNull;

import com.example.dailycareai.session.SessionManager;
import com.example.dailycareai.session.SessionModel;
import com.example.dailycareai.sql.DatabaseHelper;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class FaceAnalysis {

    //variable to store face detector's default setting.
    FaceDetectorOptions faceDetectorOptions;
    List<FaceModel> faceModels = new ArrayList<>();

    //variables to store average for each face behaviour.
    float leftEyeOpen_mean;
    float rightEyeOpen_mean;
    float headEulerAngleX_mean;
    float headEulerAngleY_mean;
    float headEulerAngleZ_mean;
    float smiling_mean;
    float blinking_mean;

    //Object to handle diagnostic data.
    FaceDiagnostic diagnostic;
    //variable to handle face analysis status.

    //variable used to check the amount of analysis.
    int amountOfAnalysis;
    //At least 50% of the images must contain face.
    final int AMOUNT_FACE_MIM = amountOfAnalysis / 2;
    final int ENABLE = 1;
    final int DISABLE = 0;

    //Variables to close camera analysis (success or fail).
    private final int SUCCESS = 100;    //default value in the database
    private final int IMAGE_FAIL = 101;
    private final int UNKNOWN_FACE = 102;
    private final int CONTROL_CLOSED = 0;

    //Context required to access database.
    Context context;

    //default constructor.
    public FaceAnalysis(Context context) {

        this.context = context;

        // Face_ detector's default settings to be applied before execute image face detection and analysis.
        this.faceDetectorOptions = new FaceDetectorOptions.Builder()
                .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST) //or PERFORMANCE_MODE_FAST
                .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL) //to identify facial "landmarks": eyes, ears, nose, cheeks, mouth, etc.
                .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL) //to classify faces into categories such as "smiling", and "eyes open".
                //.setContourMode(FaceDetectorOptions.CONTOUR_MODE_ALL) //to detect the contours of facial features.
                .enableTracking()
                .build();
    }

    //Perform faces analysis.
    public void PerformFaceAnalysis(List<InputImage> imageList) {

        //set amount of analysis to zero.
        amountOfAnalysis = 0;

        //Getting an instance of Face Detector.
        FaceDetector detector = FaceDetection.getClient(faceDetectorOptions);

        //perform image analysis for each frame.
        for (InputImage image: imageList) {

            Task<List<Face>> result = detector.process(image)
                    .addOnSuccessListener(
                            new OnSuccessListener<List<Face>>() {
                                @Override
                                public void onSuccess(List<Face> faces) {

                                    if(faces.size() > 0) {

                                        //getting face's parameters to be analyzed.
                                        FaceModel faceModel = new FaceModel(
                                                faces.get(0).getLeftEyeOpenProbability(),
                                                faces.get(0).getRightEyeOpenProbability(),
                                                faces.get(0).getSmilingProbability(),
                                                faces.get(0).getHeadEulerAngleX(),
                                                faces.get(0).getHeadEulerAngleY(),
                                                faces.get(0).getHeadEulerAngleZ()
                                        );

                                        //add to faceModel list.
                                        faceModels.add(faceModel);
                                    }
                                }
                            })

                    .addOnFailureListener(
                            new OnFailureListener() {

                                @Override
                                public void onFailure(@NonNull Exception e) {

                                    e.printStackTrace();
                                }
                            })
                    .addOnCompleteListener(
                            new OnCompleteListener<List<Face>>() {

                                @Override
                                public void onComplete(@NonNull Task<List<Face>> task) {

                                    //add up amount of analysis.
                                    amountOfAnalysis++;

                                    //All images checked and 50% containing faces is required to perform face analysis.
                                    if(amountOfAnalysis == imageList.size() && faceModels.size() > AMOUNT_FACE_MIM) {

                                        //call method to perform face analysis.
                                        likelihoodAnalysis();

                                        //Set date format and insert it into diagnostic.
                                        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss MM/dd/yyyy", Locale.US);
                                        diagnostic.setDateDiagnostic(dateFormat.format(new Date()));

                                        //insert account id into diagnostic.
                                        SessionManager sessionManager = new SessionManager(context);
                                        SessionModel sessionModel = sessionManager.GetActiveSession();
                                        diagnostic.setAccountId(sessionModel.getAccountId());

                                        //insert diagnostic in the database
                                        DatabaseHelper databaseHelper = new DatabaseHelper(context);
                                        databaseHelper.AddDiagnostic(diagnostic);

                                        //close camera analysis with success in the database (AI Camera Face Detection Control).
                                        AICameraModel aiCameraModel = databaseHelper.GetAICameraFaceDetection();

                                        aiCameraModel.setFaceDetectionStatus(SUCCESS);
                                        aiCameraModel.setFaceDetectionControl(CONTROL_CLOSED);

                                        databaseHelper.SetAICameraFaceDetection(aiCameraModel);

                                        //close database
                                        databaseHelper.close();
                                    }
                                    //all images analyzed, however not achieving the minimum required for face recognition.
                                    else if(amountOfAnalysis == imageList.size()) {

                                        //update camera detection control in the database.
                                        DatabaseHelper databaseHelper = new DatabaseHelper(context);

                                        //close camera analysis with fail.
                                        AICameraModel aiCameraModel = databaseHelper.GetAICameraFaceDetection();

                                        aiCameraModel.setFaceDetectionStatus(UNKNOWN_FACE);
                                        aiCameraModel.setFaceDetectionControl(CONTROL_CLOSED);

                                        databaseHelper.SetAICameraFaceDetection(aiCameraModel);

                                        //close database
                                        databaseHelper.close();
                                    }
                                }
                            }
                    ) ;
        }

    }

    /*
    Parameters to fatigue analysis:
    EyeOpenProbability > 0.5
    SmilingProbability > 0.2
    HeadEulerAngle > 5.00
    standard deviation
    */
    private void likelihoodAnalysis() {

        //create new Face Diagnostic Object.
        diagnostic = new FaceDiagnostic();

        //calling method to calculate average for each parameter.
        CalculateAverage();

        //Awake: 100, Slightly_sleepy: 101; Extremely_sleepy: 102; Sleeping: 103
        int drowsiness = CheckDrowsiness();
        diagnostic.setDrowsiness(drowsiness);

        //
        int stableHeadPosition = CheckHeadMovements();
        diagnostic.setStableHeadPosition(stableHeadPosition);

        //
        int regularBlinking = CheckBlinking();
        diagnostic.setRegularBlinking(regularBlinking);

        //
        int smiling = CheckSmile();
        diagnostic.setSmiling(smiling);

        //overall evaluation: return drowsiness and adjective
        switch(drowsiness) {

            case 100:
                if(stableHeadPosition == ENABLE && regularBlinking == ENABLE
                        && smiling == ENABLE) {
                    //Vibrant 100-1
                    diagnostic.setOverallDiagnostic(1001);
                }
                else if(stableHeadPosition == ENABLE && regularBlinking == ENABLE) {
                    //Lively 100-2
                    diagnostic.setOverallDiagnostic(1002);
                }
                else if(stableHeadPosition == ENABLE || regularBlinking == ENABLE) {
                    //Attentive 100-3
                    diagnostic.setOverallDiagnostic(1003);
                }
                else {
                    //Awake 100-0
                    diagnostic.setOverallDiagnostic(1000);
                }
                break;

            case 101:
                if(stableHeadPosition == ENABLE && regularBlinking == ENABLE
                        && smiling == ENABLE) {
                    //Serene 101-1
                    diagnostic.setOverallDiagnostic(1011);
                }
                else if(stableHeadPosition == ENABLE && regularBlinking == ENABLE) {
                    //Steady 101-2
                    diagnostic.setOverallDiagnostic(1012);
                }
                else if(stableHeadPosition == ENABLE || regularBlinking == ENABLE) {
                    //scattered 101-3
                    diagnostic.setOverallDiagnostic(1013);
                }
                else {
                    //Slightly_sleepy 101-0
                    diagnostic.setOverallDiagnostic(1010);
                }
                break;

            case 102:
                if(stableHeadPosition == ENABLE && regularBlinking == ENABLE
                        && smiling == ENABLE) {
                    //Drowsy 102-1
                    diagnostic.setOverallDiagnostic(1021);
                }
                else if(stableHeadPosition == ENABLE && regularBlinking == ENABLE) {
                    //Tired 102-2
                    diagnostic.setOverallDiagnostic(1022);
                }
                else if(stableHeadPosition == ENABLE || regularBlinking == ENABLE) {
                    //Tired 102-3
                    diagnostic.setOverallDiagnostic(1023);
                }
                else {
                    //Exhausted 102-0
                    diagnostic.setOverallDiagnostic(1020);
                }
                break;

            case 103:
                //Sleeping 103-0
                diagnostic.setOverallDiagnostic(1030);
                break;

            default:
                //unknown.
                diagnostic.setOverallDiagnostic(0);
                break;
        }
    }

    private void CalculateAverage() {

        float leftEyeOpen_sum = 0.000000000f;
        float rightEyeOpen_sum = 0.000000000f;

        float headEulerAngleX_sum = 0.000000000f;
        float headEulerAngleY_sum = 0.000000000f;
        float headEulerAngleZ_sum = 0.000000000f;

        float smiling_sum = 0.000000000f;

        //variables to calculate blinking average.
        final float EYE_OPEN = 0.500000000f;
        final float EYE_CLOSE = 0.150000000f;
        int blinking_sum = 0;

        for (int i = 0; i< faceModels.size(); i++) {

            leftEyeOpen_sum += faceModels.get(i).getLeftEyeOpenProbability();
            rightEyeOpen_sum += faceModels.get(i).getRightEyeOpenProbability();

            headEulerAngleX_sum += faceModels.get(i).getHeadEulerAngleX();
            headEulerAngleY_sum += faceModels.get(i).getHeadEulerAngleY();
            headEulerAngleZ_sum += faceModels.get(i).getHeadEulerAngleZ();

            smiling_sum += faceModels.get(i).getSmileProbability();

            if((faceModels.get(i).getLeftEyeOpenProbability() < EYE_CLOSE
                    || faceModels.get(i).getRightEyeOpenProbability() < EYE_CLOSE)
                    && i > 0
                    && (faceModels.get(i-1).getLeftEyeOpenProbability() > EYE_OPEN
                    || faceModels.get(i-1).getRightEyeOpenProbability() > EYE_OPEN)) {

                blinking_sum++;
            }
        }

        //calculating average for each parameter.
        leftEyeOpen_mean = leftEyeOpen_sum / faceModels.size();
        rightEyeOpen_mean = rightEyeOpen_sum / faceModels.size();

        headEulerAngleX_mean = headEulerAngleX_sum / faceModels.size();
        headEulerAngleY_mean = headEulerAngleY_sum / faceModels.size();
        headEulerAngleZ_mean = headEulerAngleZ_sum / faceModels.size();

        smiling_mean = smiling_sum / faceModels.size();

        blinking_mean = Float.valueOf(String.valueOf(blinking_sum) + ".000000000")
                            / Float.valueOf(String.valueOf(faceModels.size()) + ".000000000");
    }

    /*
    drowsiness check
    Parameters:
      eye open > 0.5
      eye close < 0.2
      EyeOpen_average: drowsiness: < 95% awake: > 95%
    */
    private int CheckDrowsiness() { //by eyes analysis

        final float AWAKE = 0.970000000f;
        final float SLIGHTLY_SLEEPY = 0.500000000f;
        final float EXTREMELY_SLEEPY = 0.100000000f;

        final float AWAKE_SMILING = 0.050000000f;
        final float SMILING_MIN = 0.200000000f;

        if(leftEyeOpen_mean > AWAKE && rightEyeOpen_mean > AWAKE) {
            return 100; //"awake";
        }
        else if(smiling_mean > SMILING_MIN && leftEyeOpen_mean > AWAKE_SMILING
                && rightEyeOpen_mean > AWAKE_SMILING) {
            return 100; //"awake";
        }
        else if(leftEyeOpen_mean > SLIGHTLY_SLEEPY && rightEyeOpen_mean > SLIGHTLY_SLEEPY) {
            return 101; //"slightly_sleepy";
        }
        else if(leftEyeOpen_mean > EXTREMELY_SLEEPY && rightEyeOpen_mean > EXTREMELY_SLEEPY) {
            return 102; //"extremely_sleepy";
        }
        else {
            return 103; //"sleeping";
        }
    }

    /*
    Check blinking pattern.
    Usually, tiredness can lead to an irregular blinking patterns(irregular blinking per time).
    Also, one common sign of fatigue is an increase in the frequency of blinking.
    When someone is tired, they may blink more frequently as their eyes try to stay moist and prevent dryness,
    which can be exacerbated by fatigue.
    Parameters:
    interchange: amount of "eye open" before "closed eye"
    standard deviation: low: awake high: tiredness
    */
    private int CheckBlinking() {

        //the use case indicates up to 3% of the time blinking.
        final float BLINKING_THRESHOLD = 0.030000000f;

        if( blinking_mean < BLINKING_THRESHOLD) {

            return ENABLE;
        }
        else {
            return DISABLE;
        }
    }

    /*
    Smiling check.
    Usually, people don't smile when they are tired.
    Parameter: false: smiling_average < 0.5
    */
    private int CheckSmile() {

        final float SMILING_MIN = 0.500000000f;

        if(smiling_mean > SMILING_MIN) {
            return ENABLE;
        }
        else {
            return DISABLE;
        }
    }

    /*
    head movement check
    One of the signs of tiredness is the lack of firmness in supporting the head, generating constant moments.
    Parameters: steady and smooth head movements.
    How to calculate:
        standard deviation of axes X and Y (head movements).
        base on used case, standard deviation higher than 4.00 indicates lack of firmness in supporting the head.
    */
    private int CheckHeadMovements() {

        //calculating standard deviation
        double headEulerAngleY_deviation;
        double headEulerAngleX_deviation;
        double headEulerAngleZ_deviation;

        final float  DEVIATION_THRESHOLD_Z = 1.000000000f;
        final float  DEVIATION_THRESHOLD_X = 1.000000000f;
        final float  DEVIATION_THRESHOLD_Y = 0.500000000f;  //Check??

        float sumSquaredDiffX = 0.000000000f;
        float sumSquaredDiffY = 0.000000000f;
        float sumSquaredDiffZ = 0.000000000f;

        for (FaceModel faceModel: faceModels) {

            sumSquaredDiffX += Math.pow(faceModel.getHeadEulerAngleX() - headEulerAngleX_mean, 2);
            sumSquaredDiffY += Math.pow(faceModel.getHeadEulerAngleY() - headEulerAngleY_mean, 2);
            sumSquaredDiffZ += Math.pow(faceModel.getHeadEulerAngleZ() - headEulerAngleZ_mean, 2);
        }
        double varianceX = sumSquaredDiffX / faceModels.size();
        double varianceY = sumSquaredDiffY / faceModels.size();
        double varianceZ = sumSquaredDiffZ / faceModels.size();

        headEulerAngleX_deviation = Math.sqrt(varianceX);
        headEulerAngleY_deviation = Math.sqrt(varianceY);
        headEulerAngleZ_deviation = Math.sqrt(varianceZ);

        if( headEulerAngleX_deviation > DEVIATION_THRESHOLD_X
                || headEulerAngleY_deviation > DEVIATION_THRESHOLD_Y
                || headEulerAngleZ_deviation > DEVIATION_THRESHOLD_Z) {

            return DISABLE;
        }
        else {

            return ENABLE;
        }
    }
}

