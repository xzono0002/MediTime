package com.mediteam.meditime.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mediteam.meditime.Helper.MedReminder;
import com.mediteam.meditime.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class AddMed extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseUser firebaseUser;
    private DatabaseReference databaseReference;
    private TextView everydaySetTime, customSetTime, errorText;
    private EditText mediName, everydayPill, customPill, pillOnTube, etNote;
    private ImageButton bckHome, saveMedi;
    private Button tabletBtn, capsuleBtn, everydayBtn, customDateBtn, addPill, minusPill;
    private Spinner selectTube;
    ArrayAdapter<String> adapter;
    ArrayList<String> assignedTubes = new ArrayList<>();
    private String tubeSelected, dosForm, schedule;
    private boolean tabletSelected = false, capsuleSelected = false, everydaySelected = false, customSelected = false;
    private int pillQuantity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_med);

        mAuth = FirebaseAuth.getInstance();
        firebaseUser = mAuth.getCurrentUser();

        bckHome = findViewById(R.id.backHome);
        saveMedi = findViewById(R.id.saveMedi);
        mediName = findViewById(R.id.etMedication);
        pillOnTube = findViewById(R.id.etPillsOnTube);
        etNote = findViewById(R.id.etNote);
        tabletBtn = findViewById(R.id.tablet);
        capsuleBtn = findViewById(R.id.capsule);
        everydayBtn = findViewById(R.id.everyday);
        customDateBtn =findViewById(R.id.customDate);
        customSetTime = findViewById(R.id.custom_time);
        errorText = findViewById(R.id.add_med_error);

        selectTube = findViewById(R.id.tubeSelect);

        databaseReference = FirebaseDatabase.getInstance().getReference("MedRemind");
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange (@NonNull DataSnapshot snapshot) {
                    if(snapshot.exists()){
                        for (DataSnapshot requestSnapshot : snapshot.getChildren()) {

                            // Check if the user ID matches the current user's ID
                            String userID = requestSnapshot.child("userid").getValue(String.class);
                            if (userID != null && userID.equals(firebaseUser.getUid())) {
                                // The userID matches the current user, proceed with processing the tube data
                                String tube = requestSnapshot.child("tubeSelection").getValue(String.class); // Get the tube name
                                if (tube != null) {
                                    assignedTubes.add(tube);
                                }
                            }
                        }
                        updateAdapter();
                    } else {
                        //if MedRemind has no value
                        updateAdapter();
                    }
            }

            @Override
            public void onCancelled (@NonNull DatabaseError error) {
                Log.e("DatabaseError", "Error retrieving data from the database: " + error.getMessage());
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

        setVariable();
        innitMiscellaneous();

    }

    private void updateAdapter () {
        // Add all tubes from the resource array to the non-assigned list
        String[] allTubes = getResources().getStringArray(R.array.tube);

        List<String> updatedTubeList = new ArrayList<>(Arrays.asList(allTubes));
        updatedTubeList.removeAll(assignedTubes);

        //convert list back to array
        String[] updatedTubesArray = updatedTubeList.toArray(new String[0]);

        //setup adapter for the spinner view
        adapter = new ArrayAdapter<>(AddMed.this, R.layout.selected_spinner_item, updatedTubesArray);
        adapter.setDropDownViewResource(R.layout.spinner_list);

        selectTube.setAdapter(adapter);
    }


    private void setVariable () {

        //when back button is clicked
        bckHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View view) {
                onBackPressed();
            }
        });

        //when spinner is clicked
        selectTube.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected (AdapterView<?> parent, View view, int pos, long l) {
                tubeSelected = parent.getItemAtPosition(pos).toString();
            }

            @Override
            public void onNothingSelected (AdapterView<?> adapterView) {

            }
        });

        //when tablet form is selected
        tabletBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View view) {
                tabletBtn.setBackgroundResource(R.drawable.add_med_button_onclick);
                tabletBtn.setTextColor(AddMed.this.getResources().getColor(R.color.dark10));
                capsuleBtn.setBackgroundResource(R.drawable.add_med_button_normal);
                capsuleBtn.setTextColor(AddMed.this.getResources().getColor(R.color.light));
                tabletSelected = true;
                capsuleSelected = false;
                dosForm = tabletBtn.getText().toString();
            }
        });

        //when capsule form is selected
        capsuleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View view) {
                capsuleBtn.setBackgroundResource(R.drawable.add_med_button_onclick);
                capsuleBtn.setTextColor(AddMed.this.getResources().getColor(R.color.dark10));
                tabletBtn.setBackgroundResource(R.drawable.add_med_button_normal);
                tabletBtn.setTextColor(AddMed.this.getResources().getColor(R.color.light));
                capsuleSelected = true;
                tabletSelected = false;
                dosForm = capsuleBtn.getText().toString();
            }
        });
    }

    private Boolean validateFields(){
        String pillName = mediName.getText().toString();
        String pillsOnTube = pillOnTube.getText().toString();
        String etNotes = etNote.getText().toString();
        String defaultText = "-- Select Available Tube --";

        if(pillName.isEmpty() || etNotes.isEmpty() || pillsOnTube.isEmpty()){
            errorText.setVisibility(View.VISIBLE);
            return false;
        } else if(pillsOnTube.equals("0")) {
            errorText.setVisibility(View.VISIBLE);
            return false;
        } else if(!tabletSelected && !capsuleSelected){
            errorText.setVisibility(View.VISIBLE);
            return false;
        } else if(!everydaySelected && !customSelected){
            errorText.setVisibility(View.VISIBLE);
            return false;
        } else if(tubeSelected.equals(defaultText)) {
            errorText.setVisibility(View.VISIBLE);
            return false;
        } else return true;
    }

    private void saveMedSchedule () {
        String medTitle = mediName.getText().toString();
        String pillsOntube = pillOnTube.getText().toString();
        String notes = etNote.getText().toString();
        MedReminder medReminder = new MedReminder();
        medReminder.setMedicine(medTitle);
        medReminder.setTubeSelection(tubeSelected);
        medReminder.setPillForm(dosForm);
        medReminder.setSchedule(schedule);
        medReminder.setUserid(firebaseUser.getUid());
        medReminder.setPillsOnTube(pillsOntube);
        medReminder.setNotes(notes);

        databaseReference = FirebaseDatabase.getInstance().getReference("MedRemind");
        String key = databaseReference.child("MedReminder").push().getKey();
        
        databaseReference.child(key).setValue(medReminder).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete (@NonNull Task<Void> task) {
                medReminder.setMedId(key);
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

        //everyday sched events

        //when everydayBTN is clicked
        everydayBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View view) {

                if (bottomSheetBehavior_everyday.getState() != BottomSheetBehavior.STATE_EXPANDED) {
                    if(bottomSheetBehavior_custom.getState() != BottomSheetBehavior.STATE_COLLAPSED){
                        bottomSheetBehavior_custom.setState(BottomSheetBehavior.STATE_COLLAPSED);
                    }
                    bottomSheetBehavior_everyday.setState(BottomSheetBehavior.STATE_EXPANDED);
                    addNewEverydayItem();
                } else {
                    bottomSheetBehavior_everyday.setState(BottomSheetBehavior.STATE_COLLAPSED);
                }
            }
        });

        //when okBTN is clicked
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
            }
        });

        //when the newItem is selected
        everydaySched.findViewById(R.id.new_everyday_sched).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View view) {
                addNewEverydayItem();
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
                    addNewCustomItem();
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
            }
        });

        customSched.findViewById(R.id.new_custom_sched).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View view) {
                addNewCustomItem();
            }
        });
    }


    private void addNewEverydayItem(){
        ScrollView scrollView = findViewById(R.id.everyday_scroll);
        LinearLayout everydayTable = findViewById(R.id.everyday_table);

        LinearLayout newEverydayRow = (LinearLayout) AddMed.this.getLayoutInflater().inflate(R.layout.everyday_item,null);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(0, 15, 0, 0);
        everydayTable.addView(newEverydayRow, layoutParams);


        // Set click listener for the TextView to open the TimePickerDialog
        everydaySetTime = newEverydayRow.findViewById(R.id.everyday_time);
        everydaySetTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TimePickerDialog timePicker = new TimePickerDialog(AddMed.this, new TimePickerDialog.OnTimeSetListener() {
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
        });

        //When addPill button is clicked, pill quantity of the instance increases
        addPill = newEverydayRow.findViewById(R.id.everyday_pill_plus);
        addPill.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View view) {
                everydayPill = newEverydayRow.findViewById(R.id.everydayPill);
                pillQuantity = Integer.parseInt(everydayPill.getText().toString());
                pillQuantity++;
                everydayPill.setText(String.valueOf(pillQuantity));
            }
        });

        //When minusPill button is clicked, pill quantity of the instance decreases
        minusPill = newEverydayRow.findViewById(R.id.everyday_pill_minus);
        minusPill.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View view) {
                everydayPill = newEverydayRow.findViewById(R.id.everydayPill);
                pillQuantity = Integer.parseInt(everydayPill.getText().toString());
                pillQuantity--;
                everydayPill.setText(String.valueOf(pillQuantity));
            }
        });

        // Add scroll behavior to scroll down to the newly added item
        scrollView.post(new Runnable() {
            @Override
            public void run () {
                scrollView.fullScroll(View.FOCUS_DOWN);
            }
        });
    }

    private void addNewCustomItem(){
        ScrollView scrollView = findViewById(R.id.custom_scroll);
        LinearLayout customTable = findViewById(R.id.custom_table);

        LinearLayout newCustomRow = (LinearLayout) AddMed.this.getLayoutInflater().inflate(R.layout.custom_item,null);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(0, 15, 0, 0);
        customTable.addView(newCustomRow, layoutParams);


        // Set click listener for the TextView to open the TimePickerDialog
        customSetTime = newCustomRow.findViewById(R.id.custom_time);
        customSetTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TimePickerDialog timePicker = new TimePickerDialog(AddMed.this, new TimePickerDialog.OnTimeSetListener() {
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
        });

        //When addPill button is clicked, pill quantity of the instance increases
        addPill = newCustomRow.findViewById(R.id.custom_pill_plus);
        addPill.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View view) {
                customPill = newCustomRow.findViewById(R.id.customPill);
                pillQuantity = Integer.parseInt(customPill.getText().toString());
                pillQuantity++;
                customPill.setText(String.valueOf(pillQuantity));
            }
        });

        //When minusPill button is clicked, pill quantity of the instance decreases
        minusPill = newCustomRow.findViewById(R.id.custom_pill_minus);
        minusPill.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View view) {
                customPill = newCustomRow.findViewById(R.id.customPill);
                pillQuantity = Integer.parseInt(customPill.getText().toString());
                pillQuantity--;
                customPill.setText(String.valueOf(pillQuantity));
            }
        });

        // Add scroll behavior to scroll down to the newly added item
        scrollView.post(new Runnable() {
            @Override
            public void run () {
                scrollView.fullScroll(View.FOCUS_DOWN);
            }
        });
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