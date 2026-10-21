/*
 * File:        SmsNotifier.java
 * Project:     Scalable, a weight-tracking app (Option 3)
 * Course:      CS 360 Mobile Architecture and Programming
 *              Southern New Hampshire University
 * Author:      Matthew Randall
 * Created:     October 1, 2026
 * Version:     2.0
 *
 * Description: Helper that reads and saves the signed-in user's SMS setting and phone number,
 *              checks every condition for sending (hardware, setting, number, permission),
 *              and sends the goal text.
 */
package com.zybooks.randall_matthew_scalable;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.telephony.SmsManager;

import androidx.core.content.ContextCompat;

/**
 * Everything Scalable needs to know before it sends a goal text, kept in one place.
 *
 * A text is only sent when all four of these are true: the device can send SMS at all, the
 * user turned the setting on, the user entered a complete phone number, and Android currently
 * grants the SEND_SMS permission. The permission is checked again right before every send, because the
 * user can revoke it in Android settings at any time. If any check fails, nothing is sent and
 * the app carries on normally, showing the goal message on screen instead.
 *
 * The on/off choice and the phone number belong to the signed-in user and are saved in their
 * row of the users table (through UserDao), so the setting is remembered between launches and
 * each person sharing a phone gets texts at their own number, or not at all if they opted out.
 * When nobody is signed in, texts count as off.
 *
 * @author Matthew Randall
 * @version 2.0
 */
public final class SmsNotifier {

    private SmsNotifier() {
        // Utility class; not meant to be created.
    }

    /**
     * Checks whether this device has the hardware to send text messages. Phones do, but many
     * tablets don't.
     *
     * @param context any context in the app
     * @return true if the device can send SMS
     */
    public static boolean deviceSupportsSms(Context context) {
        return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_TELEPHONY_MESSAGING);
    }

    /**
     * Checks whether the user currently grants the SEND_SMS permission.
     *
     * @param context any context in the app
     * @return true if the permission is granted right now
     */
    public static boolean hasPermission(Context context) {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS)
                == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * @param context any context in the app
     * @return true if the signed-in user turned on SMS notifications
     */
    public static boolean isEnabled(Context context) {
        long userId = SessionManager.getUserId(context);
        return userId != UserDao.NO_USER && new UserDao(context).isSmsEnabled(userId);
    }

    /**
     * Saves whether SMS notifications are turned on for the signed-in user.
     *
     * @param context any context in the app
     * @param enabled true to turn texts on, false to turn them off
     */
    public static void setEnabled(Context context, boolean enabled) {
        long userId = SessionManager.getUserId(context);
        if (userId != UserDao.NO_USER) {
            new UserDao(context).setSmsEnabled(userId, enabled);
        }
    }

    /**
     * @param context any context in the app
     * @return the signed-in user's 10-digit phone number for goal texts, or an empty string if
     *         no complete number has been entered
     */
    public static String getPhone(Context context) {
        long userId = SessionManager.getUserId(context);
        return userId == UserDao.NO_USER ? "" : new UserDao(context).getSmsPhone(userId);
    }

    /**
     * Saves the phone number that the signed-in user's goal texts are sent to. Callers pass the
     * result of toValidPhone(), so only a complete number or an empty string is ever stored.
     *
     * @param context any context in the app
     * @param phone   a 10-digit phone number, or an empty string to clear it
     */
    public static void setPhone(Context context, String phone) {
        long userId = SessionManager.getUserId(context);
        if (userId != UserDao.NO_USER) {
            new UserDao(context).setSmsPhone(userId, phone);
        }
    }

    /**
     * Checks a typed phone number and returns it as plain digits if it is a complete U.S. number.
     * Formatting characters like parentheses, spaces, and dashes are ignored, and a leading
     * country code of 1 is allowed and dropped, so "(555) 123-4567" and "1 555-123-4567" both
     * come back as "5551234567". Anything shorter or longer comes back empty, so an incomplete
     * number is never saved or texted.
     *
     * @param typed the phone number as the user typed it
     * @return the 10-digit number, or an empty string if it isn't a complete number
     */
    public static String toValidPhone(String typed) {
        String digits = typed.replaceAll("\\D", "");
        if (digits.length() == 11 && digits.startsWith("1")) {
            digits = digits.substring(1);
        }
        return digits.length() == 10 ? digits : "";
    }

    /**
     * Checks every condition for sending a text: hardware, setting, phone number, and permission.
     *
     * @param context any context in the app
     * @return true only when a text can be sent right now
     */
    public static boolean canSend(Context context) {
        return deviceSupportsSms(context)
                && isEnabled(context)
                && !getPhone(context).isEmpty()
                && hasPermission(context);
    }

    /**
     * Sends the goal-reached text if, and only if, canSend() allows it.
     *
     * @param context    any context in the app
     * @param goalWeight the goal the user reached, in pounds, included in the message
     * @return true if the text was handed to Android to send, false if it was skipped or failed
     */
    public static boolean sendGoalReached(Context context, float goalWeight) {
        if (!canSend(context)) {
            return false;
        }
        String message = context.getString(R.string.sms_preview, WeightEntry.formatWeight(goalWeight));
        try {
            SmsManager smsManager = context.getSystemService(SmsManager.class);
            if (smsManager == null) {
                return false;
            }
            smsManager.sendTextMessage(getPhone(context), null, message, null, null);
            return true;
        } catch (SecurityException | IllegalArgumentException e) {
            // Permission was pulled at the last moment, or the number is not valid. Either way
            // the app keeps working and the user still sees the message on screen.
            return false;
        }
    }
}
