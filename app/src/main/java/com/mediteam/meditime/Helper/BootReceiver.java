package com.mediteam.meditime.Helper;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;
import java.util.List;
import java.util.Map;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive (Context context, Intent intent) {
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            // Logic to reset all alarms from persistent storage
            resetAlarms(context);
        }
    }

    private void resetAlarms (Context context) {
        FirebaseAuth mAuth= FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();

        if(user!=null){
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("MedRemind").child(user.getUid());

            // Fetch alarms from the database and reset them
            databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange (@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot medReminderSnapshot : snapshot.getChildren()) {
                        MedReminder medReminder = medReminderSnapshot.getValue(MedReminder.class);
                        if (medReminder != null) {
                            DataSnapshot scheduleSnapshot = medReminderSnapshot.child("schedule");
                            if(snapshot.exists()){
                                // If schedule is a Map
                                for (DataSnapshot scheduleItemSnapshot : scheduleSnapshot.getChildren()) {
                                    ScheduleItem scheduleItem = scheduleItemSnapshot.getValue(ScheduleItem.class);
                                    setAlarmFromData(context, medReminder, scheduleItem);
                                }
                            } else if (scheduleSnapshot.getValue() instanceof List) {
                                // If schedule is a List
                                for (DataSnapshot scheduleItemSnapshot : scheduleSnapshot.getChildren()) {
                                    ScheduleItem scheduleItem = scheduleItemSnapshot.getValue(ScheduleItem.class);
                                    setAlarmFromData(context, medReminder, scheduleItem);
                                }
                            }
                        } else {
                            Log.d("BootReceiver", "MedReminder is null for user");
                        }
                    }
                }

                @Override
                public void onCancelled (@NonNull DatabaseError error) {
                    Log.d("BootReceiver", "Database error: " + error.getMessage());
                }
            });
        } else {
            Log.d("BootReceiver", "User is null");
        }
    }

    private void setAlarmFromData (Context context, MedReminder medReminder, ScheduleItem scheduleItem) {
        // Parse alarmItem to retrieve necessary data
        boolean isEveryday;
        String time = scheduleItem.getTimes();
        int hours = Integer.parseInt(time.split(":")[0]);
        int minutes = Integer.parseInt(time.split(":")[1]);
        int requestCode = scheduleItem.getRequestCode();
        String medicineName = medReminder.getMedicine();
        int dayOfWeek = scheduleItem.getRepeat();
        String repeatStyle = medReminder.getRepeatStyle();
        if(repeatStyle.equals("Everyday")){
            isEveryday = true;
        } else isEveryday = false;

        if (isEveryday) {
            setEverydayAlarm(context, hours, minutes, requestCode, medicineName);
        } else {
            setCustomAlarm(context, hours, minutes, requestCode, dayOfWeek, medicineName);
        }
    }

    private int getDayOfWeek(int dayofWeek) {
        switch (dayofWeek) {
            case 0:
                return Calendar.SUNDAY;
            case 1:
                return Calendar.MONDAY;
            case 2:
                return Calendar.TUESDAY;
            case 3:
                return Calendar.WEDNESDAY;
            case 4:
                return Calendar.THURSDAY;
            case 5:
                return Calendar.FRIDAY;
            case 6:
                return Calendar.SATURDAY;
            default:
                return 7; // Invalid day
        }
    }

    private void setCustomAlarm (Context context, int hours, int minutes, int requestCode, int dayOfWeek, String medicineName) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.putExtra("medicineName", medicineName);
        intent.putExtra("alarmTime", String.format("%02d:%02d", hours, minutes));

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hours);
        calendar.set(Calendar.MINUTE, minutes);
        calendar.set(Calendar.SECOND, 0);

        int currentDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        int targetDayOfWeek = getDayOfWeek(dayOfWeek);
        int daysUntilNext = (targetDayOfWeek - currentDayOfWeek + 7) % 7;

        // If the target day is today but the time has already passed, move to the next week
        if (daysUntilNext == 0 && calendar.getTimeInMillis() <= System.currentTimeMillis()) {
            daysUntilNext += 7;
        }

        calendar.add(Calendar.DAY_OF_YEAR, daysUntilNext);

        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY * 7, pendingIntent);
    }

    private void setEverydayAlarm (Context context, int hours, int minutes, int requestCode, String medicineName) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.putExtra("medicineName", medicineName);
        intent.putExtra("alarmTime", String.format("%02d:%02d", hours, minutes));

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT);

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
}
