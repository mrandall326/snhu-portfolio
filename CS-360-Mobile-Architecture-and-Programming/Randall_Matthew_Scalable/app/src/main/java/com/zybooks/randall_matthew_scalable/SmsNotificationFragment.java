/*
 * File:        SmsNotificationFragment.java
 * Project:     Scalable, a weight-tracking app (Option 3)
 * Course:      CS 360 Mobile Architecture and Programming
 *              Southern New Hampshire University
 * Author:      Matthew Randall
 * Created:     October 1, 2026
 * Version:     1.0
 *
 * Description: Text notification settings screen. Walks the user through the SEND_SMS
 *              permission request and shows a status that matches their answer.
 */
package com.zybooks.randall_matthew_scalable;

import android.Manifest;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.telephony.PhoneNumberUtils;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.CompoundButton;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.zybooks.randall_matthew_scalable.databinding.FragmentSmsBinding;

/**
 * SMS notification screen, where the user chooses whether Scalable may text them when they
 * reach their goal weight.
 *
 * How the permission flow works:
 * 1. The switch starts off. Nothing is requested until the user turns it on.
 * 2. When the switch is turned on, the app checks whether SEND_SMS is already granted.
 *    If it is, the setting turns on right away.
 * 3. If it isn't, and Android says an explanation is appropriate (the user declined once
 *    before), a short dialog explains why the app is asking before Android's own prompt
 *    appears. Otherwise Android's prompt appears directly.
 * 4. If the user allows it, the status reads "On." If the user denies it, the switch flips
 *    back off and the status explains that the app still works but won't send texts.
 * 5. If the user has denied it permanently, Android won't show the prompt again, so the
 *    status explains that and an "Open app settings" button appears.
 * 6. A text can't be sent without somewhere to send it, so while the setting is on and there
 *    is no complete phone number, the status asks for one instead of reading "On." It switches
 *    to "On" as soon as a full 10-digit number is typed. The field formats the number as the
 *    user types, and shows an error if they leave the field or press Done with a number that is
 *    too short or too long.
 *
 * The setting and phone number belong to the signed-in user (see SmsNotifier), while the
 * permission itself belongs to the phone. So a second user on the same phone may find the
 * permission already granted, and only needs to turn the switch on and enter their number.
 *
 * The status is re-checked every time the screen is shown, so if the user changes the
 * permission in Android settings and comes back, the screen reflects it.
 *
 * @author Matthew Randall
 * @version 1.0
 */
public class SmsNotificationFragment extends Fragment {

    public static final String ARG_GOAL_WEIGHT = "goal_weight";

    /** Phone numbers are formatted and checked as U.S. numbers. */
    private static final String PHONE_REGION = "US";

    /** The possible states the status card can describe. */
    private enum Status { OFF, ON, NEEDS_PHONE, DENIED, BLOCKED, UNSUPPORTED }

    private FragmentSmsBinding binding;

    /** Set after a denial when Android will no longer show the permission prompt. */
    private boolean permanentlyDenied;

    /**
     * Set when the user answers "Don't allow." Android pauses and resumes this screen around its
     * prompt, so this flag keeps the "Permission denied" status from being replaced by the plain
     * "Off" status when onResume runs right after the answer comes back.
     */
    private boolean userDenied;

