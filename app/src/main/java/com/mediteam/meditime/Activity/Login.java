package com.mediteam.meditime.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.mediteam.meditime.R;

public class Login extends AppCompatActivity {

    private Button signUp, logIn, forgotPass;
    private EditText loginEmail, loginPass;
    private TextView error;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;
    private static final String Tag = "Login";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        logIn = findViewById(R.id.logIn);
        signUp = findViewById(R.id.signupRedirect);
        error = findViewById(R.id.logError);
        forgotPass = findViewById(R.id.forgotPassword);

        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Login.this, AccountCreation.class));
                finish();
            }
        });

        loginEmail = findViewById(R.id.emailLogin);
        loginPass = findViewById(R.id.passwordLogin);
        progressBar = findViewById(R.id.loginProgress);

        logIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!validateField()){

                } else {
                    logIn.setVisibility(View.GONE);
                    progressBar.setVisibility(View.VISIBLE);
                    checkUser();
                }
            }
        });

        //open reset password module
        forgotPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View view) {
                startActivity(new Intent(Login.this, ForgotPass.class));
            }
        });
    }

    public Boolean validateField(){
        String email = loginEmail.getText().toString();
        String pass = loginPass.getText().toString();

        if(email.isEmpty() | pass.isEmpty()){
            error.setVisibility(View.VISIBLE);
            error.setText(R.string.error_empty);
            return false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            error.setVisibility(View.VISIBLE);
            error.setText(R.string.error_email);
            loginEmail.requestFocus();
            return false;
        }else{
            return true;
        }
    }

    public void checkUser(){
        String userEmail = loginEmail.getText().toString().trim();
        String userPass = loginPass.getText().toString().trim();

        mAuth.signInWithEmailAndPassword(userEmail, userPass).addOnCompleteListener(Login.this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete (@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){

                    logIn.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.GONE);

                    Toast.makeText(Login.this, "Login successfully!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(Login.this, PinModule.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK
                            | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                } else {
                    try {
                        throw task.getException();
                    } catch (FirebaseAuthInvalidUserException e){
                        error.setVisibility(View.VISIBLE);
                        error.setText(R.string.error_no_user);
                    } catch (FirebaseAuthInvalidCredentialsException e){
                        error.setVisibility(View.VISIBLE);
                        error.setText(R.string.error_login);
                    } catch (Exception e) {
                        Log.e(Tag, e.getMessage());
                        Toast.makeText(Login.this, e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                    logIn.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(Login.this, "Login failed. Please try again!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}