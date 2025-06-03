package com.example.cp470_proj_oct24_java;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;

//user select photos - which will be narrowed down
public class SelectPhotosActivity extends AppCompatActivity {

    private static final String TAG = SelectPhotosActivity.class.getSimpleName();
    private static final int MAX_FINAL_PHOTOS = 30;
    private static final int REQUEST_CODE_SELECT_PHOTOS = 1;
    private static final int REQUEST_CODE_PERMISSION = 2;

    private ArrayList<Uri> selectedPhotos = new ArrayList<>();//array of photos given by user

    private ProgressDialog progressDialog; // ProgressDialog for the loader


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "inside onCreate");

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_select_photos);

        //back button
        Button backButton = findViewById(R.id.back_button_select_photos);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        //    backButton.setOnClickListener(view -> onBackPressed());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Request storage permission if not already granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_CODE_PERMISSION);
        }


        // Initialize the ProgressDialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Please wait while we process your photos...");
        progressDialog.setCancelable(false); // Prevent user from canceling the loader


    }



    // Start an intent to select photos from the gallery
    public void selectPhotos(View view) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_CODE_SELECT_PHOTOS);

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_SELECT_PHOTOS && resultCode == RESULT_OK) {
            selectedPhotos.clear(); // Clear any previously selected photos

            if (data != null) {// Multiple images selected

                if (data.getClipData() != null) {
                    int count = data.getClipData().getItemCount();
                    for (int i = 0; i < count; i++) {
                        Uri imageUri = data.getClipData().getItemAt(i).getUri();
                        selectedPhotos.add(imageUri); // Add each selected image URI to the list


                        // Get and log the properties of the selected image
                        getPhotoProperties(imageUri);

                    }
                    print("Selected " + selectedPhotos.size() + " photos.");

                } else if (data.getData() != null) {  // Single image selected
                    Uri imageUri = data.getData();
                    selectedPhotos.add(imageUri); // Add single selected image URI to the list
                    print("Selected 1 photo.");


                    // Get and log the properties of the selected image
                    getPhotoProperties(imageUri);

                }
            }
        }
    }


    // Confirm the photos user selected

    public void clickConfirmPhotos(View view) {
        Log.i(TAG, "inside clickConfirmPhotos");

        String err_message = "INVALID SELECTION: Must select a number between 1-30";

        int selectedPhotosCount = selectedPhotos.size();
        Log.i(TAG,"selectedPhotosCount " + String.valueOf(selectedPhotosCount));

        if (0<selectedPhotosCount  && selectedPhotosCount <= MAX_FINAL_PHOTOS) { //valid



            // Show ProgressDialog
            ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("Analyzing photos, please wait...");
            progressDialog.setCancelable(false);
            progressDialog.show();

            // Simulate processing delay in the background
            new Thread(() -> {
                try {
                    Thread.sleep(2000); // Simulate processing (e.g., 2 seconds)

                    // Transition to DisplayResultsActivity
                    Intent intent = new Intent(this, DisplayResultsActivity.class);
                    intent.putParcelableArrayListExtra("selectedPhotos", selectedPhotos);// Pass the ArrayList of URIs
                    startActivity(intent);

                } catch (InterruptedException e) {
                    Log.e(TAG, "Error during photo processing: " + e.getMessage());
                } finally {
                    // Dismiss the ProgressDialog
                    progressDialog.dismiss();
                }
            }).start();


        }else{ //invalid
            print(err_message);
        }

    }


    // Print using a toast - short
    public void print(String str) {
        Toast toast = Toast.makeText(this, str, Toast.LENGTH_SHORT);
        toast.show();
    }

    // Print using a toast - long
    public void printLong(String str) {
        Toast toast = Toast.makeText(this, str, Toast.LENGTH_LONG);
        toast.show();
    }




    private void getPhotoProperties(Uri imageUri) {
        // Define the projection (the columns we want to query)
        String[] projection = {
                MediaStore.Images.Media.DISPLAY_NAME,   // The name of the file
                MediaStore.Images.Media.DATE_TAKEN,     // The date the photo was taken
                MediaStore.Images.Media.SIZE            // The size of the file (optional)
        };

        // Query the MediaStore content resolver
        try (Cursor cursor = getContentResolver().query(imageUri, projection, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                // Get column indexes for each property
                int displayNameIndex = cursor.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME);
                int dateTakenIndex = cursor.getColumnIndex(MediaStore.Images.Media.DATE_TAKEN);
                int sizeIndex = cursor.getColumnIndex(MediaStore.Images.Media.SIZE);

                // Check if the indexes are valid (>= 0)
                if (displayNameIndex >= 0) {
                    String displayName = cursor.getString(displayNameIndex);
                    Log.i(TAG, "Photo Name: " + displayName);
                } else {
                    Log.e(TAG, "Column DISPLAY_NAME not found.");
                }

                if (dateTakenIndex >= 0) {
                    long dateTakenMillis = cursor.getLong(dateTakenIndex);
                    Date dateTaken = new Date(dateTakenMillis);
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                    String formattedDate = dateFormat.format(dateTaken);
                    Log.i(TAG, "Date Taken: " + formattedDate);
                } else {
                    Log.e(TAG, "Column DATE_TAKEN not found.");
                }

                if (sizeIndex >= 0) {
                    long size = cursor.getLong(sizeIndex);
                    Log.i(TAG, "Size: " + size + " bytes");
                } else {
                    Log.e(TAG, "Column SIZE not found.");
                }

            } else {
                Log.e(TAG, "Cursor is empty or failed to move to the first row.");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error retrieving photo properties", e);
        }
    }


}
