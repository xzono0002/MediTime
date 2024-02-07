package com.mediteam.meditime;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.opengl.Visibility;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class PinModule extends AppCompatActivity {

    Button pressZero, pressOne, pressTwo, pressThree, pressFour, pressFive, pressSix, pressSeven,
    pressEight, pressNine, pressBack, forgotPass, signinRed;
    EditText pin;
    TextView userGreet;
    DatabaseReference reference;
    private FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pin_module);

        mAuth = FirebaseAuth.getInstance();

        pressZero = findViewById(R.id.zeroBTN);
        pressOne = findViewById(R.id.oneBTN);
        pressTwo = findViewById(R.id.twoBTN);
        pressThree = findViewById(R.id.threeBTN);
        pressFour = findViewById(R.id.fourBTN);
        pressFive = findViewById(R.id.fiveBTN);
        pressSix = findViewById(R.id.sixBTN);
        pressSeven = findViewById(R.id.sevenBTN);
        pressEight = findViewById(R.id.eightBTN);
        pressNine = findViewById(R.id.nineBTN);
        pressBack = findViewById(R.id.bckSpace);
        signinRed = findViewById(R.id.signinRedirect);
        pin = findViewById(R.id.pinText);
        userGreet = findViewById(R.id.userGreetings);

        showUserData();

        pin.setShowSoftInputOnFocus(false);
        pin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(getString(R.string.pin_hint).equals(pin.getText().toString())){
                    pin.setText("");
                }
            }
        });
    }

    private void updateText (String strToAdd){
        String oldStr = pin.getText().toString();
        int cursorPos = pin.getSelectionStart();
        String leftStr = oldStr.substring(0, cursorPos);
        String rightStr = oldStr.substring(cursorPos);

        if (getString(R.string.pin_hint).equals(pin.getText().toString())) {
            pin.setText(strToAdd);
            pin.setSelection(cursorPos + 1);
            validatePinLen();
        } else {
            pin.setText(String.format("%s%s%s", leftStr, strToAdd, rightStr));
            pin.setSelection(cursorPos + 1);
            validatePinLen();
        }
    }

    public void clickZero(View view){
        updateText("0");
    }

    public void clickOne(View view){
        updateText("1");
    }

    public void clickTwo(View view){
        updateText("2");
    }

    public void clickThree(View view){
        updateText("3");
    }

    public void clickFour(View view){
        updateText("4");
    }

    public void clickFive(View view){
        updateText("5");
    }

    public void clickSix(View view){
        updateText("6");
    }

    public void clickSeven(View view){
        updateText("7");
    }

    public void clickEight(View view){
        updateText("8");
    }

    public void clickNine(View view){
        updateText("9");
    }

    public void validatePinLen (){
        int textLen = pin.getText().length();
        int pinMaxLen = 4;
        String pin_hint = getString(R.string.pin_hint);

        if (textLen == 0){
            pressBack.setVisibility(View.INVISIBLE);
        } if (textLen >= 1) {
            pressBack.setVisibility(View.VISIBLE);
        } if (textLen == pinMaxLen){
            validateUser();
        }
    }

    public void bckSpace(View view){
        int cursorPos = pin.getSelectionStart();
        int textLen = pin.getText().length();

        if (cursorPos != 0 && textLen != 0){
            SpannableStringBuilder selection = (SpannableStringBuilder) pin.getText();

            selection.replace(cursorPos - 1, cursorPos, "");
            pin.setText(selection);
            pin.setSelection(cursorPos -1);

            validatePinLen();
        }
    }

    public void showUserData(){

        FirebaseUser firebaseUser= mAuth.getCurrentUser();
        String userID = firebaseUser.getUid();

        reference = FirebaseDatabase.getInstance().getReference("users");
        reference.child(userID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange (@NonNull DataSnapshot snapshot) {
                HelperClass helperClass = snapshot.getValue(HelperClass.class);
                if(helperClass != null){
                    String unFromDB = helperClass.username;

                    String greet = "Welcome, " + unFromDB + "!";
                    userGreet.setText(greet);
                }
            }

            @Override
            public void onCancelled (@NonNull DatabaseError error) {
                userGreet.setText("Something went wrong. Can't find user.");
            }
        });
    }

    public void validateUser(){

        FirebaseUser firebaseUser= mAuth.getCurrentUser();
        String userPin = pin.getText().toString();
        String userID =firebaseUser.getUid();

        reference = FirebaseDatabase.getInstance().getReference("users");
        reference.child(userID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                HelperClass helperClass = snapshot.getValue(HelperClass.class);
                if(helperClass != null){
                    String pinFromDB = helperClass.pin;
                    if (pinFromDB.equals(userPin)){
                        Intent intent = new Intent(PinModule.this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK
                                | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(PinModule.this, "You entered the wrong pin. Please try again!", Toast.LENGTH_LONG).show();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(PinModule.this, "Something went wrong. Please try again!", Toast.LENGTH_LONG).show();
            }
        });
    }

    public void logout(View view){
        Toast.makeText(PinModule.this, "Account Signed out. Redirecting to login page.", Toast.LENGTH_LONG).show();
        mAuth.signOut();
        Intent intent = new Intent(PinModule.this, Login.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK
                | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}