package com.example.dailycareai.ui.videoanalysis;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;

import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.MediaController;
import android.widget.Toast;

import android.Manifest;
import com.example.dailycareai.databinding.FragmentVideoAnalysisBinding;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class VideoAnalysis extends Fragment {

    FragmentVideoAnalysisBinding binding;
    private MediaPlayer mediaPlayer;
    private MediaMetadataRetriever retriever; //to extract media rotation
    private int rotationDegrees;  //image rotation
    private MediaController mediaController;
    String videoPath;
    Uri uri;
    private final String[] externalStoragePermission = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE};
    private static final int REQUEST_CODE_READ_EXTERNAL_STORAGE = 100;
    private ExecutorService executorService;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentVideoAnalysisBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        // Checking external storage permission
        checkExternalStoragePermission();

        // Set ClickListener to video button
        binding.btnLoadVideo.setOnClickListener(v -> {
            try {
                //Load video from directory.
                loadVideoFromDirectory();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        // Thread pool for video playback and frame extraction
        executorService = Executors.newFixedThreadPool(1);

        return view;
    }

    private void checkExternalStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(getContext(), externalStoragePermission[0]) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(getActivity(), externalStoragePermission, REQUEST_CODE_READ_EXTERNAL_STORAGE);
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                try {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                    intent.addCategory("android.intent.category.DEFAULT");
                    intent.setData(Uri.parse(String.format("package:%s", getContext().getPackageName())));
                    startActivity(intent);
                } catch (Exception e) {
                    Intent intent = new Intent();
                    intent.setAction(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                    startActivity(intent);
                }
            }
        }
    }

    private void loadVideoFromDirectory() throws IOException {

        //emulator/storage/emulated/0/Movies/
        videoPath = "/storage/emulated/0/Movies/Truck_driver_drowsiness.mp4";

        //Required for FrameExtractor class.
        ExtractVideoRotation();

        //Bind VideoView (interface) to file video path.
        uri = Uri.parse(videoPath);
        binding.videoView.setVideoURI(uri);

        //Setup Media Controle.
        mediaController = new MediaController(getContext());
        binding.videoView.setMediaController(mediaController);
        mediaController.setAnchorView(binding.videoView);

        //Play video and start frame extractions.
        executorService.submit(() -> {
            binding.videoView.setOnPreparedListener(mp -> {
                mediaPlayer = mp;
                mediaPlayer.start();
                startFrameExtraction();
            });
        });

        // Video completed, handle if needed.
        binding.videoView.setOnCompletionListener(mp -> {
        });

        //Error handler.
        binding.videoView.setOnErrorListener((mp, what, extra) -> false);
    }

    //Method to get video rotation from the analyzed video file.
    private void ExtractVideoRotation() throws IOException {

        retriever = new MediaMetadataRetriever();

        try {
            // Set the data source (video file)
            retriever.setDataSource(videoPath);

            // Extract rotation metadata
            String rotation = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION);

            //Set to zero in case of failure.
            if (rotation != null) {
                rotationDegrees = Integer.parseInt(rotation);
            } else {
                rotationDegrees = 0;
            }
        }
        catch (Exception e){
            e.printStackTrace();
        } finally {
            //release the retriever when done
            retriever.release();
        }
    }

    //Method to start frame extraction
    private void startFrameExtraction() {

        FrameExtractor frameExtractor = new FrameExtractor(videoPath, rotationDegrees, getContext());
        try {
            //run frame extractor!
            frameExtractor.extractAndDecodeFrames();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    //Shutdown thread.
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}