package com.example.pomodo;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import com.example.pomodo.login.LoginActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import java.util.Locale;

public class DashboardActivity extends AppCompatActivity {

    private final static int REQUEST_CODE_SETTINGS = 0;
    private final static int COUNTDOWN_INTERVAL = 150;
    private final static int NOTIFICATION_ID = 0;
    private final static String CHANNEL_ID = "toggle-channel";
    private CountDownTimer countDownTimer;
    private TextView countdownTimeLabel;
    private ProgressBar countdownProgressBar;
    private Button startPauseButton;
    private Button cancelButton;
    private Animation blinking;
    private CharSequence startStatusLabel;
    private CharSequence pauseStatusLabel;
    private CharSequence resumeStatusLabel;
    private long setWorkDurationInMillis ;
    private long setBreakDurationInMillis ;
    private boolean isCountdownRunning;
    private long currentTotalDurationInMillis;
    private long timeLeftInMillis;
    private boolean isWorkMode;
    private int colourPrimary;
    private int colourSecondary;
    private int colourText;
    private boolean isLightTheme;
    private long backPressedTime;
    private int breakProgress;
    private int workProgress;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
// Initialize the "set time" (default work-mode time is loaded on the first creation).

        currentTotalDurationInMillis = setWorkDurationInMillis;
        timeLeftInMillis = currentTotalDurationInMillis;

        // Instantiate initial timer.
        countDownTimer = new PomodoroTimer(setWorkDurationInMillis, COUNTDOWN_INTERVAL);

        // Refers to a boolean indicating if timer is running.
        isCountdownRunning = false;

        // Refers to a boolean indicating if user is currently in the work interval.
        isWorkMode = true;

        // Refers to a boolean indicating if app is in light theme.
        isLightTheme = true;

        // Create notification channels for newer APIs (26 and up)
        createNotificationChannel();

        // Update the custom app colour scheme depending on the current colour scheme.
        updateColourSchemeColour();

        // Get resources.
        startStatusLabel = getResources().getText(R.string.start_status_label);
        pauseStatusLabel = getResources().getText(R.string.pause_status_label);
        resumeStatusLabel = getResources().getText(R.string.resume_status_label);

//        lightSettingsButtonImage = getResources().getDrawable(R.drawable.light_settings);
//        darkSettingsButtonImage = getResources().getDrawable(R.drawable.dark_settings);

        // Set up reference instance variables with resource.
        countdownTimeLabel = findViewById(R.id.countdownTimer);
        countdownProgressBar = findViewById(R.id.progressCountdownBar);
        startPauseButton = findViewById(R.id.startPauseButton);
        cancelButton = findViewById(R.id.cancelButton);


        // Set instance variables with corresponding object listeners.
        startPauseButton.setOnClickListener(new ButtonListener());
        cancelButton.setOnClickListener(new ButtonListener());


        // Initiate an object for Blinking animation modifier.
        blinking = new AlphaAnimation(0.0f, 1.0f);
        blinking.setDuration(500);
        blinking.setStartOffset(20);
        blinking.setRepeatMode(Animation.REVERSE);
        blinking.setRepeatCount(Animation.INFINITE);

        // Set up other all the other widget colour schemes now that they are referenced.
        setProgressBarColour(colourPrimary);
        countdownTimeLabel.startAnimation(blinking);
        updateWidgetColourScheme();


        SharedPreferences savedPrefs = getSharedPreferences("SettingsPrefs", MODE_PRIVATE);
        breakProgress = savedPrefs.getInt("breakSeekBarProgress", breakProgress);
        workProgress = savedPrefs.getInt("workSeekBarProgress", workProgress);


        setWorkDurationInMillis = convertMinToMillis(workProgress);
        setBreakDurationInMillis = convertMinToMillis(breakProgress);
        updateTimerWidgets();
        updateCurrentTotalTime();

