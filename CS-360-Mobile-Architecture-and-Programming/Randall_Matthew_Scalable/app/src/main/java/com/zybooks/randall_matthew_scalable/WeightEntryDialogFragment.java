/*
 * File:        WeightEntryDialogFragment.java
 * Project:     Scalable, a weight-tracking app (Option 3)
 * Course:      CS 360 Mobile Architecture and Programming
 *              Southern New Hampshire University
 * Author:      Matthew Randall
 * Created:     October 1, 2026
 * Version:     1.0
 *
 * Description: Dialog for adding or editing a weigh-in, with a date picker, weight
 *              validation, and a Delete option in edit mode.
 */
package com.zybooks.randall_matthew_scalable;

import android.app.Dialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.WindowManager;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.DateValidatorPointBackward;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.zybooks.randall_matthew_scalable.databinding.DialogWeightEntryBinding;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;

/**
 * Dialog for adding a new weigh-in or editing an existing one.
 *
 * It opens over the grid so the user keeps their history in view. With no arguments it is in
 * add mode, titled "Add weigh-in", with today's date filled in. With an entry's ID, date, and
 * weight it is in edit mode, titled "Edit weigh-in", with those values filled in and an extra
 * Delete button.
 *
 * Save stays disabled until a weight is typed, so it can never be pressed with an empty
 * field, and a weight outside 50 to 700 lb is caught with a message under the field instead of
 * being saved. The result goes back to WeightLogFragment through the Fragment Result API.
 *
 * @author Matthew Randall
 * @version 1.0
 */
public class WeightEntryDialogFragment extends DialogFragment {

    // Argument names, matching the argument entries in nav_graph.xml.
    public static final String ARG_ID = "entry_id";
    public static final String ARG_EPOCH_DAY = "entry_epoch_day";
    public static final String ARG_WEIGHT = "entry_weight";

    // Result key and fields read by WeightLogFragment.
    public static final String RESULT_KEY = "weight_entry_result";
    public static final String RESULT_ID = "result_id";
    public static final String RESULT_EPOCH_DAY = "result_epoch_day";
    public static final String RESULT_WEIGHT = "result_weight";
    public static final String RESULT_DELETED = "result_deleted";

    /** Believable limits for an adult weigh-in in pounds, used to catch typos like 1974. */
    private static final float MIN_WEIGHT = 50f;
    private static final float MAX_WEIGHT = 700f;

    private static final String STATE_EPOCH_DAY = "state_epoch_day";
    private static final String DATE_PICKER_TAG = "date_picker";

    private DialogWeightEntryBinding binding;
    private long entryId = -1L;
    private LocalDate selectedDate;

    /**
     * onStart can run more than once (for example after returning to the app), so the listeners
     * are attached only once.
     */
    private boolean listenersAttached;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // LayoutInflater.from is used here because calling getLayoutInflater() inside
        // onCreateDialog would ask the dialog fragment to build its dialog again.
        binding = DialogWeightEntryBinding.inflate(LayoutInflater.from(requireContext()));

        // Read the arguments. An ID of -1 means this is a new entry.
        Bundle args = getArguments() != null ? getArguments() : new Bundle();
        entryId = args.getLong(ARG_ID, -1L);
        boolean editing = entryId != -1L;
        long epochDay = args.getLong(ARG_EPOCH_DAY, -1L);
        selectedDate = epochDay == -1L ? LocalDate.now() : LocalDate.ofEpochDay(epochDay);

        // Keep a date the user picked if the dialog is rebuilt, for example after a rotation.
        if (savedInstanceState != null) {
            selectedDate = LocalDate.ofEpochDay(
                    savedInstanceState.getLong(STATE_EPOCH_DAY, selectedDate.toEpochDay()));
        } else if (editing) {
            binding.weightInput.setText(WeightEntry.formatWeight(args.getFloat(ARG_WEIGHT)));
        }
        showDate();

