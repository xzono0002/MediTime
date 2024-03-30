package com.mediteam.meditime.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
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
import com.mediteam.meditime.Helper.AlarmReceiver;
import com.mediteam.meditime.Helper.MedReminder;
import com.mediteam.meditime.Helper.ScheduleItem;
import com.mediteam.meditime.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class AddMed extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseUser firebaseUser;
    private DatabaseReference databaseReference;
    private TextView errorText;
    private EditText mediName, pillOnTube, etNote;
    private ImageButton bckHome, saveMedi;
    private Button tabletBtn, capsuleBtn, everydayBtn, customDateBtn, addPill, minusPill, inMedi, outMedi;
    private Spinner selectTube;
    private ProgressBar progressBar;
    ArrayAdapter<String> adapter, adapterDays;
    ArrayList<String> assignedTubes = new ArrayList<>();
    private String tubeSelected, dosForm, schedule, day, time, key;
    private int pills;
    private boolean tabletSelected = false, capsuleSelected = false, everydaySelected = false, customSelected = false, insideMedi = true, outsideMedi = false;
    MedReminder medReminder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_med);

        mAuth = FirebaseAuth.getInstance();
        firebaseUser = mAuth.getCurrentUser();

        medReminder = new MedReminder();

        bckHome = findViewById(R.id.backHome);
        saveMedi = findViewById(R.id.saveMedi);
        mediName = findViewById(R.id.etMedication);
        pillOnTube = findViewById(R.id.etPillsOnTube);
        etNote = findViewById(R.id.etNote);
        tabletBtn = findViewById(R.id.tablet);
        capsuleBtn = findViewById(R.id.capsule);
        everydayBtn = findViewById(R.id.everyday);
        customDateBtn =findViewById(R.id.customDate);
        errorText = findViewById(R.id.add_med_error);
        inMedi = findViewById(R.id.in_medi);
        outMedi = findViewById(R.id.out_medi);
        progressBar = findViewById(R.id.progressBar);

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
                    progressBar.bringToFront();
                    progressBar.setVisibility(View.VISIBLE);
                    saveMedi.setEnabled(false);
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
        adapter = new ArrayAdapter<>(AddMed.this, R.layout.selected_spinner_tube, updatedTubesArray);
        adapter.setDropDownViewResource(R.layout.spinner_list_tube);

        selectTube.setAdapter(adapter);
    }


    private void setVariable () {

        inMedi.setSelected(true);
        inMedi.setBackgroundResource(R.drawable.add_med_button_onclick);

        outMedi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LinearLayout dosCon = findViewById(R.id.dos_container);
                LinearLayout tubeSel = findViewById(R.id.tube_sel_container);
                dosCon.setVisibility(View.VISIBLE);
                tubeSel.setVisibility(View.GONE);
                inMedi.setBackgroundResource(R.drawable.add_med_button_normal);
                outMedi.setBackgroundResource(R.drawable.add_med_button_onclick);
                outMedi.setTextColor(AddMed.this.getColor(R.color.dark10));
                inMedi.setTextColor(AddMed.this.getColor(R.color.light));
                outMedi.setSelected(true);
                inMedi.setSelected(false);
            }
        });

        inMedi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LinearLayout dosCon = findViewById(R.id.dos_container);
                LinearLayout tubeSel = findViewById(R.id.tube_sel_container);
                dosCon.setVisibility(View.GONE);
                tubeSel.setVisibility(View.VISIBLE);
                outMedi.setBackgroundResource(R.drawable.add_med_button_normal);
                inMedi.setBackgroundResource(R.drawable.add_med_button_onclick);
                inMedi.setTextColor(AddMed.this.getColor(R.color.dark10));
                outMedi.setTextColor(AddMed.this.getColor(R.color.light));
                inMedi.setSelected(true);
                outMedi.setSelected(false);
            }
        });

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

        if (inMedi.isSelected()){
            if(pillName.isEmpty() || pillsOnTube.isEmpty()){
                errorText.setVisibility(View.VISIBLE);
                return false;
            } else if(pillsOnTube.equals("0")) {
                errorText.setVisibility(View.VISIBLE);
                return false;
            }
            else if(!everydaySelected && !customSelected){
                errorText.setVisibility(View.VISIBLE);
                return false;
            } else if(tubeSelected.equals(defaultText)) {
                errorText.setVisibility(View.VISIBLE);
                return false;
            } else {
                medReminder.setMedicine(pillName);
                medReminder.setPillsOnTube(pillsOnTube);
                medReminder.setNotes(etNotes);
                medReminder.setTubeSelection(tubeSelected);
                medReminder.setRepeat(schedule);
                medReminder.setInStorage(true);
                return true;
            }
        } else if (outMedi.isSelected()){
            if(pillName.isEmpty() || pillsOnTube.isEmpty()){
                errorText.setVisibility(View.VISIBLE);
                return false;
            } else if(pillsOnTube.equals("0")) {
                errorText.setVisibility(View.VISIBLE);
                return false;
            } else if(!tabletSelected && !capsuleSelected){
                errorText.setVisibility(View.VISIBLE);
                return false;
            }
            else if(!everydaySelected && !customSelected){
                errorText.setVisibility(View.VISIBLE);
                return false;
            } else {
                medReminder.setMedicine(pillName);
                medReminder.setPillsOnTube(pillsOnTube);
                medReminder.setNotes(etNotes);
                medReminder.setPillForm(dosForm);
                medReminder.setRepeat(schedule);
                medReminder.setInStorage(false);
                return true;
            }
        }
        return true;
    }

    private void saveMedSchedule () {

        String userID = firebaseUser.getUid();
        medReminder.setUserid(userID);

        databaseReference = FirebaseDatabase.getInstance().getReference("MedRemind");
        key = databaseReference.child("MedReminder").push().getKey();
        medReminder.setMedId(key);

        databaseReference.child(key).setValue(medReminder).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete (@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    // If everyday is selected, get everyday items
                    if(everydaySelected){
                        LinearLayout everydayTable = findViewById(R.id.everyday_table);
                        for (int i = 1; i < everydayTable.getChildCount(); i++){
                            String everydayKey = databaseReference.child(key).child("everyday").push().getKey();

                            View view = everydayTable.getChildAt(i);
                            time = ((TextView) view.findViewById(R.id.everyday_time)).getText().toString();
                            pills = Integer.parseInt(((EditText) view.findViewById(R.id.everydayPill)).getText().toString());

                            databaseReference.child(key).child("schedule").child(everydayKey).child("days").setValue(" ");
                            databaseReference.child(key).child("schedule").child(everydayKey).child("times").setValue(time);
                            databaseReference.child(key).child("schedule").child(everydayKey).child("pillQuantities").setValue(pills);

                            //Extract the AM/PM from the time string
                            String[] parts = time.split(" ");
                            String[] timeParts = parts[0].split(":"); // Extract hours and minutes from the time string

                            int hour = Integer.parseInt(timeParts[0]);
                            int minute = Integer.parseInt(timeParts[1]);

                            // Convert hour to 24-hour format if necessary
                            if (parts[1].equals("PM")) {
                                hour = (hour == 12) ? 12 : hour + 12; // Add 12 to hour if it's PM, but keep 12 PM as it is
                            } else {
                                hour = (hour == 12) ? 0 : hour; // If it's AM and hour is 12, set hour to 0, otherwise keep hour as it is
                            }

                            ScheduleItem scheduleItem = new ScheduleItem();
                            scheduleItem.setDay(" ");
                            scheduleItem.setTime(time);
                            scheduleItem.setPillQuantities(pills);

                            setEverydayAlarm(hour, minute, key, everydayKey);

                        }
                    }

                    // If custom day is selected, get custom items
                    else if(customSelected) {
                        LinearLayout customTable = findViewById(R.id.custom_table);
                        for (int i = 1; i < customTable.getChildCount(); i++){
                            String customKey = databaseReference.child(key).child("custom").push().getKey();

                            View view = customTable.getChildAt(i);
                            day = ((Spinner) view.findViewById(R.id.custom_day)).getSelectedItem().toString();
                            time = ((TextView) view.findViewById(R.id.custom_time)).getText().toString();
                            pills = Integer.parseInt(((EditText) view.findViewById(R.id.customPill)).getText().toString());

                            databaseReference.child(key).child("schedule").child(customKey).child("days").setValue(day);
                            databaseReference.child(key).child("schedule").child(customKey).child("times").setValue(time);
                            databaseReference.child(key).child("schedule").child(customKey).child("pillQuantities").setValue(pills);

                            // Extract hours and minutes from the time string
                            String[] parts = time.split(" ");
                            String[] timeParts = parts[0].split(":");

                            int hour = Integer.parseInt(timeParts[0]);
                            int minute = Integer.parseInt(timeParts[1]);

                            ScheduleItem scheduleItem = new ScheduleItem();
                            scheduleItem.setDay(day);
                            scheduleItem.setTime(time);
                            scheduleItem.setPillQuantities(pills);

                            setCustomAlarm(hour, minute, key, customKey, day);
                        }
                    }

                    Toast.makeText(AddMed.this, "Med Reminder added successfully",
                            Toast.LENGTH_SHORT).show();

                    Intent intent=new Intent(AddMed.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK
                            | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                } else {
                    saveMedi.setEnabled(true);
                    progressBar.setVisibility(View.GONE);
                }
            }
        });
    }

   private void setEverydayAlarm(int hours, int minutes, String reminderKey, String childKey){
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, AlarmReceiver.class);
        intent.putExtra("reminderKey", reminderKey);
        intent.putExtra("childKey", childKey);

       // Generate a unique request code based on reminderKey and childKey
       int requestCode = (reminderKey + childKey).hashCode();

       PendingIntent pendingIntent = PendingIntent.getBroadcast(this, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hours);
        calendar.set(Calendar.MINUTE, minutes);
        calendar.set(Calendar.SECOND, 0);

       if(calendar.getTimeInMillis() <= System.currentTimeMillis()){
           calendar.add(Calendar.DAY_OF_YEAR, 1);
       }

       alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);

    }

    // Method to set alarms for custom schedule
    private void setCustomAlarm(int hours, int minutes, String reminderKey, String childKey, String dayOfWeek) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, AlarmReceiver.class);
        intent.putExtra("reminderKey", reminderKey);
        intent.putExtra("childKey", childKey);

        // Generate a unique request code based on reminderKey and childKey
        int requestCode = (reminderKey + childKey).hashCode();

        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hours);
        calendar.set(Calendar.MINUTE, minutes);
        calendar.set(Calendar.SECOND, 0);

        // Get the current day of the week
        int currentDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);

        // Get the target day of the week
        int targetDayOfWeek = getDayOfWeek(dayOfWeek);

        // Calculate the number of days until the next selected day
        int daysUntilNext = (targetDayOfWeek - currentDayOfWeek + 7) % 7;

        // Adjust the calendar to the next selected day
        calendar.add(Calendar.DAY_OF_YEAR, daysUntilNext);

        // Set the alarm for the next occurrence
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY * 7, pendingIntent);
    }

    private int getDayOfWeek(String day) {
        switch (day) {
            case "SUN":
                return Calendar.SUNDAY;
            case "MON":
                return Calendar.MONDAY;
            case "TUE":
                return Calendar.TUESDAY;
            case "WED":
                return Calendar.WEDNESDAY;
            case "THU":
                return Calendar.THURSDAY;
            case "FRI":
                return Calendar.FRIDAY;
            case "SAT":
                return Calendar.SATURDAY;
            default:
                return -1; // Invalid day
        }
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

                    LinearLayout everydayTable = findViewById(R.id.everyday_table);
                    if (everydayTable.getChildCount() == 1){
                        addNewEverydayItem();
                    }

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
                schedule = "Everyday";

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
        customDateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View view) {
                if (bottomSheetBehavior_custom.getState() != BottomSheetBehavior.STATE_EXPANDED) {
                    if(bottomSheetBehavior_everyday.getState() != BottomSheetBehavior.STATE_COLLAPSED){
                        bottomSheetBehavior_everyday.setState(BottomSheetBehavior.STATE_COLLAPSED);
                    }
                    bottomSheetBehavior_custom.setState(BottomSheetBehavior.STATE_EXPANDED);

                    LinearLayout customTable = findViewById(R.id.custom_table);
                    if (customTable.getChildCount() == 1){
                        addNewCustomItem();
                    }

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
                schedule = "Custom";

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
        TextView everydaySetTime = newEverydayRow.findViewById(R.id.everyday_time);
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
                EditText everydayPill = newEverydayRow.findViewById(R.id.everydayPill);
                int pillQuantity = Integer.parseInt(everydayPill.getText().toString());
                pillQuantity++;
                everydayPill.setText(String.valueOf(pillQuantity));
            }
        });

        //When minusPill button is clicked, pill quantity of the instance decreases
        minusPill = newEverydayRow.findViewById(R.id.everyday_pill_minus);
        minusPill.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View view) {
                EditText everydayPill = newEverydayRow.findViewById(R.id.everydayPill);
                int pillQuantity = Integer.parseInt(everydayPill.getText().toString());
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

        // Setup adapter for the spinner view
        Spinner daySpinner = newCustomRow.findViewById(R.id.custom_day);
        adapterDays = new ArrayAdapter<>(AddMed.this, R.layout.selected_spinner_days, getResources().getStringArray(R.array.days));
        adapterDays.setDropDownViewResource(R.layout.spinner_list_days);
        daySpinner.setAdapter(adapterDays);

        // Set click listener for the TextView to handle handle selected items
        daySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected (AdapterView<?> parent, View view, int pos, long l) {
//                tubeSelected = parent.getItemAtPosition(pos).toString();
            }

            @Override
            public void onNothingSelected (AdapterView<?> adapterView) {

            }
        });

        // Set click listener for the TextView to open the TimePickerDialog
        TextView customSetTime = newCustomRow.findViewById(R.id.custom_time);
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
                EditText customPill = newCustomRow.findViewById(R.id.customPill);
                int pillQuantity = Integer.parseInt(customPill.getText().toString());
                pillQuantity++;
                customPill.setText(String.valueOf(pillQuantity));
            }
        });

        //When minusPill button is clicked, pill quantity of the instance decreases
        minusPill = newCustomRow.findViewById(R.id.custom_pill_minus);
        minusPill.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View view) {
                EditText customPill = newCustomRow.findViewById(R.id.customPill);
                int pillQuantity = Integer.parseInt(customPill.getText().toString());
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
}