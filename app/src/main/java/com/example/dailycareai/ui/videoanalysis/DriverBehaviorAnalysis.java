package com.example.dailycareai.ui.videoanalysis;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.YuvImage;
import android.media.Image;
import android.util.Log;

import org.tensorflow.lite.Interpreter;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.HashMap;
import java.util.Map;

import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;
import org.tensorflow.lite.DataType;

public class DriverBehaviorAnalysis {

    private final Interpreter tflite;

    public DriverBehaviorAnalysis(Context context) throws IOException {

        tflite = new Interpreter(loadModelFile(context));
    }

    private MappedByteBuffer loadModelFile(Context context) throws IOException {

        AssetFileDescriptor fileDescriptor = context.getAssets().openFd("driver-behavior-model.tflite");
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    public void runInference(Image image) {

        // Convert android.media.Image to Bitmap
        Bitmap bitmap = imageToBitmap(image);

        // Assume inputBuffer is in the right format, you may need to set the appropriate byte order
        // Create TensorImage from ByteBuffer (Assuming FLOAT32 data type)
        TensorImage inputImage = new TensorImage(DataType.FLOAT32);
        inputImage.load(bitmap);

        // Prepare the output arrays
        float[][][] outputBoundingBoxes = new float[1][40][4]; // Shape [1, 40, 4]
        float[][] outputClassLabels = new float[1][40];        // Shape [1, 40]
        float[][] outputScores = new float[1][40];             // Shape [1, 40]

        // Output map to hold the different output tensors
        Map<Integer, Object> outputMap = new HashMap<>();
        outputMap.put(0, outputBoundingBoxes);
        outputMap.put(1, outputClassLabels);
        outputMap.put(2, outputScores);

        // Run the model
        tflite.runForMultipleInputsOutputs(new Object[]{inputImage.getBuffer()}, outputMap);

        // Process the outputs
        for (int i = 0; i < 40; i++) {
            float[] box = outputBoundingBoxes[0][i];
            float label = outputClassLabels[0][i];
            float score = outputScores[0][i];

            // Perform actions with the bounding box, label, and score
            Log.d("***__DRIVER_BEHAVIOR", "Detection " + i + ": Box [" + box[0] + ", " + box[1] + ", " + box[2] + ", " + box[3] + "], Label: " + label + ", Score: " + score);
        }
    }

    private Bitmap imageToBitmap(Image image) {
        Image.Plane[] planes = image.getPlanes();
        ByteBuffer buffer = planes[0].getBuffer();
        int pixelStride = planes[0].getPixelStride();
        int rowStride = planes[0].getRowStride();
        int rowPadding = rowStride - pixelStride * image.getWidth();
        int width = image.getWidth();
        int height = image.getHeight();

        // Create a bitmap with the exact width and height of the image
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        // Create a temporary buffer to store the pixel data
        ByteBuffer tempBuffer = ByteBuffer.allocateDirect(width * height * 4); // 4 bytes per pixel (ARGB_8888)
        for (int i = 0; i < height; i++) {
            buffer.position(i * rowStride);
            buffer.get(tempBuffer.array(), i * width * 4, width * 4);
        }

        tempBuffer.rewind();
        bitmap.copyPixelsFromBuffer(tempBuffer);

        // If the image format is YUV, convert it to a valid format like RGB
        if (image.getFormat() == ImageFormat.YUV_420_888) {
            YuvImage yuvImage = new YuvImage(tempBuffer.array(), ImageFormat.NV21, width, height, null);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            yuvImage.compressToJpeg(new android.graphics.Rect(0, 0, width, height), 100, out);
            byte[] imageBytes = out.toByteArray();
            bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
        }

        return bitmap;
    }




}

//    public void runInference(ByteBuffer inputBuffer) {
//
//        float[][][] outputBoundingBoxes = new float[1][40][4]; // Shape [1, 40, 4]
//        float[][] outputClassLabels = new float[1][40];        // Shape [1, 40]
//        float[][] outputScores = new float[1][40];             // Shape [1, 40]
//
//        // Output map to hold the different output tensors
//        Map<Integer, Object> outputMap = new HashMap<>();
//        outputMap.put(0, outputBoundingBoxes);
//        outputMap.put(1, outputClassLabels);
//        outputMap.put(2, outputScores);
//
//        // Run the model
//        tflite.runForMultipleInputsOutputs(new Object[]{inputBuffer}, outputMap);
//
//
//        // Process the outputs
//        for (int i = 0; i < 40; i++) {
//            float[] box = outputBoundingBoxes[0][i];
//            float label = outputClassLabels[0][i];
//            float score = outputScores[0][i];
//
//            // Perform actions with the bounding box, label, and score
//            Log.d("***__DRIVER_BEHAVIOR","Detection " + i + ": Box [" + box[0] + ", " + box[1] + ", " + box[2] + ", " + box[3] + "], Label: " + label + ", Score: " + score);
//        }
//    }




//float[][][] output = new float[1][40][4];

//        tflite.run(inputBuffer, output);
//
//        // Process the output (example)
//        for (int i = 0; i < output[0].length; i++) {
//            float[] box = output[0][i];
//            Log.d("***__ Driver_Behavior", "Box " + i + ": " + box[0] + ", " + box[1] + ", " + box[2] + ", " + box[3]);
//        }

// Output map to hold the different output tensors
//Object[] outputArray = {outputBoundingBoxes, outputClassLabels, outputScores};
