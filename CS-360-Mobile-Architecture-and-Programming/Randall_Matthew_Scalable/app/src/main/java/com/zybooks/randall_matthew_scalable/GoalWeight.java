/*
 * File:        GoalWeight.java
 * Project:     Scalable, a weight-tracking app (Option 3)
 * Course:      CS 360 Mobile Architecture and Programming
 *              Southern New Hampshire University
 * Author:      Matthew Randall
 * Created:     October 14, 2026
 * Version:     1.0
 *
 * Description: Model for a user's goal weight, matching one row of the goal_weight table, plus
 *              the rules for when the goal counts as reached and how far along the user is.
 */
package com.zybooks.randall_matthew_scalable;

/**
 * A user's goal weight and where they started from when they set it.
 *
 * Scalable never assumes the user is trying to lose weight. When a goal is saved, the latest
 * weigh-in becomes the starting point, and comparing the two decides the direction once: working
 * down to the goal or working up to it. Locking the direction in means later weigh-ins can't flip
 * it, so "reached" always means arriving at the goal from the side the user started on.
 *
 * If a goal is saved before there are any weigh-ins, there is no starting point yet. The first
 * weigh-in added after that becomes the start (see WeightLogFragment).
 *
 * This class has no Android code in it, so its rules are unit tested in GoalWeightTest.
 *
 * @author Matthew Randall
 * @version 1.0
 */
public class GoalWeight {

    private final float goal;
    private final float startWeight;
    private final boolean workingDown;

    /**
     * Creates a goal from values already decided, such as a row read from the database.
     *
     * @param goal        the goal weight in pounds
     * @param startWeight the latest weigh-in when the goal was set, or 0 if there wasn't one
     * @param workingDown true if the user is heading down to the goal, false if heading up
     */
    public GoalWeight(float goal, float startWeight, boolean workingDown) {
        this.goal = goal;
        this.startWeight = startWeight;
        this.workingDown = workingDown;
    }

    /**
     * Creates a new goal using the latest weigh-in as the starting point. A goal equal to the
     * latest weight counts as working down, so the very next weigh-in at or below it reaches it.
     *
     * @param goal         the goal weight in pounds
     * @param latestWeight the newest weigh-in in pounds, or 0 if there are none yet
     * @return the new goal
     */
    public static GoalWeight startingFrom(float goal, float latestWeight) {
        return new GoalWeight(goal, latestWeight, latestWeight >= goal);
    }

    /** @return the goal weight in pounds */
    public float getGoal() {
        return goal;
    }

    /** @return the weigh-in the goal was measured from, or 0 if there wasn't one */
    public float getStartWeight() {
        return startWeight;
    }

    /** @return true if the user is heading down to the goal, false if heading up */
    public boolean isWorkingDown() {
        return workingDown;
    }

    /** @return true once there is a starting weigh-in to measure progress from */
    public boolean hasStart() {
        return startWeight > 0f;
    }

    /**
     * Checks whether a weigh-in has reached or passed the goal in the direction the user was
     * heading: at or below the goal when working down, at or above it when working up.
     *
     * @param latestWeight the newest weigh-in in pounds
     * @return true if that weigh-in reaches the goal
     */
    public boolean isReachedBy(float latestWeight) {
        if (!hasStart() || latestWeight <= 0f) {
            return false;
        }
        return workingDown ? latestWeight <= goal : latestWeight >= goal;
    }

    /**
     * Works out how much of the trip from the starting weight to the goal is done. A weigh-in
     * that moves away from the goal counts as 0 rather than going negative, and the result stops
     * at 99 until the goal is actually reached, so the bar never shows "100%" a pound early
     * because of rounding.
     *
     * @param latestWeight the newest weigh-in in pounds
     * @return a whole-number percent from 0 to 100
     */
    public int percentComplete(float latestWeight) {
        if (isReachedBy(latestWeight)) {
            return 100;
        }
        float totalTrip = Math.abs(startWeight - goal);
        if (!hasStart() || totalTrip == 0f) {
            return 0;
        }
        float covered = workingDown ? startWeight - latestWeight : latestWeight - startWeight;
        int percent = Math.round(Math.max(0f, covered) / totalTrip * 100f);
        return Math.min(percent, 99);
    }
}
