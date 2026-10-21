/*
 * File:        PasswordHasher.java
 * Project:     Scalable, a weight-tracking app (Option 3)
 * Course:      CS 360 Mobile Architecture and Programming
 *              Southern New Hampshire University
 * Author:      Matthew Randall
 * Created:     October 14, 2026
 * Version:     1.0
 *
 * Description: Turns a password into a salted PBKDF2 hash for the users table, and checks a
 *              typed password against a stored hash without ever keeping the password itself.
 */
package com.zybooks.randall_matthew_scalable;

import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * Password hashing for Scalable's accounts.
 *
 * Passwords are never written to the database. Instead, each one is run through PBKDF2 with
 * HMAC-SHA256, a slow, salted hash built for passwords, and only the result is saved. Every
 * account gets its own random salt, so two people who pick the same password still end up with
 * different hashes, and a precomputed table of common passwords is useless against the database.
 *
 * The stored value packs everything needed to check a password later into one string:
 *
 *     iterations:salt:hash      (salt and hash are Base64)
 *
 * Saving the iteration count with each hash means the count can be raised in a later version
 * without breaking existing accounts, since older hashes still carry the count they were made with.
 *
 * Hashing is deliberately slow, so callers run it off the main thread (see LoginFragment).
 * This class only uses standard Java, so it is unit tested on the computer in PasswordHasherTest.
 *
 * @author Matthew Randall
 * @version 1.0
 */
public final class PasswordHasher {

    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";

    /**
     * Number of hashing rounds. Higher is harder to brute-force but slower to sign in. This
     * count keeps sign-in to a fraction of a second on a current phone.
     */
    static final int ITERATIONS = 120_000;

    /** 16 random bytes (128 bits) of salt per account. */
    private static final int SALT_BYTES = 16;

    /** Length of the derived hash, 256 bits to match SHA-256. */
    private static final int HASH_BITS = 256;

    private static final String SEPARATOR = ":";

    /** Cryptographically strong random numbers for the salt. */
    private static final SecureRandom RANDOM = new SecureRandom();

    private PasswordHasher() {
        // Utility class; not meant to be created.
    }

    /**
     * Hashes a new password with a fresh random salt.
     *
     * @param password the password as typed; the caller should clear the array afterward
     * @return the value to store in the users table, in the form iterations:salt:hash
     */
    public static String hash(char[] password) {
        byte[] salt = new byte[SALT_BYTES];
        RANDOM.nextBytes(salt);
        byte[] hash = derive(password, salt, ITERATIONS, HASH_BITS);

        Base64.Encoder encoder = Base64.getEncoder();
        return ITERATIONS + SEPARATOR + encoder.encodeToString(salt)
                + SEPARATOR + encoder.encodeToString(hash);
    }

    /**
     * Checks a typed password against a stored hash by hashing it again with the stored salt
     * and iteration count and comparing the results.
     *
     * @param password the password as typed; the caller should clear the array afterward
     * @param stored   the value from the users table, or null if there is no such user
     * @return true only if the password matches
     */
    public static boolean verify(char[] password, String stored) {
        if (stored == null) {
            return false;
        }
        String[] parts = stored.split(SEPARATOR);
        if (parts.length != 3) {
            return false;
        }
        try {
            int iterations = Integer.parseInt(parts[0]);
            Base64.Decoder decoder = Base64.getDecoder();
            byte[] salt = decoder.decode(parts[1]);
            byte[] expected = decoder.decode(parts[2]);
            byte[] actual = derive(password, salt, iterations, expected.length * 8);

            // MessageDigest.isEqual takes the same time whether the first byte differs or the
            // last one does, so the comparison doesn't leak how close a guess was.
            return MessageDigest.isEqual(expected, actual);
        } catch (IllegalArgumentException e) {
            // A damaged stored value (bad number or bad Base64) can never match.
            return false;
        }
    }

    /**
     * Runs PBKDF2 and wipes the copy of the password held by the key spec when done.
     *
     * @param password   the password characters
     * @param salt       the salt for this account
     * @param iterations number of hashing rounds
     * @param bits       length of the result in bits
     * @return the derived hash bytes
     */
    private static byte[] derive(char[] password, byte[] salt, int iterations, int bits) {
        PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, bits);
        try {
            return SecretKeyFactory.getInstance(ALGORITHM).generateSecret(spec).getEncoded();
        } catch (GeneralSecurityException e) {
            // PBKDF2WithHmacSHA256 is built into every Android version Scalable supports, so
            // this only happens if the platform itself is broken.
            throw new IllegalStateException("Password hashing is not available", e);
        } finally {
            spec.clearPassword();
        }
    }
}
