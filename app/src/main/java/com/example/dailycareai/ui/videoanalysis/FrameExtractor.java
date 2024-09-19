package com.example.dailycareai.ui.videoanalysis;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.media.Image;
import android.media.ImageReader;
import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Surface;

import com.google.mlkit.vision.common.InputImage;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FrameExtractor {
    private static final String TAG = "FrameExtractor";
    private MediaExtractor mediaExtractor;
    private MediaCodec mediaCodec;
    private final String videoPath;
    private int rotationDegrees;  //image rotation
    private volatile boolean isEOS = false;
    private ExecutorService executorService;
    private Context context;

    DriverAnalysis driverAnalysis;

    public FrameExtractor(String videoPath, int rotationDegrees, Context context) {
        this.videoPath = videoPath;
        this.context = context;

        // Two threads: one for decoding, one for analysis
        this.executorService = Executors.newFixedThreadPool(2);

        this.rotationDegrees = rotationDegrees;
    }

    public void extractAndDecodeFrames() throws IOException {

        //instantiating driver analysis class to analyze driver's behavior and condition.
        driverAnalysis = new DriverAnalysis(context);

        executorService.submit(() -> {
            try {
                setupMediaComponents();
                decodeFrames();
                releaseResources();

            } catch (IOException e) {
                Log.e(TAG, "Error extracting and decoding frames", e);
            }
        });
    }

    private void setupMediaComponents() throws IOException {
        mediaExtractor = new MediaExtractor();
        mediaExtractor.setDataSource(videoPath);

        int trackIndex = findVideoTrackIndex();
        if (trackIndex == -1) {
            throw new RuntimeException("No video track found in " + videoPath);
        }

        MediaFormat format = mediaExtractor.getTrackFormat(trackIndex);
        mediaCodec = MediaCodec.createDecoderByType(format.getString(MediaFormat.KEY_MIME));

        // Create an ImageReader to receive the decoded frames
        int imgWidth = format.getInteger(MediaFormat.KEY_WIDTH);
        int imgHeight = format.getInteger(MediaFormat.KEY_HEIGHT);  //Formats: Bitmap, NV21 ByteBuffer or YUV_420_888 media.Image.
        ImageReader imageReader = ImageReader.newInstance(imgWidth, imgHeight, ImageFormat.YUV_420_888, 5); //maxImage: buffering images to avoid dropping frames.
        Surface surface = imageReader.getSurface();

        // Attach the Surface to the MediaCodec
        mediaCodec.configure(format, surface, null, 0);
        mediaCodec.start();

        // Set the ImageReader listener
        Handler mainHandler = new Handler(Looper.getMainLooper());
        //When image available, trigger
        imageReader.setOnImageAvailableListener(new ImageAvailableListener(), mainHandler);
    }

    private int findVideoTrackIndex() {
        for (int i = 0; i < mediaExtractor.getTrackCount(); i++) {
            MediaFormat format = mediaExtractor.getTrackFormat(i);
            String mime = format.getString(MediaFormat.KEY_MIME);
            if (mime.startsWith("video/")) {
                mediaExtractor.selectTrack(i);
                return i;
            }
        }
        return -1;
    }

    private void decodeFrames() {
        MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
        while (!isEOS) {
            processInputBuffer();
            int outIndex = mediaCodec.dequeueOutputBuffer(bufferInfo, 10000);
            if (outIndex >= 0) {
                // Render the buffer to the ImageReader's Surface
                mediaCodec.releaseOutputBuffer(outIndex, true);
            }
        }
    }

    private void processInputBuffer() {
        int inIndex = mediaCodec.dequeueInputBuffer(10000);
        if (inIndex >= 0) {
            ByteBuffer buffer = mediaCodec.getInputBuffer(inIndex);
            int sampleSize = mediaExtractor.readSampleData(buffer, 0);
            if (sampleSize < 0) {
                mediaCodec.queueInputBuffer(inIndex, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                isEOS = true;
            } else {
                mediaCodec.queueInputBuffer(inIndex, 0, sampleSize, mediaExtractor.getSampleTime(), 0);
                mediaExtractor.advance();
            }
        }
    }

    private void releaseResources() {
        mediaCodec.stop();
        mediaCodec.release();
        mediaExtractor.release();
        executorService.shutdown();

        //stop driver's face analysis when video frames playback finishes.
        driverAnalysis.StopDriverFaceAnalysis();
    }

    private class ImageAvailableListener implements ImageReader.OnImageAvailableListener {

        //Use in case of file exporting.
        //private File photoFile;
        //private File photoDirectory;
        //Bitmap imageBitmap;

        ImageAvailableListener() {

            //Use in case of file exporting.
            //CreateOutputDirectory();
        }

        @Override
        public void onImageAvailable(ImageReader reader) {

            executorService.submit(() -> {

                try (Image image = reader.acquireLatestImage()) {
                    if (image != null) {

                        //Use in case of file exporting.
                        //exportImage(image);

                        //Carry out face analysis.
                        driverAnalysis.PerformFaceAnalysis(InputImage.fromMediaImage(image, rotationDegrees));
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error processing image", e);
                }

            });
        }

        //Use in case of file exporting.
//        private void exportImage(Image image) throws IOException {
//
//            imageBitmap = getBitmapFromImage(image);
//
//            photoFile = new File(photoDirectory, "face_" + System.currentTimeMillis() + ".jpg");
//            FileOutputStream fos = new FileOutputStream(photoFile);
//            imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
//            fos.flush();
//            fos.close();
//        }

        //Use in case of file exporting.
//        private Bitmap getBitmapFromImage(Image image) {
//
//            if (image.getFormat() != ImageFormat.YUV_420_888) {
//                throw new IllegalArgumentException("Invalid image format");
//            }
//
//            Image.Plane[] planes = image.getPlanes();
//
//            ByteBuffer yBuffer = planes[0].getBuffer();
//            ByteBuffer uBuffer = planes[1].getBuffer();
//            ByteBuffer vBuffer = planes[2].getBuffer();
//
//            int ySize = yBuffer.remaining();
//            int uSize = uBuffer.remaining();
//            int vSize = vBuffer.remaining();
//
//            byte[] nv21 = new byte[ySize + uSize + vSize];
//            yBuffer.get(nv21, 0, ySize);
//            vBuffer.get(nv21, ySize, vSize);
//            uBuffer.get(nv21, ySize + vSize, uSize);
//
//            int width = image.getWidth();
//            int height = image.getHeight();
//
//            YuvImage yuvImage = new YuvImage(nv21, ImageFormat.NV21, width, height, null);
//            ByteArrayOutputStream out = new ByteArrayOutputStream();
//            yuvImage.compressToJpeg(new Rect(0, 0, width, height), 100, out);
//
//            byte[] data = out.toByteArray();
//            BitmapFactory.Options options = new BitmapFactory.Options();
//            options.inMutable = true;
//
//            //Image Proxy converted to Bitmap.
//            return  BitmapFactory.decodeByteArray(data, 0, data.length, options);
//        }

        //Use in case of file exporting.
//        public void CreateOutputDirectory() {
//
//            //Emulator: /storage/emulated/0/Pictures/Daily_Care_Pic/.
//            photoDirectory = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/Daily_Care_Pic/");
//
//            if(!photoDirectory.exists()) {
//                try {
//                    photoDirectory.mkdirs();
//                    Log.d("CREATE DIRECTORY", "Directory created successfully");
//                }
//                catch (Exception e) {
//                    Log.d("CREATE DIRECTORY", "Fail to create directory");
//                }
//            }
//            else {
//                Log.d("CREATE DIRECTORY", "Directory already exists");
//            }
//        }
    }
}
