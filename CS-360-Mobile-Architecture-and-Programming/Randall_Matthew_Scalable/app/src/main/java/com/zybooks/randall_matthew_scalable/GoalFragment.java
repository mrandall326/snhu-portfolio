/*
 * File:        GoalFragment.java
 * Project:     Scalable, a weight-tracking app (Option 3)
 * Course:      CS 360 Mobile Architecture and Programming
 *              Southern New Hampshire University
 * Author:      Matthew Randall
 * Created:     October 1, 2026
 * Version:     1.0
 *
 * Description: Goal weight screen. Takes the goal, explains which direction it represents,
 *              and returns it to the weigh-in grid.
 */
package com.zybooks.randall_matthew_scalable;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;

import androidx.annotation.NonNull;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.zybooks.randall_matthew_scalable.databinding.FragmentGoalBinding;

import java.time.LocalDate;

/**
 * Goal weight screen.
 *
 * Shows the latest weigh-in for context, takes the goal as a number, and explains in a live
 * sentence which direction that goal represents (down to it, up to it, or holding steady). Save
 * stays disabled until a goal is typed. The saved goal goes back to WeightLogFragment
 * through the Fragment Result API, and WeightLogFragment writes it to the goal_weight table,
 * one row per user.
 *
 * @author Matthew Randall
 * @version 1.0
 */
public class GoalFragment extends Fragment {

    // Argument names, matching the argument entries in nav_graph.xml.
    public static final String ARG_CURRENT_GOAL = "current_goal";
    public static final String ARG_LATEST_WEIGHT = "latest_weight";
    public static final String ARG_LATEST_EPOCH_DAY = "latest_epoch_day";

    // Result key and field read by WeightLogFragment.
    public static final String RESULT_KEY = "goal_result";
    public static final String RESULT_GOAL = "result_goal";

    /** Same believable range as a weigh-in, so a goal can't be set that couldn't be logged. */
    private static final float MIN_GOAL = 50f;
    private static final float MAX_GOAL = 700f;

    private FragmentGoalBinding binding;
    private float latestWeight;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentGoalBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle args = getArguments() != null ? getArguments() : new Bundle();
        float currentGoal = args.getFloat(ARG_CURRENT_GOAL, 0f);
        latestWeight = args.getFloat(ARG_LATEST_WEIGHT, 0f);
        long latestEpochDay = args.getLong(ARG_LATEST_EPOCH_DAY, -1L);

        // Latest weigh-in card, so the user picks a goal with their most recent number in front
        // of them.
        if (latestWeight > 0f && latestEpochDay != -1L) {
            binding.latestWeightValue.setText(getString(R.string.goal_latest_value,
                    WeightEntry.formatWeight(latestWeight),
                    WeightEntry.formatRowDate(LocalDate.ofEpochDay(latestEpochDay))));
        } else {
            binding.latestWeightValue.setText(R.string.goal_latest_none);
        }

        // Pre-fill the existing goal the first time the screen opens.
        if (savedInstanceState == null && currentGoal > 0f) {
            binding.goalInput.setText(WeightEntry.formatWeight(currentGoal));
        }

        binding.goalInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable s) {
                binding.goalLayout.setError(null);
                updateDirectionAndButton();
            }
        });

        // Done on the keyboard saves, the same as the Save goal button.
        binding.goalInput.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE && binding.goalSaveButton.isEnabled()) {
                saveGoal();
                return true;
            }
            return false;
        });

        binding.goalSaveButton.setOnClickListener(v -> saveGoal());
        binding.goalCancelButton.setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigateUp());

        // When the keyboard opens, this screen gets shorter and the Save and Cancel buttons drop
        // below the bottom edge. Scrolling to the end of the screen at that moment brings them
        // back into view while the goal field and its direction sentence stay visible, so the
        // user can reach Save without scrolling by hand.
        NestedScrollView scrollView = binding.getRoot();
        View goalInput = binding.goalInput;
        scrollView.addOnLayoutChangeListener((v, left, top, right, bottom,
                                              oldLeft, oldTop, oldRight, oldBottom) -> {
            boolean gotShorter = (bottom - top) < (oldBottom - oldTop);
            if (gotShorter && goalInput.hasFocus()) {
                scrollView.post(() -> scrollView.smoothScrollTo(0,
                        scrollView.getChildAt(0).getHeight()));
            }
        });

        updateDirectionAndButton();
    }

    /**
     * Updates the sentence under the goal field and turns Save on or off. The sentence is worded
     * so it is equally encouraging whether the goal is above or below the latest weigh-in.
     */
    private void updateDirectionAndButton() {
        float goal = parseGoal();
        binding.goalSaveButton.setEnabled(goal > 0f);

        if (goal <= 0f || latestWeight <= 0f) {
            binding.goalDirection.setText(R.string.goal_direction_hint);
            return;
        }
        float difference = latestWeight - goal;
        String amount = WeightEntry.formatWeight(Math.abs(difference));
        if (amount.equals(WeightEntry.formatWeight(0f))) {
            binding.goalDirection.setText(R.string.goal_direction_same);
        } else if (difference > 0) {
            binding.goalDirection.setText(getString(R.string.goal_direction_down, amount));
        } else {
            binding.goalDirection.setText(getString(R.string.goal_direction_up, amount));
        }
    }

    /** Validates the goal, then sends it back to the grid and closes this screen. */
    private void saveGoal() {
        float goal = parseGoal();
        if (goal < MIN_GOAL || goal > MAX_GOAL) {
            binding.goalLayout.setError(getString(R.string.entry_weight_error));
            return;
        }
        Bundle result = new Bundle();
        result.putFloat(RESULT_GOAL, Math.round(goal * 10f) / 10f);
        getParentFragmentManager().setFragmentResult(RESULT_KEY, result);
        NavHostFragment.findNavController(this).navigateUp();
    }

    /** Returns the typed goal, or 0 if the field is empty or not a number. */
    private float parseGoal() {
        Editable text = binding.goalInput.getText();
        if (text == null || text.toString().trim().isEmpty()) {
            return 0f;
        }
        try {
            return Float.parseFloat(text.toString().trim());
        } catch (NumberFormatException e) {
            return 0f;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
