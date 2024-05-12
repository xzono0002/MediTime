package com.mediteam.meditime.Helper;

public class RegisteredUsers {
    public String name;
    public String username;
    public String pin;

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

    public RegisteredUsers (String name, String username, String pin) {
        this.name = name;
        this.username = username;
        this.pin = pin;
    }

    public RegisteredUsers () {

    }
}
