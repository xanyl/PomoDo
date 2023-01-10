package com.example.pomodo.login;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.pomodo.DashboardActivity;
import com.example.pomodo.R;
import com.example.pomodo.signup.SignupActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {
    private EditText mEmailEditText;
    private EditText mPasswordEditText;
    private Button mSignInButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        TextView signUp = findViewById(R.id.signUp);
        TextView forgetpassword = findViewById(R.id.forgetPassword);
        mEmailEditText = findViewById(R.id.username);
        mPasswordEditText = findViewById(R.id.password);
        mSignInButton = findViewById(R.id.login);

        mSignInButton.setOnClickListener(view -> {
            String email = mEmailEditText.getText().toString();
            String password = mPasswordEditText.getText().toString();
            signIn(email, password);
        });
        signUp.setOnClickListener(view -> {
            // Start the Login activity
            Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
            startActivity(intent);
        });
        forgetpassword.setOnClickListener(view -> {
            // Start the Login activity
            Intent intent = new Intent(LoginActivity.this, ForgetPassword.class);
            startActivity(intent);
        });
    }

    private void signIn(String email, String password) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        try {
            auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            FirebaseUser user = auth.getCurrentUser();
                            Intent intent = new Intent(LoginActivity.this, DashboardActivity.class);
                            startActivity(intent);

//                            updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user
                            Toast.makeText(LoginActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
//                            updateUI(null);

                            // Check for specific error and display a corresponding message
                            try {
                                throw task.getException();
                            } catch (FirebaseAuthInvalidUserException e) {
                                // User doesn't exist
                                Toast.makeText(LoginActivity.this, "User doesn't exist.", Toast.LENGTH_SHORT).show();
                            } catch (FirebaseAuthInvalidCredentialsException e) {
                                // Invalid email or password
                                Toast.makeText(LoginActivity.this, "Invalid email or password.", Toast.LENGTH_SHORT).show();
                            } catch (Exception e) {
                                // Other errors
                                Toast.makeText(LoginActivity.this, "An error occurred.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        } catch (Exception e) {
            // Handle exceptions here
            Toast.makeText(LoginActivity.this, "An error occurred.", Toast.LENGTH_SHORT).show();
        }
    }
}