    /** Android's permission prompt. The callback receives the user's answer. */
    private final ActivityResultLauncher<String> permissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                if (binding == null) {
                    return;
                }
                if (granted) {
                    permanentlyDenied = false;
                    userDenied = false;
                    SmsNotifier.setEnabled(requireContext(), true);
                    showEnabledStatus();
                } else {
                    // If Android won't explain again, the user chose "Don't allow" for good
                    // and the only way back is through Android settings.
                    permanentlyDenied = !shouldShowRequestPermissionRationale(Manifest.permission.SEND_SMS);
                    userDenied = true;
                    SmsNotifier.setEnabled(requireContext(), false);
                    setSwitchQuietly(false);
                    showStatus(permanentlyDenied ? Status.BLOCKED : Status.DENIED);
                }
            });

    /** Reacts to the user flipping the switch. */
    private final CompoundButton.OnCheckedChangeListener switchListener = (button, isChecked) -> {
        if (isChecked) {
            requestSmsPermission();
        } else {
            SmsNotifier.setEnabled(requireContext(), false);
            showStatus(Status.OFF);
        }
    };

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentSmsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Message preview with the user's real goal, or a sample goal if none is set yet.
        float goal = getArguments() != null ? getArguments().getFloat(ARG_GOAL_WEIGHT, 0f) : 0f;
        binding.smsPreview.setText(getString(R.string.sms_preview,
                WeightEntry.formatWeight(goal > 0f ? goal : 185.0f)));

        // Restore the saved phone number, shown in the usual (555) 123-4567 format.
        // The saved number is checked again here, so anything that isn't a complete number is
        // cleared rather than shown as if it were valid.
        String savedPhone = SmsNotifier.toValidPhone(SmsNotifier.getPhone(requireContext()));
        SmsNotifier.setPhone(requireContext(), savedPhone);
        if (!savedPhone.isEmpty()) {
            binding.phoneInput.setText(PhoneNumberUtils.formatNumber(savedPhone, PHONE_REGION));
        }

        // Add the parentheses, space, and dash automatically as the user types digits.
        binding.phoneInput.addTextChangedListener(new PhoneNumberFormattingTextWatcher(PHONE_REGION));

        // Save the number as the user types, but only once it is a complete phone number.
        // Until then an empty number is saved, so a partial number can never be texted.
        binding.phoneInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable s) {
                binding.phoneLayout.setError(null);
                SmsNotifier.setPhone(requireContext(), SmsNotifier.toValidPhone(s.toString()));
                // While texts are on, finishing or clearing the number changes whether one can
                // be sent, so the status follows along as the user types.
                if (SmsNotifier.isEnabled(requireContext())) {
                    showEnabledStatus();
                }
            }
        });

        // An error only appears once the user is done with the field, either by leaving it or by
        // pressing Done, so it doesn't flash red on every digit while the number is half typed.
        binding.phoneInput.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                checkPhoneComplete();
            }
        });
        binding.phoneInput.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                checkPhoneComplete();
            }
            // Returning false lets the keyboard close as it normally would.
            return false;
        });

        binding.smsSwitch.setOnCheckedChangeListener(switchListener);
        binding.openSettingsButton.setOnClickListener(v -> openAppSettings());
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshFromSystem();
    }

    /**
     * Brings the switch and status in line with reality: whether the device can text at all,
     * whether the user turned the setting on, and whether Android still grants the permission.
     */
    private void refreshFromSystem() {
        if (!SmsNotifier.deviceSupportsSms(requireContext())) {
            setSwitchQuietly(false);
            binding.smsSwitch.setEnabled(false);
            showStatus(Status.UNSUPPORTED);
            return;
        }

        boolean granted = SmsNotifier.hasPermission(requireContext());
        boolean enabled = SmsNotifier.isEnabled(requireContext());

        if (enabled && !granted) {
            // The setting was on, but the permission was taken away in Android settings.
            SmsNotifier.setEnabled(requireContext(), false);
            setSwitchQuietly(false);
            showStatus(Status.DENIED);
        } else if (enabled) {
            setSwitchQuietly(true);
            showEnabledStatus();
        } else if (permanentlyDenied && !granted) {
            setSwitchQuietly(false);
            showStatus(Status.BLOCKED);
        } else if (userDenied && !granted) {
            setSwitchQuietly(false);
            showStatus(Status.DENIED);
        } else {
            setSwitchQuietly(false);
            showStatus(Status.OFF);
        }
    }

    /** Called when the switch is turned on. Checks first, explains if needed, then asks. */
    private void requestSmsPermission() {
        if (SmsNotifier.hasPermission(requireContext())) {
            SmsNotifier.setEnabled(requireContext(), true);
            showEnabledStatus();
            return;
        }
        if (shouldShowRequestPermissionRationale(Manifest.permission.SEND_SMS)) {
            // The user said no once before, so explain why before Android asks again.
            new MaterialAlertDialogBuilder(requireContext())
                    .setIcon(R.drawable.ic_sms)
                    .setTitle(R.string.sms_rationale_title)
                    .setMessage(R.string.sms_rationale_message)
                    .setPositiveButton(R.string.sms_rationale_continue,
                            (dialog, which) -> permissionLauncher.launch(Manifest.permission.SEND_SMS))
                    .setNegativeButton(R.string.sms_rationale_not_now, (dialog, which) -> {
                        userDenied = false;
                        setSwitchQuietly(false);
                        showStatus(Status.OFF);
                    })
                    .setCancelable(false)
                    .show();
        } else {
            permissionLauncher.launch(Manifest.permission.SEND_SMS);
        }
    }

    /**
     * Shows an error under the phone field if something was typed but it isn't a complete
     * 10-digit number. An empty field is left alone, since the status already asks for a number.
     */
    private void checkPhoneComplete() {
        if (binding == null) {
            return;
        }
        Editable text = binding.phoneInput.getText();
        String typed = text == null ? "" : text.toString().trim();
        if (!typed.isEmpty() && SmsNotifier.toValidPhone(typed).isEmpty()) {
            binding.phoneLayout.setError(getString(R.string.sms_phone_error));
        }
    }

    /**
     * Shows the status for a setting that is on and allowed. It only reads "On" when there is
     * also a complete phone number to send to, since SmsNotifier won't send a text without one.
     */
    private void showEnabledStatus() {
        boolean hasPhone = !SmsNotifier.getPhone(requireContext()).isEmpty();
        showStatus(hasPhone ? Status.ON : Status.NEEDS_PHONE);
    }

    /** Updates the status card's sentence and shows the settings button only when it helps. */
    private void showStatus(Status status) {
        if (binding == null) {
            return;
        }
        int message;
        switch (status) {
            case ON:
                message = R.string.sms_status_on;
                break;
            case NEEDS_PHONE:
                message = R.string.sms_status_needs_phone;
                break;
            case DENIED:
                message = R.string.sms_status_denied;
                break;
            case BLOCKED:
                message = R.string.sms_status_blocked;
                break;
            case UNSUPPORTED:
                message = R.string.sms_status_unsupported;
                break;
            case OFF:
            default:
                message = R.string.sms_status_off;
                break;
        }
        binding.smsStatus.setText(message);
        binding.openSettingsButton.setVisibility(status == Status.BLOCKED ? View.VISIBLE : View.GONE);

        // The phone number only matters while texts are on, so it is only editable then.
        boolean on = status == Status.ON || status == Status.NEEDS_PHONE;
        binding.phoneLayout.setEnabled(on);
        binding.phoneInput.setEnabled(on);
    }

    /** Changes the switch without triggering its listener, so code changes don't re-ask. */
    private void setSwitchQuietly(boolean checked) {
        binding.smsSwitch.setOnCheckedChangeListener(null);
        binding.smsSwitch.setChecked(checked);
        binding.smsSwitch.setOnCheckedChangeListener(switchListener);
    }

    /** Opens Android's settings page for Scalable so the user can allow SMS there. */
    private void openAppSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.fromParts("package", requireContext().getPackageName(), null));
        startActivity(intent);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
