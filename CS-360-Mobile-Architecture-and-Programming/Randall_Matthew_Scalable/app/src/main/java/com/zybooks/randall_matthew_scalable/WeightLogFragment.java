/*
 * File:        WeightLogFragment.java
 * Project:     Scalable, a weight-tracking app (Option 3)
 * Course:      CS 360 Mobile Architecture and Programming
 *              Southern New Hampshire University
 * Author:      Matthew Randall
 * Created:     October 1, 2026
 * Version:     2.0
 *
 * Description: Weigh-in grid and home screen. Shows the goal summary and the signed-in user's
 *              weigh-ins from the database, saves adds, edits, and deletes back to it, and
 *              reacts when a weigh-in reaches the goal.
 */
package com.zybooks.randall_matthew_scalable;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.zybooks.randall_matthew_scalable.databinding.FragmentWeightLogBinding;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * The weigh-in grid, which is the home screen once the user is signed in.
 *
 * It shows the goal summary card, the grid of weigh-ins with a delete button on every row,
 * and the Add weight button. Adding and editing happen in WeightEntryDialogFragment, and
 * the goal is set in GoalFragment; both send their results back here through the
 * Fragment Result API, and this screen updates the list and the summary.
 *
 * The weigh-ins come from the daily_weight table and the goal from the goal_weight table, both
 * for the user SessionManager says is signed in. Each add, edit, and delete is written to the
 * database first and the list is then reloaded from it, so what the grid shows is always what is
 * actually saved. A new user starts with an empty grid and fills it in themselves.
 *
 * The tables are small (one row per day per user), so these reads and writes are quick enough
 * to run directly on the main thread without the user noticing.
 *
 * @author Matthew Randall
 * @version 2.0
 */
public class WeightLogFragment extends Fragment {

    private FragmentWeightLogBinding binding;
    private WeightEntryAdapter adapter;

    private WeightDao weightDao;
    private GoalDao goalDao;

    /** The signed-in user whose weigh-ins this screen shows. */
    private long userId = UserDao.NO_USER;

    /**
     * The signed-in user's weigh-ins, newest first, as last loaded from the database. The adapter
     * holds on to this same list, so it is refilled in place rather than replaced.
     */
    private final List<WeightEntry> entries = new ArrayList<>();

    /** The signed-in user's goal, or null if they haven't set one. */
    private GoalWeight goal;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        userId = SessionManager.getUserId(requireContext());
        weightDao = new WeightDao(requireContext());
        goalDao = new GoalDao(requireContext());

        // Results from the add/edit dialog and the goal screen. Registering here, with this
        // fragment as the lifecycle owner, means a result is delivered once this screen is
        // visible again, never while it is hidden behind another one.
        getParentFragmentManager().setFragmentResultListener(
                WeightEntryDialogFragment.RESULT_KEY, this, (key, result) -> handleEntryResult(result));
        getParentFragmentManager().setFragmentResultListener(
                GoalFragment.RESULT_KEY, this, (key, result) -> {
                    saveGoal(result.getFloat(GoalFragment.RESULT_GOAL));
                    if (binding != null) {
                        updateSummary();
                        Snackbar.make(binding.getRoot(), R.string.goal_saved, Snackbar.LENGTH_SHORT)
                                .setAnchorView(binding.addWeightFab)
                                .show();
                    }
                });
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentWeightLogBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // This screen only makes sense for a signed-in user. If the session is missing, go back
        // to the login screen instead of showing an empty grid that can't be saved to.
        if (userId == UserDao.NO_USER) {
            NavHostFragment.findNavController(this).navigate(R.id.action_weightLog_to_login);
            return;
        }

