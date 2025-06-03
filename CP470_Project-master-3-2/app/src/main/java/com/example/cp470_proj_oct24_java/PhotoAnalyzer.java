package com.example.cp470_proj_oct24_java;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;

import java.io.InputStream;


public class PhotoAnalyzer {

    private static final String TAG =  PhotoAnalyzer.class.getSimpleName();
    private static final double BLUR_THRESHOLD = 2.7; //4.5 // Zaid - Please Adjust threshold for stricter blur detection

    // Callback interface for photo analysis results
    public interface AnalysisCallback {
        void onAnalysisComplete(boolean isValid, String message);
    }

    /**
     * Analyze a photo for blur, faces, open eyes, and other attributes.
     *
     * @param context  The application context.
     * @param photoUri The URI of the photo to analyze.
     * @param callback The callback to report the results.
     */
    public static void analyzePhoto(Context context, Uri photoUri, AnalysisCallback callback) {
        try {
            // Load the photo as a Bitmap (THis is necessary for ML Kit)
            InputStream inputStream = context.getContentResolver().openInputStream(photoUri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

            if (bitmap == null) {
                callback.onAnalysisComplete(false, "Error loading photo.");
                return;
            }

            // Step 1-Check if the image is blurred
            boolean isBlurred = isImageBlurred(bitmap);
            if (isBlurred) {
                callback.onAnalysisComplete(false, "The photo is too blurred.");
                Toast.makeText(context, "The photo is too blurred.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Step 2-Perform face detection using ML Kit
            InputImage image = InputImage.fromBitmap(bitmap, 0);
            FaceDetectorOptions options = new FaceDetectorOptions.Builder()
                    .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
                    .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
                    .build();

            FaceDetector detector = FaceDetection.getClient(options);
            detector.process(image)
                    .addOnSuccessListener(faces -> {
                        if (faces.isEmpty()) {
                            callback.onAnalysisComplete(false, "No face detected.");
                        } else {

                            Float leftEyeOpenProb,rightEyeOpenProb;
                            boolean eyesOpen;

                            for (Face face : faces) {
                                leftEyeOpenProb = face.getLeftEyeOpenProbability();
                                rightEyeOpenProb = face.getRightEyeOpenProbability();

                                eyesOpen = leftEyeOpenProb != null && rightEyeOpenProb != null &&
                                        leftEyeOpenProb > 0.5 && rightEyeOpenProb > 0.5;

                                if (eyesOpen) {
                                    callback.onAnalysisComplete(true, "Face detected with eyes open.");
                                    return;
                                }
                            }
                            callback.onAnalysisComplete(false, "Face detected but eyes are closed.");
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error analyzing photo: " + e.getMessage());
                        callback.onAnalysisComplete(false, "Error detecting face: " + e.getMessage());
                    });

        } catch (Exception e) {
            Log.e(TAG, "Error analyzing photo: " + e.getMessage());
            callback.onAnalysisComplete(false, "Error analyzing photo: " + e.getMessage());
        }
    }

    /**
     * Check if an image is blurred based on pixel intensity variance.
     *
     * @param bitmap The image to check.
     * @return True if the image is blurred, false otherwise.
     */
    private static boolean isImageBlurred(Bitmap bitmap) {
        // Convert the image to grayscale
        Bitmap grayscaleBitmap = toGrayscale(bitmap);

        // Calculate the variance of pixel intensity
        double variance = calculateVariance(grayscaleBitmap);
        variance = variance/1000;

        Log.i(TAG, "Blur Detection Variance: " + variance);
        Log.i(TAG, "BLUR Threshold: " + BLUR_THRESHOLD);

        // Determine if the image is blurred
        return variance < BLUR_THRESHOLD;
    }

    /**
     * Convert a Bitmap to grayscale.
     *
     * @param bitmap The original Bitmap.
     * @return A grayscale Bitmap.
     */
    private static Bitmap toGrayscale(Bitmap bitmap) {
        Bitmap grayscaleBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);


        for (int y = 0; y < bitmap.getHeight(); y++) {

            int pixel,gray,grayPixel;

            for (int x = 0; x < bitmap.getWidth(); x++) {
                 pixel = bitmap.getPixel(x, y);
                 gray = (int) (Color.red(pixel) * 0.3 + Color.green(pixel) * 0.59 + Color.blue(pixel) * 0.11);
                 grayPixel = Color.rgb(gray, gray, gray);
                grayscaleBitmap.setPixel(x, y, grayPixel);
            }
        }

        return grayscaleBitmap;
    }

    /**
     * Calculate the variance of pixel intensity in a grayscale image.
     *
     * @param bitmap The grayscale Bitmap.
     * @return The variance of pixel intensity.
     */
    private static double calculateVariance(Bitmap bitmap) {
        long sum = 0;
        long sumOfSquares = 0;
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int totalPixels = width * height;

        for (int y = 0; y < height; y++) {
            int pixel,intensity;

            for (int x = 0; x < width; x++) {
                pixel = bitmap.getPixel(x, y);
                intensity = Color.red(pixel); // All channels are the same in grayscale

                sum += intensity;
                sumOfSquares += intensity * intensity;
            }
        }

        double mean = sum / (double) totalPixels;
        double meanOfSquares = sumOfSquares / (double) totalPixels;

        return meanOfSquares - (mean * mean);
    }
}

