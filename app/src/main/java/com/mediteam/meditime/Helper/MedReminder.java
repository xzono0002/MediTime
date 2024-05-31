package com.mediteam.meditime.Helper;

import java.io.Serializable;
import java.util.Map;

public class MedReminder implements Serializable {
    private String userId;
    private String medId;
    private String medicine;
    private int pillsOnTube;
    private String notes;
    private int tubeSelection;
    private String pillForm;
    private String repeatStyle;
    private Boolean inStorage;

    private Map<String, ScheduleItem> schedule;

    public MedReminder () {
    }

    @Override
    public String toString () {
        return medicine;
    }

    public String getUserId () {
        return userId;
    }

    public void setUserId (String userId) {
        this.userId = userId;
    }

    public String getMedId () {
        return medId;
    }

    public void setMedId (String medId) {
        this.medId = medId;
    }

    public int getPillsOnTube () {
        return pillsOnTube;
    }

    public void setPillsOnTube (int pillsOnTube) {
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

    public int getTubeSelection () {
        return tubeSelection;
    }

    public void setTubeSelection (int tubeSelection) {
        this.tubeSelection = tubeSelection;
    }

    public String getPillForm () {
        return pillForm;
    }

    public void setPillForm (String pillForm) {
        this.pillForm = pillForm;
    }

    public String getRepeatStyle () {
        return repeatStyle;
    }

    public void setRepeatStyle (String repeatStyle) {
        this.repeatStyle = repeatStyle;
    }

    public Boolean getInStorage() {
        return inStorage;
    }

    public void setInStorage(Boolean inStorage) {
        this.inStorage = inStorage;
    }
}


