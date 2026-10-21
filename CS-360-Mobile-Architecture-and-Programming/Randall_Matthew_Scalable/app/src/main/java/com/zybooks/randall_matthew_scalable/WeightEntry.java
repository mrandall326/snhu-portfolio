/*
 * File:        WeightEntry.java
 * Project:     Scalable, a weight-tracking app (Option 3)
 * Course:      CS 360 Mobile Architecture and Programming
 *              Southern New Hampshire University
 * Author:      Matthew Randall
 * Created:     October 1, 2026
 * Version:     2.0
 *
 * Description: Model for one weigh-in (date and weight), plus the shared formatting used
 *              to display weights and dates the same way on every screen.
 */
package com.zybooks.randall_matthew_scalable;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * One weigh-in: a date and a weight in pounds.
 *
 * This mirrors a row in the daily_weight table, which holds _id, user_id, entry_date, and
 * weight. The user ID is left out here because the grid only ever shows the signed-in user's
 * entries, and WeightDao already filters by it.
 *
 * An entry never changes after it is created. Edits are saved to the database through WeightDao,
 * and the grid then reloads fresh entries from it, so there is no second copy that could fall out
 * of step with what is actually saved.
 *
 * @author Matthew Randall
 * @version 2.0
 */
public class WeightEntry {

    /** Short date used in the grid, for example "Wed, Oct 1". */
    private static final DateTimeFormatter ROW_DATE =
            DateTimeFormatter.ofPattern("EEE, MMM d", Locale.getDefault());

    /** Longer date used in dialogs and messages, for example "Wed, Oct 1, 2026". */
    private static final DateTimeFormatter FULL_DATE =
            DateTimeFormatter.ofPattern("EEE, MMM d, yyyy", Locale.getDefault());

    private final long id;
    private final LocalDate date;
    private final float weight;

    /**
     * Creates a weigh-in.
     *
     * @param id     unique ID for the entry (_id in the daily_weight table)
     * @param date   the day the weight was recorded
     * @param weight the weight in pounds
     */
    public WeightEntry(long id, LocalDate date, float weight) {
        this.id = id;
        this.date = date;
        this.weight = weight;
    }

    /** @return the unique ID of this entry */
    public long getId() {
        return id;
    }

    /** @return the day this weight was recorded */
    public LocalDate getDate() {
        return date;
    }

    /** @return the weight in pounds */
    public float getWeight() {
        return weight;
    }

    /**
     * Formats a weight with exactly one decimal place, such as 197.4.
     *
     * @param weight the weight in pounds
     * @return the weight as display text, without the unit
     */
    public static String formatWeight(float weight) {
        return String.format(Locale.getDefault(), "%.1f", weight);
    }

    /**
     * Formats a date for a grid row, such as "Wed, Oct 1".
     *
     * @param date the date to format
     * @return the short display form of the date
     */
    public static String formatRowDate(LocalDate date) {
        return date.format(ROW_DATE);
    }

    /**
     * Formats a date for dialogs and messages, such as "Wed, Oct 1, 2026".
     *
     * @param date the date to format
     * @return the full display form of the date
     */
    public static String formatFullDate(LocalDate date) {
        return date.format(FULL_DATE);
    }
}
