/*
 * File:        LoginFragment.java
 * Project:     Scalable, a weight-tracking app (Option 3)
 * Course:      CS 360 Mobile Architecture and Programming
 *              Southern New Hampshire University
 * Author:      Matthew Randall
 * Created:     October 1, 2026
 * Version:     2.0
 *
 * Description: Login screen, which also creates new accounts. Checks the username and password
 *              against the users table, saves new accounts to it, and moves on to the weigh-in
 *              grid once the user is signed in.
 */
package com.zybooks.randall_matthew_scalable;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.zybooks.randall_matthew_scalable.databinding.FragmentLoginBinding;

import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Login screen, which also handles creating a new account.
 *
 * Sign in looks up the username in the users table and checks the password against the stored
 * hash. Create account first checks the username and password meet the minimum lengths, then
 * adds a new row to the users table if the username is free. Either way, a successful result
 * records the user in SessionManager and opens their weigh-in grid, and any problem is explained
 * in the message line under the password field instead of moving on.
 *
 * Password hashing is deliberately slow, so both checks run on a background thread. While one
 * is running, the fields and buttons are disabled so the user can't start a second attempt.
 *
 * The password is read out of the field as a char array rather than a String and wiped as soon
 * as the check finishes. A String can't be cleared and may stay in memory until the garbage
 * collector gets to it, while an array can be overwritten right away.
 *
 * @author Matthew Randall
 * @version 2.0
 */
public class LoginFragment extends Fragment {

    /** Shortest username a new account may use. */
    private static final int MIN_USERNAME_LENGTH = 3;

    /** Shortest password a new account may use. */
    private static final int MIN_PASSWORD_LENGTH = 8;

    private FragmentLoginBinding binding;
    private UserDao userDao;

    /** One background thread for password hashing, so attempts run one at a time. */
    private final ExecutorService hashingExecutor = Executors.newSingleThreadExecutor();

    /** Hands results from the background thread back to the main thread, which owns the views. */
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentLoginBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        userDao = new UserDao(requireContext());

        // Re-check the buttons every time either field changes, so they turn on the moment both
        // fields have something in them and turn back off if either is cleared.
        TextWatcher watcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable s) {
                updateButtons();
            }
        };
        binding.usernameInput.addTextChangedListener(watcher);
        binding.passwordInput.addTextChangedListener(watcher);

        // Pressing Done on the keyboard from the password field signs in, so a returning user
        // can go from typing to their history without lifting a thumb to the button.
        binding.passwordInput.setOnEditorActionListener((v, actionId, event) -> {
            boolean donePressed = actionId == EditorInfo.IME_ACTION_DONE
                    || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER);
            if (donePressed && binding.signInButton.isEnabled()) {
                signIn();
                return true;
            }
            return false;
        });

        binding.signInButton.setOnClickListener(v -> signIn());
        binding.createAccountButton.setOnClickListener(v -> createAccount());

        updateButtons();
    }

    /**
     * Enables both buttons only when the username and password are both filled in, and hides the
     * message line, since any message it showed was about what was typed before.
     */
    private void updateButtons() {
        boolean ready = !getUsername().isEmpty() && getPasswordLength() > 0;
        binding.signInButton.setEnabled(ready);
        binding.createAccountButton.setEnabled(ready);
        binding.loginMessage.setVisibility(View.GONE);
    }

    /** Checks the typed username and password against the users table. */
    private void signIn() {
        String username = getUsername();
        char[] password = getPassword();
        setBusy(true);
        hashingExecutor.execute(() -> {
            long userId = userDao.authenticate(username, password);
            Arrays.fill(password, '\0');
            mainHandler.post(() -> onSignInResult(userId));
        });
    }

    /**
     * Shows the result of a sign-in attempt. The same message is used whether the username
     * doesn't exist or the password is wrong, so the screen never confirms which usernames
     * have accounts.
     *
     * @param userId the signed-in user's ID, or UserDao.NO_USER if the check failed
     */
    private void onSignInResult(long userId) {
        if (binding == null) {
            return;
        }
        setBusy(false);
        if (userId == UserDao.NO_USER) {
            binding.passwordInput.setText(null);
            showMessage(getString(R.string.login_error_mismatch));
            return;
        }
        finishSignIn(userId);
    }

    /** Checks the new account's username and password, then saves it to the users table. */
    private void createAccount() {
        String username = getUsername();
        if (username.length() < MIN_USERNAME_LENGTH) {
            showMessage(getString(R.string.login_error_username_short, MIN_USERNAME_LENGTH));
            return;
        }
        if (getPasswordLength() < MIN_PASSWORD_LENGTH) {
            showMessage(getString(R.string.login_error_password_short, MIN_PASSWORD_LENGTH));
            return;
        }

        char[] password = getPassword();
        setBusy(true);
        hashingExecutor.execute(() -> {
            long userId = userDao.createAccount(username, password);
            Arrays.fill(password, '\0');
            mainHandler.post(() -> onCreateAccountResult(username, userId));
        });
    }

    /**
     * Shows the result of creating an account. A taken username leaves both fields filled in,
     * so a returning user who tapped the wrong button can just tap Sign in.
     *
     * @param username the username that was requested
     * @param userId   the new user's ID, or UserDao.NO_USER if the username was taken
     */
    private void onCreateAccountResult(String username, long userId) {
        if (binding == null) {
            return;
        }
        setBusy(false);
        if (userId == UserDao.NO_USER) {
            showMessage(getString(R.string.login_error_taken, username));
            return;
        }
        Toast.makeText(requireContext(),
                getString(R.string.login_message_account_created, username),
                Toast.LENGTH_SHORT).show();
        finishSignIn(userId);
    }

    /** Remembers the signed-in user and opens their weigh-in grid. */
    private void finishSignIn(long userId) {
        SessionManager.signIn(requireContext(), userId);
        NavHostFragment.findNavController(this).navigate(R.id.action_login_to_weightLog);
    }

    /**
     * Turns the fields and buttons off while a check runs in the background, and back on after.
     *
     * @param busy true while a sign-in or new account is being checked
     */
    private void setBusy(boolean busy) {
        binding.usernameInput.setEnabled(!busy);
        binding.passwordInput.setEnabled(!busy);
        if (busy) {
            binding.signInButton.setEnabled(false);
            binding.createAccountButton.setEnabled(false);
        } else {
            updateButtons();
        }
    }

    /** Shows a problem in the message line under the password field. */
    private void showMessage(String message) {
        binding.loginMessage.setText(message);
        binding.loginMessage.setVisibility(View.VISIBLE);
    }

    private String getUsername() {
        Editable text = binding.usernameInput.getText();
        return text == null ? "" : text.toString().trim();
    }

    /** Copies the typed password into a new char array, which the caller wipes when done. */
    private char[] getPassword() {
        Editable text = binding.passwordInput.getText();
        if (text == null) {
            return new char[0];
        }
        char[] password = new char[text.length()];
        text.getChars(0, text.length(), password, 0);
        return password;
    }

    private int getPasswordLength() {
        Editable text = binding.passwordInput.getText();
        return text == null ? 0 : text.length();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Stop the background thread when the screen is gone for good. A check already running
        // finishes on its own, and its result is ignored because binding is null by then.
        hashingExecutor.shutdown();
    }
}
