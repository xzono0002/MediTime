package com.mediteam.meditime;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;

public class DashboardFragment extends Fragment {

    private FirebaseAuth mAuth;
    private FirebaseUser firebaseUser;
    private DatabaseReference reference;
    private TextView userGreet;
    private Button addMed;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_dashboard, container, false);

        mAuth = FirebaseAuth.getInstance();
        firebaseUser= mAuth.getCurrentUser();

        userGreet = root.findViewById(R.id.greetUser);
        addMed = root.findViewById(R.id.addMedic);

        addMed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View view) {
                Intent intent = new Intent(getActivity(), AddMed.class);
                startActivity(intent);
            }
        });

        DisplayUser();
        return  root;
    }

    private void DisplayUser () {
        String userName = firebaseUser.getDisplayName();
        String greet = "Good day, " + userName;

        userGreet.setText(greet);
    }
}