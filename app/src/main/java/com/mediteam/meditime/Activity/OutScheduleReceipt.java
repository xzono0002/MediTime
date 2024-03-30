package com.mediteam.meditime.Activity;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.mediteam.meditime.Adapter.PillViewAdapter;
import com.mediteam.meditime.Helper.AlarmReceiver;
import com.mediteam.meditime.Helper.MedReminder;
import com.mediteam.meditime.Helper.ScheduleItem;
import com.mediteam.meditime.R;
import com.mediteam.meditime.databinding.ActivityOutscheduleReceiptBinding;

import java.util.ArrayList;

public class OutScheduleReceipt extends AppCompatActivity {
    ActivityOutscheduleReceiptBinding binding;
    DatabaseReference reference;
    MedReminder object;
    private int num = 1;
    private String childKey;

    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOutscheduleReceiptBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        getIntentExtra();
        fetchKeys();
        setVariable();
        innitSched();
    }

    private void innitSched () {
        String requestID = object.getMedId();

        Query reference = FirebaseDatabase.getInstance().getReference("MedRemind").child(requestID);

        reference.orderByChild("schedule").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange (@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    ArrayList<ScheduleItem> list = new ArrayList<>();

                    for (DataSnapshot childSnapshot : snapshot.child("schedule").getChildren()) {
                        String days = childSnapshot.child("days").getValue(String.class);
                        String times = childSnapshot.child("times").getValue(String.class);
                        Integer pillQuantitiesObject = childSnapshot.child("pillQuantities").getValue(Integer.class);

                        // Check if pillQuantitiesObject is null
                        int pillQuantities = (pillQuantitiesObject != null) ? pillQuantitiesObject.intValue() : 0;

                        ScheduleItem scheduleItem = new ScheduleItem();

                        scheduleItem.setDay(days);
                        scheduleItem.setTime(times);
                        scheduleItem.setPillQuantities(pillQuantities);

                        list.add(scheduleItem);
                    }

                    // Extract data for MedReminder
                    String repeat = snapshot.child("repeat").getValue(String.class);

                    // Add MedReminder to list2
                    MedReminder medReminder = new MedReminder();
                    medReminder.setRepeat(repeat);

                    if (!list.isEmpty()){
                        binding.schedMed.setLayoutManager(new LinearLayoutManager(OutScheduleReceipt.this, LinearLayoutManager.VERTICAL, false));
                        RecyclerView.Adapter adapter = new PillViewAdapter(list, medReminder);
                        binding.schedMed.setAdapter(adapter);
                    }
                }
            }

            @Override
            public void onCancelled (@NonNull DatabaseError error) {

            }
        });

    }

    private void fetchKeys () {
        String requestID = object.getMedId();
        reference = FirebaseDatabase.getInstance().getReference("MedRemind").child(requestID);

        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange (@NonNull DataSnapshot snapshot) {
                for (DataSnapshot reminderSnapshot : snapshot.getChildren()){
                    // Check if the reminder has a 'schedule' node
                    if (reminderSnapshot.hasChild("schedule")) {
                        for (DataSnapshot childSnapshot : reminderSnapshot.child("schedule").getChildren()) {
                            childKey = childSnapshot.getKey();
                        }
                    }
                }
            }

            @Override
            public void onCancelled (@NonNull DatabaseError error) {

            }
        });
    }

    private void cancelAlarm(String reminderKey, String childKey){
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, (reminderKey + childKey).hashCode(), intent, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmManager.cancel(pendingIntent);
        pendingIntent.cancel();
    }

    private void setVariable () {
        binding.backHome.setOnClickListener(view -> finish());
        binding.medTitle.setText(object.getMedicine());
        binding.refilledPills.setText(object.getPillsOnTube());
        binding.dosForm.setText(object.getPillForm());
        binding.sched.setText(object.getRepeat());
        binding.note.setText(object.getNotes());

        LinearLayout everyday = findViewById(R.id.everyday_table_row_out);
        LinearLayout custom = findViewById(R.id.custom_table_row_out);

        String repeat = object.getRepeat();
        Log.d("repeatVal", "Repeat value: " + repeat);

        if (repeat != null && repeat.equals("Everyday")) {
            // Set the LinearLayout to be visible
            everyday.setVisibility(View.VISIBLE);
            custom.setVisibility(View.GONE);
        } else if(repeat != null && repeat.equals("Custom")) {
            // Set the LinearLayout to be gone (or invisible, depending on your requirement)
            custom.setVisibility(View.VISIBLE);
            everyday.setVisibility(View.GONE);
        }

        binding.editMedi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View view) {
                startActivity(new Intent(OutScheduleReceipt.this, EditMed.class));
            }
        });

        binding.optionsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View view) {
                LinearLayout options = findViewById(R.id.options_misc);
                final BottomSheetBehavior<LinearLayout> bottomSheetBehavior_options = BottomSheetBehavior.from(options);

                if (bottomSheetBehavior_options.getState() != BottomSheetBehavior.STATE_EXPANDED) {
                    bottomSheetBehavior_options.setState(BottomSheetBehavior.STATE_EXPANDED);

                } else {
                    bottomSheetBehavior_options.setState(BottomSheetBehavior.STATE_COLLAPSED);
                }

                options.findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick (View view) {
                        bottomSheetBehavior_options.setState(BottomSheetBehavior.STATE_COLLAPSED);
                    }
                });

                options.findViewById(R.id.delete_btn).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick (View view) {
                        String itemKey = object.getMedId();

                        AlertDialog.Builder builder = new AlertDialog.Builder(OutScheduleReceipt.this);
                        View dialogView = getLayoutInflater().inflate(R.layout.delete_alert, null);
                        builder.setView(dialogView);

                        Button confirmBTN = dialogView.findViewById(R.id.confirm_delete);
                        Button cancelBTN = dialogView.findViewById(R.id.cancel_delete);

                        AlertDialog alertDialog = builder.create();
                        alertDialog.show();
                        bottomSheetBehavior_options.setState(BottomSheetBehavior.STATE_COLLAPSED);

                        confirmBTN.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick (View view) {
                                reference = FirebaseDatabase.getInstance().getReference("MedRemind");

                                reference.child(itemKey).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess (Void unused) {
                                        //Deletion succesfull
                                        cancelAlarm(itemKey, childKey);
                                        Toast.makeText(OutScheduleReceipt.this, "Item deleted successfully", Toast.LENGTH_SHORT).show();
                                        Intent intent = new Intent(OutScheduleReceipt.this, MainActivity.class);
                                        startActivity(intent);
                                        finish();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure (@NonNull Exception e) {
                                        //Deletion failed
                                        Toast.makeText(OutScheduleReceipt.this, "Failed to delete item: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });

                                alertDialog.dismiss();
                            }
                        });

                        cancelBTN.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick (View view) {
                                alertDialog.dismiss();
                            }
                        });
                    }
                });
            }
        });
    }

    private void getIntentExtra () {
        object = (MedReminder) getIntent().getSerializableExtra("object");
    }
}
