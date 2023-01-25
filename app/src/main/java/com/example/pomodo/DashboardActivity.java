 package com.example.pomodo;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.example.pomodo.login.LoginActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class DashboardActivity extends AppCompatActivity {
    private ProgressBar progressCountdownBar;
    private TextView countdownTimer;
    private Button startPauseButton;
    private Button cancelButton;
    private CountDownTimer countDownTimer;
    private boolean isTimerRunning;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        progressCountdownBar = findViewById(R.id.progressCountdownBar);
        countdownTimer = findViewById(R.id.countdownTimer);
        startPauseButton = findViewById(R.id.startPauseButton);
        cancelButton = findViewById(R.id.cancelButton);
        // Get the app bar
        ActionBar appBar = getSupportActionBar();
        // Enable the app bar's "home" button, which will also show the settings button
        appBar.setDisplayHomeAsUpEnabled(true);
        // Initialize CountDownTimer
        countDownTimer = new CountDownTimer(60000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                progressCountdownBar.setProgress((int) millisUntilFinished / 1000);
                countdownTimer.setText(String.valueOf(millisUntilFinished / 1000));
            }
            @Override
            public void onFinish() {
                progressCountdownBar.setProgress(0);
                countdownTimer.setText("0");
                startPauseButton.setText(R.string.start_status_label);
                isTimerRunning = false;
            }
        };
        startPauseButton.setOnClickListener(view -> {
            if (!isTimerRunning) {
                countDownTimer.start();
                startPauseButton.setText(R.string.pause_status_label);
                isTimerRunning = true;
            } else {
                countDownTimer.cancel();
                startPauseButton.setText(R.string.start_status_label);
                isTimerRunning = false;
            }
        });

        cancelButton.setOnClickListener(view -> {
            countDownTimer.cancel();
            progressCountdownBar.setProgress(0);
            countdownTimer.setText("60");
            startPauseButton.setText(R.string.start_status_label);
            isTimerRunning = false;
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            // Open the settings activity
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }
        if (id == R.id.logout) {
            // Check if user is signed in
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user != null) {
                // User is signed in, log out
                try {
                    FirebaseAuth.getInstance().signOut();
                    Intent loginIntent = new Intent(this, LoginActivity.class);
                    startActivity(loginIntent);
                } catch (Exception e) {
                    // Show error message to the user
                    Toast.makeText(this, "An error occurred while logging out", Toast.LENGTH_SHORT).show();
                }
            } else {
                // User is not signed in
                // Handle this case as needed
                Toast.makeText(this, "You are not signed in", Toast.LENGTH_SHORT).show();
                Intent loginIntent = new Intent(this, LoginActivity.class);
                startActivity(loginIntent);
            }
        }
        return super.onOptionsItemSelected(item);
    }
}