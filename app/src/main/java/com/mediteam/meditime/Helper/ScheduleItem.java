package com.mediteam.meditime.Helper;

public class ScheduleItem {

    private int repeat;
    private String times;
    private int requestCode;
    private int pillQuantities;

    // Add getters and setters for the fields


    public int getRepeat () {
        return repeat;
    }

    public void setRepeat (int repeat) {
        this.repeat = repeat;
    }

    public String getTimes () {
        return times;
    }

    public void setTimes (String times) {
        this.times = times;
    }

    public int getRequestCode () {
        return requestCode;
    }

    public void setRequestCode (int requestCode) {
        this.requestCode = requestCode;
    }

    public int getPillQuantities () {
        return pillQuantities;
    }

    public void setPillQuantities (int pillQuantities) {
        this.pillQuantities = pillQuantities;
    }

    public ScheduleItem (int repeat, String times, int requestCode, int pillQuantities) {
        this.repeat = repeat;
        this.times = times;
        this.requestCode = requestCode;
        this.pillQuantities = pillQuantities;
    }

    public ScheduleItem(){

    }
}
