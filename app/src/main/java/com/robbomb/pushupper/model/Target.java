package com.robbomb.pushupper.model;

import org.joda.time.DateTime;

/**
 * Created by NewRob on 1/12/2016.
 */
public class Target {
    private int targetId;
    private int workoutId;
    private DateTime dateTime;
    private int reps;

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

    public int getWorkoutId() {
        return workoutId;
    }

    public void setWorkoutId(int workoutId) {
        this.workoutId = workoutId;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Target{");
        sb.append("workoutId=").append(workoutId);
        sb.append(", dateTime=").append(dateTime);
        sb.append(", reps=").append(reps);
        sb.append('}');
        return sb.toString();
    }
}
