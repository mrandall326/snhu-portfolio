/*
 * File:        GoalDao.java
 * Project:     Scalable, a weight-tracking app (Option 3)
 * Course:      CS 360 Mobile Architecture and Programming
 *              Southern New Hampshire University
 * Author:      Matthew Randall
 * Created:     October 14, 2026
 * Version:     1.0
 *
 * Description: Reads and writes the goal_weight table, which holds each user's goal weight and
 *              the starting point it is measured from.
 */
package com.zybooks.randall_matthew_scalable;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.zybooks.randall_matthew_scalable.ScalableDatabase.GoalTable;

/**
 * Data access for the goal_weight table. Each user has at most one goal, so saving a goal
 * replaces the old one instead of adding a second row.
 *
 * @author Matthew Randall
 * @version 1.0
 */
public class GoalDao {

    private final ScalableDatabase database;

    /**
     * @param context any context in the app
     */
    public GoalDao(Context context) {
        database = ScalableDatabase.getInstance(context);
    }

    /**
     * Loads a user's goal.
     *
     * @param userId the signed-in user
     * @return the goal, or null if the user hasn't set one
     */
    public GoalWeight getGoal(long userId) {
        try (Cursor cursor = database.getReadableDatabase().query(GoalTable.TABLE,
                new String[]{GoalTable.COL_GOAL, GoalTable.COL_START, GoalTable.COL_WORKING_DOWN},
                GoalTable.COL_USER_ID + " = ?", new String[]{String.valueOf(userId)},
                null, null, null)) {
            if (!cursor.moveToFirst()) {
                return null;
            }
            return new GoalWeight(cursor.getFloat(0), cursor.getFloat(1), cursor.getInt(2) == 1);
        }
    }

    /**
     * Saves a user's goal, replacing any goal they had before.
     *
     * @param userId the signed-in user
     * @param goal   the goal to save
     */
    public void saveGoal(long userId, GoalWeight goal) {
        ContentValues values = new ContentValues();
        values.put(GoalTable.COL_USER_ID, userId);
        values.put(GoalTable.COL_GOAL, goal.getGoal());
        values.put(GoalTable.COL_START, goal.getStartWeight());
        values.put(GoalTable.COL_WORKING_DOWN, goal.isWorkingDown() ? 1 : 0);
        // user_id is the primary key, so CONFLICT_REPLACE swaps out the existing row.
        database.getWritableDatabase().insertWithOnConflict(
                GoalTable.TABLE, null, values, SQLiteDatabase.CONFLICT_REPLACE);
    }
}