        // Tapping the date field or its calendar icon opens the date picker.
        binding.dateInput.setOnClickListener(v -> openDatePicker());
        binding.dateLayout.setEndIconOnClickListener(v -> openDatePicker());

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext())
                .setTitle(editing ? R.string.entry_edit_title : R.string.entry_add_title)
                .setView(binding.getRoot())
                .setNegativeButton(R.string.cancel, null)
                // The real click handler is attached in onStart so an invalid weight can keep
                // the dialog open instead of closing it.
                .setPositiveButton(R.string.save, null);
        if (editing) {
            builder.setNeutralButton(R.string.delete, (dialog, which) -> sendDelete());
        }

        AlertDialog dialog = builder.create();
        // Bring up the number keyboard right away, since the weight is what the user came to type.
        if (dialog.getWindow() != null) {
            dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        }
        binding.weightInput.requestFocus();
        return dialog;
    }

    @Override
    public void onStart() {
        super.onStart();
        AlertDialog dialog = (AlertDialog) getDialog();
        if (dialog == null) {
            return;
        }
        Button saveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        if (listenersAttached || saveButton == null) {
            return;
        }
        listenersAttached = true;
        saveButton.setOnClickListener(v -> trySave());

        // Save is only enabled while the weight field has text in it.
        saveButton.setEnabled(!getWeightText().isEmpty());
        binding.weightInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable s) {
                saveButton.setEnabled(!getWeightText().isEmpty());
                binding.weightLayout.setError(null);
            }
        });

        // The keyboard's Done key saves, the same as tapping Save.
        binding.weightInput.setOnEditorActionListener((v, actionId, event) -> {
            if (saveButton.isEnabled()) {
                trySave();
            }
            return true;
        });
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong(STATE_EPOCH_DAY, selectedDate.toEpochDay());
    }

    /**
     * Opens the Material date picker. Future dates are blocked, since a weight can't be recorded
     * for a day that hasn't happened yet.
     */
    private void openDatePicker() {
        if (getChildFragmentManager().findFragmentByTag(DATE_PICKER_TAG) != null) {
            return;
        }
        CalendarConstraints constraints = new CalendarConstraints.Builder()
                .setValidator(DateValidatorPointBackward.now())
                .build();
        // The picker works in UTC milliseconds, so the date is converted at UTC midnight.
        long selection = selectedDate.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli();
        MaterialDatePicker<Long> picker = MaterialDatePicker.Builder.datePicker()
                .setTitleText(R.string.entry_pick_date)
                .setSelection(selection)
                .setCalendarConstraints(constraints)
                .build();
        picker.addOnPositiveButtonClickListener(millis -> {
            selectedDate = Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate();
            showDate();
        });
        picker.show(getChildFragmentManager(), DATE_PICKER_TAG);
    }

    private void showDate() {
        binding.dateInput.setText(WeightEntry.formatFullDate(selectedDate));
    }

    /** Checks the weight, then either shows an error under the field or sends the result. */
    private void trySave() {
        float weight;
        try {
            weight = Float.parseFloat(getWeightText());
        } catch (NumberFormatException e) {
            weight = -1f;
        }
        if (weight < MIN_WEIGHT || weight > MAX_WEIGHT) {
            binding.weightLayout.setError(getString(R.string.entry_weight_error));
            return;
        }
        // Round to one decimal place, matching how weights are shown everywhere else.
        weight = Math.round(weight * 10f) / 10f;

        Bundle result = new Bundle();
        result.putLong(RESULT_ID, entryId);
        result.putLong(RESULT_EPOCH_DAY, selectedDate.toEpochDay());
        result.putFloat(RESULT_WEIGHT, weight);
        getParentFragmentManager().setFragmentResult(RESULT_KEY, result);
        dismiss();
    }

    /** Edit mode only: tells the grid to delete this entry (the grid offers Undo). */
    private void sendDelete() {
        Bundle result = new Bundle();
        result.putLong(RESULT_ID, entryId);
        result.putBoolean(RESULT_DELETED, true);
        getParentFragmentManager().setFragmentResult(RESULT_KEY, result);
    }

    private String getWeightText() {
        Editable text = binding.weightInput.getText();
        return text == null ? "" : text.toString().trim();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
