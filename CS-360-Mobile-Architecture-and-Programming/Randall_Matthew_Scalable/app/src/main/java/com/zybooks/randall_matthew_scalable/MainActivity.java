/*
 * File:        MainActivity.java
 * Project:     Scalable, a weight-tracking app (Option 3)
 * Course:      CS 360 Mobile Architecture and Programming
 *              Southern New Hampshire University
 * Author:      Matthew Randall
 * Created:     October 1, 2026
 * Version:     2.0
 *
 * Description: Single host activity. Keeps content clear of the system bars and keyboard,
 *              opens on the login screen or the signed-in user's weigh-ins, sets up the top
 *              app bar, and connects it to the navigation graph that swaps the screens.
 */
package com.zybooks.randall_matthew_scalable;

import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.navigation.NavController;
import androidx.navigation.NavGraph;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.zybooks.randall_matthew_scalable.databinding.ActivityMainBinding;

/**
 * The single activity that hosts every screen in Scalable.
 *
 * Each screen is a fragment listed in res/navigation/nav_graph.xml. This activity keeps the
 * screens clear of the system bars and the keyboard, sets up the top app bar, connects it to the
 * navigation controller, and hides it on the login screen. It also picks the first screen:
 * a user who signed in before and never signed out goes straight to their weigh-ins. Keeping one
 * activity means the app bar, the Back behavior, and the screen transitions are defined once and
 * behave the same everywhere.
 *
 * @author Matthew Randall
 * @version 2.0
 */
public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;
    private NavController navController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Draw behind the status and navigation bars, then pad the root view so nothing
        // important ends up hidden underneath them. The keyboard is included as well: while it
        // is open, the bottom padding grows to the keyboard's height, so each screen shrinks to
        // fit above it and can be scrolled instead of being covered.
        EdgeToEdge.enable(this);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets bars = insets.getInsets(
                    WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.ime());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        setSupportActionBar(binding.toolbar);

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment_content_main);
        if (navHostFragment == null) {
            return;
        }
        navController = navHostFragment.getNavController();

        // The graph starts on the login screen. If someone is still signed in from last time,
        // start on their weigh-in grid instead. This only runs on a fresh launch; when Android
        // rebuilds the activity (after a rotation, for example) the navigation host restores
        // whatever screen the user was on, and replacing the graph would throw that away.
        if (savedInstanceState == null && SessionManager.isSignedIn(this)) {
            NavGraph graph = navController.getNavInflater().inflate(R.navigation.nav_graph);
            graph.setStartDestination(R.id.weightLogFragment);
            navController.setGraph(graph);
        }

        // Login and the weigh-in grid are both "top level" screens, so neither shows a Back
        // arrow. Without this, the grid would show an arrow pointing back to a login screen
        // that is no longer on the back stack.
        appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.loginFragment, R.id.weightLogFragment).build();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

        // The login screen has its own large logo and title, so the app bar is hidden there to
        // keep the screen clean. It reappears on every other screen to show where the user is.
        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            boolean onLogin = destination.getId() == R.id.loginFragment;
            binding.appBar.setVisibility(onLogin ? View.GONE : View.VISIBLE);
        });
    }

    /** Handles the Back arrow in the app bar on the goal and notification screens. */
    @Override
    public boolean onSupportNavigateUp() {
        return (navController != null && NavigationUI.navigateUp(navController, appBarConfiguration))
                || super.onSupportNavigateUp();
    }
}
