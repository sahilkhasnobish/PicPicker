package com.example.cp470_proj_oct24_java;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.FileInputStream;
import java.util.ArrayList;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;
import android.view.View;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cp470_proj_oct24_java.PhotosAdapter;
import com.example.cp470_proj_oct24_java.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;// Method to update the RecyclerView with valid photos
import java.util.ArrayList;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import com.example.cp470_proj_oct24_java.ui.login.userpasswordDBHelper;
import android.database.sqlite.SQLiteDatabase;
import android.content.ContentValues;

//phoebe udpate nov 30


public class DisplayResultsActivity extends AppCompatActivity {

    private static final String TAG = DisplayResultsActivity.class.getSimpleName();

    private ArrayList<Uri> selectedPhotos;
    private ArrayList<Uri> validPhotos; //array of photos to display after checking for eye blinks


    private int photosAnalyzed = 0; //  Counter for analyzed photos
    private int totalPhotos = 0; // - Total number of photos to analyze

    ProgressBar progressBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        Log.i(TAG, "inside onCreate");

        setContentView(R.layout.activity_display_results); //set layout


        TextView textView = findViewById(R.id.display_photos_text);

        //progress bar - for anayzling and displaying pics
        progressBar = findViewById(R.id.progress_bar_display_photos);
        progressBar.setVisibility(View.VISIBLE);
        progressBar.setProgress(10);
        Log.i(TAG, "loading progressBar");

        //photos are loaded from the db when View Best Photos is opened
        ArrayList<Uri> savedImageUris = getImageUrisFromDatabase();

        if (savedImageUris.isEmpty()) {
            Log.i(TAG, "No photos found.");
            //Toast.makeText(this, "No saved photos to display.", Toast.LENGTH_SHORT).show();
        } else {
            Log.i(TAG, "Displaying " + savedImageUris.size() + " photos.");
            validPhotos = savedImageUris;
            updateRecyclerView();
        }

        //get the pics the user selected- from select photos activity // Retrieve the ArrayList of URIs
        selectedPhotos = getIntent().getParcelableArrayListExtra("selectedPhotos");


