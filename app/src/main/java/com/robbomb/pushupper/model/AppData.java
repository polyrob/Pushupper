package com.robbomb.pushupper.model;

import org.joda.time.DateTime;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by NewRob on 1/6/2016.
 */
public class AppData implements Serializable {

    private boolean firstTime;
    private int currentTargetId;
    private ArrayList<Target> targets;

    public AppData() {
        history = new ArrayList<>();
        todays = new ArrayList<>();
    }

    private List<LoggedSet> history;
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

    public void setFirstTime(boolean firstTime) {
        this.firstTime = firstTime;
    }

    public boolean isFirstTime() {
        return firstTime;
    }

    public int getCurrentTargetId() {
        return currentTargetId;
    }

    public void setCurrentTargetId(int currentTargetId) {
        this.currentTargetId = currentTargetId;
    }

    public void setTargets(ArrayList<Target> targets) {
        this.targets = targets;
    }

    public ArrayList<Target> getTargets() {
        return targets;
    }

    public Target getCurrentTarget() {
        if (targets.size() == 0) return null;
        return targets.get(targets.size() - 1);
    }

    public Target getFirstTarget() {
        if (targets == null || targets.size() == 0) return null;
        return targets.get(0);
    }

//    public Target getTargetForDay(DateTime date) {
//        Target target;
//        for (Target t : targets) {
//            if (date.isBefore(t.getDateTime())) {
//
//            }
//        }
//
//        return target;
//    }

    public Target getTargetForId(int id){
        for (Target t : targets) {
            if (t.getTargetId() == id) return t;
        }
        return null;
    }
}
