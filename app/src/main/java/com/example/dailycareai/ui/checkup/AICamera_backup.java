package com.example.dailycareai.ui.checkup;
/*
Student Name: Elton Henrique de Oliveira Evangelista
Student ID: 300371029
Course: CSIS 4175-050 – Mobile Application Development II
Instructor: Reza, Abbasi
April 08, 2024
 */

import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.annotation.OptIn;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ExperimentalGetImage;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.LifecycleOwner;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.media.Image;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ProgressBar;

import com.example.dailycareai.R;
import com.example.dailycareai.databinding.FragmentCheckupAicameraBinding;
import com.example.dailycareai.session.SessionModel;
import com.example.dailycareai.sql.DatabaseHelper;
import com.example.dailycareai.ui.home.HomeFragment;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.common.InputImage;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class AICamera_backup extends Fragment {

    //binding fragment views.
    FragmentCheckupAicameraBinding binding;

    //variable for camera provider
    ProcessCameraProvider cameraProvider;

    //variable to check camera permission
    private String[] cameraPermissions = new String[]{
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
    };

    //Initially set to front camera
    private int currentCamera = CameraSelector.LENS_FACING_FRONT;

    //maximum amount of frames to be analyzed.
    //100 frames takes around 20 seconds of analysis.
    private final int MAX_FRAMES = 100;

    //Instantiate image analysis class.
    private ImageAnalysis imageAnalysis;

    //Variables to open camera analysis control.
    //Considering the asynchronous image analysis process, they area used as a control
    //to indicate the status of face analysis and return to the UI accordingly.
    //Both classes, AICamera and FaceAnalysis read and update the database,
    //working together as a traffic light signal.
    private final int SUCCESS = 100;    //default values in the database
    private final int IMAGE_FAIL = 101;
    private final int UNKNOWN_FACE = 102;
    private final int EMPTY = 103;
    private final int CONTROL_CLOSED = 0;
    private final int CONTROL_OPEN = 1;
    AICameraModel aiCameraModel;

    //Instantiating FaceDiagnostic as global to send data to Home Fragment.
    FaceDiagnostic faceDiagnostic;

    //Instantiating Database
    DatabaseHelper databaseHelper;

    //Instantiating Progress Bar.
    ProgressBar progressBar;
    int progressBarCounter = 0;

    //Instantiating animation
    Animation scaleUpTakePic;
    Animation scaleDownTakePic;
    Animation scaleUpSwitchCam;
    Animation scaleDownSwitchCam;

    private final String waitMessage = "Please, wait up to 45 seconds, ensuring the camera is directed " +
            "towards your face for optimal results.";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        binding = FragmentCheckupAicameraBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        //setup progress bar
        binding.progressBar.setVisibility(View.INVISIBLE);

        //checking app's access to camera. If not, ask user for permission.
        if(!RequestCameraPermission()) {

            ActivityCompat.requestPermissions(getActivity(), cameraPermissions, 0);
        }
        else {
            //start camera.
            StartCamera();

            //call method to set on click listener to start face analysis
            SetupCamera();

            //set button animation.
            SetCameraInteraction();
        }

        return view;
    }

    private void SetupCamera() {

        //creating am object of Image Analysis
        imageAnalysis = new ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_YUV_420_888)
                .build();

        //set click listener to image button.
        binding.btnStartImageAnalysis.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                //animate button
                v.startAnimation(scaleUpTakePic);
                v.startAnimation(scaleDownTakePic);

                //start progress bar.
                StartProgressBar();

                //list of images to be analyzed. InputImage is the format recognized by Image Analysis Class.
                List<InputImage> inputImages = new ArrayList<>();

                //
                imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(getActivity()),
                        new ImageAnalysis.Analyzer() {

                            @OptIn(markerClass = ExperimentalGetImage.class) @Override
                            public void analyze(@NonNull ImageProxy image) {

                                try {

                                    //inputImages.add(InputImage.fromMediaImage(image.getImage(), 90));

                                    //first, convert ImageProxy to Bitmap (required to FaceDetector).

                                    Image mediaImage = image.getImage();

                                    Image.Plane[] planes = mediaImage.getPlanes();

                                    ByteBuffer yBuffer = planes[0].getBuffer();
                                    ByteBuffer uBuffer = planes[1].getBuffer();
                                    ByteBuffer vBuffer = planes[2].getBuffer();

                                    int ySize = yBuffer.remaining();
                                    int uSize = uBuffer.remaining();
                                    int vSize = vBuffer.remaining();

                                    byte[] nv21 = new byte[ySize + uSize + vSize];

                                    yBuffer.get(nv21, 0, ySize);
                                    vBuffer.get(nv21, ySize, vSize);
                                    uBuffer.get(nv21, ySize + vSize, uSize);

                                    int width = image.getWidth();
                                    int height = image.getHeight();

                                    YuvImage yuvImage = new YuvImage(nv21, ImageFormat.NV21, width, height, null);
                                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                                    yuvImage.compressToJpeg(new Rect(0, 0, width, height), 100, out);

                                    byte[] data = out.toByteArray();
                                    BitmapFactory.Options options = new BitmapFactory.Options();
                                    options.inMutable = true;

                                    //Image Proxy converted to Bitmap.
                                    Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length, options);

                                    //Add bitmap image to the list of InputImage.
                                    inputImages.add(InputImage.fromBitmap(bitmap, 90));

                                    //Close current image/frame and release the next one.
                                    image.close();


                                    //stop Image Analysis when MAX_FRAMES has been reached.
                                    if(inputImages.size() > MAX_FRAMES) {

                                        //Clear Image Analyzer to stop analyzing images/frames.
                                        imageAnalysis.clearAnalyzer();

                                        //loading database class.
                                        databaseHelper = new DatabaseHelper(getContext());

                                        //retrieving data from database.
                                        aiCameraModel = databaseHelper.GetAICameraFaceDetection();

                                        //update process control and detection status.
                                        aiCameraModel.setFaceDetectionControl(CONTROL_OPEN); //open to analyze.
                                        aiCameraModel.setFaceDetectionStatus(EMPTY); //initial status.

                                        //update database
                                        databaseHelper.SetAICameraFaceDetection(aiCameraModel);

                                        databaseHelper.close();

                                        //Request faces analysis (asynchronous process).
                                        FaceAnalysis faceAnalysis = new FaceAnalysis(getContext());
                                        faceAnalysis.PerformFaceAnalysis(inputImages);

                                        //call method to monitor face analysis response and update UI.
                                        MonitorFaceAnalysis();
                                    }
                                }
                                catch (Exception e) {

                                    e.printStackTrace();
                                }
                            }
                        });
            }
        });

        //set click listener to switch camera button.
        binding.btnSwitchCamera.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                //animate button
                v.startAnimation(scaleUpSwitchCam);
                v.startAnimation(scaleDownSwitchCam);

                if(currentCamera == CameraSelector.LENS_FACING_FRONT) {

                    currentCamera = CameraSelector.LENS_FACING_BACK;
                }
                else {

                    currentCamera = CameraSelector.LENS_FACING_FRONT;
                }

                cameraProvider.unbindAll();
                BindPreview(cameraProvider);
            }
        });
    }

    //method to monitor face analysis response and update UI.
    private void MonitorFaceAnalysis() {

        /*
        thread to monitor face analysis responses in the database.
        getting camera face detection status from database
        It's bound to asynchronous image process.
        */

        ExecutorService executor = Executors.newSingleThreadExecutor();

        Future<String> future = executor.submit(new Callable<String>() {

            @Override
            public String call() throws Exception {

                do {

                    try {
                        //Instantiating database.
                        databaseHelper = new DatabaseHelper(getContext());

                        //
                        aiCameraModel = databaseHelper.GetAICameraFaceDetection();

                        //close database
                        databaseHelper.close();

                        //
                        if(aiCameraModel.getFaceDetectionControl() == CONTROL_CLOSED) {

                            //start wrapping up data to send and display at the Home Fragment.

                            //Instantiate Bundle.
                            Bundle bundle = new Bundle();

                            //get analysis status
                            switch (aiCameraModel.getFaceDetectionStatus()) {

                                case SUCCESS:

                                    try {

                                        //Instantiating database.
                                        databaseHelper = new DatabaseHelper(getContext());

                                        //Instantiating Session Model
                                        SessionModel sessionModel = databaseHelper.GetActiveSession();

                                        //Instantiating FaceDiagnostic.
                                        faceDiagnostic = databaseHelper.GetLatestDiagnosticByAccountId(sessionModel.getAccountId());

                                        databaseHelper.close();

                                        //send diagnostic id to Bundle.
                                        bundle.putInt("Diagnostic_Result", faceDiagnostic.getDiagnosticId());

                                        break;
                                    }
                                    catch (SQLiteException e) {
                                        e.printStackTrace();
                                    }

                                case IMAGE_FAIL:

                                    //send diagnostic id to Bundle.
                                    bundle.putString("Diagnostic_Fail", "Image_Fail");
                                    break;

                                case UNKNOWN_FACE:

                                    //send diagnostic id to Bundle.
                                    bundle.putString("Diagnostic_Fail", "Unknown_Fail");
                                    break;

                                case EMPTY:

                                    //send diagnostic id to Bundle.
                                    bundle.putString("Diagnostic_Fail", "Empty");
                                    break;

                                default:

                                    //send diagnostic id to Bundle.
                                    bundle.putString("Diagnostic_Fail", "Empty");
                                    break;
                            }

                            //send result to be displayed at the Home Fragment.
                            HomeFragment homeFragment = new HomeFragment();
                            homeFragment.setArguments(bundle);

                            //call method to set new fragment.
                            SetActiveFragment(homeFragment);
                        }
                    }
                    catch (Exception e) {

                        e.printStackTrace();
                    }
                }
                //keep checking.
                while (aiCameraModel.getFaceDetectionControl() == CONTROL_OPEN);

                return "Task_Completed";
            }
        });

        //close thread.
        executor.shutdown();
    }

    //method to set camera views interaction (visual animation).
    public void SetCameraInteraction() {

        scaleUpTakePic = AnimationUtils.loadAnimation(getContext(), R.anim.scale_up);
        scaleDownTakePic = AnimationUtils.loadAnimation(getContext(), R.anim.scale_down);

        scaleUpSwitchCam = AnimationUtils.loadAnimation(getContext(), R.anim.scale_up);
        scaleDownSwitchCam = AnimationUtils.loadAnimation(getContext(), R.anim.scale_down);

        binding.txtCameraMessage.setVisibility(View.INVISIBLE);
    }

    public void StartProgressBar() {

        //setup progress bar
        binding.progressBar.setVisibility(View.VISIBLE);

        //set message in UI
        binding.txtCameraMessage.setText(waitMessage);
        binding.txtCameraMessage.setVisibility(View.VISIBLE);

        progressBar = new ProgressBar(binding.progressBar.getContext());

        final Timer timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {

                progressBarCounter++;

                progressBar.setProgress(progressBarCounter);

                if(progressBarCounter == 400) {

                    timer.cancel();
                }
            }
        };

        timer.schedule(task, 0, 100);
    }

    //method to startup Camera
    private void StartCamera() {

        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(getActivity());

        cameraProviderFuture.addListener(() -> {

            try {

                cameraProvider = cameraProviderFuture.get();
                BindPreview(cameraProvider);
            } catch (Exception e) {

                Log.d("CameraX", "Error getting camera provider ", e);
            }

        }, ContextCompat.getMainExecutor(getActivity()));
    }

    //method to bind preview camera
    private void BindPreview(ProcessCameraProvider cameraProvider) {

        Preview preview = new Preview.Builder().build();
        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(currentCamera)
                .build();
        preview.setSurfaceProvider(binding.viewCamera.getSurfaceProvider());

        Camera camera = cameraProvider.bindToLifecycle(
                (LifecycleOwner) getActivity(),
                cameraSelector,
                preview,
                imageAnalysis);
    }

    //method to check whether the app has access to camera or not.
    private boolean RequestCameraPermission() {

        boolean permission = true;

        for (int i = 0; i < cameraPermissions.length; i++) {

            if (ContextCompat.checkSelfPermission(getContext(), cameraPermissions[i])
                    != PackageManager.PERMISSION_GRANTED) {
                permission = false;
                break;
            }
        }
        return permission;
    }

    //method to setup fragment
    private void SetActiveFragment(Fragment fragment) {

        FragmentManager manager = getActivity().getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.replace(R.id.nav_host_fragment_activity_main, fragment);
        transaction.commit();
    }

    //onDestroy method: close Camera.
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;

        // Unbind Camera provider
        if (cameraProvider != null) {

            cameraProvider.unbindAll();
        }
    }

}