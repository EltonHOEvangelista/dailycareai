package com.example.dailycareai.ui.checkup;

import android.database.sqlite.SQLiteException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.media.Image;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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

public class AICamera extends Fragment {

    private FragmentCheckupAicameraBinding binding;
    private ProcessCameraProvider cameraProvider;
    private String[] cameraPermissions = new String[]{
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
    };
    private int currentCamera = CameraSelector.LENS_FACING_FRONT;
    private static final int MAX_FRAMES = 100;
    private ImageAnalysis imageAnalysis;
    private static final int SUCCESS = 100;
    private static final int IMAGE_FAIL = 101;
    private static final int UNKNOWN_FACE = 102;
    private static final int EMPTY = 103;
    private static final int CONTROL_CLOSED = 0;
    private static final int CONTROL_OPEN = 1;
    private AICameraModel aiCameraModel;
    private FaceDiagnostic faceDiagnostic;
    private DatabaseHelper databaseHelper;
    private ProgressBar progressBar;
    private int progressBarCounter = 0;
    private Animation scaleUpTakePic, scaleDownTakePic, scaleUpSwitchCam, scaleDownSwitchCam;
    private final String waitMessage = "Please, wait up to 45 seconds, ensuring the camera is directed " +
            "towards your face for optimal results.";
    private ExecutorService executorService;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        binding = FragmentCheckupAicameraBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        binding.progressBar.setVisibility(View.INVISIBLE);

        if(!requestCameraPermission()) {
            ActivityCompat.requestPermissions(getActivity(), cameraPermissions, 0);
        } else {
            initializeExecutor();
            startCamera();
            setupCamera();
            setCameraInteraction();
        }

