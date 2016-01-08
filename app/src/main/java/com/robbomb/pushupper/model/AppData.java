package com.robbomb.pushupper.model;

import org.joda.time.DateTime;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by NewRob on 1/6/2016.
 */
public class AppData implements Serializable {

    public AppData() {
        history = new ArrayList<>();
        todays = new ArrayList<>();
    }

    private List<LoggedSet> history;
    private int dayOneReps;
    private DateTime dayOneDate;
    private int lastReps;
    private List<LoggedSet> todays;

    private int repsToday;
    private int repsRemaining;


    public List<LoggedSet> getHistory() {
        return history;
    }

    public void setHistory(List<LoggedSet> history) {
        this.history = history;
    }

    public void setDayOneReps(int dayOneReps) {
        this.dayOneReps = dayOneReps;
    }

    public int getDayOneReps() {
        return dayOneReps;
    }

    public void setDayOneDate(DateTime dayOneDate) {
        this.dayOneDate = dayOneDate;
    }

    public DateTime getDayOneDate() {
        return dayOneDate;
    }

    public void setLastReps(int lastReps) {
        this.lastReps = lastReps;
    }

    public int getLastReps() {
        return lastReps;
    }


    public void setTodays(List<LoggedSet> todays) {
        this.todays = todays;
    }

    public List<LoggedSet> getTodays() {
        return todays;
    }

    public void setRepsToday(int repsToday) {
        this.repsToday = repsToday;
    }

    public int getRepsToday() {
        return repsToday;
    }

    public void setRepsRemaining(int repsRemaining) {
        this.repsRemaining = repsRemaining;
    }

    public int getRepsRemaining() {
        return repsRemaining;
    }

}
