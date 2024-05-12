package com.mediteam.meditime.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mediteam.meditime.Helper.RegisteredUsers;
import com.mediteam.meditime.R;

public class ChangeEmail extends AppCompatActivity {

    private ImageButton back;
    private Button changeEmail;
    private EditText oldEmail, newEmail, pass;
    private TextView error;
    private String oldEmailText, newEmailText, passText;
    private ProgressBar progressBar;
    private FirebaseAuth mauth;
    private DatabaseReference reference;
    FirebaseUser firebaseUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_email);

        mauth = FirebaseAuth.getInstance();
        firebaseUser = mauth.getCurrentUser();

        back = findViewById(R.id.changeEmail_back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View view) {
                onBackPressed();
            }
        });

        error = findViewById(R.id.changeEmail_error);
        oldEmail = findViewById(R.id.changeEmail_oldEmail);
        newEmail = findViewById(R.id.changeEmail_newEmail);
        pass = findViewById(R.id.changeEmail_pass);
        progressBar = findViewById(R.id.changeEmail_progress);

        changeEmail = findViewById(R.id.changeEmail_btn);
        changeEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View view) {
                if(!validateFields()){} else {
                    changeEmail.setVisibility(View.GONE);
                    progressBar.setVisibility(View.VISIBLE);
                    saveUpdate();
                }
            }
        });
    }

    private boolean validateFields () {
        oldEmailText = oldEmail.getText().toString();
        newEmailText = newEmail.getText().toString();
        passText = pass.getText().toString();

        if(oldEmailText.isEmpty() || newEmailText.isEmpty() || passText.isEmpty()){
            error.setVisibility(View.VISIBLE);
            error.setText("Fields can't be empty. Try again!");
            return false;
        } else if(!Patterns.EMAIL_ADDRESS.matcher(oldEmailText).matches()) {
            oldEmail.requestFocus();
            error.setVisibility(View.VISIBLE);
            error.setText(R.string.error_email);
            return false;
        } else if(!Patterns.EMAIL_ADDRESS.matcher(newEmailText).matches()) {
            newEmail.requestFocus();
            error.setVisibility(View.VISIBLE);
            error.setText(R.string.error_email);
            return false;
        } else return true;
    }

    private void saveUpdate () {
        oldEmailText = oldEmail.getText().toString().trim();
        newEmailText = newEmail.getText().toString().trim();
        passText = pass.getText().toString().trim();

        AuthCredential credential = EmailAuthProvider.getCredential(oldEmailText, passText);
        firebaseUser.reauthenticate(credential).addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                firebaseUser.verifyBeforeUpdateEmail(newEmailText).addOnCompleteListener(updateTask ->{
                    changeEmail.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.GONE);
                    if(updateTask.isSuccessful()){
                        AlertDialog.Builder builder = new AlertDialog.Builder(ChangeEmail.this);
                        View dialogView = getLayoutInflater().inflate(R.layout.change_email_alert, null);
                        builder.setView(dialogView);

                        Button confirmBTN = dialogView.findViewById(R.id.email_alert_close);

                        AlertDialog alertDialog = builder.create();
                        alertDialog.show();

                        confirmBTN.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick (View view) {
                                Intent intent = new Intent(Intent.ACTION_MAIN);
                                intent.addCategory(Intent.CATEGORY_APP_EMAIL);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            }
                        });
                    } else {
                        error.setVisibility(View.VISIBLE);
                        error.setText("Failed to update email address. Please try again!");
                    }
                });

            } else {
                changeEmail.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);

                error.setVisibility(View.VISIBLE);
                error.setText("Email or password is incorrect. Please try again!");
            }
        });
    }
}