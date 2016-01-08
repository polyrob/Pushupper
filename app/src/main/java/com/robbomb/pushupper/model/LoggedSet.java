package com.robbomb.pushupper.model;

import org.joda.time.DateTime;

import java.io.Serializable;

/**
 * Created by NewRob on 1/4/2016.
 */
public class LoggedSet implements Serializable {
    private DateTime dateTime;
    private int reps;

    public LoggedSet() {
    }

    public LoggedSet(DateTime datetime, int reps) {
        this.dateTime = datetime;
        this.reps = reps;
    }

    public DateTime getDateTime() {
        return dateTime;
    }

    public void setDateTime(DateTime dateTime) {
        this.dateTime = dateTime;
    }

    public int getReps() {
        return reps;
    }

    public void setReps(int reps) {
        this.reps = reps;
    }
}
