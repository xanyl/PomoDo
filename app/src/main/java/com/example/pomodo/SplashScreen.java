package com.example.pomodo;

import androidx.appcompat.app.AppCompatActivity;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;
import android.widget.TextView;

import com.example.pomodo.login.LoginActivity;

public class SplashScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        TextView text = findViewById(R.id.textView);
        int SPLASH_TIME_OUT = 1500; // 3000ms = 3s
        ObjectAnimator fadeIn = ObjectAnimator.ofFloat(text, "alpha", 0f, 1f);
        fadeIn.setDuration(2000); // 2 seconds
        fadeIn.start();
        new Handler().postDelayed(() -> {
            Intent homeIntent = new Intent(SplashScreen.this, LoginActivity.class);
            startActivity(homeIntent);
            finish();
        }, SPLASH_TIME_OUT);
    }
}