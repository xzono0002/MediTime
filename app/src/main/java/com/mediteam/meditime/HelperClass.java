package com.mediteam.meditime;

public class HelperClass {
    String name, username, pin;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPin() {
        return pin;
    }

    public void setPin(String pin) {
        this.pin = pin;
    }

    public HelperClass(String name, String username, String pin) {
        this.name = name;
        this.username = username;
        this.pin = pin;
    }

    public HelperClass () {

    }
}