        ActionBar appBar = getSupportActionBar();
        // Enable the app bar's "home" button, which will also show the settings button
        assert appBar != null;
        appBar.setDisplayHomeAsUpEnabled(true);
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
            // Creating new intent, passing the required variable for setting display.
            Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
//            intent.putExtra("isLightTheme", isLightTheme);
//            intent.putExtra("setWorkDurationInMillis", setWorkDurationInMillis);
//            intent.putExtra("setBreakDurationInMillis", setBreakDurationInMillis);

            // Start activity with request for to reference it again when the settings activity is done.
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


    class PomodoroTimer extends CountDownTimer {

        /*
         * This constructor accepts two parameters to initialize the timer to be used. First, the
         * countdownInMillis argument determines the countdown time for the new timer. Second, the
         * countdownInterval indicates the refresh time of the timer (how often the countdown
         * updates.
         */
        PomodoroTimer(long countdownInMillis, long countdownInterval) {
            super(countdownInMillis, countdownInterval);

            // Set instance variable to acknowledge new timer.
            timeLeftInMillis = countdownInMillis;
        }

        @Override
        public void onTick(long millisUntilFinished) {

            // update instance variable responsible to keep track of current timer's countdown.
            timeLeftInMillis = millisUntilFinished;

            // update widget countdown widgets and text responsible for displaying the text.
            updateTimerWidgets();

        }


        @Override
        public void onFinish() {
            // Execute necessary changes and steps to set up for the next timer.
            toggleWorkMode();
            // Execute onFinish specific prompts to inform user that current timer is up.
            if (isWorkMode) {
                countdownTimeLabel.setText(R.string.countdown_work_label);
                countdownTimeLabel.setTextColor(colourPrimary);
            } else {
                countdownTimeLabel.setText(R.string.countdown_break_label);
                countdownTimeLabel.setTextColor(colourSecondary);
            }
            // Set countdown progressbar to 0 just in case.
            countdownProgressBar.setProgress(0);
            // Startup timer standby mode.
            timerStandby();
            // Send notification to notify that current timer is up.
            sendTimerToggleNotification();

        }
    }
    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        // Initialize variables required to set up notification channel.
        CharSequence name = getString(R.string.channel_name);
        String description = getString(R.string.channel_name);
        int importance = NotificationManager.IMPORTANCE_HIGH;
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
        channel.setDescription(description);
        // Register the channel with the system. The importance or any other notification
        // behaviors cannot be changed after this.
        NotificationManager channelManager = getSystemService(NotificationManager.class);

