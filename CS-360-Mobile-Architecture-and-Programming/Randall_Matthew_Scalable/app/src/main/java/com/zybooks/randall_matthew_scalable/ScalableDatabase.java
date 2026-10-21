/*
 * File:        ScalableDatabase.java
 * Project:     Scalable, a weight-tracking app (Option 3)
 * Course:      CS 360 Mobile Architecture and Programming
 *              Southern New Hampshire University
 * Author:      Matthew Randall
 * Created:     October 14, 2026
 * Version:     1.0
 *
 * Description: Creates and opens Scalable's SQLite database and defines its three tables:
 *              users, daily_weight, and goal_weight.
 */
package com.zybooks.randall_matthew_scalable;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * The SQLite database behind Scalable, stored privately on the phone as scalable.db.
 *
 * The database is created empty the first time the app runs. Users fill it themselves by
 * creating an account and logging weigh-ins, and everything stays on the phone between launches.
 *
 * Tables:
 * <pre>
 *   users          _id, username, password_hash, sms_enabled, sms_phone
 *   daily_weight   _id, user_id, entry_date, weight
 *   goal_weight    user_id, goal_weight, start_weight, working_down
 * </pre>
 *
 * Design choices:
 * - Every weigh-in and goal belongs to one user through user_id, so several people can share a
 *   phone and each sees only their own history.
 * - daily_weight allows one weigh-in per user per day (UNIQUE user_id, entry_date). The grid
 *   asks before replacing a day that already has one.
 * - goal_weight uses user_id as its primary key, so each user has at most one goal.
 * - Foreign keys are switched on, and deleting a user removes their weigh-ins and goal with them.
 * - Dates are stored as epoch days (days since January 1, 1970), which sort correctly as numbers
 *   and convert straight to and from java.time.LocalDate.
 * - Passwords are never stored, only a salted hash from PasswordHasher.
 *
 * The table and column names are constants in the nested classes below, so the DAO classes
 * that read and write each table (UserDao, WeightDao, GoalDao) never repeat them as loose strings.
 *
 * Only one instance is ever created (see getInstance), because SQLite works best when the whole
 * app shares a single connection.
 *
 * @author Matthew Randall
 * @version 1.0
 */
public final class ScalableDatabase extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "scalable.db";

    /** Raise this number whenever the table layout changes, and handle it in onUpgrade. */
    private static final int DATABASE_VERSION = 1;

    private static ScalableDatabase instance;

    /** Names for the users table, which holds one row per account. */
    static final class UserTable {
        static final String TABLE = "users";
        static final String COL_ID = "_id";
        static final String COL_USERNAME = "username";
        static final String COL_PASSWORD_HASH = "password_hash";
        static final String COL_SMS_ENABLED = "sms_enabled";
        static final String COL_SMS_PHONE = "sms_phone";

        private UserTable() { }
    }

    /** Names for the daily_weight table, which holds one row per weigh-in. */
    static final class WeightTable {
        static final String TABLE = "daily_weight";
        static final String COL_ID = "_id";
        static final String COL_USER_ID = "user_id";
        static final String COL_DATE = "entry_date";
        static final String COL_WEIGHT = "weight";

        private WeightTable() { }
    }

    /** Names for the goal_weight table, which holds at most one row per user. */
    static final class GoalTable {
        static final String TABLE = "goal_weight";
        static final String COL_USER_ID = "user_id";
        static final String COL_GOAL = "goal_weight";
        static final String COL_START = "start_weight";
        static final String COL_WORKING_DOWN = "working_down";

        private GoalTable() { }
    }

    /**
     * Returns the one shared database helper, creating it the first time it is needed.
     *
     * @param context any context; the application context is kept so no screen is leaked
     * @return the shared helper
     */
    public static synchronized ScalableDatabase getInstance(Context context) {
        if (instance == null) {
            instance = new ScalableDatabase(context.getApplicationContext());
        }
        return instance;
    }

    private ScalableDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /** SQLite leaves foreign keys off unless asked, so they are turned on for every connection. */
    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    /** Builds the three empty tables the first time the database is opened. */
    @Override
    public void onCreate(SQLiteDatabase db) {
        // COLLATE NOCASE makes "Matt" and "matt" the same username, so nobody can register a
        // look-alike of an existing account by changing capitalization.
        db.execSQL("CREATE TABLE " + UserTable.TABLE + " ("
                + UserTable.COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + UserTable.COL_USERNAME + " TEXT NOT NULL UNIQUE COLLATE NOCASE, "
                + UserTable.COL_PASSWORD_HASH + " TEXT NOT NULL, "
                + UserTable.COL_SMS_ENABLED + " INTEGER NOT NULL DEFAULT 0, "
                + UserTable.COL_SMS_PHONE + " TEXT NOT NULL DEFAULT '')");

        // The UNIQUE pair also gives SQLite an index on (user_id, entry_date), which is exactly
        // how the grid looks entries up.
        db.execSQL("CREATE TABLE " + WeightTable.TABLE + " ("
                + WeightTable.COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + WeightTable.COL_USER_ID + " INTEGER NOT NULL REFERENCES "
                + UserTable.TABLE + "(" + UserTable.COL_ID + ") ON DELETE CASCADE, "
                + WeightTable.COL_DATE + " INTEGER NOT NULL, "
                + WeightTable.COL_WEIGHT + " REAL NOT NULL, "
                + "UNIQUE (" + WeightTable.COL_USER_ID + ", " + WeightTable.COL_DATE + "))");

        db.execSQL("CREATE TABLE " + GoalTable.TABLE + " ("
                + GoalTable.COL_USER_ID + " INTEGER PRIMARY KEY REFERENCES "
                + UserTable.TABLE + "(" + UserTable.COL_ID + ") ON DELETE CASCADE, "
                + GoalTable.COL_GOAL + " REAL NOT NULL, "
                + GoalTable.COL_START + " REAL NOT NULL DEFAULT 0, "
                + GoalTable.COL_WORKING_DOWN + " INTEGER NOT NULL DEFAULT 1)");
    }

    /**
     * Called when DATABASE_VERSION goes up. Version 1 is the first released layout, so there is
     * nothing to migrate yet. A future version would add ALTER TABLE steps here, one per version,
     * so users keep their history through the update.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // No earlier versions exist.
    }
}
