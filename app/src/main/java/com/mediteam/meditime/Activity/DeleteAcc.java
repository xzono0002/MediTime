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

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
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

public class DeleteAcc extends AppCompatActivity {

    private ImageButton back;
    private Button delAcc;
    private EditText email, pass, pin;
    private String emailText, passText, pinText;
    private TextView error;
    private ProgressBar progressBar;
    private FirebaseAuth mauth;
    private DatabaseReference reference;
    FirebaseUser firebaseUser;

    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delete_acc);

        mauth = FirebaseAuth.getInstance();
        firebaseUser = mauth.getCurrentUser();

        back = findViewById(R.id.delAcc_back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View view) {
                onBackPressed();
            }
        });

        error = findViewById(R.id.delAcc_error);
        email = findViewById(R.id.delAcc_email);
        pass = findViewById(R.id.delAcc_pass);
        pin = findViewById(R.id.delAcc_pin);
        progressBar = findViewById(R.id.delAcc_progress);

        delAcc = findViewById(R.id.delAcc_btn);
        delAcc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View view) {
                if(!validateFields()){} else {
                    delAcc.setVisibility(View.GONE);
                    progressBar.setVisibility(View.VISIBLE);
                    saveUpdate();
                }
            }
        });

    }

    private boolean validateFields () {
        emailText = email.getText().toString();
        passText = pass.getText().toString();
        pinText = pin.getText().toString();

        if(emailText.isEmpty() || passText.isEmpty() || pinText.isEmpty()){
            error.setVisibility(View.VISIBLE);
            error.setText("Fields can't be empty. Try again!");
            return false;
        } else if(!Patterns.EMAIL_ADDRESS.matcher(emailText).matches()) {
            email.requestFocus();
            error.setVisibility(View.VISIBLE);
            error.setText(R.string.error_email);
            return false;
        } else return true;
    }

    private void saveUpdate(){
        String userID = firebaseUser.getUid();
        emailText = email.getText().toString().trim();
        passText = pass.getText().toString().trim();
        pinText = pin.getText().toString().trim();

        AuthCredential credential = EmailAuthProvider.getCredential(emailText, passText);
        firebaseUser.reauthenticate(credential).addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                reference = FirebaseDatabase.getInstance().getReference("RegisteredUsers").child(userID);
                reference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange (@NonNull DataSnapshot snapshot) {
                        String currentPIN = snapshot.child("pin").getValue(String.class);
                        if(pinText.equals(currentPIN)){

                            AlertDialog.Builder builder = new AlertDialog.Builder(DeleteAcc.this);
                            View dialogView = getLayoutInflater().inflate(R.layout.delete_account_alert, null);
                            builder.setView(dialogView);

                            Button confirmBTN = dialogView.findViewById(R.id.delAcc_delete);
                            Button cancelBTN = dialogView.findViewById(R.id.delAcc_cancel);

                            AlertDialog alertDialog = builder.create();
                            alertDialog.show();

                            delAcc.setVisibility(View.VISIBLE);
                            progressBar.setVisibility(View.GONE);

                            confirmBTN.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick (View view) {
                                    reference.removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess (Void unused) {
                                            //Deletion succesfull
                                            Toast.makeText(DeleteAcc.this, "Account deleted successfully", Toast.LENGTH_SHORT).show();
                                            Intent intent = new Intent(DeleteAcc.this, Login.class);
                                            startActivity(intent);
                                            finish();
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure (@NonNull Exception e) {
                                            //Deletion failed
                                            Toast.makeText(DeleteAcc.this, "Failed to delete item: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });

                                    alertDialog.dismiss();
                                }
                            });

                            cancelBTN.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick (View view) {
                                    alertDialog.dismiss();
                                }
                            });
                        } else {
                            Toast.makeText(DeleteAcc.this, "Something went wrong. Please try again!", Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onCancelled (@NonNull DatabaseError error) {
                        delAcc.setVisibility(View.VISIBLE);
                        progressBar.setVisibility(View.GONE);
                    }
                });
            } else {
                delAcc.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);

                error.setVisibility(View.VISIBLE);
                error.setText("Email or password is incorrect. Please try again!");
            }
        });
    }

}