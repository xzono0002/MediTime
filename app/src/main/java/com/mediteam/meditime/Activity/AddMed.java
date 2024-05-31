package com.mediteam.meditime.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
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
    private View rootView;
    private TextView errorText;
    private AutoCompleteTextView mediName;
    private EditText pillOnTube, etNote;
    private ImageButton bckHome, saveMedi;
    private Button tabletBtn, capsuleBtn, everydayBtn, customDateBtn, addPill, minusPill, inMedi, outMedi;
    private Spinner selectTube;
    private ProgressBar progressBar;
    ArrayAdapter<String> adapter, adapterDays;
    ArrayList<String> assignedTubes = new ArrayList<>();
    private String tubeSelected, dosForm, schedule, day, time, key, tubeKey;
    private int pills;
    private int scheduleNumEveryday = 1;
    private int scheduleNumCustom = 1;
    private boolean tabletSelected = false, capsuleSelected = false, everydaySelected = false, customSelected = false;
    String[] medicines = new String[]{
            "Losartan", "Amlodipine", "Atorvastatin", "Rosuvastatin", "Trimetazidine", "Metformin",
            "Isosorbide mononitrate", "Azithromycin", "Lagundi", "Cefuroxime",
            "Ipratropium", "Salbutamol"
    };
    MedReminder medReminder;
    private int requestCode;
    private static final String TAG = "AddMed";
    private BottomSheetBehavior<LinearLayout> bottomSheetBehavior_everyday;
    private BottomSheetBehavior<LinearLayout> bottomSheetBehavior_custom;
    LinearLayout everydaySched, customSched;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_med);

        mAuth = FirebaseAuth.getInstance();
        firebaseUser = mAuth.getCurrentUser();

        medReminder = new MedReminder();

        rootView = findViewById(R.id.rootLayout);
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

        everydaySched =findViewById(R.id.everyday_sched_misc);
        bottomSheetBehavior_everyday = BottomSheetBehavior.from(everydaySched);
        customSched =findViewById(R.id.custom_sched_misc);
        bottomSheetBehavior_custom = BottomSheetBehavior.from(customSched);

        selectTube = findViewById(R.id.tubeSelect);

        String userID = firebaseUser.getUid();
        databaseReference = FirebaseDatabase.getInstance().getReference("MedRemind").child(userID);

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange (@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    for (DataSnapshot requestSnapshot : snapshot.getChildren()) {

                        Integer tube = requestSnapshot.child("tubeSelection").getValue(Integer.class); // Get the value of occupied tubes in database
                        String tubeName = "";
                        Log.d(TAG, "tube: " + tube);

                        if (tube != null) {
                            if(tube == 0){
                                tubeName = "Tube A";
                            } else if(tube == 1){
                                tubeName = "Tube B";
                            } else if(tube == 2){
                                tubeName = "Tube C";
                            } else if(tube == 3){
                                tubeName = "Tube D";
                            } else if(tube == 4){
                                tubeName = "Tube E";
                            }

                            assignedTubes.add(tubeName); // Set the values of tube to be removed from spinner
                        }
                    }
                        updateAdapter(); // update list on spinner
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
        setupUI(rootView);
    }

    private void setupUI (View rootView) {
        if(!(rootView instanceof EditText)){
            rootView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    hideSoftKeyboard(AddMed.this);
                    return false;
                }
            });
        }

        // If a layout container, iterate over children and set up touch listener.
        if (rootView instanceof ViewGroup) {
            for (int i = 0; i < ((ViewGroup) rootView).getChildCount(); i++) {
                View innerView = ((ViewGroup) rootView).getChildAt(i);
                setupUI(innerView);
            }
        }
    }

    private void hideSoftKeyboard (Activity activity) {
        InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        if (activity.getCurrentFocus() != null) {
            inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
        }    }

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

        // Create an ArrayAdapter using the string array and a custom spinner layout
        ArrayAdapter<String> adapter2 = new ArrayAdapter<>(AddMed.this,
                R.layout.spinner_list_tube, medicines);

        // Apply the adapter to the AutoCompleteTextView
        mediName.setAdapter(adapter2);
    }

    private Boolean validateFields(){
        key = databaseReference.child("MedReminder").push().getKey();
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
                int tubeStock = Integer.parseInt(pillsOnTube);

                if(tubeSelected.equals("Tube A")){
                    tubeKey = "0";
                } else if(tubeSelected.equals("Tube B")){
                    tubeKey = "1";
                } else if(tubeSelected.equals("Tube C")){
                    tubeKey = "2";
                } else if(tubeSelected.equals("Tube D")){
                    tubeKey = "3";
                } else if(tubeSelected.equals("Tube E")){
                    tubeKey = "4";
                }

                medReminder.setMedicine(pillName);
                medReminder.setPillsOnTube(tubeStock);
                medReminder.setNotes(etNotes);
                medReminder.setTubeSelection(Integer.parseInt(tubeKey));
                medReminder.setRepeatStyle(schedule);
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
                int tubeStock = Integer.parseInt(pillsOnTube);
                tubeKey = key;

                medReminder.setMedicine(pillName);
                medReminder.setPillsOnTube(tubeStock);
                medReminder.setTubeSelection(6);
                medReminder.setNotes(etNotes);
                medReminder.setPillForm(dosForm);
                medReminder.setRepeatStyle(schedule);
                medReminder.setInStorage(false);
                return true;
            }
        }
        return true;
    }

    private void saveMedSchedule () {

        String userID = firebaseUser.getUid();
        medReminder.setUserId(userID);

        databaseReference = FirebaseDatabase.getInstance().getReference("MedRemind").child(userID);
        medReminder.setMedId(tubeKey);

        databaseReference.child(tubeKey).setValue(medReminder).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete (@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    // If everyday is selected, get everyday items

                    if(everydaySelected){
                        databaseReference.child(String.valueOf(tubeKey)).child("schedNumber").setValue(scheduleNumEveryday);

                        LinearLayout everydayTable = findViewById(R.id.everyday_table);
                        for (int i = 1; i < everydayTable.getChildCount(); i++){
                            int schedID = i-1;
                            View view = everydayTable.getChildAt(i);
                            processScheduleItem(view, true, key, tubeKey, databaseReference, schedID, mediName.getText().toString());
                        }
                    }

                    // If custom day is selected, get custom items
                    else if(customSelected) {
                        databaseReference.child(String.valueOf(tubeKey)).child("schedNumber").setValue(scheduleNumCustom);

                        LinearLayout customTable = findViewById(R.id.custom_table);
                        for (int i = 1; i < customTable.getChildCount(); i++){
                            int schedID = i-1;
                            View view = customTable.getChildAt(i);
                            processScheduleItem(view, false, key, tubeKey, databaseReference, schedID, mediName.getText().toString());
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

    private String convertTo24HourFormat(String time) {
        String[] parts = time.split(" ");
        String[] timeParts = parts[0].split(":");
        int hour = Integer.parseInt(timeParts[0]);
        int minute = Integer.parseInt(timeParts[1]);

        if (parts[1].equals("PM")) {
            hour = (hour == 12) ? 12 : hour + 12;
        } else {
            hour = (hour == 12) ? 0 : hour;
        }

        String alarmHour = (hour < 10) ? "0" + hour : String.valueOf(hour);
        String alarmMinute = (minute < 10) ? "0" + minute : String.valueOf(minute);

        return alarmHour + ":" + alarmMinute;
    }

    private int getDaySchedule(String day) {
        switch (day) {
            case "SUN": return 0;
            case "MON": return 1;
            case "TUE": return 2;
            case "WED": return 3;
            case "THU": return 4;
            case "FRI": return 5;
            case "SAT": return 6;
            default: return 7; // Default to everyday if not matched
        }
    }

    private void setAlarm(boolean isEveryday, int hours, int minutes, String key, String childKey, String medicineName, String day) {
        if (isEveryday) {
            setEverydayAlarm(hours, minutes, key, childKey, medicineName);
        } else {
            setCustomAlarm(hours, minutes, key, childKey, day, medicineName);
        }
    }

    private void processScheduleItem(View view, boolean isEveryday, String key, String tubeKey, DatabaseReference databaseReference, int scheduleIndex, String medicineName) {
        String time, day;
        int pills, daySched;
        if (isEveryday) {
            time = ((TextView) view.findViewById(R.id.everyday_time)).getText().toString();
            pills = Integer.parseInt(((EditText) view.findViewById(R.id.everydayPill)).getText().toString());
            day = "EVERYDAY";
            daySched = 7;
        } else {
            time = ((TextView) view.findViewById(R.id.custom_time)).getText().toString();
            pills = Integer.parseInt(((EditText) view.findViewById(R.id.customPill)).getText().toString());
            day = ((Spinner) view.findViewById(R.id.custom_day)).getSelectedItem().toString();
            daySched = getDaySchedule(day);
        }
        String alarmTime = convertTo24HourFormat(time);

        String childKey = databaseReference.child(key).child(isEveryday ? "everyday" : "custom").push().getKey();
        int hours = Integer.parseInt(alarmTime.split(":")[0]);
        int minutes = Integer.parseInt(alarmTime.split(":")[1]);

        setAlarm(isEveryday, hours, minutes, key, childKey, medicineName, day);

        ScheduleItem scheduleItem = new ScheduleItem(daySched, alarmTime, requestCode, pills);
        databaseReference.child(tubeKey).child("schedule").child(String.valueOf(scheduleIndex)).setValue(scheduleItem);
    }


    private void setEverydayAlarm(int hours, int minutes, String reminderKey, String childKey, String medicineName){
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, AlarmReceiver.class);
        intent.putExtra("reminderKey", reminderKey);
        intent.putExtra("childKey", childKey);
        intent.putExtra("medicineName", medicineName);
        intent.putExtra("alarmTime", String.format("%02d:%02d", hours, minutes));

       // Generate a unique request code based on reminderKey and childKey
       requestCode = (reminderKey + childKey).hashCode();

       PendingIntent pendingIntent = PendingIntent.getBroadcast(this, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT);

       Calendar calendar = Calendar.getInstance();
       calendar.set(Calendar.HOUR_OF_DAY, hours);
       calendar.set(Calendar.MINUTE, minutes);
       calendar.set(Calendar.SECOND, 0);

       // If the specified time for today has already passed, set the alarm for the same time tomorrow
       if (System.currentTimeMillis() >= calendar.getTimeInMillis()){
           calendar.add(Calendar.DAY_OF_YEAR, 1); // Add one day to set the alarm for tomorrow
       }

       // Set the alarm to repeat every day at the specified time
       alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,
               calendar.getTimeInMillis(),
               AlarmManager.INTERVAL_DAY,
               pendingIntent);

    }

    // Method to set alarms for custom schedule
    private void setCustomAlarm(int hours, int minutes, String reminderKey, String childKey, String dayOfWeek, String medicineName) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, AlarmReceiver.class);
        intent.putExtra("reminderKey", reminderKey);
        intent.putExtra("childKey", childKey);
        intent.putExtra("medicineName", medicineName);
        intent.putExtra("alarmTime", String.format("%02d:%02d", hours, minutes));

        // Generate a unique request code based on reminderKey and childKey
        requestCode = (reminderKey + childKey).hashCode();

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

        // If the target day is today but the time has already passed, move to the next week
        if (daysUntilNext == 0 && calendar.getTimeInMillis() <= System.currentTimeMillis()) {
            daysUntilNext += 7;
        }

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

        if (bottomSheetBehavior_everyday.getState() == BottomSheetBehavior.STATE_EXPANDED) {
            bottomSheetBehavior_everyday.setState(BottomSheetBehavior.STATE_COLLAPSED);
            hideSoftKeyboard(AddMed.this);
        }
        if (bottomSheetBehavior_custom.getState() == BottomSheetBehavior.STATE_EXPANDED) {
            bottomSheetBehavior_custom.setState(BottomSheetBehavior.STATE_COLLAPSED);
            hideSoftKeyboard(AddMed.this);
        }

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
                if(scheduleNumEveryday < 3){
                    addNewEverydayItem();
                    scheduleNumEveryday++;
                }
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
                if(scheduleNumCustom < 3){
                    addNewCustomItem();
                    scheduleNumCustom++;
                }
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
                            if(hours != 12) {
                                hours = hours - 12;
                            }
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

                if(pillQuantity >= 2){
                    minusPill.setEnabled(true);
                }
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

                if(pillQuantity == 1){
                    minusPill.setEnabled(false);
                }
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
                            if(hours != 12) {
                                hours = hours - 12;
                            }
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
                if(pillQuantity >= 2){
                    minusPill.setEnabled(true);
                }
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

                if(pillQuantity == 1){
                    minusPill.setEnabled(false);
                }
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