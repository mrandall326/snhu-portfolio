/*
 * File:        WeightDao.java
 * Project:     Scalable, a weight-tracking app (Option 3)
 * Course:      CS 360 Mobile Architecture and Programming
 *              Southern New Hampshire University
 * Author:      Matthew Randall
 * Created:     October 14, 2026
 * Version:     1.0
 *
 * Description: Reads and writes the daily_weight table: loading a user's weigh-ins for the grid,
 *              and adding, updating, deleting, and restoring them.
 */
package com.zybooks.randall_matthew_scalable;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.zybooks.randall_matthew_scalable.ScalableDatabase.WeightTable;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Data access for the daily_weight table, covering all four database operations the grid needs:
 * create (add a weigh-in), read (load the grid), update (change a weigh-in's date or weight), and
 * delete (remove a weigh-in, with a way to put it back for Undo).
 *
 * Every query and change includes the user's ID in its WHERE clause, so one user's actions can
 * only ever touch their own rows, even if an entry ID from someone else were passed in.
 *
 * @author Matthew Randall
 * @version 1.0
 */
public class WeightDao {

    private final ScalableDatabase database;

    /**
     * @param context any context in the app
     */
    public WeightDao(Context context) {
        database = ScalableDatabase.getInstance(context);
    }

    /**
     * Loads all of a user's weigh-ins, newest first, which is the order the grid shows them.
     *
     * @param userId the signed-in user
     * @return the weigh-ins, or an empty list if there are none yet
     */
    public List<WeightEntry> getEntries(long userId) {
        List<WeightEntry> entries = new ArrayList<>();
        try (Cursor cursor = database.getReadableDatabase().query(WeightTable.TABLE,
                new String[]{WeightTable.COL_ID, WeightTable.COL_DATE, WeightTable.COL_WEIGHT},
                WeightTable.COL_USER_ID + " = ?", new String[]{String.valueOf(userId)},
                null, null, WeightTable.COL_DATE + " DESC")) {
            while (cursor.moveToNext()) {
                entries.add(new WeightEntry(
                        cursor.getLong(0),
                        LocalDate.ofEpochDay(cursor.getLong(1)),
                        cursor.getFloat(2)));
            }
        }
        return entries;
    }

    /**
     * Adds a new weigh-in or updates an existing one, optionally removing a different weigh-in
     * that already holds the same date. Both steps run in one transaction, so the user never ends
     * up with the old entry deleted but the new one missing, or the other way around.
     *
     * @param userId     the signed-in user
     * @param entryId    the ID of the weigh-in being edited, or -1 to add a new one
     * @param replacedId the ID of another weigh-in on the same date that the user agreed to
     *                   replace, or -1 if there isn't one
     * @param date       the weigh-in date
     * @param weight     the weight in pounds
     * @return true if the weigh-in was saved
     */
    public boolean saveEntry(long userId, long entryId, long replacedId, LocalDate date, float weight) {
        SQLiteDatabase db = database.getWritableDatabase();
        db.beginTransaction();
        try {
            if (replacedId != -1L) {
                deleteRow(db, userId, replacedId);
            }

            ContentValues values = new ContentValues();
            values.put(WeightTable.COL_DATE, date.toEpochDay());
            values.put(WeightTable.COL_WEIGHT, weight);

            boolean saved;
            if (entryId == -1L) {
                values.put(WeightTable.COL_USER_ID, userId);
                saved = db.insert(WeightTable.TABLE, null, values) != -1L;
            } else {
                saved = db.update(WeightTable.TABLE, values, ownRow(), args(userId, entryId)) == 1;
            }

            // Only keep the changes if the save itself worked; otherwise the delete above is
            // rolled back too.
            if (saved) {
                db.setTransactionSuccessful();
            }
            return saved;
        } finally {
            db.endTransaction();
        }
    }

    /**
     * Removes one weigh-in.
     *
     * @param userId  the signed-in user
     * @param entryId the weigh-in to remove
     */
    public void deleteEntry(long userId, long entryId) {
        deleteRow(database.getWritableDatabase(), userId, entryId);
    }

    /**
     * Puts a deleted weigh-in back with its original ID, date, and weight. Used by Undo.
     * If another weigh-in was added on that date in the meantime, the restore is skipped rather
     * than overwriting the newer entry.
     *
     * @param userId the signed-in user
     * @param entry  the weigh-in that was deleted
     * @return true if it was restored
     */
    public boolean restoreEntry(long userId, WeightEntry entry) {
        ContentValues values = new ContentValues();
        values.put(WeightTable.COL_ID, entry.getId());
        values.put(WeightTable.COL_USER_ID, userId);
        values.put(WeightTable.COL_DATE, entry.getDate().toEpochDay());
        values.put(WeightTable.COL_WEIGHT, entry.getWeight());
        // The UNIQUE (user_id, entry_date) rule refuses the row if that date is taken again,
        // and insert() reports the refusal by returning -1.
        return database.getWritableDatabase().insert(WeightTable.TABLE, null, values) != -1L;
    }

    private static void deleteRow(SQLiteDatabase db, long userId, long entryId) {
        db.delete(WeightTable.TABLE, ownRow(), args(userId, entryId));
    }

    /** WHERE clause that matches one weigh-in, and only if it belongs to the given user. */
    private static String ownRow() {
        return WeightTable.COL_ID + " = ? AND " + WeightTable.COL_USER_ID + " = ?";
    }

    /** Arguments for ownRow(), in the same order as its two question marks. */
    private static String[] args(long userId, long entryId) {
        return new String[]{String.valueOf(entryId), String.valueOf(userId)};
    }
}
