/*
 * File:        AppContextTest.java
 * Project:     Scalable, a weight-tracking app (Option 3)
 * Course:      CS 360 Mobile Architecture and Programming
 *              Southern New Hampshire University
 * Author:      Matthew Randall
 * Created:     October 2, 2026
 * Version:     1.0
 *
 * Description: Instrumented test that runs on a device or emulator and confirms the app
 *              installs under its expected package name.
 */
package com.zybooks.randall_matthew_scalable;

import static org.junit.Assert.assertEquals;

import android.content.Context;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * A quick check, run on a device or emulator, that the installed app is Scalable under the
 * package name it is built with.
 *
 * @author Matthew Randall
 * @version 1.0
 */
@RunWith(AndroidJUnit4.class)
public class AppContextTest {

    @Test
    public void appUsesExpectedPackageName() {
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        assertEquals("com.zybooks.randall_matthew_scalable", appContext.getPackageName());
    }
}
