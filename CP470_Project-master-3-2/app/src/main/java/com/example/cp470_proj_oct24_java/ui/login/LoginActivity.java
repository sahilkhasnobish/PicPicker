package com.example.cp470_proj_oct24_java.ui.login;

import android.app.Activity;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import com.example.cp470_proj_oct24_java.MainActivity;
import com.example.cp470_proj_oct24_java.R;
import com.example.cp470_proj_oct24_java.ui.login.LoginViewModel;
import com.example.cp470_proj_oct24_java.ui.login.LoginViewModelFactory;
import com.example.cp470_proj_oct24_java.databinding.ActivityLoginBinding;
import com.google.firebase.auth.FirebaseAuth;


//todo - add shared preferneces for email

public class LoginActivity extends AppCompatActivity {

    private LoginViewModel loginViewModel;
    private ActivityLoginBinding binding;

    private static final String TAG= LoginActivity.class.getSimpleName();

    //store temporary user and password
    private static final String LOGIN_EMAIL="CP470@gmail.com";
    private static final String LOGIN_PASSWORD="password";

    private userpasswordDBHelper db;

    //FirebaseAuth mAuth = FirebaseAuth.getInstance();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG,"inside onCreate");

        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // initializes the DB, and saves our temporary user and password into the DB.
        // In further versions of this project, this will be modified to become a create login page.
        db = new userpasswordDBHelper(this);
        boolean insertLoginCredentials = db.insertLoginCredentials(LOGIN_EMAIL, LOGIN_PASSWORD);

        loginViewModel = new ViewModelProvider(this, new LoginViewModelFactory())
                .get(LoginViewModel.class);

        final EditText usernameEditText = binding.username;
        final EditText passwordEditText = binding.password;
        final Button loginButton = binding.login;
        final ProgressBar loadingProgressBar = binding.loading;




        loginViewModel.getLoginFormState().observe(this, new Observer<LoginFormState>() {
            @Override
            public void onChanged(@Nullable LoginFormState loginFormState) {
                if (loginFormState == null) {
                    return;
                }
                loginButton.setEnabled(loginFormState.isDataValid());
                if (loginFormState.getUsernameError() != null) {
                    usernameEditText.setError(getString(loginFormState.getUsernameError()));
                }
                if (loginFormState.getPasswordError() != null) {
                    passwordEditText.setError(getString(loginFormState.getPasswordError()));
                }
            }
        });


        loginViewModel.getLoginResult().observe(this, new Observer<LoginResult>() {
            @Override
            public void onChanged(@Nullable LoginResult loginResult) {
                if (loginResult == null) {
                    return;
                }
                loadingProgressBar.setVisibility(View.GONE);
                if (loginResult.getError() != null) {
                    showLoginFailed(loginResult.getError());
                }

                if (loginResult.getSuccess() != null) {




                    Log.d(TAG,"valid log in"); //it doesnt account for empty strings
                }
//                setResult(Activity.RESULT_OK);
//

            }
        });

        TextWatcher afterTextChangedListener = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // ignore
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // ignore
            }

            @Override
            public void afterTextChanged(Editable s) {
                loginViewModel.loginDataChanged(usernameEditText.getText().toString(),
                        passwordEditText.getText().toString());
            }
        };
        usernameEditText.addTextChangedListener(afterTextChangedListener);
        passwordEditText.addTextChangedListener(afterTextChangedListener);
        passwordEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    loginViewModel.login(usernameEditText.getText().toString(),
                            passwordEditText.getText().toString());
                }
                return false;
            }
        });



        //clicking the log in button - here is where we check the user name and password is ok
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String username_entered= usernameEditText.getText().toString();
                String password_entered = passwordEditText.getText().toString();

                //ensure user name and password fields are not empty, and compare the result with what is stored in the DB.
                if(!username_entered.isEmpty() && !password_entered.isEmpty() && db.validate_user(username_entered,password_entered)){


                    //ask user permission to photos before they can log in-  dialog box
                    AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this); //dialog box

                    builder.setTitle(R.string.access_photo_title)
                            .setMessage(R.string.access_photo_message)

                            .setPositiveButton(R.string.dialog_box_OK_message, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    // User clicked OK button

                                    loadingProgressBar.setVisibility(View.VISIBLE);
                                    loginViewModel.login(usernameEditText.getText().toString(), passwordEditText.getText().toString());

                                    //go to main activity
                                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                    startActivity(intent);

                                }
                            })

                            .setNegativeButton(R.string.dialog_box_cancel_message, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    // User cancelled the dialog

                                    printLong(getText(R.string.login_error_access).toString());


                                }
                            })
                            .show();
                }

                else{
                    printLong(getText(R.string.login_error_invalid_info).toString());

                }

            }
        });
    }

    private void showLoginFailed(@StringRes Integer errorString) {
        Toast.makeText(getApplicationContext(), errorString, Toast.LENGTH_SHORT).show();
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