package com.mediteam.meditime.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.mediteam.meditime.Helper.MedReminder;
import com.mediteam.meditime.Helper.RegisteredUsers;
import com.mediteam.meditime.R;

import org.w3c.dom.Text;

import java.util.Locale;

public class AddMed extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseUser firebaseUser;
    private DatabaseReference databaseReference;
    private TextView everydaySetTime, customSetTime, errorText;
    private EditText mediName, everydayPill, customPill;
    private ImageButton bckHome, saveMedi;
    private Button tabletBtn, capsuleBtn, everydayBtn, customDateBtn, addPill, minusPill;
    private Spinner selectTube;
    ArrayAdapter<CharSequence> adapter;
    private String tubeSelected, dosForm, schedule;
    private boolean tabletSelected = false, capsuleSelected = false, everydaySelected = false, customSelected = false;
    private int pillQuantity = 0;

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
        everydayBtn = findViewById(R.id.everyday);
        customDateBtn =findViewById(R.id.customDate);
        everydayPill = findViewById(R.id.setPill);
        addPill = findViewById(R.id.pill_plus);
        mediName = findViewById(R.id.etMedication);
        minusPill = findViewById(R.id.pill_minus);
        everydaySetTime = findViewById(R.id.everyday_set_time);
        customSetTime = findViewById(R.id.custom_time);
        errorText = findViewById(R.id.add_med_error);

        selectTube = findViewById(R.id.tubeSelect);
        adapter = ArrayAdapter.createFromResource(this, R.array.tube, R.layout.selected_spinner_item);
        adapter.setDropDownViewResource(R.layout.spinner_list);
        selectTube.setAdapter(adapter);
        selectTube.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected (AdapterView<?> parent, View view, int pos, long l) {
                tubeSelected = parent.getItemAtPosition(pos).toString();
            }

            @Override
            public void onNothingSelected (AdapterView<?> adapterView) {

            }
        });

        saveMedi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View view) {
                if(!validateFields()){

                } else{
                    saveMedSchedule();
                }
            }
        });

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
                tabletBtn.setTextColor(AddMed.this.getResources().getColor(R.color.dark10));
                capsuleBtn.setBackgroundResource(R.drawable.add_med_button_normal);
                tabletSelected = true;
                capsuleSelected = false;
                dosForm = tabletBtn.getText().toString();
            }
        });

        capsuleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View view) {
                capsuleBtn.setBackgroundResource(R.drawable.add_med_button_onclick);
                capsuleBtn.setTextColor(AddMed.this.getResources().getColor(R.color.dark10));
                tabletBtn.setBackgroundResource(R.drawable.add_med_button_normal);
                capsuleSelected = true;
                tabletSelected = false;
                dosForm = capsuleBtn.getText().toString();
            }
        });

        innitMiscellaneous();

    }

    private Boolean validateFields(){
        String pillName = mediName.getText().toString();

        if(pillName.isEmpty()){
            errorText.setVisibility(View.VISIBLE);
            return false;
        } else if(!tabletSelected && !capsuleSelected){
            errorText.setVisibility(View.VISIBLE);
            return false;
        } else if(!everydaySelected && !customSelected){
            errorText.setVisibility(View.VISIBLE);
            return false;
        } else if(tubeSelected == null) {
            errorText.setVisibility(View.VISIBLE);
            return false;
        } else return true;
    }

    private void saveMedSchedule () {
        String medTitle = mediName.getText().toString();
        MedReminder medReminder = new MedReminder();
        medReminder.setMedicine(medTitle);
        medReminder.setTubeSelection(tubeSelected);
        medReminder.setPillForm(dosForm);
        medReminder.setSchedule(schedule);
        medReminder.setUserid(firebaseUser.getUid());

        databaseReference = FirebaseDatabase.getInstance().getReference("MedRemind");
        String key = databaseReference.child("MedReminder").push().getKey();
        
        databaseReference.child(key).setValue(medReminder).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete (@NonNull Task<Void> task) {
                Toast.makeText(AddMed.this, "Med Reminder added successfully",
                        Toast.LENGTH_SHORT).show();

                Intent intent=new Intent(AddMed.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK
                        | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }
        });
    }

    private void innitMiscellaneous () {
        final LinearLayout everydaySched =findViewById(R.id.everyday_sched_misc);
        final BottomSheetBehavior<LinearLayout> bottomSheetBehavior_everyday = BottomSheetBehavior.from(everydaySched);
        final LinearLayout customSched =findViewById(R.id.custom_sched_misc);
        final BottomSheetBehavior<LinearLayout> bottomSheetBehavior_custom = BottomSheetBehavior.from(customSched);

        String setPill = everydayPill.getText().toString();

        //everyday sched events

        everydayBtn.setOnClickListener(new View.OnClickListener() {
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
                everydayBtn.setBackgroundResource(R.drawable.add_med_button_onclick);
                everydayBtn.setTextColor(AddMed.this.getResources().getColor(R.color.dark10));
                customDateBtn.setBackgroundResource(R.drawable.add_med_button_normal);
                everydaySelected = true;
                customSelected = false;
                schedule = everydayBtn.getText().toString();

                bottomSheetBehavior_everyday.setState(BottomSheetBehavior.STATE_COLLAPSED);
//                if(setPill.equals("0")){
//                    Toast.makeText(AddMed.this, "Set an appropriate amount of pills to consume", Toast.LENGTH_LONG).show();
//                } else {
//
//                }
            }
        });

        everydaySched.findViewById(R.id.new_time_sched).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View view) {
                ScrollView scrollView = findViewById(R.id.everyday_scroll);

                LinearLayout everydayTable = findViewById(R.id.everyday_table);
                LinearLayout newEverydayRow = (LinearLayout) AddMed.this.getLayoutInflater().inflate(R.layout.everyday_item,null);
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                layoutParams.setMargins(0, 15, 0, 0);
                everydayTable.addView(newEverydayRow, layoutParams);

                scrollView.post(new Runnable() {
                    @Override
                    public void run () {
                        scrollView.fullScroll(View.FOCUS_DOWN);
                    }
                });
            }
        });

        everydaySetTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View view) {
                openEverydayDialog();
            }
        });

        //custom sched events

        //open
        customDateBtn.setOnClickListener(new View.OnClickListener() {
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

                customDateBtn.setBackgroundResource(R.drawable.add_med_button_onclick);
                customDateBtn.setTextColor(AddMed.this.getResources().getColor(R.color.dark10));
                everydayBtn.setBackgroundResource(R.drawable.add_med_button_normal);
                customSelected = true;
                everydaySelected = false;
                schedule = customDateBtn.getText().toString();

                bottomSheetBehavior_custom.setState(BottomSheetBehavior.STATE_COLLAPSED);
//                if(setPill.equals("0")){
//                    Toast.makeText(AddMed.this, "Set an appropriate amount of pills to consume", Toast.LENGTH_LONG).show();
//                } else {
//
//                }
            }
        });

        customSched.findViewById(R.id.new_custom_sched).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View view) {
                ScrollView scrollView = findViewById(R.id.custom_scroll);
                LinearLayout customTable = findViewById(R.id.custom_table);
                LinearLayout newCustomRow = (LinearLayout) AddMed.this.getLayoutInflater().inflate(R.layout.custom_item,null);
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                layoutParams.setMargins(0, 15, 0, 0);
                customTable.addView(newCustomRow, layoutParams);

                scrollView.post(new Runnable() {
                    @Override
                    public void run () {
                        scrollView.fullScroll(View.FOCUS_DOWN);
                    }
                });
            }
        });

        customSetTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View view) {
                openCustomDialog();
            }
        });
    }

    public void AddPill(View view){
        pillQuantity++;
        everydayPill.setText(String.valueOf(pillQuantity));
    }

    public void MinusPill(View view){
        pillQuantity--;
        everydayPill.setText(String.valueOf(pillQuantity));
    }

    private void openEverydayDialog(){
        TimePickerDialog timePicker = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet (TimePicker timePicker, int hours, int minutes) {
                String ampm;
                if (hours < 12) {
                    ampm = "AM";
                    if (hours == 0) {
                        hours = 12;  // midnight
                    }
                } else {
                    ampm = "PM";
                    hours = hours - 12;
                }
                everydaySetTime.setText(String.format(Locale.getDefault(), "%02d:%02d %s", hours, minutes, ampm));
            }
        }, 12, 00, false);

        timePicker.show();
    }

    private void openCustomDialog(){
        TimePickerDialog timePicker = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet (TimePicker timePicker, int hours, int minutes) {
                String ampm;
                if (hours < 12) {
                    ampm = "AM";
                    if (hours == 0) {
                        hours = 12;  // midnight
                    }
                } else {
                    ampm = "PM";
                    hours = hours - 12;
                }
                customSetTime.setText(String.format(Locale.getDefault(), "%02d:%02d %s", hours, minutes, ampm));
            }
        }, 12, 00, false);

        timePicker.show();
    }
}