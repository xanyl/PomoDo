package com.example.pomodo;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.nfc.Tag;
import android.os.Bundle;
import android.util.Log;
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
    private int breakProgress;
    private int workProgress;
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
    private TextView breakLabel;
    private TextView breakDescriptionLabel;
    private ConstraintLayout settingsLayout;
    private TextView workLabel;
    private TextView workDescriptionLabel;
    private TextView themeDescriptionLabel;
    private TextView themeLabel;
    private RadioGroup themeRadioGroup;
    private final static int minTimeInMinutes = 1;
    private SharedPreferences savedPrefs;

    @Override
    protected void onPause() {
        super.onPause();
        SharedPreferences.Editor editor = savedPrefs.edit();
        editor.putInt("breakProgress", breakProgress);
        editor.putInt("workProgress", workProgress);
        editor.apply();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        saveButton = findViewById(R.id.saveButton);
        breakSeekBar = findViewById(R.id.breakSeekBar);
        breakStatusView = findViewById(R.id.breakStatusView);
        workSeekBar = findViewById(R.id.workSeekBar);
        workStatusView = findViewById(R.id.workStatusView);


        // Get variables from main activity, where this activity is called.
        Intent intent = getIntent();

        // Save appropriate variables retrieved from main to display current user settings.
        isLightTheme = intent.getBooleanExtra("isLightTheme", true);
        newBreakDurationInMillis = intent.getLongExtra("setBreakDurationInMillis",
                DEFAULT_BREAK_DURATION);
        newWorkDurationInMillis = intent.getLongExtra("setWorkDurationInMillis",
                DEFAULT_WORK_DURATION);
        breakStatus = convertMillisToMin(newBreakDurationInMillis);
        workStatus = convertMillisToMin(newWorkDurationInMillis);
        // get SharedPreferences object for saving variables onPause.
        savedPrefs = getSharedPreferences( "SettingsPrefs", MODE_PRIVATE );
        breakProgress = savedPrefs.getInt("breakProgress", 0);
        workProgress = savedPrefs.getInt("workProgress", 0);
        settingsLayout = findViewById(R.id.settingsLayout); // assign constraint layout
        themeRadioGroup = findViewById(R.id.themeRadioGroup); // assign radio group
        lightThemeRadioButton = findViewById(R.id.lightThemeRadioButton); // assign light theme radio button
        darkThemeRadioButton = findViewById(R.id.darkThemeRadioButton); // assign dark theme radio button
        breakLabel = findViewById(R.id.breakLabel);
        setAppBar();

//Break Status and textview
        breakSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                currentProgress = progress;
                breakStatusView.setText(""+progress + " Minutes");
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
//Work Status and textview
        workSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                currentProgress = progress;
                workStatusView.setText(""+ progress + " Minutes");
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        //Save Settings
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Save the current progress of breakSeekBar and workSeekBar
                saveSettings(breakProgress, workProgress);
                Toast.makeText(SettingsActivity.this, "Settings saved", Toast.LENGTH_SHORT).show();
            }

            private void saveSettings(int breakProgress, int workProgress) {
                SharedPreferences.Editor editor = savedPrefs.edit();
                editor.putInt("breakProgress", breakProgress);
                editor.putInt("workProgress", workProgress);
                editor.putBoolean("isLightTheme", isLightTheme);
                editor.putLong("newBreakDurationInMillis", newBreakDurationInMillis);
                editor.putLong("newWorkDurationInMillis", newWorkDurationInMillis);
                editor.apply();
//                Log.d("TAG", String.valueOf(workProgress));
            }
        });
    }
//    private long convertMinToMillis(int minutes) {
//
//        // Return operation, converting minutes to milliseconds.
//        return (minutes * 60 * 1000);
//
//    }
    /*
     * This method accepts a parameter of milliseconds in long to be converted into an int value of
     * minutes.
     */
    private int convertMillisToMin(long millis) {
        // Return operation from millis to minutes.
        return ((int)(millis / 60 / 1000));
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
        }
        else {
            themeRadioGroup.check(R.id.darkThemeRadioButton);
        }
    }

    private void updateActivityColourScheme() {
         // assign text view for break label
        // Update the colour scheme colours followed by the update of all widgets.
        updateColourSchemeColour();
        updateWidgetColourScheme();
    }

}