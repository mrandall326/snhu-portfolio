/*
 * File:        GoalWeightTest.java
 * Project:     Scalable, a weight-tracking app (Option 3)
 * Course:      CS 360 Mobile Architecture and Programming
 *              Southern New Hampshire University
 * Author:      Matthew Randall
 * Created:     October 14, 2026
 * Version:     1.0
 *
 * Description: Unit tests for GoalWeight, which decides when a goal is reached and how much of
 *              the way there the user is, whether they are working down or up to it.
 */
package com.zybooks.randall_matthew_scalable;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Tests GoalWeight. It has no Android code in it, so these tests run on the computer without
 * needing a device or emulator.
 *
 * @author Matthew Randall
 * @version 1.0
 */
public class GoalWeightTest {

    @Test
    public void goalBelowStartMeansWorkingDown() {
        GoalWeight goal = GoalWeight.startingFrom(185f, 203f);
        assertTrue(goal.isWorkingDown());
        assertFalse(goal.isReachedBy(190f));
        assertTrue(goal.isReachedBy(185f));
        assertTrue(goal.isReachedBy(184.2f));
    }

    @Test
    public void goalAboveStartMeansWorkingUp() {
        GoalWeight goal = GoalWeight.startingFrom(150f, 135f);
        assertFalse(goal.isWorkingDown());
        assertFalse(goal.isReachedBy(149.8f));
        assertTrue(goal.isReachedBy(150f));
        assertTrue(goal.isReachedBy(151f));
    }

    @Test
    public void goalWithoutAStartingWeighInIsNeverReached() {
        GoalWeight goal = GoalWeight.startingFrom(185f, 0f);
        assertFalse(goal.hasStart());
        assertFalse(goal.isReachedBy(180f));
        assertEquals(0, goal.percentComplete(180f));
    }

    @Test
    public void percentCountsTheShareOfTheTripCovered() {
        GoalWeight goal = GoalWeight.startingFrom(180f, 200f);
        assertEquals(0, goal.percentComplete(200f));
        assertEquals(50, goal.percentComplete(190f));
        assertEquals(100, goal.percentComplete(180f));
    }

    @Test
    public void movingAwayFromTheGoalShowsZeroNotNegative() {
        GoalWeight goal = GoalWeight.startingFrom(180f, 200f);
        assertEquals(0, goal.percentComplete(205f));
    }

    @Test
    public void percentStopsAt99UntilTheGoalIsReached() {
        // 0.1 lb short of a 20 lb trip would round to 100 without the cap.
        GoalWeight goal = GoalWeight.startingFrom(180f, 200f);
        assertEquals(99, goal.percentComplete(180.1f));
    }
}
