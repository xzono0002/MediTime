package com.mediteam.meditime.Activity;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.mediteam.meditime.R;

public class ChangePass extends AppCompatActivity {

    private EditText email, oldPass, newPass, conPass;
    private TextView error;
    private Button changePass;
    private ImageButton back;
    private ProgressBar progressBar;
    private String emailText, oldPassText, newPassText, conPassText;
    private ImageView lengthCard, upCard, lowCard, numCard, specCard;
    private boolean isAtLeast8 = false, hasUpperCase = false, hasLowerCase = false, hasNumber = false, hasSymbol = false;
    private FirebaseAuth mAuth;
    FirebaseUser firebaseUser;


    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_pass);

        mAuth = FirebaseAuth.getInstance();
        firebaseUser = mAuth.getCurrentUser();

        back = findViewById(R.id.changePass_back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View view) {
                onBackPressed();
            }
        });

        email = findViewById(R.id.changePass_email);
        oldPass = findViewById(R.id.changePass_oldPass);
        newPass = findViewById(R.id.changePass_newPass);
        conPass = findViewById(R.id.changePass_conPass);
        error = findViewById(R.id.changePass_error);

        lengthCard = findViewById(R.id.lengthChecker);
        upCard = findViewById(R.id.upChecker);
        lowCard = findViewById(R.id.lowChecker);
        numCard = findViewById(R.id.numChecker);
        specCard = findViewById(R.id.specChecker);

        lengthCard.getBackground().setAlpha(128);
        upCard.getBackground().setAlpha(128);
        lowCard.getBackground().setAlpha(128);
        numCard.getBackground().setAlpha(128);
        specCard.getBackground().setAlpha(128);

        progressBar = findViewById(R.id.changePass_progress);

        changePass = findViewById(R.id.changePass_btn);
        changePass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View view) {
                if(!validateField()){} else{
                    changePass.setVisibility(View.GONE);
                    progressBar.setVisibility(View.VISIBLE);
                    changePassword();
                }
            }
        });

        inputChange();
    }

    private void changePassword () {
        emailText = email.getText().toString().trim();
        oldPassText = oldPass.getText().toString().trim();
        newPassText = newPass.getText().toString().trim();

        AuthCredential credential = EmailAuthProvider.getCredential(emailText, oldPassText);
        firebaseUser.reauthenticate(credential).addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                firebaseUser.updatePassword(newPassText).addOnCompleteListener(updateTask ->{
                    changePass.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.GONE);
                    if(updateTask.isSuccessful()){
                        Toast.makeText(ChangePass.this, "Username updated sucessfully!", Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(ChangePass.this, ProfileFragment.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK
                                | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    } else {
                        error.setVisibility(View.VISIBLE);
                        error.setText("Failed to update password. Please try again!");
                    }
                });

            } else {
                changePass.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);

                error.setVisibility(View.VISIBLE);
                error.setText("Email or password is incorrect. Please try again!");
            }
        });
    }

    private Boolean validateField(){
        oldPassText = oldPass.getText().toString();
        newPassText = newPass.getText().toString();
        conPassText = conPass.getText().toString();
        emailText = email.getText().toString();

        if(emailText.isEmpty() || oldPassText.isEmpty() || newPassText.isEmpty() || conPassText.isEmpty()){
            error.setVisibility(View.VISIBLE);
            error.setText(R.string.error_empty);
            return false;
        } else if(!newPassText.equals(conPassText)) {
            error.setVisibility(View.VISIBLE);
            error.setText(R.string.error_password);
            return false;
        } else if(!Patterns.EMAIL_ADDRESS.matcher(emailText).matches()) {
            email.requestFocus();
            error.setVisibility(View.VISIBLE);
            error.setText(R.string.error_email);
            return false;
        } else if(isAtLeast8 == false | hasLowerCase == false | hasUpperCase == false | hasNumber == false | hasSymbol == false) {
            error.setVisibility(View.VISIBLE);
            error.setText(R.string.error_weak_pass);
            newPass.requestFocus();
            return false;
        }
        else {
            return true;
        }
    }

    private void checkPass(){
        String pass = newPass.getText().toString();

        // password length
        if(pass.length() >= 8){
            isAtLeast8 = true;
            lengthCard.getBackground().setAlpha(255);
        } else {
            isAtLeast8 = false;
            lengthCard.getBackground().setAlpha(128);
        }

        // check if password has upper case letter
        if(pass.matches("(.*[A-Z].*)")){
            hasUpperCase = true;
            upCard.getBackground().setAlpha(255);
        } else{
            hasUpperCase = false;
            upCard.getBackground().setAlpha(128);
        }

        // check if password has lower case letter
        if(pass.matches("(.*[a-z].*)")){
            hasLowerCase = true;
            lowCard.getBackground().setAlpha(255);
        } else{
            hasLowerCase = false;
            lowCard.getBackground().setAlpha(128);
        }

        // check number
        if(pass.matches("(.*[0-9].*)")){
            hasNumber = true;
            numCard.getBackground().setAlpha(255);
        } else{
            hasNumber = false;
            numCard.getBackground().setAlpha(128);
        }

        // check for symbol
        if(pass.matches("^(?=.*[_.()@#$%^&+=!? ]).*$")){
            hasSymbol = true;
            specCard.getBackground().setAlpha(255);
        } else{
            hasSymbol = false;
            specCard.getBackground().setAlpha(128);
        }
    }

    private void inputChange () {
        email.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged (CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged (CharSequence charSequence, int i, int i1, int i2) {
                checkPass();
            }

            @Override
            public void afterTextChanged (Editable editable) {

            }
        });

        newPass.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged (CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged (CharSequence charSequence, int i, int i1, int i2) {
                checkPass();
            }

            @Override
            public void afterTextChanged (Editable editable) {

            }
        });

        conPass.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged (CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged (CharSequence charSequence, int i, int i1, int i2) {
                checkPass();
            }

            @Override
            public void afterTextChanged (Editable editable) {

            }
        });
    }
}