        // The grid: a thin divider between rows keeps the columns easy to follow across the screen.
        adapter = new WeightEntryAdapter(entries, new WeightEntryAdapter.Listener() {
            @Override
            public void onEdit(WeightEntry entry) {
                openEntryDialog(entry);
            }

            @Override
            public void onDelete(WeightEntry entry) {
                confirmDelete(entry);
            }
        });
        binding.weightGrid.setAdapter(adapter);
        binding.weightGrid.addItemDecoration(
                new DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL));

        // Add weight opens the dialog in add mode.
        binding.addWeightFab.setOnClickListener(v -> openEntryDialog(null));

        // The summary card and its Edit goal button both lead to the goal screen.
        binding.summaryCard.setOnClickListener(v -> openGoal());
        binding.editGoalButton.setOnClickListener(v -> openGoal());

        // Shrink the Add weight button to just its icon while scrolling down, so it covers less
        // of the grid, and bring the label back when scrolling up.
        binding.weightGrid.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0) {
                    binding.addWeightFab.shrink();
                } else if (dy < 0) {
                    binding.addWeightFab.extend();
                }
            }
        });

        // App bar actions for this screen only. Tying the menu to the view's lifecycle removes
        // it automatically when the user moves to another screen.
        requireActivity().addMenuProvider(new MenuProvider() {
            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                menuInflater.inflate(R.menu.menu_main, menu);
            }

            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.action_goal) {
                    openGoal();
                    return true;
                } else if (id == R.id.action_sms) {
                    openSmsSettings();
                    return true;
                } else if (id == R.id.action_sign_out) {
                    signOut();
                    return true;
                }
                return false;
            }
        }, getViewLifecycleOwner(), Lifecycle.State.RESUMED);

        reloadFromDatabase();
        refreshScreen();
    }

    // ---------------------------------------------------------------------------------------
    // Navigation
    // ---------------------------------------------------------------------------------------

    /** Opens the add/edit dialog. Passing null opens it in add mode with today's date. */
    private void openEntryDialog(WeightEntry entry) {
        Bundle args = new Bundle();
        if (entry != null) {
            args.putLong(WeightEntryDialogFragment.ARG_ID, entry.getId());
            args.putLong(WeightEntryDialogFragment.ARG_EPOCH_DAY, entry.getDate().toEpochDay());
            args.putFloat(WeightEntryDialogFragment.ARG_WEIGHT, entry.getWeight());
        }
        NavHostFragment.findNavController(this).navigate(R.id.action_weightLog_to_entryDialog, args);
    }

    /** Opens the goal screen, passing the current goal and latest weigh-in for context. */
    private void openGoal() {
        Bundle args = new Bundle();
        args.putFloat(GoalFragment.ARG_CURRENT_GOAL, getGoalValue());
        if (!entries.isEmpty()) {
            args.putFloat(GoalFragment.ARG_LATEST_WEIGHT, entries.get(0).getWeight());
            args.putLong(GoalFragment.ARG_LATEST_EPOCH_DAY, entries.get(0).getDate().toEpochDay());
        }
        NavHostFragment.findNavController(this).navigate(R.id.action_weightLog_to_goal, args);
    }

    /** Opens the SMS notification screen, passing the goal so its preview shows the real number. */
    private void openSmsSettings() {
        Bundle args = new Bundle();
        args.putFloat(SmsNotificationFragment.ARG_GOAL_WEIGHT, getGoalValue());
        NavHostFragment.findNavController(this).navigate(R.id.action_weightLog_to_sms, args);
    }

    /** Forgets the signed-in user and returns to the login screen. */
    private void signOut() {
        SessionManager.signOut(requireContext());
        NavHostFragment.findNavController(this).navigate(R.id.action_weightLog_to_login);
    }

    // ---------------------------------------------------------------------------------------
    // Add, edit, delete
    // ---------------------------------------------------------------------------------------

    /**
     * Reads a result from the add/edit dialog and decides what to do with it: delete the entry,
     * ask before replacing a day that already has a weigh-in, or save it straight away.
     *
     * @param result the bundle sent by WeightEntryDialogFragment
     */
    private void handleEntryResult(Bundle result) {
        long id = result.getLong(WeightEntryDialogFragment.RESULT_ID, -1L);
        WeightEntry existing = findById(id);

        if (result.getBoolean(WeightEntryDialogFragment.RESULT_DELETED, false)) {
            if (existing != null) {
                deleteWithUndo(existing);
            }
            return;
        }

        LocalDate date = LocalDate.ofEpochDay(result.getLong(WeightEntryDialogFragment.RESULT_EPOCH_DAY));
        float weight = result.getFloat(WeightEntryDialogFragment.RESULT_WEIGHT);

        // Scalable keeps one weigh-in per day. If a different entry already has this date, the
        // user is asked whether to replace it, so an earlier weight is never overwritten without
        // them seeing it first. Editing an entry without changing its date skips this check.
        WeightEntry sameDay = findByDate(date);
        if (sameDay != null && sameDay != existing) {
            confirmReplace(existing, sameDay, date, weight);
        } else {
            saveEntry(existing, null, date, weight);
        }
    }

    /**
     * Shows both weights side by side and lets the user choose whether the new one replaces the
     * weigh-in already recorded for that day. Cancel leaves the history exactly as it was.
     *
     * @param existing the entry being edited, or null when adding a new one
     * @param sameDay  the entry that already holds this date
     * @param date     the date chosen in the dialog
     * @param weight   the weight typed in the dialog
     */
    private void confirmReplace(WeightEntry existing, WeightEntry sameDay, LocalDate date, float weight) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.replace_title)
                .setMessage(getString(R.string.replace_message,
                        WeightEntry.formatWeight(sameDay.getWeight()),
                        WeightEntry.formatFullDate(date),
                        WeightEntry.formatWeight(weight)))
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.replace_confirm,
                        (dialog, which) -> saveEntry(existing, sameDay, date, weight))
                .show();
    }

    /**
     * Saves a weigh-in to the database, reloads the grid from it, and checks whether this save
     * reached the goal.
     *
     * @param existing the entry being edited, or null when adding a new one
     * @param replaced an entry on the same date that the user agreed to replace, or null
     * @param date     the weigh-in date
     * @param weight   the weight in pounds
     */
    private void saveEntry(WeightEntry existing, WeightEntry replaced, LocalDate date, float weight) {
        // Remember whether the goal was already reached, so the goal message and text only go
        // out on the save that reaches it, not again on every save after that.
        boolean wasReached = isGoalReached();

        long entryId = existing != null ? existing.getId() : -1L;
        long replacedId = replaced != null ? replaced.getId() : -1L;
        boolean saved = weightDao.saveEntry(userId, entryId, replacedId, date, weight);
        reloadFromDatabase();

        // A goal saved before any weigh-ins existed gets its starting point and direction from
        // the first weigh-in added after it.
        if (goal != null && !goal.hasStart()) {
            saveGoal(goal.getGoal());
        }
        refreshScreen();

        if (binding == null) {
            return;
        }
        if (!saved) {
            Snackbar.make(binding.getRoot(), R.string.save_failed_snackbar, Snackbar.LENGTH_LONG)
                    .setAnchorView(binding.addWeightFab)
                    .show();
            return;
        }
        int messageId = existing != null ? R.string.updated_snackbar : R.string.added_snackbar;
        // If this save is the one that reaches the goal, tell the user. A text is sent only when
        // SmsNotifier confirms the device can text, the setting is on, a complete phone number
        // is saved, and permission is granted. The goal message shows inside the app either way,
        // so a user who declined SMS still sees it.
        if (isGoalReached() && !wasReached) {
            boolean texted = SmsNotifier.sendGoalReached(requireContext(), goal.getGoal());
            int goalMessage = texted ? R.string.goal_reached_text_sent : R.string.goal_reached_in_app;
            Snackbar.make(binding.getRoot(),
                            getString(goalMessage, WeightEntry.formatWeight(goal.getGoal())),
                            Snackbar.LENGTH_LONG)
                    .setAnchorView(binding.addWeightFab)
                    .show();
        } else {
            Snackbar.make(binding.getRoot(), messageId, Snackbar.LENGTH_SHORT)
                    .setAnchorView(binding.addWeightFab)
                    .show();
        }
    }

    /** Asks before deleting, since a removed weigh-in is part of the user's history. */
    private void confirmDelete(WeightEntry entry) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.delete_title)
                .setMessage(getString(R.string.delete_message,
                        WeightEntry.formatWeight(entry.getWeight()),
                        WeightEntry.formatFullDate(entry.getDate())))
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.delete_confirm, (dialog, which) -> deleteWithUndo(entry))
                .show();
    }

    /**
     * Removes an entry from the database and offers Undo for a few seconds in case it was a
     * mistake. Undo writes the same entry back with its original ID.
     */
    private void deleteWithUndo(WeightEntry entry) {
        weightDao.deleteEntry(userId, entry.getId());
        reloadFromDatabase();
        refreshScreen();
        if (binding == null) {
            return;
        }
        Snackbar.make(binding.getRoot(), R.string.deleted_snackbar, Snackbar.LENGTH_LONG)
                .setAnchorView(binding.addWeightFab)
                .setAction(R.string.undo, v -> {
                    weightDao.restoreEntry(userId, entry);
                    reloadFromDatabase();
                    refreshScreen();
                })
                .show();
    }

    // ---------------------------------------------------------------------------------------
    // Database
    // ---------------------------------------------------------------------------------------

    /** Refills the list and the goal from the database for the signed-in user. */
    private void reloadFromDatabase() {
        entries.clear();
        entries.addAll(weightDao.getEntries(userId));
        goal = goalDao.getGoal(userId);
    }

    /**
     * Saves a goal to the database, using the latest weigh-in as its starting point and to
     * decide the direction (see GoalWeight).
     *
     * @param goalValue the goal weight in pounds
     */
    private void saveGoal(float goalValue) {
        float latest = entries.isEmpty() ? 0f : entries.get(0).getWeight();
        goal = GoalWeight.startingFrom(goalValue, latest);
        goalDao.saveGoal(userId, goal);
    }

    // ---------------------------------------------------------------------------------------
    // Display
    // ---------------------------------------------------------------------------------------

    /** Redraws the grid, the empty state, and the summary card from the current list. */
    @SuppressLint("NotifyDataSetChanged")
    private void refreshScreen() {
        if (binding == null) {
            return;
        }
        adapter.notifyDataSetChanged();
        boolean empty = entries.isEmpty();
        binding.emptyState.setVisibility(empty ? View.VISIBLE : View.GONE);
        binding.weightGrid.setVisibility(empty ? View.GONE : View.VISIBLE);
        updateSummary();
    }

    /**
     * Fills in the goal summary card: goal, latest weight, progress bar and its labels, and the
     * one-sentence message.
     *
     * The wording never assumes the user is losing weight. It reports the distance to the goal,
     * and the progress bar measures the trip from the weight the user was at when they set the
     * goal to the goal itself, in whichever direction that is. Once the goal is reached the bar
     * fills and turns amber, and the message says whether the user landed on the goal or went
     * past it, and by how much.
     */
    private void updateSummary() {
        if (binding == null) {
            return;
        }
        boolean hasGoal = goal != null;
        boolean hasEntries = !entries.isEmpty();

        binding.goalValue.setText(hasGoal
                ? getString(R.string.weight_value, WeightEntry.formatWeight(goal.getGoal()))
                : getString(R.string.summary_not_set));
        binding.latestValue.setText(hasEntries
                ? getString(R.string.weight_value, WeightEntry.formatWeight(entries.get(0).getWeight()))
                : getString(R.string.summary_not_set));
        binding.editGoalButton.setText(hasGoal ? R.string.summary_edit_goal : R.string.summary_set_goal);

        if (!hasGoal || !hasEntries || !goal.hasStart()) {
            binding.summaryMessage.setText(hasGoal ? R.string.summary_no_entries : R.string.summary_no_goal);
            binding.goalProgress.setProgressCompat(0, false);
            binding.progressLabels.setVisibility(View.GONE);
            setProgressColor(false);
            return;
        }

        float latest = entries.get(0).getWeight();
        binding.progressLabels.setVisibility(View.VISIBLE);
        binding.progressStartLabel.setText(getString(R.string.progress_start,
                WeightEntry.formatWeight(goal.getStartWeight())));
        binding.progressGoalLabel.setText(getString(R.string.progress_goal,
                WeightEntry.formatWeight(goal.getGoal())));

        if (isGoalReached()) {
            // Landing exactly on the goal and going past it get different messages, so a user
            // who overshoots can see by how much.
            String past = WeightEntry.formatWeight(Math.abs(latest - goal.getGoal()));
            if (past.equals(WeightEntry.formatWeight(0f))) {
                binding.summaryMessage.setText(R.string.summary_reached);
            } else {
                binding.summaryMessage.setText(getString(R.string.summary_passed, past));
            }
            binding.progressPercentLabel.setText(R.string.progress_done);
            binding.goalProgress.setProgressCompat(100, true);
            setProgressColor(true);
            return;
        }

        binding.summaryMessage.setText(getString(R.string.summary_to_go,
                WeightEntry.formatWeight(Math.abs(latest - goal.getGoal()))));

        // Percent of the distance covered. Moving toward the goal fills the bar; moving away
        // from it (for example, a weigh-in heavier than the start for someone working down)
        // leaves it empty rather than going negative. GoalWeight holds the math.
        int percent = goal.percentComplete(latest);
        binding.progressPercentLabel.setText(getString(R.string.progress_percent, percent));
        binding.goalProgress.setProgressCompat(percent, true);
        setProgressColor(false);
    }

    /**
     * Colors the progress bar teal while the user is on the way and amber, the color used for
     * the goal throughout the app, once the goal is reached.
     *
     * @param reached true if the goal has been reached
     */
    private void setProgressColor(boolean reached) {
        // The colors come from colors.xml, which has light and dark versions, so the bar
        // follows the theme the same way the rest of the card does.
        int color = ContextCompat.getColor(requireContext(),
                reached ? R.color.scalable_tertiary : R.color.scalable_primary);
        binding.goalProgress.setIndicatorColor(color);
    }

    /**
     * True when the newest weigh-in has reached or passed the goal in the direction the user
     * was heading when they set it: someone working down reaches it at or below the goal, and
     * someone working up reaches it at or above the goal.
     */
    private boolean isGoalReached() {
        return goal != null && !entries.isEmpty() && goal.isReachedBy(entries.get(0).getWeight());
    }

    /** @return the goal in pounds, or 0 if none is set, for passing to other screens */
    private float getGoalValue() {
        return goal != null ? goal.getGoal() : 0f;
    }

    // ---------------------------------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------------------------------

    private WeightEntry findById(long id) {
        for (WeightEntry entry : entries) {
            if (entry.getId() == id) {
                return entry;
            }
        }
        return null;
    }

    private WeightEntry findByDate(LocalDate date) {
        for (WeightEntry entry : entries) {
            if (entry.getDate().equals(date)) {
                return entry;
            }
        }
        return null;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
