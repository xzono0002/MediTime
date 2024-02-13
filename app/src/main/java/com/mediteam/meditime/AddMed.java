package com.mediteam.meditime;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;

public class AddMed extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private FirebaseAuth mAuth;
    private FirebaseUser firebaseUser;
    private DatabaseReference databaseReference;
    private ImageButton bckHome, saveMedi;
    private Button tabletBtn, capsuleBtn;
    private Spinner selectTube;
    ArrayAdapter<CharSequence> adapter;
    private boolean tabletSelected = false, capsuleSelected = false, everydaySelected = false, customSelected = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_med);

        mAuth = FirebaseAuth.getInstance();
        firebaseUser = mAuth.getCurrentUser();

        bckHome = findViewById(R.id.backHome);
        saveMedi = findViewById(R.id.saveMedi);
        tabletBtn = findViewById(R.id.tablet);
        capsuleBtn = findViewById(R.id.capsule);

        selectTube = findViewById(R.id.tubeSelect);
        adapter = ArrayAdapter.createFromResource(this, R.array.tube, R.layout.selected_spinner_item);
        adapter.setDropDownViewResource(R.layout.spinner_list);
        selectTube.setAdapter(adapter);
        selectTube.setOnItemSelectedListener(this);

        bckHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View view) {
                onBackPressed();
            }
        });

        tabletBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View view) {
                tabletBtn.setBackgroundResource(R.drawable.add_med_button_onclick);
                capsuleBtn.setBackgroundResource(R.drawable.add_med_button_normal);
                tabletSelected = true;
                capsuleSelected = false;
            }
        });

        capsuleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View view) {
                capsuleBtn.setBackgroundResource(R.drawable.add_med_button_onclick);
                tabletBtn.setBackgroundResource(R.drawable.add_med_button_normal);
                capsuleSelected = true;
                tabletSelected = false;
            }
        });

        innitMiscellaneous();

    }

    private void innitMiscellaneous () {
        final LinearLayout everydaySched =findViewById(R.id.everyday_sched_misc);
        final BottomSheetBehavior<LinearLayout> bottomSheetBehavior_everyday = BottomSheetBehavior.from(everydaySched);
        final Button everyday =findViewById(R.id.everyday);
        final LinearLayout customSched =findViewById(R.id.custom_sched_misc);
        final BottomSheetBehavior<LinearLayout> bottomSheetBehavior_custom = BottomSheetBehavior.from(customSched);
        final Button customDay =findViewById(R.id.customDate);


        //everyday sched events

        everyday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View view) {

                if (bottomSheetBehavior_everyday.getState() != BottomSheetBehavior.STATE_EXPANDED) {
                    if(bottomSheetBehavior_custom.getState() != BottomSheetBehavior.STATE_COLLAPSED){
                        bottomSheetBehavior_custom.setState(BottomSheetBehavior.STATE_COLLAPSED);
                    }
                    bottomSheetBehavior_everyday.setState(BottomSheetBehavior.STATE_EXPANDED);
                } else {
                    bottomSheetBehavior_everyday.setState(BottomSheetBehavior.STATE_COLLAPSED);
                }
            }
        });

        everydaySched.findViewById(R.id.everyday_ok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View view) {
                everyday.setBackgroundResource(R.drawable.add_med_button_onclick);
                customDay.setBackgroundResource(R.drawable.add_med_button_normal);
                everydaySelected = true;
                customSelected = false;

                bottomSheetBehavior_everyday.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }
        });

        everydaySched.findViewById(R.id.new_time_sched).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View view) {
                LinearLayout everydayTable = findViewById(R.id.everyday_table);
                LinearLayout newEverydayRow = (LinearLayout) AddMed.this.getLayoutInflater().inflate(R.layout.everyday_item,null);
                everydayTable.addView(newEverydayRow);
            }
        });

        //custom sched events

        //open
        customDay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View view) {
                if (bottomSheetBehavior_custom.getState() != BottomSheetBehavior.STATE_EXPANDED) {
                    if(bottomSheetBehavior_everyday.getState() != BottomSheetBehavior.STATE_COLLAPSED){
                        bottomSheetBehavior_everyday.setState(BottomSheetBehavior.STATE_COLLAPSED);
                    }
                    bottomSheetBehavior_custom.setState(BottomSheetBehavior.STATE_EXPANDED);
                } else {
                    bottomSheetBehavior_custom.setState(BottomSheetBehavior.STATE_COLLAPSED);
                }
            }
        });

        customSched.findViewById(R.id.custom_ok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View view) {
                customDay.setBackgroundResource(R.drawable.add_med_button_onclick);
                everyday.setBackgroundResource(R.drawable.add_med_button_normal);
                customSelected = true;
                everydaySelected = false;

                bottomSheetBehavior_custom.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }
        });

        customSched.findViewById(R.id.new_custom_sched).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View view) {
                LinearLayout customTable = findViewById(R.id.custom_table);
                LinearLayout newCustomRow = (LinearLayout) AddMed.this.getLayoutInflater().inflate(R.layout.custom_item,null);
                customTable.addView(newCustomRow);
            }
        });
    }

    @Override
    public void onItemSelected (AdapterView<?> adapterView, View view, int i, long l) {

    }

    @Override
    public void onNothingSelected (AdapterView<?> adapterView) {

    }
}