package de.sknauer.alarmmpdremote.model;

/**
 * Created by sebastian on 28.01.15.
 */
public class Alarm {
    private int hour;
    private int minute;
    private boolean enabled;
    private String name;
    private String playlist;

    public Alarm(int hour, int minute, boolean enabled) {
        this.hour = hour;
        this.minute = minute;
        this.enabled = enabled;
        this.name = hour + ":" + minute;
        this.playlist = "WakeUp";
    }

    public int getHour() {
        return hour;
    }

    public void setHour(int hour) {
        this.hour = hour;
    }

    public int getMinute() {
        return minute;
    }

    public void setMinute(int minute) {
        this.minute = minute;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getPlaylist() {
        return playlist;
    }

    public void setPlaylist(String playlist) {
        this.playlist = playlist;
    }

    public void toggle() {
        if (enabled)
            this.enabled = false;
        else enabled = true;
    }
}
