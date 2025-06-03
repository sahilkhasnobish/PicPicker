package com.example.cp470_proj_oct24_java;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.cp470_proj_oct24_java.ui.login.LoginActivity;
//last updated : nov 30

/*
general flow of activities

login  ->main

main -> select photos --> display results

main<-> display results
 */



public class MainActivity extends AppCompatActivity {

    private static final String TAG= MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG,"inside onCreate");

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    //go to logout page
    public void clickLogout(View view){
        Log.i(TAG,"inside clickLogout");
        Intent intent = new Intent(this,LoginActivity.class);
        startActivity(intent);
    }


    //go to upload photos page //goes to select photos instead
    public void clickUploadPhotos(View view){
        Log.i(TAG,"inside clickUploadPhotos");
        //Intent intent = new Intent(this,UploadPhotosActivity.class);//skip asking for a number
        Intent intent = new Intent(this,SelectPhotosActivity.class);
        startActivity(intent);
    }

    //go to display photos page
    public void clickDisplayPhotos(View view){
        Log.i(TAG,"inside clickDisplayPhotos");
        Intent intent = new Intent(this,DisplayResultsActivity.class);
        startActivity(intent);
    }



    //print using a toast -short
    public void print(String str){
        Toast toast = Toast.makeText(this,str, Toast.LENGTH_SHORT);
        toast.show();

    }

    //print using a toast - long
    public void printLong(String str){
        Toast toast = Toast.makeText(this,str, Toast.LENGTH_LONG);
        toast.show();

    }
}