        if (selectedPhotos == null & savedImageUris.isEmpty()) { //user did not select photos //they came from main page. //and no saved instance
            Log.i(TAG, "user did not go to select photos");

            textView.setText("You didn\'t select any photos");
            progressBar.setProgress(100);
            progressBar.setVisibility(View.INVISIBLE);//hide the progress bar


        } else { //user has selected photos
            Log.i(TAG, "user selected photos");

            textView.setText(R.string.display_results_message);
            progressBar.setProgress(50);

            analyzePhotosWithMachineLearning();


            progressBar.setProgress(100);
            progressBar.setVisibility(View.INVISIBLE); //hide progress bar
            Log.i(TAG, "100% progress");

        }
    }


    //Analyze with machine learning
    public void analyzePhotosWithMachineLearning() {
        validPhotos = new ArrayList<>();

        // Check if the photos were properly received
        if (selectedPhotos != null) {
            Log.i(TAG, "Received " + selectedPhotos.size() + " photos.");
            totalPhotos = selectedPhotos.size();

            for (Uri photoUri : selectedPhotos) {
                PhotoAnalyzer.analyzePhoto(this, photoUri, (isValid, message) -> {
                    photosAnalyzed++; // Increment counter for analyzed photos

                    if (isValid) {
                        Log.i(TAG, "Photo URI: " + photoUri.toString());
                        validPhotos.add(photoUri);

                        print("Photo " + photosAnalyzed + " of " + totalPhotos + " is valid: " + message);
                        Log.i(TAG, "Photo " + photosAnalyzed + " of " + totalPhotos + " is valid: " + message);


                    } else {
                        // Display reason why the photo is invalid
                        runOnUiThread(() -> print("Photo " + photosAnalyzed + " of " + totalPhotos + " is invalid: " + message));


                        Log.i(TAG, "Photo invalid: " + message);
                    }

                    // Update RecyclerView when all photos are analyzed
                    if (photosAnalyzed == totalPhotos) {
                        updateRecyclerView();//display photos to layout
                    }
                });
            }

        } else {
            Log.i(TAG, "No photos received.");
        }

    }


    //update the photos on the gui
    private void updateRecyclerView() {
        Log.i(TAG, "Updating RecyclerView with valid photos...");

//        RecyclerView photosRecyclerView = findViewById(R.id.photos_recycler_view);
//        photosRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));
//        PhotosAdapter adapter = new PhotosAdapter(this, validPhotos);
//        photosRecyclerView.setAdapter(adapter);


        //fragment
        PhotoDisplayFragment fragment = PhotoDisplayFragment.newInstance(validPhotos);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();

        if (validPhotos.isEmpty()) {
            print("No valid photos to display.");

        } else {
            print("Displaying " + validPhotos.size() + " valid photos.");

        }

        // progressBar.setVisibility(View.INVISIBLE); //hide progress bar

    }


    // save photos to phone app only if user clicks the save button //saving valid photos to the exteranal vm storage
    public void clickSavePhotos(View view) {

        Log.i(TAG, "Inside clickSavePhotos");
        //cold boot the vm to see :   /storage/emulated/0/Pictures/photo_1732476262383.jpg


        if (validPhotos != null && !validPhotos.isEmpty()) {
            print("Saving " + validPhotos.size() + " valid photos.");


            //save photo to app with database
            SQLiteDatabase db = new userpasswordDBHelper(this).getWritableDatabase();

            for (Uri photoUri : validPhotos) {
                try {
                    //get bitmap
                    InputStream inputStream = getContentResolver().openInputStream(photoUri);
                    Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

                    //convert to array
                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
                    byte[] imageBytes = byteArrayOutputStream.toByteArray();

                    //save photos to db
                    ContentValues contentValues = new ContentValues();
                    contentValues.put("image", imageBytes);

                    long result = db.insert("Photos", null, contentValues);
                    if (result == -1) {
                        Log.e(TAG, "failed to insert photo into database");
                    } else {
                        Log.i(TAG, "photo saved to database with ID: " + result);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error saving photo: " + e.getMessage());
                }

                try {
                    savePhotoToDevice(photoUri);

                } catch (IOException e) {
                    Log.e(TAG, "Error saving photo: " + photoUri.toString(), e);
                    print("Failed to save photo.");
                }
            }
        } else {
            print("No valid photos to save.");
        }
    }

    private void savePhotoToDevice(Uri photoUri) throws IOException {

        // Get the input stream from the URI
        InputStream inputStream = getContentResolver().openInputStream(photoUri);

        // Create a new file in the DCIM directory
        File picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File photoFile = new File(picturesDir, "photo_" + System.currentTimeMillis() + ".jpg");

        // Write the image to the file
        OutputStream outputStream = new FileOutputStream(photoFile);
        byte[] buffer = new byte[1024];
        int length;
        while ((length = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, length);
        }

        outputStream.flush();
        inputStream.close();
        outputStream.close();

        Log.i(TAG, "Saved photo to: " + photoFile.getAbsolutePath());
        print("Photo saved to device.");
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.i(TAG, "inside onSaveInstanceState");
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        Log.i(TAG, "inside onRestoreInstanceState");
    }


    // Back to main activity
    public void backToMainMenu(View view) {
        Log.i(TAG, "inside backToMainMenu");
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }


    // Print using a toast - short
    public void print(String str) {
        Toast toast = Toast.makeText(this, str, Toast.LENGTH_SHORT);
        toast.show();

    }

    //print using a toast - long
    public void printLong(String str) {
        Toast toast = Toast.makeText(this, str, Toast.LENGTH_LONG);
        toast.show();

    }

    private ArrayList<Uri> getImageUrisFromDatabase() {
        ArrayList<Uri> imageUris = new ArrayList<>();
        SQLiteDatabase db = new userpasswordDBHelper(this).getReadableDatabase();

        Cursor cursor = db.query("Photos", new String[]{"image"}, null, null, null, null, null);

        if (cursor != null) {
            while (cursor.moveToNext()) {
                byte[] imageBytes = cursor.getBlob(cursor.getColumnIndexOrThrow("image"));
                Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                Uri imageUri = saveBitmapToCache(bitmap);
                if (imageUri != null) {
                    imageUris.add(imageUri);
                }
            }
            cursor.close();
        }
        return imageUris;
    }

    private Uri saveBitmapToCache(Bitmap bitmap) {
        try {
            File cacheDir = new File(getCacheDir(), "images");
            if (!cacheDir.exists()) {
                cacheDir.mkdirs();
            }
            File tempFile = File.createTempFile("photo_", ".jpg", cacheDir);
            FileOutputStream fos = new FileOutputStream(tempFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.close();
            return Uri.fromFile(tempFile);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}