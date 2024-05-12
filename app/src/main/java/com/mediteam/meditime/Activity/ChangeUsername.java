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
import com.google.firebase.auth.EmailAuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mediteam.meditime.Helper.MedReminder;
import com.mediteam.meditime.Helper.RegisteredUsers;
import com.mediteam.meditime.R;
import com.mediteam.meditime.databinding.ActivityChangeUsernameBinding;

public class ChangeUsername extends AppCompatActivity {

    private ImageButton back;
    private Button changeUN;
    private EditText email, oldUN, newUN, pass;
    private String emailText, oldUNText, newUNText, passText;
    private TextView error;
    private ProgressBar progressBar;
    private FirebaseAuth mauth;
    private DatabaseReference reference;
    FirebaseUser firebaseUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_username);

        mauth = FirebaseAuth.getInstance();
        firebaseUser = mauth.getCurrentUser();

        back = findViewById(R.id.changeUN_back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View view) {
                onBackPressed();
            }
        });

        error = findViewById(R.id.changeUN_error);
        email = findViewById(R.id.changeUN_email);
        oldUN = findViewById(R.id.changeUN_oldUN);
        newUN = findViewById(R.id.changeUN_newUN);
        pass = findViewById(R.id.changeUN_pass);
        progressBar = findViewById(R.id.changeUN_progress);

        changeUN = findViewById(R.id.changeUN_btn);
        changeUN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View view) {
                if(!validateFields()){} else {
                    changeUN.setVisibility(View.GONE);
                    progressBar.setVisibility(View.VISIBLE);
                    saveUpdate();
                }
            }
        });
    }

    private boolean validateFields () {
        emailText = email.getText().toString();
        oldUNText = oldUN.getText().toString();
        newUNText = newUN.getText().toString();
        passText = pass.getText().toString();

        if(emailText.isEmpty() || oldUNText.isEmpty() || newUNText.isEmpty() || passText.isEmpty()){
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

    private void saveUpdate () {
        String userID = firebaseUser.getUid();
        emailText = email.getText().toString().trim();
        passText = pass.getText().toString().trim();
        oldUNText = oldUN.getText().toString().trim();
        newUNText = newUN.getText().toString().trim();

        AuthCredential credential = EmailAuthProvider.getCredential(emailText, passText);
        firebaseUser.reauthenticate(credential).addOnCompleteListener(task -> {
           if(task.isSuccessful()){
               reference = FirebaseDatabase.getInstance().getReference("RegisteredUsers").child(userID);
               reference.addListenerForSingleValueEvent(new ValueEventListener() {
                   @Override
                   public void onDataChange (@NonNull DataSnapshot snapshot) {
                       String currentUN = snapshot.child("username").getValue(String.class);
                       if(oldUNText.equals(currentUN)){
                           reference.child("username").setValue(newUNText).addOnCompleteListener(task1 -> {
                               if(task1.isSuccessful()){
                                   RegisteredUsers registeredUsers = new RegisteredUsers();
                                   registeredUsers.setUsername(newUNText);

                                   changeUN.setVisibility(View.VISIBLE);
                                   progressBar.setVisibility(View.GONE);

                                   Toast.makeText(ChangeUsername.this, "Username updated sucessfully!", Toast.LENGTH_LONG).show();
                                   Intent intent = new Intent(ChangeUsername.this, ProfileFragment.class);
                                   intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK
                                           | Intent.FLAG_ACTIVITY_NEW_TASK);
                                   startActivity(intent);
                                   finish();
                               } else {
                                   Toast.makeText(ChangeUsername.this, "Something went wrong. Please try again!", Toast.LENGTH_LONG).show();
                               }
                           });
                       } else {
                           changeUN.setVisibility(View.VISIBLE);
                           progressBar.setVisibility(View.GONE);

                           oldUN.requestFocus();
                           Toast.makeText(ChangeUsername.this, "You entered the wrong username. Please try again!", Toast.LENGTH_LONG).show();
                       }
                   }

                   @Override
                   public void onCancelled (@NonNull DatabaseError error) {
                       changeUN.setVisibility(View.VISIBLE);
                       progressBar.setVisibility(View.GONE);
                   }
               });
           } else {
               changeUN.setVisibility(View.VISIBLE);
               progressBar.setVisibility(View.GONE);

               error.setVisibility(View.VISIBLE);
               error.setText("Email or password is incorrect. Please try again!");
           }
        });
    }
}