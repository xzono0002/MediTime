package com.mediteam.meditime.Helper;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MedReminder implements Serializable {
    private String Userid;
    private String medId;
    private String medicine;
    private String pillsOnTube;
    private String notes;
    private String tubeSelection;
    private String pillForm;
    private String repeat;
    private Map<String, ScheduleItem> schedule;

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

    public String getMedId () {
        return medId;
    }

    public void setMedId (String medId) {
        this.medId = medId;
    }

    public String getPillsOnTube () {
        return pillsOnTube;
    }

    public void setPillsOnTube (String pillsOnTube) {
        this.pillsOnTube = pillsOnTube;
    }

    public String getNotes () {
        return notes;
    }

    public void setNotes (String notes) {
        this.notes = notes;
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

    public String getRepeat () {
        return repeat;
    }

    public void setRepeat (String repeat) {
        this.repeat = repeat;
    }

}


