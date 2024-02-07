package com.mediteam.meditime;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.nfc.Tag;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class AccountCreation extends AppCompatActivity {

    private Pattern pattern;
    private Matcher matcher;
    private Button createAcc;
    private Button logAcc;
    private EditText signupName, signupEmail, signupUN, signupPass, signupPass2, signupPin;
    private ImageView lengthCard, upCard, lowCard, numCard, specCard;
    private TextView error;
    private ProgressBar progressBar;
    private DatabaseReference reference;
    private FirebaseAuth mAuth;
    private static final String Tag = "AccountCreation";
    private boolean isAtLeast8 = false, hasUpperCase = false, hasLowerCase = false, hasNumber = false, hasSymbol = false;

    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_creation);

        mAuth = FirebaseAuth.getInstance();

        createAcc = findViewById(R.id.signUp);
        logAcc = findViewById(R.id.loginRedirect);

        logAcc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View view) {
                startActivity(new Intent(AccountCreation.this, Login.class));
                finish();
            }
        });

        signupName = findViewById(R.id.name);
        signupEmail = findViewById(R.id.email);
        signupUN = findViewById(R.id.username);
        signupPass = findViewById(R.id.password);
        signupPass2 = findViewById(R.id.confirmPass);
        signupPin = findViewById(R.id.setPIN);
        error = findViewById(R.id.error_text);
        progressBar = findViewById(R.id.signupProgress);
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

        createAcc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View view) {

                String password = signupPass.getText().toString();
                String confirmPassword = signupPass2.getText().toString();

                if(!validateField()) {

                } else {
                    if(password.equals(confirmPassword)) {
                        createAcc.setVisibility(View.GONE);
                        progressBar.setVisibility(View.VISIBLE);

                        createAcc();

                    } else {
                        error.setVisibility(View.VISIBLE);
                        error.setText(R.string.error_password);
                    }
                }
            }
        });

        inputChange();
    }

    public void checkPassword(){
        String pass = signupPass.getText().toString();

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

    public Boolean validateField () {
        String name = signupName.getText().toString();
        String email = signupEmail.getText().toString();
        String username = signupUN.getText().toString();
        String pass = signupPass.getText().toString();
        String conPass = signupPass2.getText().toString();
        String pin = signupPin.getText().toString();

        if(name.isEmpty() | email.isEmpty() | username.isEmpty() | pass.isEmpty() | conPass.isEmpty() | pin.isEmpty()) {
            error.setVisibility(View.VISIBLE);
            error.setText(R.string.error_empty);
            return false;
        } else if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            signupEmail.requestFocus();
            error.setVisibility(View.VISIBLE);
            error.setText(R.string.error_email);
            return false;
        } else if(isAtLeast8 == false | hasLowerCase == false | hasUpperCase == false | hasNumber == false | hasSymbol == false) {
            error.setVisibility(View.VISIBLE);
            error.setText(R.string.error_weak_pass);
            signupPass.requestFocus();
            return false;
        } else return true;

    }

    public void createAcc () {
        String name = signupName.getText().toString();
        String email = signupEmail.getText().toString();
        String username = signupUN.getText().toString();
        String password = signupPass.getText().toString();
        String pin = signupPin.getText().toString();

        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete (@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()) {

                    FirebaseUser firebaseUser=mAuth.getCurrentUser();

                    UserProfileChangeRequest profileChangeRequest = new UserProfileChangeRequest.Builder().setDisplayName(name).build();
                    firebaseUser.updateProfile(profileChangeRequest);

                    HelperClass helperClass = new HelperClass(name, username, pin);

                    reference = FirebaseDatabase.getInstance().getReference("users");
                    reference.child(firebaseUser.getUid()).setValue(helperClass).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete (@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                firebaseUser.sendEmailVerification();

                                createAcc.setVisibility(View.VISIBLE);
                                progressBar.setVisibility(View.GONE);

                                Toast.makeText(AccountCreation.this, "Signup successful! Please verify your email address.",
                                        Toast.LENGTH_SHORT).show();

                                Intent intent=new Intent(AccountCreation.this, PinModule.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK
                                        | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                                finish();
                            } else{
                                Toast.makeText(AccountCreation.this, "Signup failed. Please try again!",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                } else {
                    try {
                        throw task.getException();
                    } catch(FirebaseAuthWeakPasswordException e) {
                        error.setVisibility(View.VISIBLE);
                        error.setText(R.string.error_weak_pass);
                        signupPass.requestFocus();
                    } catch(FirebaseAuthInvalidCredentialsException e) {
                        error.setVisibility(View.VISIBLE);
                        error.setText(R.string.error_email);
                        signupEmail.requestFocus();
                    } catch(FirebaseAuthUserCollisionException e) {
                        error.setVisibility(View.VISIBLE);
                        error.setText(R.string.error_user_exist);
                        signupEmail.requestFocus();
                    } catch(Exception e) {
                        Log.e(Tag, e.getMessage());
                        Toast.makeText(AccountCreation.this, e.getMessage(), Toast.LENGTH_LONG).show();
                    }

                    createAcc.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.GONE);
                }
            }
        });
    }

    public void inputChange(){
        signupName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged (CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged (CharSequence charSequence, int i, int i1, int i2) {
                checkPassword();
            }

            @Override
            public void afterTextChanged (Editable editable) {

            }
        });

        signupEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged (CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged (CharSequence charSequence, int i, int i1, int i2) {
                checkPassword();
            }

            @Override
            public void afterTextChanged (Editable editable) {

            }
        });

        signupUN.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged (CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged (CharSequence charSequence, int i, int i1, int i2) {
                checkPassword();
            }

            @Override
            public void afterTextChanged (Editable editable) {

            }
        });

        signupPass.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged (CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged (CharSequence charSequence, int i, int i1, int i2) {
                checkPassword();
            }

            @Override
            public void afterTextChanged (Editable editable) {

            }
        });

        signupPass2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged (CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged (CharSequence charSequence, int i, int i1, int i2) {
                checkPassword();
            }

            @Override
            public void afterTextChanged (Editable editable) {

            }
        });

        signupPin.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged (CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged (CharSequence charSequence, int i, int i1, int i2) {
                checkPassword();
            }

            @Override
            public void afterTextChanged (Editable editable) {

            }
        });
    }
}