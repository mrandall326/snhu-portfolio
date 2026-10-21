/*
 * File:        SessionManager.java
 * Project:     Scalable, a weight-tracking app (Option 3)
 * Course:      CS 360 Mobile Architecture and Programming
 *              Southern New Hampshire University
 * Author:      Matthew Randall
 * Created:     October 14, 2026
 * Version:     1.0
 *
 * Description: Remembers which user is signed in, so the app opens straight to their weigh-ins
 *              until they choose Sign out.
 */
package com.zybooks.randall_matthew_scalable;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Keeps track of the signed-in user between launches.
 *
 * Only the user's ID is saved, in a private preferences file that other apps can't read. No
 * username or password is kept here. Every screen that reads or writes the database asks this
 * class whose data to use.
 *
 * @author Matthew Randall
 * @version 1.0
 */
public final class SessionManager {

    private static final String PREFS = "scalable_session";
    private static final String KEY_USER_ID = "signed_in_user_id";

    private SessionManager() {
        // Utility class; not meant to be created.
    }

    /**
     * @param context any context in the app
     * @return the signed-in user's ID, or UserDao.NO_USER if nobody is signed in
     */
    public static long getUserId(Context context) {
        return prefs(context).getLong(KEY_USER_ID, UserDao.NO_USER);
    }

    /**
     * @param context any context in the app
     * @return true if a user is signed in
     */
    public static boolean isSignedIn(Context context) {
        return getUserId(context) != UserDao.NO_USER;
    }

    /**
     * Records a successful sign-in or new account.
     *
     * @param context any context in the app
     * @param userId  the user who just signed in
     */
    public static void signIn(Context context, long userId) {
        prefs(context).edit().putLong(KEY_USER_ID, userId).apply();
    }

    /**
     * Forgets the signed-in user, so the login screen shows next time.
     *
     * @param context any context in the app
     */
    public static void signOut(Context context) {
        prefs(context).edit().remove(KEY_USER_ID).apply();
    }

    private static SharedPreferences prefs(Context context) {
        return context.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }
}