        return view;
    }

    private void initializeExecutor() {
        executorService = Executors.newFixedThreadPool(4); // Initialize a thread pool with 4 threads
    }

    private void setupCamera() {
        imageAnalysis = new ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_YUV_420_888)
                .build();

        binding.btnStartImageAnalysis.setOnClickListener(v -> {
            v.startAnimation(scaleUpTakePic);
            v.startAnimation(scaleDownTakePic);
            startProgressBar();
            List<InputImage> inputImages = new ArrayList<>();

            imageAnalysis.setAnalyzer(executorService, image -> {
                analyzeImage(inputImages, image);
            });
        });

        binding.btnSwitchCamera.setOnClickListener(v -> {
            v.startAnimation(scaleUpSwitchCam);
            v.startAnimation(scaleDownSwitchCam);
            switchCamera();
        });
    }

    @OptIn(markerClass = ExperimentalGetImage.class)
    private void analyzeImage(List<InputImage> inputImages, @NonNull ImageProxy image) {
        try {

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

            if (inputImages.size() > MAX_FRAMES) {
                // Stop analyzing further frames
                imageAnalysis.clearAnalyzer();

                executorService.submit(() -> {
                    // Update the database and request face analysis in a background thread
                    updateDatabaseForAnalysis();
                    requestFaceAnalysis(inputImages);
                    monitorFaceAnalysis();
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // Always close the ImageProxy to avoid memory leaks
            //image.close();
        }
    }

    private void updateDatabaseForAnalysis() {
        databaseHelper = new DatabaseHelper(getContext());
        aiCameraModel = databaseHelper.GetAICameraFaceDetection();
        aiCameraModel.setFaceDetectionControl(CONTROL_OPEN);
        aiCameraModel.setFaceDetectionStatus(EMPTY);
        databaseHelper.SetAICameraFaceDetection(aiCameraModel);
        databaseHelper.close();
    }

    private void requestFaceAnalysis(List<InputImage> inputImages) {
        FaceAnalysis faceAnalysis = new FaceAnalysis(getContext());
        faceAnalysis.PerformFaceAnalysis(inputImages);
    }

    private void monitorFaceAnalysis() {
        executorService.submit(() -> {
            while (true) {
                updateUiAfterFaceAnalysis();
                if (aiCameraModel.getFaceDetectionControl() == CONTROL_CLOSED) {
                    break;
                }
                try {
                    Thread.sleep(100); // Avoid busy-waiting
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    private void updateUiAfterFaceAnalysis() {
        try {
            databaseHelper = new DatabaseHelper(getContext());
            aiCameraModel = databaseHelper.GetAICameraFaceDetection();
            databaseHelper.close();

            if (aiCameraModel.getFaceDetectionControl() == CONTROL_CLOSED) {
                handleAnalysisResult();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleAnalysisResult() {
        Bundle bundle = new Bundle();
        switch (aiCameraModel.getFaceDetectionStatus()) {
            case SUCCESS:
                sendSuccessToHomeFragment(bundle);
                break;
            case IMAGE_FAIL:
                bundle.putString("Diagnostic_Fail", "Image_Fail");
                break;
            case UNKNOWN_FACE:
                bundle.putString("Diagnostic_Fail", "Unknown_Fail");
                break;
            case EMPTY:
                bundle.putString("Diagnostic_Fail", "Empty");
                break;
            default:
                bundle.putString("Diagnostic_Fail", "Empty");
                break;
        }
        transitionToHomeFragment(bundle);
    }

    private void sendSuccessToHomeFragment(Bundle bundle) {
        try {
            databaseHelper = new DatabaseHelper(getContext());
            SessionModel sessionModel = databaseHelper.GetActiveSession();
            faceDiagnostic = databaseHelper.GetLatestDiagnosticByAccountId(sessionModel.getAccountId());
            databaseHelper.close();
            bundle.putInt("Diagnostic_Result", faceDiagnostic.getDiagnosticId());
        } catch (SQLiteException e) {
            e.printStackTrace();
        }
    }

    private void transitionToHomeFragment(Bundle bundle) {
        HomeFragment homeFragment = new HomeFragment();
        homeFragment.setArguments(bundle);
        setActiveFragment(homeFragment);
    }

    public void setCameraInteraction() {
        scaleUpTakePic = AnimationUtils.loadAnimation(getContext(), R.anim.scale_up);
        scaleDownTakePic = AnimationUtils.loadAnimation(getContext(), R.anim.scale_down);
        scaleUpSwitchCam = AnimationUtils.loadAnimation(getContext(), R.anim.scale_up);
        scaleDownSwitchCam = AnimationUtils.loadAnimation(getContext(), R.anim.scale_down);
        binding.txtCameraMessage.setVisibility(View.INVISIBLE);
    }

    public void startProgressBar() {
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.txtCameraMessage.setText(waitMessage);
        binding.txtCameraMessage.setVisibility(View.VISIBLE);
        progressBar = new ProgressBar(binding.progressBar.getContext());
        Timer timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                progressBarCounter++;
                progressBar.setProgress(progressBarCounter);
                if (progressBarCounter == 400) {
                    timer.cancel();
                }
            }
        };
        timer.schedule(task, 0, 100);
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(getActivity());
        cameraProviderFuture.addListener(() -> {
            try {
                cameraProvider = cameraProviderFuture.get();
                bindPreview(cameraProvider);
            } catch (Exception e) {
                Log.d("CameraX", "Error getting camera provider ", e);
            }
        }, ContextCompat.getMainExecutor(getActivity()));
    }

    private void bindPreview(ProcessCameraProvider cameraProvider) {
        Preview preview = new Preview.Builder().build();
        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(currentCamera)
                .build();
        preview.setSurfaceProvider(binding.viewCamera.getSurfaceProvider());
        cameraProvider.bindToLifecycle(
                (LifecycleOwner) getActivity(),
                cameraSelector,
                preview,
                imageAnalysis);
    }

    private boolean requestCameraPermission() {
        for (String permission : cameraPermissions) {
            if (ContextCompat.checkSelfPermission(getContext(), permission)
                    != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    private void setActiveFragment(Fragment fragment) {
        FragmentManager manager = getActivity().getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.replace(R.id.nav_host_fragment_activity_main, fragment);
        transaction.commit();
    }

    private void switchCamera() {
        currentCamera = (currentCamera == CameraSelector.LENS_FACING_FRONT) ?
                CameraSelector.LENS_FACING_BACK : CameraSelector.LENS_FACING_FRONT;
        cameraProvider.unbindAll();
        bindPreview(cameraProvider);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        if (cameraProvider != null) {
            cameraProvider.unbindAll();
        }
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}