/*
 * File:        PasswordHasherTest.java
 * Project:     Scalable, a weight-tracking app (Option 3)
 * Course:      CS 360 Mobile Architecture and Programming
 *              Southern New Hampshire University
 * Author:      Matthew Randall
 * Created:     October 14, 2026
 * Version:     1.0
 *
 * Description: Unit tests for PasswordHasher, which hashes new passwords and checks typed
 *              passwords against the hashes stored in the users table.
 */
package com.zybooks.randall_matthew_scalable;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Tests PasswordHasher. It only uses standard Java, so these tests run on the computer without
 * needing a device or emulator.
 *
 * @author Matthew Randall
 * @version 1.0
 */
public class PasswordHasherTest {

    @Test
    public void correctPasswordIsAccepted() {
        String stored = PasswordHasher.hash("scale123".toCharArray());
        assertTrue(PasswordHasher.verify("scale123".toCharArray(), stored));
    }

    @Test
    public void wrongPasswordIsRejected() {
        String stored = PasswordHasher.hash("scale123".toCharArray());
        assertFalse(PasswordHasher.verify("scale124".toCharArray(), stored));
        // Passwords are case sensitive, unlike usernames.
        assertFalse(PasswordHasher.verify("SCALE123".toCharArray(), stored));
    }

    @Test
    public void storedValueNeverContainsThePassword() {
        String stored = PasswordHasher.hash("scale123".toCharArray());
        assertFalse(stored.contains("scale123"));
    }

    @Test
    public void storedValueHasIterationsSaltAndHash() {
        String[] parts = PasswordHasher.hash("scale123".toCharArray()).split(":");
        assertEquals(3, parts.length);
        assertEquals(String.valueOf(PasswordHasher.ITERATIONS), parts[0]);
    }

    @Test
    public void samePasswordGetsADifferentHashEachTime() {
        // Each account gets its own random salt, so matching passwords don't produce matching
        // hashes, and one cracked hash says nothing about another account.
        String first = PasswordHasher.hash("scale123".toCharArray());
        String second = PasswordHasher.hash("scale123".toCharArray());
        assertNotEquals(first, second);
        assertTrue(PasswordHasher.verify("scale123".toCharArray(), first));
        assertTrue(PasswordHasher.verify("scale123".toCharArray(), second));
    }

    @Test
    public void missingOrDamagedStoredValueIsRejected() {
        assertFalse(PasswordHasher.verify("scale123".toCharArray(), null));
        assertFalse(PasswordHasher.verify("scale123".toCharArray(), ""));
        assertFalse(PasswordHasher.verify("scale123".toCharArray(), "not-a-hash"));
        assertFalse(PasswordHasher.verify("scale123".toCharArray(), "abc:!!!:???"));
    }
}
