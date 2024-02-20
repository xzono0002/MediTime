package com.mediteam.meditime.Activity;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

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
    private Button addMed;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

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
        initSchedMed();
        return  root;
    }

    private void initSchedMed () {
        reference = FirebaseDatabase.getInstance().getReference("MedRemind");
        binding.dashProgress.setVisibility(View.VISIBLE);
        ArrayList<MedReminder> list = new ArrayList<>();

        Query query = reference.orderByChild("userid").equalTo(firebaseUser.getUid());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange (@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    for (DataSnapshot issue : snapshot.getChildren()){
                        list.add(issue.getValue(MedReminder.class));
                    }

                    if (list.size() > 0){
                        binding.medView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
                        RecyclerView.Adapter adapter = new PillRemindAdapter(list);
                        binding.medView.setAdapter(adapter);
                    }

                    binding.dashProgress.setVisibility(View.GONE);
                }
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