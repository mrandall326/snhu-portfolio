/*
 * File:        WeightEntryAdapter.java
 * Project:     Scalable, a weight-tracking app (Option 3)
 * Course:      CS 360 Mobile Architecture and Programming
 *              Southern New Hampshire University
 * Author:      Matthew Randall
 * Created:     October 1, 2026
 * Version:     1.0
 *
 * Description: RecyclerView adapter that binds weigh-ins to rows of the grid and reports
 *              edit and delete actions back to the screen.
 */
package com.zybooks.randall_matthew_scalable;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.zybooks.randall_matthew_scalable.databinding.ItemWeightEntryBinding;

import java.util.List;

/**
 * Fills the weigh-in grid, one row per WeightEntry.
 *
 * The adapter only displays the list it is given and reports taps back to the screen through
 * Listener. It never changes the list itself, so the screen that owns the data, and the
 * database behind it, stay the one place where entries are added, edited, or removed.
 *
 * @author Matthew Randall
 * @version 1.0
 */
public class WeightEntryAdapter extends RecyclerView.Adapter<WeightEntryAdapter.RowHolder> {

    /** Callbacks for the two things a user can do to a row. */
    public interface Listener {
        /**
         * The user tapped the row to edit it.
         *
         * @param entry the weigh-in shown in that row
         */
        void onEdit(WeightEntry entry);

        /**
         * The user tapped the row's delete button or pressed and held the row.
         *
         * @param entry the weigh-in shown in that row
         */
        void onDelete(WeightEntry entry);
    }

    /** Entries sorted newest first, owned by WeightLogFragment. */
    private final List<WeightEntry> entries;
    private final Listener listener;

    /**
     * Creates the adapter.
     *
     * @param entries  the weigh-ins to show, newest first; the adapter reads but never changes it
     * @param listener receives edit and delete actions from the rows
     */
    public WeightEntryAdapter(List<WeightEntry> entries, Listener listener) {
        this.entries = entries;
        this.listener = listener;
    }

    @NonNull
    @Override
    public RowHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemWeightEntryBinding rowBinding = ItemWeightEntryBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new RowHolder(rowBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull RowHolder holder, int position) {
        WeightEntry entry = entries.get(position);
        // The list is newest first, so the entry just below this one is the previous weigh-in.
        WeightEntry previous = position + 1 < entries.size() ? entries.get(position + 1) : null;
        holder.bind(entry, previous, listener);
    }

    @Override
    public int getItemCount() {
        return entries.size();
    }

    /** Holds the views for one row so they are looked up once, not on every scroll. */
    static class RowHolder extends RecyclerView.ViewHolder {

        private final ItemWeightEntryBinding rowBinding;

        RowHolder(ItemWeightEntryBinding rowBinding) {
            super(rowBinding.getRoot());
            this.rowBinding = rowBinding;
        }

        void bind(WeightEntry entry, WeightEntry previous, Listener listener) {
            Context context = rowBinding.getRoot().getContext();
            String dateText = WeightEntry.formatRowDate(entry.getDate());

            rowBinding.rowDate.setText(dateText);
            rowBinding.rowWeight.setText(context.getString(
                    R.string.weight_value, WeightEntry.formatWeight(entry.getWeight())));
            rowBinding.rowChange.setText(describeChange(context, entry, previous));

            // Descriptions that name the date, so a screen reader says which row each control
            // belongs to instead of repeating "Delete" for every row.
            rowBinding.getRoot().setContentDescription(
                    context.getString(R.string.row_edit_description, dateText));
            rowBinding.rowDelete.setContentDescription(
                    context.getString(R.string.row_delete_description, dateText));

            // Tap a row to edit it. Delete is offered two ways: the trash button at the end of
            // every row, which is easy to find, and pressing and holding the row, which is the
            // shortcut many Android lists use. Both go to the same confirmation dialog, so
            // neither one removes anything without asking first.
            rowBinding.getRoot().setOnClickListener(v -> listener.onEdit(entry));
            rowBinding.getRoot().setOnLongClickListener(v -> {
                listener.onDelete(entry);
                return true;
            });
            rowBinding.rowDelete.setOnClickListener(v -> listener.onDelete(entry));
        }

        /**
         * Describes the change from the previous weigh-in with an arrow and the amount.
         * The arrow shows direction only; it is not colored as good or bad.
         */
        private static String describeChange(Context context, WeightEntry entry, WeightEntry previous) {
            if (previous == null) {
                return context.getString(R.string.change_first);
            }
            float difference = entry.getWeight() - previous.getWeight();
            String amount = WeightEntry.formatWeight(Math.abs(difference));
            if (amount.equals(WeightEntry.formatWeight(0f))) {
                return context.getString(R.string.change_none);
            }
            return difference > 0
                    ? context.getString(R.string.change_up, amount)
                    : context.getString(R.string.change_down, amount);
        }
    }
}
