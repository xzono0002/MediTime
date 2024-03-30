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
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

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

public class ChangePin extends AppCompatActivity {

    private ImageButton back;
    private Button changePIN;
    private EditText email, pass, oldPin, newPin, conPin;
    private String emailText, passText, oldPinText, newPinText, conPinText;
    private TextView error;
    private ProgressBar progressBar;
    private FirebaseAuth mauth;
    private DatabaseReference reference;
    FirebaseUser firebaseUser;

    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_pin);

        mauth = FirebaseAuth.getInstance();
        firebaseUser = mauth.getCurrentUser();

        back = findViewById(R.id.changePin_back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View view) {
                onBackPressed();
            }
        });

        error = findViewById(R.id.changePin_error);
        email = findViewById(R.id.changePin_email);
        pass = findViewById(R.id.changePin_pass);
        oldPin = findViewById(R.id.changePin_oldPin);
        newPin = findViewById(R.id.changePin_newPin);
        conPin = findViewById(R.id.changePin_conPin);
        progressBar = findViewById(R.id.changePin_progress);

        changePIN = findViewById(R.id.changePin_btn);
        changePIN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View view) {
                if(!validateFields()){} else {
                    changePIN.setVisibility(View.GONE);
                    progressBar.setVisibility(View.VISIBLE);
                    saveUpdate();
                }
            }
        });
    }

    private boolean validateFields () {
        emailText = email.getText().toString();
        passText = pass.getText().toString();
        oldPinText = oldPin.getText().toString();
        newPinText = newPin.getText().toString();
        conPinText = conPin.getText().toString();

        if(emailText.isEmpty() || passText.isEmpty() || oldPinText.isEmpty() || newPinText.isEmpty() || conPinText.isEmpty()){
            error.setVisibility(View.VISIBLE);
            error.setText("Fields can't be empty. Try again!");
            return false;
        } else if(!Patterns.EMAIL_ADDRESS.matcher(emailText).matches()) {
            email.requestFocus();
            error.setVisibility(View.VISIBLE);
            error.setText(R.string.error_email);
            return false;
        } else if(!newPinText.equals(conPinText)) {
            error.setVisibility(View.VISIBLE);
            error.setText("* Pin doesn't match. Please try again!");
            return false;
        } else return true;
    }

    private void saveUpdate(){
        String userID = firebaseUser.getUid();
        emailText = email.getText().toString().trim();
        passText = pass.getText().toString().trim();
        oldPinText = oldPin.getText().toString().trim();
        newPinText = newPin.getText().toString().trim();

        AuthCredential credential = EmailAuthProvider.getCredential(emailText, passText);
        firebaseUser.reauthenticate(credential).addOnCompleteListener(task -> {
           if(task.isSuccessful()){
               reference = FirebaseDatabase.getInstance().getReference("RegisteredUsers").child(userID);
               reference.addListenerForSingleValueEvent(new ValueEventListener() {
                   @Override
                   public void onDataChange (@NonNull DataSnapshot snapshot) {
                       String currentPIN = snapshot.child("pin").getValue(String.class);
                       if(oldPinText.equals(currentPIN)){
                           reference.child("pin").setValue(newPinText).addOnCompleteListener(task1 -> {
                              if(task1.isSuccessful()){
                                  RegisteredUsers registeredUsers = new RegisteredUsers();
                                  registeredUsers.setPin(newPinText);

                                  changePIN.setVisibility(View.VISIBLE);
                                  progressBar.setVisibility(View.GONE);

                                  Toast.makeText(ChangePin.this, "Username updated sucessfully!", Toast.LENGTH_LONG).show();
                                  Intent intent = new Intent(ChangePin.this, ProfileFragment.class);
                                  intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK
                                          | Intent.FLAG_ACTIVITY_NEW_TASK);
                                  startActivity(intent);
                                  finish();
                              } else {
                                  Toast.makeText(ChangePin.this, "Something went wrong. Please try again!", Toast.LENGTH_LONG).show();
                              }
                           });
                       } else {
                           Toast.makeText(ChangePin.this, "Something went wrong. Please try again!", Toast.LENGTH_LONG).show();
                       }
                   }

                   @Override
                   public void onCancelled (@NonNull DatabaseError error) {
                       changePIN.setVisibility(View.VISIBLE);
                       progressBar.setVisibility(View.GONE);
                   }
               });
           } else {
               changePIN.setVisibility(View.VISIBLE);
               progressBar.setVisibility(View.GONE);

               error.setVisibility(View.VISIBLE);
               error.setText("Email or password is incorrect. Please try again!");
           }
        });
    }
}