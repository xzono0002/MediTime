package com.mediteam.meditime.Helper;

public class ScheduleItem {

    private String days;
    private String times;
    private int pillQuantities;

    // Add getters and setters for the fields

    public String getDay () {
        return days;
    }

    public void setDay (String day) {
        this.days = day;
    }


    public String getTime () {
        return times;
    }

    public void setTime (String time) {
        this.times = time;
    }


    public int getPillQuantities () {
        return pillQuantities;
    }

    public void setPillQuantities (int pillQuantities) {
        this.pillQuantities = pillQuantities;
    }
}
