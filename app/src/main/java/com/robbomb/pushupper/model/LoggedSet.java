package com.robbomb.pushupper.model;

import org.joda.time.DateTime;

import java.io.Serializable;

/**
 * Created by NewRob on 1/4/2016.
 */
public class LoggedSet implements Serializable {
    private int targetId;
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

    public int getTargetId() {
        return targetId;
    }

    public void setTargetId(int targetId) {
        this.targetId = targetId;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("LoggedSet{");
        sb.append("targetId=").append(targetId);
        sb.append(", dateTime=").append(dateTime);
        sb.append(", reps=").append(reps);
        sb.append('}');
        return sb.toString();
    }
}
