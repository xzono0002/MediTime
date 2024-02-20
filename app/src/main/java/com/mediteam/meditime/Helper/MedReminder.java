package com.mediteam.meditime.Helper;

public class MedReminder {
    private String Userid;
    private String medicine;
    private String tubeSelection;
    private String pillForm;
    private String schedule;
    private String day;
    private int time;

    public MedReminder () {
    }

    @Override
    public String toString () {
        return medicine;
    }

    public String getUserid () {
        return Userid;
    }

    public void setUserid (String userid) {
        Userid = userid;
    }

    public String getMedicine () {
        return medicine;
    }

    public void setMedicine (String medicine) {
        this.medicine = medicine;
    }

    public String getTubeSelection () {
        return tubeSelection;
    }

    public void setTubeSelection (String tubeSelection) {
        this.tubeSelection = tubeSelection;
    }

    public String getPillForm () {
        return pillForm;
    }

    public void setPillForm (String pillForm) {
        this.pillForm = pillForm;
    }

    public String getSchedule () {
        return schedule;
    }

    public void setSchedule (String schedule) {
        this.schedule = schedule;
    }

    public String getDay () {
        return day;
    }

    public void setDay (String day) {
        this.day = day;
    }

    public int getTime () {
        return time;
    }

    public void setTime (int time) {
        this.time = time;
    }
}
