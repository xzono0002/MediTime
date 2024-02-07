package com.mediteam.meditime;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.io.FileReader;

public class ProfileFragment extends Fragment {

    private FirebaseAuth mAuth;
    FirebaseUser firebaseUser;
    private DatabaseReference reference;
    private TextView userFN, userUN;
    private ImageView userDP;
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

        userDP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View view) {
                Intent intent = new Intent(getActivity(), ChangeProfileDP.class);
                startActivity(intent);
            }
        });

        return root;
    }

    public void displayUser(){
        String userID = firebaseUser.getUid();

        reference = FirebaseDatabase.getInstance().getReference("users");
        reference.child(userID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange (@NonNull DataSnapshot snapshot) {
                HelperClass helperClass = snapshot.getValue(HelperClass.class);
                if(helperClass != null){
                    userFN.setText(firebaseUser.getDisplayName());
                    String unFromDB = helperClass.username;

                    userUN.setText("@" + unFromDB);
                    Uri uri = firebaseUser.getPhotoUrl();
                    Picasso.get()
                            .load(uri)
                            .placeholder(R.drawable.user_profile)
                            .resize(100, 100)
                            .centerCrop()
                            .into(userDP);
                } else {
                    Toast.makeText(getActivity(), "Something went wrong!",
                            Toast.LENGTH_SHORT).show();
                }
                //stop progress bar
            }

            @Override
            public void onCancelled (@NonNull DatabaseError error) {
                Toast.makeText(getActivity(), "Something went wrong!",
                        Toast.LENGTH_SHORT).show();
                //stop progress bar
            }
        });
    }
}