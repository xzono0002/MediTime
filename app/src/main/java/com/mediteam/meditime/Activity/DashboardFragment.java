package com.mediteam.meditime.Activity;

import static android.content.Intent.getIntent;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.mediteam.meditime.Activity.AddMed;
import com.mediteam.meditime.Adapter.PillRemindAdapter;
import com.mediteam.meditime.Helper.MedReminder;
import com.mediteam.meditime.R;
import com.mediteam.meditime.databinding.ActivityMainBinding;
import com.mediteam.meditime.databinding.FragmentDashboardBinding;

import java.util.ArrayList;

public class DashboardFragment extends Fragment {
    private FragmentDashboardBinding binding;
    private FirebaseAuth mAuth;
    private FirebaseUser firebaseUser;
    private DatabaseReference reference;
    private TextView userGreet;
    private FloatingActionButton addMed;
    private ProgressBar progressBar;
    private RecyclerView insideMedi, outsideMedi;
    private SwipeRefreshLayout swipeContainer;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        mAuth = FirebaseAuth.getInstance();
        firebaseUser= mAuth.getCurrentUser();

        userGreet = root.findViewById(R.id.greetUser);
        addMed = root.findViewById(R.id.addMedic);
        progressBar = root.findViewById(R.id.dashProgress);
        insideMedi = root.findViewById(R.id.medView);

        addMed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View view) {
                Intent intent = new Intent(getActivity(), AddMed.class);
                startActivity(intent);
            }
        });

        CheckForEmailVerified(firebaseUser);

        DisplayUser();
        initSchedMed();

        return root;

    }

    private void CheckForEmailVerified (FirebaseUser user) {
        if(!user.isEmailVerified()){
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            View dialogView = getLayoutInflater().inflate(R.layout.verify_alert, null);
            builder.setView(dialogView);

            Button confirmBTN = dialogView.findViewById(R.id.close_verify);
            TextView sendEmail = dialogView.findViewById(R.id.resend_email);

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

            sendEmail.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick (View view) {
                    user.sendEmailVerification();
                    Intent intent = new Intent(Intent.ACTION_MAIN);
                    intent.addCategory(Intent.CATEGORY_APP_EMAIL);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }
            });
        }
    }

    private void initSchedMed () {
        reference = FirebaseDatabase.getInstance().getReference("MedRemind");
        progressBar.setVisibility(View.VISIBLE);
        ArrayList<MedReminder> list = new ArrayList<>();

        Query query = reference.orderByChild("userid").equalTo(firebaseUser.getUid());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange (@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    for (DataSnapshot issue : snapshot.getChildren()){
                        MedReminder medReminder = issue.getValue(MedReminder.class);
                        list.add(medReminder);
                    }

                    if (list.size() > 0){
                        insideMedi.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
                        RecyclerView.Adapter adapter = new PillRemindAdapter(list);
                        insideMedi.setAdapter(adapter);
                    }

                    progressBar.setVisibility(View.GONE);
                }
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled (@NonNull DatabaseError error) {

            }
        });

    }

    private void DisplayUser () {
        String userName = firebaseUser.getDisplayName();
        String greet = "Good day, " + userName;

        userGreet.setText(greet);
    }
}