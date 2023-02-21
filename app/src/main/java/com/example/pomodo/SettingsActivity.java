package com.example.pomodo;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.os.Bundle;

import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

public class SettingsActivity extends AppCompatActivity {
    private final static long DEFAULT_WORK_DURATION = 1500000;
    private final static long DEFAULT_BREAK_DURATION = 300000;
    private int currentProgress = 0;
    private SeekBar breakSeekBar;
    private SeekBar workSeekBar;
    private TextView breakStatusView;
    private TextView workStatusView;
    private Button saveButton;
    private int breakStatus;
    private int workStatus;
    private boolean isLightTheme;
    private long newBreakDurationInMillis;
    private long newWorkDurationInMillis;
    private int colourPrimary;
    private int colourText;
    private int colourBackground;
    private RadioButton lightThemeRadioButton;
    private RadioButton darkThemeRadioButton;
    private RadioGroup themeRadioGroup;
    private TextView breakLabel;
    private TextView breakDescriptionLabel;
    private ConstraintLayout settingsLayout;
    private TextView workLabel;
    private TextView workDescriptionLabel;
    private TextView themeDescriptionLabel;
    private TextView themeLabel;
    private final static int minTimeInMinutes = 1;
    private SharedPreferences savedPrefs;
    public final String PREFS_NAME = "SeekBarPrefs";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        saveButton = findViewById(R.id.saveButton);
        breakSeekBar = findViewById(R.id.breakSeekBar);
        breakStatusView = findViewById(R.id.breakStatusView);
        workSeekBar = findViewById(R.id.workSeekBar);
        workStatusView = findViewById(R.id.workStatusView);
        themeRadioGroup = findViewById(R.id.themeRadioGroup);
        // Get variables from main activity, where this activity is called.
        Intent intent = getIntent();
        saveButton.setOnClickListener(new ButtonListener());
        breakSeekBar.setOnSeekBarChangeListener(new SeekBarListener());
        workSeekBar.setOnSeekBarChangeListener(new SeekBarListener());
        themeRadioGroup.setOnCheckedChangeListener(new RadioGroupListener());
        savedPrefs = getSharedPreferences("SettingsPrefs", MODE_PRIVATE);
        int breakProgress = savedPrefs.getInt("breakSeekBarProgress", 0);
        int workProgress = savedPrefs.getInt("workSeekBarProgress", 0);

        // Set the progress of the seek bars
//        breakSeekBar.setProgress(breakProgress);
//        workSeekBar.setProgress(workProgress);

