package com.example.dailycareai.ui.videoanalysis;

import android.content.Context;
import android.graphics.PointF;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.dailycareai.ui.checkup.FaceModel;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;
import com.google.mlkit.vision.face.FaceLandmark;
import com.google.mlkit.vision.label.ImageLabel;
import com.google.mlkit.vision.label.ImageLabeler;
import com.google.mlkit.vision.label.ImageLabeling;
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions;
import com.google.mlkit.vision.objects.DetectedObject;
import com.google.mlkit.vision.objects.ObjectDetection;
import com.google.mlkit.vision.objects.ObjectDetector;
import com.google.mlkit.vision.objects.defaults.ObjectDetectorOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DriverAnalysis {

    //variable to store face detector's default setting.
    FaceDetectorOptions faceDetectorOptions;
    FaceDetector faceDetector;
    DriverBehaviorAnalysis driverBehaviorAnalysis;
    ImageLabeler labeler;
    ObjectDetector objectDetector;
    Context context; //Context required to access database.
    List<FaceModel> faceModelList;
    private volatile boolean isRunning; // Flag to control the thread execution

    //variables to store average for each face behaviour.
    float leftEyeOpen_mean;
    float rightEyeOpen_mean;
    float headEulerAngleX_mean;
    float headEulerAngleY_mean;
    float headEulerAngleZ_mean;
    float smiling_mean;
    float blinking_mean;
    final int BLINKING_FACES_MIN = 10;
    final int CYCLE_THRESHOLD = 700;
    int cycleCounter = 0;

    //default constructor.
    public DriverAnalysis(Context context) throws IOException {

        this.context = context;

        // Face_ detector's default settings to be applied before execute image face detection and analysis.
        this.faceDetectorOptions = new FaceDetectorOptions.Builder()
                .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE) //or PERFORMANCE_MODE_FAST
                .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL) //.LANDMARK_MODE_ALL to identify facial "landmarks": eyes, ears, nose, cheeks, mouth, etc.
                .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL) //to classify faces into categories such as "smiling", and "eyes open".
                //.setContourMode(FaceDetectorOptions.CONTOUR_MODE_ALL) //to detect the contours of facial features.
                .enableTracking()
                .build();
        //initiating face detector.
        this.faceDetector = FaceDetection.getClient(faceDetectorOptions);

        //list of faceModel to analyze faces while face analysis is running
        faceModelList = new ArrayList<>();

        //start thread to perform face analysis while video is running
        isRunning = true;
        StartDriverFaceAnalysis();

        //custom machine learning model analysis
        //driverBehaviorAnalysis = new DriverBehaviorAnalysis(context);

        //image labeling (400+ labels)
//        labeler = ImageLabeling.getClient(ImageLabelerOptions.DEFAULT_OPTIONS); //or
//        ImageLabelerOptions options =
//           new ImageLabelerOptions.Builder()
//               .setConfidenceThreshold(0.7f)
//               .build();
//        labeler = ImageLabeling.getClient(options);

//        //object detection (5 objects per image, including images contours).
//        ObjectDetectorOptions detectorOptions =
//                new ObjectDetectorOptions.Builder()
//                        .setDetectorMode(ObjectDetectorOptions.STREAM_MODE)
//                        .enableClassification()  // Optional
//                        .build();
//
//        // Live detection and tracking: ObjectDetectorOptions.STREAM_MODE
//        // Multiple object detection in static images:  ObjectDetectorOptions.SINGLE_IMAGE_MODE and .enableMultipleObjects()
//
//        objectDetector = ObjectDetection.getClient(detectorOptions);
    }

    //Perform face analysis for each new frame extracted.
    public void PerformFaceAnalysis(InputImage image) {

        faceDetector.process(image)
                .addOnSuccessListener(
                        faces -> {
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
                                synchronized (faceModelList) {

                                    //add new face analysis into the faceModelList array.
                                    faceModelList.add(faceModel);
                                }

//                                List<FaceLandmark> allLandmarks = face.getAllLandmarks();
//                                for (FaceLandmark landmark : allLandmarks) {
//                                    int landmarkType = landmark.getLandmarkType();
//                                    PointF position = landmark.getPosition();   //return coordinates}
                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                //Log.d("***__ Fail_FaceAnalysis", "FaceAnalysisFail");
                            }
                        }
                )
                .addOnCompleteListener(
                        task -> {
                            //call method to perform face analysis.
                        }
                );

        //call method to analyze driver's behaviour (custom machine learning model).
        //driverBehaviorAnalysis.runInference(image.getMediaImage());

