package com.mediteam.meditime.Activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mediteam.meditime.Helper.RegisteredUsers;
import com.mediteam.meditime.R;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileFragment extends Fragment {

    private FirebaseAuth mAuth;
    FirebaseUser firebaseUser;
    private DatabaseReference reference;
    private TextView userFN, userUN;
    private SwitchCompat switchVibrate, switchSound;
    private CircleImageView userDP;
    private LinearLayout profileProgress;
    private ProgressBar progressBar;
    private Button changeUN, changeEmail, changePass, changePin, delAcc;


    @Override
    public View onCreateView (LayoutInflater inflater, ViewGroup container,
                              Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_profile, container, false);

        mAuth = FirebaseAuth.getInstance();
        firebaseUser= mAuth.getCurrentUser();
        displayUser();

        userFN = root.findViewById(R.id.userFN);
        userUN = root.findViewById(R.id.userUN);
        userDP = root.findViewById(R.id.userDP);
        changeUN = root.findViewById(R.id.changeUN);
        changeEmail = root.findViewById(R.id.changeEmail);
        changePass = root.findViewById(R.id.changePass);
        changePin = root.findViewById(R.id.changePin);
        delAcc = root.findViewById(R.id.DeleteAcc);
        progressBar = root.findViewById(R.id.profileProgress);
        profileProgress = root.findViewById(R.id.profileContainer);
        switchVibrate = root.findViewById(R.id.vibrate);
        switchSound = root.findViewById(R.id.sound);

        userDP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View view) {
                Intent intent = new Intent(getActivity(), ChangeProfileDP.class);
                startActivity(intent);
            }
        });

        setVariable();
        setPreference();
        loadPreference();
        return root;
    }

    private void loadPreference () {
        String userID = firebaseUser.getUid();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("UserPreference").child(userID);

        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange (@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String vibrateValue = snapshot.child("vibrate").getValue(String.class);
                    String soundValue = snapshot.child("sound").getValue(String.class);

                    // Update switch states based on database values
                    if (vibrateValue != null && vibrateValue.equals("true")) {
                        switchVibrate.setChecked(true);
                    }

                    if (soundValue != null && soundValue.equals("true")) {
                        switchSound.setChecked(true);
                    }
                }
            }

            @Override
            public void onCancelled (@NonNull DatabaseError error) {

            }
        });
    }

    private void setPreference () {
        String userID = firebaseUser.getUid();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("UserPreference").child(userID);

        switchVibrate.setOnCheckedChangeListener((buttonView, isChecked) ->{
            reference.child("vibrate").setValue(isChecked ? "true" : "false");
        });

        switchSound.setOnCheckedChangeListener((buttonView, isChecked) ->{
            reference.child("sound").setValue(isChecked ? "true" : "false");
        });
    }

    private void setVariable () {

        changeUN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View view) {
                startActivity( new Intent(getActivity(), ChangeUsername.class) );
            }
        });

        changeEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View view) {
                startActivity(new Intent(getActivity(), ChangeEmail.class));
            }
        });

        changePass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View view) {
                startActivity(new Intent(getActivity(), ChangePass.class));
            }
        });

        changePin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View view) {
                startActivity(new Intent(getActivity(), ChangePin.class));
            }
        });

        delAcc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View view) {
                startActivity(new Intent(getActivity(), DeleteAcc.class));
            }
        });


    }

    public void displayUser(){
        String userID = firebaseUser.getUid();

        reference = FirebaseDatabase.getInstance().getReference("RegisteredUsers");
        reference.child(userID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange (@NonNull DataSnapshot snapshot) {
               RegisteredUsers registeredUsers = snapshot.getValue(RegisteredUsers.class);
                if(registeredUsers != null){
                    userFN.setText(firebaseUser.getDisplayName());
                    String unFromDB = registeredUsers.username;

                    userUN.setText("@" + unFromDB);
                    Uri uri = firebaseUser.getPhotoUrl();
                    Picasso.get()
                            .load(uri)
                            .placeholder(R.drawable.user_profile)
                            .resize(100, 100)
                            .centerCrop()
                            .noFade()
                            .into(userDP);

                } else {
                    Toast.makeText(getActivity(), "Something went wrong!",
                            Toast.LENGTH_SHORT).show();
                }
                //stop progress bar
                progressBar.setVisibility(View.GONE);
                profileProgress.setVisibility(View.VISIBLE);
            }

            @Override
            public void onCancelled (@NonNull DatabaseError error) {
                Toast.makeText(getActivity(), "Something went wrong!",
                        Toast.LENGTH_SHORT).show();
                //stop progress bar
                progressBar.setVisibility(View.GONE);
                profileProgress.setVisibility(View.VISIBLE);
            }
        });
    }
}