        // Save appropriate variables retrieved from main to display current user settings.
        isLightTheme = intent.getBooleanExtra("isLightTheme", true);
        newBreakDurationInMillis = intent.getLongExtra("setBreakDurationInMillis", DEFAULT_BREAK_DURATION);
        newWorkDurationInMillis = intent.getLongExtra("setWorkDurationInMillis", DEFAULT_WORK_DURATION);
        breakStatus = convertMillisToMin(newBreakDurationInMillis);
        workStatus = convertMillisToMin(newWorkDurationInMillis);
        settingsLayout = findViewById(R.id.settingsLayout); // assign constraint layout
        themeRadioGroup = findViewById(R.id.themeRadioGroup); // assign radio group
        lightThemeRadioButton = findViewById(R.id.lightThemeRadioButton); // assign light theme radio button
        darkThemeRadioButton = findViewById(R.id.darkThemeRadioButton); // assign dark theme radio button
        breakLabel = findViewById(R.id.breakLabel);
        setAppBar();

    }
    class RadioGroupListener implements RadioGroup.OnCheckedChangeListener {

        /*
         * This method is automatically called when there is a change in the RadioGroup.
         */
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {

            // find which radio button is selected, change theme flag accordingly.
            if (checkedId == R.id.lightThemeRadioButton) {
                isLightTheme = true;
            } else {
                isLightTheme = false;
            }
            // Update colour scheme after changing theme.
            updateActivityColourScheme();
        }

    }


    class SeekBarListener implements SeekBar.OnSeekBarChangeListener {

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            // Declare local variables.

            // If the break seek bar is changed, set text view for break status.
            if (R.id.breakSeekBar == seekBar.getId()) {
                // Get status, add minTime to accommodated for min value of 1 in seek bar.
                breakStatus = progress + minTimeInMinutes;

                // Decide to use singular version of minute if appropriate.
                currentProgress = progress;
                breakStatusView.setText("" + progress + " Minutes");

                // Set break status and save new break duration in millis.

                newBreakDurationInMillis = convertMinToMillis(breakStatus);
                SharedPreferences.Editor editor = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit();
                editor.putInt("breakSeekBarProgress", progress);
                editor.apply();
            }
            // If the work seek bar is changed, set text view for work status.
            else if (R.id.workSeekBar == seekBar.getId()) {
                // Get status, add minTime to accommodated for min value of 1 in seek bar.
                workStatus = progress + minTimeInMinutes;

                // Decide to use singular version of minute if appropriate.
                currentProgress = progress;
                workStatusView.setText("" + progress + " Minutes");

                // Set work status and save new work duration in millis.
//                workStatusView.setText(statusView);
                newWorkDurationInMillis = convertMinToMillis(workStatus);
                SharedPreferences.Editor editor = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit();
                editor.putInt("workSeekBarProgress", progress);
                editor.apply();

            }
        }
        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            // Called when the user starts changing the SeekBar
            // Not Used / Implemented
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            // Called when the user finishes changing the SeekBar
            // Not Used / Implemented
        }
    }
    class ButtonListener implements View.OnClickListener {

        /*
         * This method is automatically called when a button is pressed, if the appropriate button
         * is pressed, perform a certain call.
         */
        @Override
        public void onClick(View v) {

            if (v.getId() == R.id.saveButton) {
                Toast.makeText(getApplicationContext(), "Settings saved!", Toast.LENGTH_SHORT).show();
                SharedPreferences.Editor editor = savedPrefs.edit();
                editor.putInt("breakSeekBarProgress", breakSeekBar.getProgress());
                editor.putInt("workSeekBarProgress", workSeekBar.getProgress());
                editor.apply();
                Intent intent = new Intent(SettingsActivity.this, DashboardActivity.class);
                startActivity(intent);
            }
        }
    }
    private long convertMinToMillis(int minutes) {

        // Return operation, converting minutes to milliseconds.
        return (minutes * 60 * 1000);

    }
    /*
     * This method accepts a parameter of milliseconds in long to be converted into an int value of
     * minutes.
     */
    private int convertMillisToMin(long millis) {
        // Return operation from millis to minutes.
        return ((int) (millis / 60 / 1000));
    }
    private void setAppBar() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }
    private void updateColourSchemeColour() {
        // Set light theme colours if appropriate.
        if (isLightTheme) {
            colourPrimary = ContextCompat.getColor(this, R.color.lightPrimary);
            colourText = ContextCompat.getColor(this, R.color.lightText);
            colourBackground = ContextCompat.getColor(this, R.color.lightBackground);
        }
        // Set dark theme colours if appropriate.
        else {
            colourPrimary = ContextCompat.getColor(this, R.color.darkPrimary);
            colourText = ContextCompat.getColor(this, R.color.darkText);
            colourBackground = ContextCompat.getColor(this, R.color.darkBackground);
        }
    }
    private void updateWidgetColourScheme() {
        // Background colour change.
        settingsLayout.setBackgroundColor(colourBackground);
        // Text colour change
        breakLabel.setTextColor(colourText);
        breakDescriptionLabel.setTextColor(colourText);
        breakStatusView.setTextColor(colourText);
        workLabel.setTextColor(colourText);
        workStatusView.setTextColor(colourText);
        workDescriptionLabel.setTextColor(colourText);
        themeLabel.setTextColor(colourText);
        themeDescriptionLabel.setTextColor(colourText);
        lightThemeRadioButton.setTextColor(colourText);
        darkThemeRadioButton.setTextColor(colourText);
        // Change tint colours of widgets with primary colour.
        breakSeekBar.setProgressTintList(ColorStateList.valueOf(colourPrimary));
        breakSeekBar.setThumbTintList(ColorStateList.valueOf(colourPrimary));
        workSeekBar.setProgressTintList(ColorStateList.valueOf(colourPrimary));
        workSeekBar.setThumbTintList(ColorStateList.valueOf(colourPrimary));
        lightThemeRadioButton.setButtonTintList(ColorStateList.valueOf(colourPrimary));
        darkThemeRadioButton.setButtonTintList(ColorStateList.valueOf(colourPrimary));
    }
    private void updateCurrentWidgetWithSettings() {
        // Update seek bar progress.
        breakSeekBar.setProgress(breakStatus - minTimeInMinutes);
        workSeekBar.setProgress(workStatus - minTimeInMinutes);
        // Update radio button checks, depending on current theme flag.
        if (isLightTheme) {
            themeRadioGroup.check(R.id.lightThemeRadioButton);
        } else {
            themeRadioGroup.check(R.id.darkThemeRadioButton);
        }
    }
    private void updateActivityColourScheme() {
        // assign text view for break label
        // Update the colour scheme colours followed by the update of all widgets.
        updateColourSchemeColour();
        updateWidgetColourScheme();
        updateCurrentWidgetWithSettings();
    }
    @Override
    public void onPause() {

        super.onPause();

        SharedPreferences.Editor editor = savedPrefs.edit();
        editor.putInt("breakSeekBarProgress", breakSeekBar.getProgress());
        editor.putInt("workSeekBarProgress", workSeekBar.getProgress());
        editor.apply();
    }
    @Override
    public void onResume() {
        // Call progress required for on resume call.
        super.onResume();
        // Restore SeekBar progress from SharedPreferences
        int breakProgress = savedPrefs.getInt("breakSeekBarProgress", 0);
        int workProgress = savedPrefs.getInt("workSeekBarProgress", 0);
        breakSeekBar.setProgress(breakProgress);
        workSeekBar.setProgress(workProgress);
    }


}