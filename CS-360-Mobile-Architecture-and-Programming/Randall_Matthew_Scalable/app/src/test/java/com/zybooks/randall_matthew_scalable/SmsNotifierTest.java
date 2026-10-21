/*
 * File:        SmsNotifierTest.java
 * Project:     Scalable, a weight-tracking app (Option 3)
 * Course:      CS 360 Mobile Architecture and Programming
 *              Southern New Hampshire University
 * Author:      Matthew Randall
 * Created:     October 2, 2026
 * Version:     1.0
 *
 * Description: Unit tests for the phone number check in SmsNotifier, which decides whether a
 *              typed number is complete enough to be saved and texted.
 */
package com.zybooks.randall_matthew_scalable;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Tests SmsNotifier.toValidPhone(), the check between what the user types in the phone field
 * and the number a goal text is sent to. The check only works with text, so these tests run on
 * the computer without needing a device or emulator.
 *
 * @author Matthew Randall
 * @version 1.0
 */
public class SmsNotifierTest {

    @Test
    public void formattedNumberIsReducedToDigits() {
        assertEquals("5551234567", SmsNotifier.toValidPhone("(555) 123-4567"));
    }

    @Test
    public void leadingCountryCodeIsDropped() {
        assertEquals("5551234567", SmsNotifier.toValidPhone("1 555-123-4567"));
        assertEquals("5551234567", SmsNotifier.toValidPhone("+1 555-123-4567"));
    }

    @Test
    public void numberThatIsTooShortIsRejected() {
        assertEquals("", SmsNotifier.toValidPhone("555-1234"));
    }

    @Test
    public void numberThatIsTooLongIsRejected() {
        // Eleven digits that don't start with the country code 1 can't be a U.S. number.
        assertEquals("", SmsNotifier.toValidPhone("55512345678"));
    }

    @Test
    public void emptyFieldIsRejected() {
        assertEquals("", SmsNotifier.toValidPhone(""));
    }
}
