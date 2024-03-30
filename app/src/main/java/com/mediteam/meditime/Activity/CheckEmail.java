package com.mediteam.meditime.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.ActionCodeSettings;
import com.google.firebase.auth.FirebaseAuth;
import com.mediteam.meditime.R;

public class CheckEmail extends AppCompatActivity {

    private Button getEmail;
    private ImageButton back;
    private EditText email;
    private TextView error;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_email);

        mAuth = FirebaseAuth.getInstance();

        email = findViewById(R.id.verEmail);
        getEmail = findViewById(R.id.getCode);
        back = findViewById(R.id.backLogin);
        error = findViewById(R.id.error_mess);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View view) {
                onBackPressed();
            }
        });

        getEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View view) {
                if(!validateField()){} else{
                    sendEmail();
                }
            }
        });

    }

    private void sendEmail () {
        String verEmail = email.getText().toString();
//        String passwordResetCode = generatePasswordResetCode();

        mAuth.sendPasswordResetEmail(verEmail)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete (@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(CheckEmail.this, "Password reset email sent", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(CheckEmail.this, Login.class));
                            finish();
                        } else {
                            Toast.makeText(CheckEmail.this, "Failed to send password reset email", Toast.LENGTH_SHORT).show();
                        }
                    }
        });
    }

    private String generatePasswordResetCode() {
        int code = (int) (Math.random() * 1000000); // Generate a random 6-digit code
        return String.valueOf(code);
    }

    private String getResetEmailWithCode(String code) {
        // Construct the password reset email with the reset code
        return "Click the following link to reset your password:\n\n"
                + "https://example.com/reset-password?code=" + code;
    }

    private boolean validateField () {
        String checkMail = email.getText().toString();

        if(checkMail.isEmpty()){
            error.setVisibility(View.VISIBLE);
            error.setText(R.string.error_empty);
            return false;
        } else if(!Patterns.EMAIL_ADDRESS.matcher(checkMail).matches()) {
            email.requestFocus();
            error.setVisibility(View.VISIBLE);
            error.setText(R.string.error_email);
            return false;
        } else return true;
    }
}