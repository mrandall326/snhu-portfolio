/*
 * File:        UserDao.java
 * Project:     Scalable, a weight-tracking app (Option 3)
 * Course:      CS 360 Mobile Architecture and Programming
 *              Southern New Hampshire University
 * Author:      Matthew Randall
 * Created:     October 14, 2026
 * Version:     1.0
 *
 * Description: Reads and writes the users table: creating accounts, checking logins, and
 *              storing each user's text notification setting and phone number.
 */
package com.zybooks.randall_matthew_scalable;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.zybooks.randall_matthew_scalable.ScalableDatabase.UserTable;

/**
 * Data access for the users table.
 *
 * createAccount and authenticate hash passwords, which is deliberately slow, so they must be
 * called off the main thread. The other methods are quick single-row reads and writes.
 *
 * The SMS setting and phone number live here, one pair per user, rather than in a settings file
 * shared by the whole phone. That way two people who share a phone each choose for themselves
 * whether to get texts, and each goal text goes to the right person's number.
 *
 * @author Matthew Randall
 * @version 1.0
 */
public class UserDao {

    /** Returned in place of a user ID when there is no matching user. */
    public static final long NO_USER = -1L;

    private final ScalableDatabase database;

    /**
     * @param context any context in the app
     */
    public UserDao(Context context) {
        database = ScalableDatabase.getInstance(context);
    }

    /**
     * Creates a new account if the username is free. Run this off the main thread.
     *
     * @param username the username, already trimmed
     * @param password the password as typed; the caller clears the array afterward
     * @return the new user's ID, or NO_USER if that username is already taken
     */
    public long createAccount(String username, char[] password) {
        // Checking first skips the slow hash when the name is taken.
        if (usernameExists(username)) {
            return NO_USER;
        }
        ContentValues values = new ContentValues();
        values.put(UserTable.COL_USERNAME, username);
        values.put(UserTable.COL_PASSWORD_HASH, PasswordHasher.hash(password));

        // The username column is also UNIQUE, so the database itself refuses a duplicate if
        // one slipped past the check above. insert() reports that refusal by returning -1.
        long id = database.getWritableDatabase().insert(UserTable.TABLE, null, values);
        return id == -1L ? NO_USER : id;
    }

    /**
     * @param username the username to look for; capitalization doesn't matter
     * @return true if an account already uses that username
     */
    public boolean usernameExists(String username) {
        try (Cursor cursor = database.getReadableDatabase().query(UserTable.TABLE,
                new String[]{UserTable.COL_ID}, UserTable.COL_USERNAME + " = ?",
                new String[]{username}, null, null, null)) {
            return cursor.moveToFirst();
        }
    }

    /**
     * Checks a username and password against the users table. Run this off the main thread.
     *
     * @param username the username, already trimmed; capitalization doesn't matter
     * @param password the password as typed; the caller clears the array afterward
     * @return the user's ID if both match, or NO_USER if the username doesn't exist or the
     *         password is wrong (the caller shows the same message either way)
     */
    public long authenticate(String username, char[] password) {
        long userId = NO_USER;
        String storedHash = null;

        // The username is passed as a selection argument (the ?), never pasted into the SQL,
        // so whatever the user types can't change the query.
        try (Cursor cursor = database.getReadableDatabase().query(UserTable.TABLE,
                new String[]{UserTable.COL_ID, UserTable.COL_PASSWORD_HASH},
                UserTable.COL_USERNAME + " = ?", new String[]{username},
                null, null, null)) {
            if (cursor.moveToFirst()) {
                userId = cursor.getLong(0);
                storedHash = cursor.getString(1);
            }
        }
        return PasswordHasher.verify(password, storedHash) ? userId : NO_USER;
    }

    /**
     * @param userId the signed-in user
     * @return true if that user turned on goal texts
     */
    public boolean isSmsEnabled(long userId) {
        try (Cursor cursor = queryUser(userId, UserTable.COL_SMS_ENABLED)) {
            return cursor.moveToFirst() && cursor.getInt(0) == 1;
        }
    }

    /**
     * @param userId  the signed-in user
     * @param enabled true to turn goal texts on, false to turn them off
     */
    public void setSmsEnabled(long userId, boolean enabled) {
        ContentValues values = new ContentValues();
        values.put(UserTable.COL_SMS_ENABLED, enabled ? 1 : 0);
        updateUser(userId, values);
    }

    /**
     * @param userId the signed-in user
     * @return that user's 10-digit phone number for goal texts, or an empty string if none
     */
    public String getSmsPhone(long userId) {
        try (Cursor cursor = queryUser(userId, UserTable.COL_SMS_PHONE)) {
            return cursor.moveToFirst() ? cursor.getString(0) : "";
        }
    }

    /**
     * @param userId the signed-in user
     * @param phone  a complete 10-digit number, or an empty string to clear it
     */
    public void setSmsPhone(long userId, String phone) {
        ContentValues values = new ContentValues();
        values.put(UserTable.COL_SMS_PHONE, phone);
        updateUser(userId, values);
    }

    /** Reads one column from one user's row. The caller closes the cursor. */
    private Cursor queryUser(long userId, String column) {
        return database.getReadableDatabase().query(UserTable.TABLE, new String[]{column},
                UserTable.COL_ID + " = ?", new String[]{String.valueOf(userId)},
                null, null, null);
    }

    /** Writes the given columns to one user's row. */
    private void updateUser(long userId, ContentValues values) {
        database.getWritableDatabase().update(UserTable.TABLE, values,
                UserTable.COL_ID + " = ?", new String[]{String.valueOf(userId)});
    }
}