        // Try to register the channel with the system.
        try {
            channelManager.createNotificationChannel(channel);
        } catch (NullPointerException exception) {
            Log.d("notificationChannel", "Unable to create notification channel");
        }
    }

    private void sendTimerToggleNotification() {
        // Declare local variables used.
        String text;
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getApplicationContext());

        // Create an explicit intent for the main activity.
        Intent intent = new Intent(getApplicationContext(), DashboardActivity.class);

        // The following properties along with the launchMode makes it so that when the
        // notification is pressed, the activity will be resumed instead of recreated.
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        // Determine text to display depending on new current mode.
        if (isWorkMode) {
            text = "Break is over, time to get to work!";
        } else {
            text = "You worked hard, time for a break!";
        }

        // Initiate notification with the correct/wanted properties.
        NotificationCompat.Builder notification = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                 .setSmallIcon(R.drawable.ic_pomodo)
                .setContentTitle("Pomodo").setContentText(text).setPriority(NotificationCompat.PRIORITY_HIGH).setCategory(NotificationCompat.CATEGORY_ALARM).setContentIntent(pendingIntent).setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE).setAutoCancel(true);

        // Send notification.
        notificationManager.notify(NOTIFICATION_ID, notification.build());
    }

    /*
     * This method cancels the notification defined by the parameters. As the parameters, the
     * current context of the activity along with the notification id is given. The context will
     * be used to create the appropriate notification manager to cancel the correct notification
     * given its ID.
     */
    private static void cancelNotification(Context context, int notifyId) {

        NotificationManager cancelManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        try {
            cancelManager.cancel(notifyId);
        } catch (NullPointerException exception) {
            Log.d("cancelNotification", "Attempted to cancel non-existent notification");
        }
    }

    /*
     * Inner class for button listeners, handling all the button events within the main activity.
     */
    class ButtonListener implements View.OnClickListener {


        /*
         * This method is automatically called when any button within main activity is pressed.
         */
        @Override
        public void onClick(View v) {

            // Cancel notifications, state of main activity has changed.
            cancelNotification(getApplicationContext(), NOTIFICATION_ID);


            // When the first timer button is pressed, run start/Resume or pause depending on state.
            if (v.getId() == R.id.startPauseButton) {
                // Determine whether the timer is running to carry out specific methods.
                if (!isCountdownRunning) {
                    startResumeTimer();
                } else {
                    pauseTimer();
                }
            }
            // When the cancel button is pressed, call the cancelTimer method.
            else if (v.getId() == R.id.cancelButton) {
                cancelTimer();
            }
        }
    }

    /*
     * This method will instantiate a new timer depending on the time left and start/resume the
     * timer.
     */
    private void startResumeTimer() {

        // Instantiate timer with current time left.
        countDownTimer = new PomodoroTimer(timeLeftInMillis, COUNTDOWN_INTERVAL);

        // Make sure label is using the correct colour and start the timer.
        countdownTimeLabel.setTextColor(colourText);
        countDownTimer.start();

        // Set timer start up mode to change anythings necessary for the running timer.
        timerStartup();
    }

    /*
     * This method will pause the current timer by cancelling it and change the widget's looks
     * depending on the current state.
     */
    private void pauseTimer() {

        // cancel current timer.
        countDownTimer.cancel();

        // Set countdown label colour for better blinking colours.
        if (isWorkMode) {
            countdownTimeLabel.setTextColor(colourPrimary);
        } else {
            countdownTimeLabel.setTextColor(colourSecondary);
        }

        // enable timer standby mode.
        timerStandby();
    }

    /*
     * This method will cancel the current timer and mode and prepare to switch to the next state's
     * timer.
     */
    private void cancelTimer() {

        // Cancel the current timer, toggle work state, and update the timer widgets for display.

        countDownTimer.cancel();
        toggleWorkMode();
        updateTimerWidgets();

        // Ensure countdownLabel has the current colour for a cancelled timer.
        countdownTimeLabel.setTextColor(colourText);

        // Enable timer standby mode.
        timerStandby();
    }

    /*
     * This method ensures that the current timer is prepared for standby mode, where the timer
     * is either paused or not started yet.
     */
    private void timerStandby() {

        // Set resume/pause button label depending on if the timer is fresh or already used.
        if (timeLeftInMillis != currentTotalDurationInMillis) {
            startPauseButton.setText(resumeStatusLabel);
        } else {
            startPauseButton.setText(startStatusLabel);
        }

        // Keep track that timer is now on standby and also start blinking.
        isCountdownRunning = false;
        countdownTimeLabel.startAnimation(blinking);
    }

    /*
     * This method ensure that the current timer is prepared for timer startup (running state). This
     * is ensuring the states of certain variables.
     */
    private void timerStartup() {

        // Keep track that timer is now in a running state, clear blinking.
        isCountdownRunning = true;
        countdownTimeLabel.clearAnimation();

        // Set startPause label to pause now that the timer is running.
        startPauseButton.setText(pauseStatusLabel);
    }

    /*
     * This method updates the colours used currently in the activity depending on the isLightTheme
     * flag. This will update the colour palette to dark or light when called.
     */
    private void updateColourSchemeColour() {
        // Light theme colours.
        int colourBackground;
        Resources resources = getResources();
        if (isLightTheme) {
            colourPrimary = resources.getColor(R.color.lightPrimary, null);
            colourSecondary = resources.getColor(R.color.lightSecondary, null);
            colourText = resources.getColor(R.color.lightText, null);
            colourBackground = resources.getColor(R.color.lightBackground, null);
        }
        // Dark theme colours.
        else {
            colourPrimary = resources.getColor(R.color.darkPrimary, null);
            colourSecondary = resources.getColor(R.color.darkSecondary, null);
            colourText = resources.getColor(R.color.darkText, null);
            colourBackground = resources.getColor(R.color.darkBackground, null);
        }
    }

    /*
     * This method will update the colour of the widgets that has to be manually updated.
     * Specifically, this method will use the current colour platte and the isLightTheme flag to
     * determine which image for the buttons to use. Additionally, this method will also use the
     * current colour platte th set the colours of the background, labels and progress bar.
     */
    private void updateWidgetColourScheme() {


        // Set label text colours accordingly.
        startPauseButton.setTextColor(colourText);
        cancelButton.setTextColor(colourText);
        countdownTimeLabel.setTextColor(colourText);

        // If on work mode, change colour for work related widgets accordingly.
        if (isWorkMode) {
            // Set countdown label with work mode colour if currently paused (not stopped, at new).
            if (!isCountdownRunning && timeLeftInMillis != currentTotalDurationInMillis) {
                countdownTimeLabel.setTextColor(colourPrimary);
            }
            // Set progress bar to work mode colour.
            setProgressBarColour(colourPrimary);
        }
        // If on break mode, change colour for break related widgets accordingly.
        else {

            // Set countdown label with break mode colour if currently paused (not stopped, at new).
            if (!isCountdownRunning && timeLeftInMillis != currentTotalDurationInMillis) {
                countdownTimeLabel.setTextColor(colourSecondary);
            }
            // Set progress bar to break mode colour.
            setProgressBarColour(colourSecondary);
        }
    }

    /*
     * This method will update the colour scheme and then update the widgets when called.
     */
    private void updateActivityColourScheme() {
        // Update the colour scheme colours followed by the update of all widgets.
        updateColourSchemeColour();
        updateWidgetColourScheme();
    }
    /*
     * This method will update the current total time if required. Specifically, this method is
     * designed to run after returning from the settings. If the current total time for the
     * current timer has changed and the current timer is not started yet, change the total time
     * to accommodate for the new change.
     */
    private void updateCurrentTotalTime() {
        // If the timer is a fresh timer, update the current total time with countdown time.
        if (timeLeftInMillis == currentTotalDurationInMillis) {
            // If current state is work mode, change the total time for total work time.
            if (isWorkMode) {
                currentTotalDurationInMillis = setWorkDurationInMillis;
            }
            // If current state is break mode, change the total time for total break time.
            else {
                currentTotalDurationInMillis = setBreakDurationInMillis;
            }
            // Change the countdown timer to the new total time since this is a fresh start.
            timeLeftInMillis = currentTotalDurationInMillis;
            // update timer widgets for the progress bar changes along with the countdown label.
            updateTimerWidgets();
        }
    }

    /*
     * This method sets the colour of the progress bar by accepting a colour value in the form of
     * an int value as the parameter.
     */
    private void setProgressBarColour(int colour) {

        // User filtering to change the colour of the progress bar drawable.
        countdownProgressBar.getProgressDrawable().setColorFilter(colour, android.graphics.PorterDuff.Mode.SRC_IN);
    }
    /*
     * This method will toggle from work mode to break mode and vise versa depending on the current
     * mode. This method will accomplish this by changing variables and modifying attributes of
     * appropriate widgets.
     */
    private void toggleWorkMode() {

        // Toggle to break mode if currently in work mode.
        if (isWorkMode) {
            isWorkMode = false;
            // Set total duration for the total break duration and set appropriate progress colour.
            currentTotalDurationInMillis = setBreakDurationInMillis;
            setProgressBarColour(colourSecondary);
        }
        // Toggle to work mode if currently in break mode.
        else {
            isWorkMode = true;
            // Set total duration for the total work duration and set appropriate progress colour.
            currentTotalDurationInMillis = setWorkDurationInMillis;
            setProgressBarColour(colourPrimary);
        }

        // Create new timer based on the new total duration.
        countDownTimer = new PomodoroTimer(currentTotalDurationInMillis, COUNTDOWN_INTERVAL);
    }



    /*
     * This method is automatically called when the returned to the main activity from another
     * activity. This method will use the parameters provided when returning back from an activity
     * to request any returned variables. With this, the settings applied in the second activity
     * can be saved once returned.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent dataIntent) {

        // Call constructor as is.
        super.onActivityResult(requestCode, resultCode, dataIntent);

        // The returned result data is identified by requestCode.
        // The request code is specified in startActivityForResult method.
        // This request code is set by startActivityForResult(intent, REQUEST_CODE_SETTINGS).
        if (requestCode == REQUEST_CODE_SETTINGS) {// If returned normally, continue to retrieve and store data.
            if (resultCode == RESULT_OK) {
                // Save data from key value map.
                isLightTheme = dataIntent.getBooleanExtra("isLightTheme", true);
                setBreakDurationInMillis = dataIntent.getLongExtra("newBreakDurationInMillis", breakProgress);
                setWorkDurationInMillis = dataIntent.getLongExtra("newWorkDurationInMillis", workProgress);

                // Update theme and update current total time.
                updateActivityColourScheme();
                updateCurrentTotalTime();
            }
        }
    }

    /*
     * This method is used to update all the countdown displaying related widgets. Specifically,
     * this method updates the countdown label and updates the progress bar as time ticks. Also,
     * ensure that while the timer is running, never let the progress percent reach 0.
     */
    private void updateTimerWidgets() {
        // Update countdown label.
        updateCountDownText();
        // Get actual current percentage of timer.
        int progressPercent = (int) (100.0 * timeLeftInMillis / currentTotalDurationInMillis);

        // If the timer is running and percent is 0, set it to 1 to avoid confusion.
        // This will be disregarded if timeLeftInMillis can be rounded to 0.
        if (isCountdownRunning && progressPercent == 0 && timeLeftInMillis > 1000) {
            countdownProgressBar.setProgress(1);
        }
        // Update progress bar based on current time left and total time if percent is not 0 while
        // running.
        else {
            countdownProgressBar.setProgress(progressPercent);
        }
    }

    /*
     * This method will update the current countdown label with the variable in charge of keeping
     * track of the time, with the correct formatting.
     */
    private void updateCountDownText() {

        // Calculate the minutes and seconds of the total time separately.
        int minutes = (int) (timeLeftInMillis / 1000) / 60;
        int seconds = (int) (timeLeftInMillis / 1000) % 60;

        // Set up the formatted string for display.
        String timeLeft = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);

        // Display formatted string.
        countdownTimeLabel.setText(timeLeft);
    }

    private long convertMinToMillis(int minutes) {

        // Return operation, converting minutes to milliseconds.
        return ((long) minutes * 60 * 1000);

    }

    /*
     * This method will be invoked when user click android device Back menu at bottom. This
     * overridden method will function as a "press back two times to terminate app" function.
     */
    @Override
    public void onBackPressed() {

        // If the second back press is within 2000 millis of the first back press, kill app.
        if (backPressedTime + 2000 > System.currentTimeMillis()) {

            // Call original back press to run required processes for a back press.
            super.onBackPressed();

            // Hard kill app.
            android.os.Process.killProcess(android.os.Process.myPid());

        }
        // If too late or first time pressing back, prompt user the instructions to exit and record
        // the time.
        else {
            // Prompt.
            Toast.makeText(getApplicationContext(), "Press back again to exit", Toast.LENGTH_SHORT).show();

            // Record time of pressing "first" back.
            backPressedTime = System.currentTimeMillis();
        }
    }
}