//        //labeling objects
//        labeler.process(image)
//                .addOnSuccessListener(
//                        new OnSuccessListener<List<ImageLabel>>() {
//                            @Override
//                            public void onSuccess(List<ImageLabel> labels) {
//
//                                for (ImageLabel label : labels) {
//                                    String text = label.getText();
//                                    float confidence = label.getConfidence();
//                                    int index = label.getIndex();
//                                    Log.d("***__ LabelDetected", "id " + index + " name " + text + " confidence " + confidence);
//                                }
//                            }
//                })
//                .addOnFailureListener(
//                        new OnFailureListener() {
//                            @Override
//                            public void onFailure(@NonNull Exception e) {
//                                // Task failed with an exception
//                                //Log.d("***__ LabelingFail", "Labeling fail");
//                            }
//                });

//        //detecting objects
//        objectDetector.process(image)
//                .addOnSuccessListener(
//                        new OnSuccessListener<List<DetectedObject>>() {
//                            @Override
//                            public void onSuccess(List<DetectedObject> detectedObjects) {
//
//                                for (DetectedObject detectedObject : detectedObjects) {
//                                    Integer trackingId = detectedObject.getTrackingId();
//                                    for (DetectedObject.Label label : detectedObject.getLabels()) {
//                                        String text = label.getText();
//                                        int index = label.getIndex();
//                                        float confidence = label.getConfidence();
//                                        Log.d("***__ ObjectDetectedYes", "id " + String.valueOf(index) + " name " + text + " confidence " + String.valueOf(confidence));
//                                    }
//                                }
//                            }
//                        })
//                .addOnFailureListener(
//                        new OnFailureListener() {
//                            @Override
//                            public void onFailure(@NonNull Exception e) {
//                                // Task failed with an exception
//                                Log.d("***__ ObjectDetectedFail", "ObjectDetected Fail");
//                            }
//                        });

    }

    //Asynchronous process. Loop to analyze images within faceModelList array.
    //One thread.
    private void StartDriverFaceAnalysis() {

        Thread driverFaceAnalysis = new Thread(new Runnable() {
            @Override
            public void run() {
                while (isRunning) {
                    synchronized (faceModelList) {

                        // Analyze the items in the list
                        if (!faceModelList.isEmpty()) {

                            //Perform driver's face analysis
                            likelihoodAnalysis();
                        }
                    }

                    try {
                        // Sleep for a short period to avoid busy-waiting
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        driverFaceAnalysis.start();  // Start thread
    }

    //isRunning is set to false when there's no more frames to be analyzed.
    public void StopDriverFaceAnalysis() {
        // This will cause the thread to exit the loop and finish
        isRunning = false;
    }

    //Perform Driver's behavior and conditions probability.
    private void likelihoodAnalysis() {

        //calling method to calculate average for each parameter.
        CalculateAverage();

        //checking drowsiness
        CheckDrowsiness();

        //Checking head moments
        CheckHeadMovements();

        //Checking blinking pattern.
        CheckBlinking();

        //checking smile.
        CheckSmile();
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
        final float EYE_CLOSE = 0.100000000f;
        int blinking_sum = 0;

        for (int i = 0; i< faceModelList.size(); i++) {

            leftEyeOpen_sum += faceModelList.get(i).getLeftEyeOpenProbability();
            rightEyeOpen_sum += faceModelList.get(i).getRightEyeOpenProbability();

            headEulerAngleX_sum += faceModelList.get(i).getHeadEulerAngleX();
            headEulerAngleY_sum += faceModelList.get(i).getHeadEulerAngleY();
            headEulerAngleZ_sum += faceModelList.get(i).getHeadEulerAngleZ();

            smiling_sum += faceModelList.get(i).getSmileProbability();

            if((faceModelList.get(i).getLeftEyeOpenProbability() < EYE_CLOSE
                    || faceModelList.get(i).getRightEyeOpenProbability() < EYE_CLOSE)
                    && i > 0
                    && (faceModelList.get(i-1).getLeftEyeOpenProbability() > EYE_OPEN
                    || faceModelList.get(i-1).getRightEyeOpenProbability() > EYE_OPEN)) {

                blinking_sum++;
            }
        }

        //calculating average for head_movement parameter.
        if(cycleCounter < CYCLE_THRESHOLD) {

            headEulerAngleX_mean = headEulerAngleX_sum / faceModelList.size();
            headEulerAngleY_mean = headEulerAngleY_sum / faceModelList.size();
            headEulerAngleZ_mean = headEulerAngleZ_sum / faceModelList.size();

            leftEyeOpen_mean = leftEyeOpen_sum / faceModelList.size();
            rightEyeOpen_mean = rightEyeOpen_sum / faceModelList.size();

            smiling_mean = smiling_sum / faceModelList.size();

            blinking_mean = Float.valueOf(blinking_sum + ".000000000")
                    / Float.valueOf(faceModelList.size() + ".000000000");

            cycleCounter++;
        }
        else {
            headEulerAngleX_mean = headEulerAngleX_sum / faceModelList.size();
            headEulerAngleY_mean = headEulerAngleY_sum / faceModelList.size();
            headEulerAngleZ_mean = headEulerAngleZ_sum / faceModelList.size();

            leftEyeOpen_mean = leftEyeOpen_sum / faceModelList.size();
            rightEyeOpen_mean = rightEyeOpen_sum / faceModelList.size();

            smiling_mean = smiling_sum / faceModelList.size();

            blinking_mean = Float.valueOf(blinking_sum + ".000000000")
                    / Float.valueOf(faceModelList.size() + ".000000000");

            //reset parameters
            cycleCounter = 0;
            synchronized (faceModelList) {
                faceModelList.clear();
            }
        }
    }

    //drowsiness check (by eyes analysis)
    private void CheckDrowsiness() {

        final float AWAKE = 0.500000000f;
        final float SLEEPY = 0.200000000f;

        final float AWAKE_SMILING = 0.100000000f;
        final float SMILING_MIN = 0.500000000f;

        if(leftEyeOpen_mean > AWAKE && rightEyeOpen_mean > AWAKE) {

            Log.d("** Driver's Condition", "Awake " + rightEyeOpen_mean + "-" + leftEyeOpen_mean);
            //Log.d("** Driver's Condition", "Awake");
        }
        else if(smiling_mean > SMILING_MIN && leftEyeOpen_mean > AWAKE_SMILING
                && rightEyeOpen_mean > AWAKE_SMILING) {

            Log.d("** Driver's Condition", "Smiling Awake " + rightEyeOpen_mean + "-" + leftEyeOpen_mean);
            //Log.d("** Driver's Condition", "Smiling Awake");
        }
        else if((leftEyeOpen_mean > AWAKE && rightEyeOpen_mean < SLEEPY) ||
                (rightEyeOpen_mean > AWAKE && leftEyeOpen_mean < SLEEPY)) {

            Log.d("** Driver's Condition", "Drowsiness " + rightEyeOpen_mean + "-" + leftEyeOpen_mean);
            //Log.d("** Driver's Condition", "Drowsiness");
        }
        else if((leftEyeOpen_mean > SLEEPY && rightEyeOpen_mean < SLEEPY) ||
                (rightEyeOpen_mean > SLEEPY && leftEyeOpen_mean < SLEEPY)) {

            Log.d("** Driver's Condition", "Unstable " + rightEyeOpen_mean + "-" + leftEyeOpen_mean);
            //Log.d("** Driver's Condition", "Unstable");
        }
        else if(leftEyeOpen_mean > SLEEPY && rightEyeOpen_mean > SLEEPY) {

            Log.d("** Driver's Condition", "Sleepy " + rightEyeOpen_mean + "-" + leftEyeOpen_mean);
            //Log.d("** Driver's Condition", "Sleepy");
        }
        else {
            Log.d("** Driver's Condition", "sleeping " + rightEyeOpen_mean + "-" + leftEyeOpen_mean);
            //Log.d("** Driver's Condition", "sleeping");
        }
    }

    /*
    head movement check
    One of the signs of tiredness is the lack of firmness in supporting the head, generating constant moments.
    Parameters: steady and smooth head movements.
    How to calculate:
        standard deviation of axes X and Y (head movements).
    */
    private void CheckHeadMovements() {

        //calculating standard deviation
        double headEulerAngleY_deviation;
        double headEulerAngleX_deviation;
        double headEulerAngleZ_deviation;

        final float  DEVIATION_THRESHOLD_Z = 4.000000000f;
        final float  DEVIATION_THRESHOLD_X = 30.000000000f;
        final float  DEVIATION_THRESHOLD_Y = 5.000000000f;

        float sumSquaredDiffX = 0.000000000f;
        float sumSquaredDiffY = 0.000000000f;
        float sumSquaredDiffZ = 0.000000000f;

        for (FaceModel faceModel: faceModelList) {

            sumSquaredDiffX += Math.pow(faceModel.getHeadEulerAngleX() - headEulerAngleX_mean, 2);
            sumSquaredDiffY += Math.pow(faceModel.getHeadEulerAngleY() - headEulerAngleY_mean, 2);
            sumSquaredDiffZ += Math.pow(faceModel.getHeadEulerAngleZ() - headEulerAngleZ_mean, 2);
        }
        double varianceX = sumSquaredDiffX / faceModelList.size();
        double varianceY = sumSquaredDiffY / faceModelList.size();
        double varianceZ = sumSquaredDiffZ / faceModelList.size();

        headEulerAngleX_deviation = Math.sqrt(varianceX);
        headEulerAngleY_deviation = Math.sqrt(varianceY);
        headEulerAngleZ_deviation = Math.sqrt(varianceZ);

        if( headEulerAngleX_deviation > DEVIATION_THRESHOLD_X) {

            Log.d("** Driver's Behaviour", "Distraction - Looking up and down " + headEulerAngleX_deviation);
            //Log.d("** Driver's Behavior", "Distraction - Looking up and down");
        }
        else{
            Log.d("** Driver's Behaviour", "Looking forward " + headEulerAngleX_deviation);
            //Log.d("** Driver's Behavior", "Looking forward");
        }

        if( headEulerAngleY_deviation > DEVIATION_THRESHOLD_Y) {

            Log.d("** Driver's Behaviour", "Distraction - Looking side " + headEulerAngleY_deviation);
            //Log.d("** Driver's Behavior", "Distraction - Looking side");
        }
        else{
            Log.d("** Driver's Behaviour", "Looking forward " + headEulerAngleY_deviation);
            //Log.d("** Driver's Behavior", "Looking forward");
        }
        if( headEulerAngleZ_deviation > DEVIATION_THRESHOLD_Z) {

            Log.d("** Driver's Behaviour", "Distraction - Looking side " + headEulerAngleZ_deviation);
            //Log.d("** Driver's Behavior", "Distraction - Looking side");
        }
        else{
            Log.d("** Driver's Behaviour", "Looking forward " + headEulerAngleZ_deviation);
            //Log.d("** Driver's Behavior", "Looking forward");
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
    private void CheckBlinking() {

        final float BLINKING_THRESHOLD = 0.300000000f;

        if(faceModelList.size() > BLINKING_FACES_MIN) {

            if( blinking_mean < BLINKING_THRESHOLD) {

                //Log.d("** Driver's Condition", "Regular Blinking " + blinking_mean);
                Log.d("** Driver's Condition", "Regular Blinking");
            }
            else {

                //Log.d("** Driver's Condition", "Drowsiness - Irregular Blinking " + blinking_mean);
                Log.d("** Driver's Condition", "Drowsiness - Irregular Blinking");
            }
        }
    }

    /*
    Smiling check.
    Usually, people don't smile when they are tired.
    Parameter: false: smiling_average < 0.5
    */
    private void CheckSmile() {

        final float SMILING_MIN = 0.500000000f;

        if(smiling_mean > SMILING_MIN) {

            Log.d("** Driver's Condition", "Smiling");
        }
        else {

            Log.d("** Driver's Condition", "Unsmiling");
        }
    }
}
