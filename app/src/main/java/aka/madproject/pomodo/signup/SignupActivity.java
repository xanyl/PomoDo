package aka.madproject.pomodo.signup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import aka.madproject.pomodo.R;
import aka.madproject.pomodo.login.LoginActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;

public class SignupActivity extends AppCompatActivity {
    private EditText mNameEditText;
    private EditText mEmailEditText;
    private EditText mPasswordEditText;
    private Button mSignupButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        TextView etGoToLogin = findViewById(R.id.etGoTOLogin);
        mNameEditText = findViewById(R.id.etUsername);
        mEmailEditText = findViewById(R.id.etEmail);
        mPasswordEditText = findViewById(R.id.etPassword);
        mSignupButton = findViewById(R.id.btnSignup);
        mSignupButton.setOnClickListener(view -> {
            String name = mNameEditText.getText().toString();
            String email = mEmailEditText.getText().toString();
            String password = mPasswordEditText.getText().toString();
            // Validate the input
            if (name.isEmpty()) {
                mNameEditText.setError("Name is required");
                return;
            }
            if (email.isEmpty()) {
                mEmailEditText.setError("Email is required");
                return;
            }
            if (password.isEmpty()) {
                mPasswordEditText.setError("Password is required");
                return;
            }
            if (password.length() < 8) {
                mPasswordEditText.setError("Password must be at least 8 characters long");
                return;
            }
            signup(email, password);
        });
        etGoToLogin.setOnClickListener(view -> {
            // Start the Login activity
            Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
            startActivity(intent);
        });
        setAppBar();
    }
    private void setAppBar() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }
    }
    private void signup(String email, String password) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    // Sign in success, update UI with the signed-in user's information
                    FirebaseUser user = auth.getCurrentUser();
//                  updateUI(user);
                } else {
                    // If sign in fails, display a message to the user
                    Toast.makeText(SignupActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
//                            updateUI(null);
                    // Check for specific error and display a corresponding message
                    try {
                        throw task.getException();
                    } catch (FirebaseAuthInvalidUserException e) {
                        // User doesn't exist
                        Toast.makeText(SignupActivity.this, "User doesn't exist.", Toast.LENGTH_SHORT).show();
                    } catch (FirebaseAuthInvalidCredentialsException e) {
                        // Invalid email or password
                        Toast.makeText(SignupActivity.this, "Invalid email or password.", Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        // Other errors
                        Toast.makeText(SignupActivity.this, "An error occurred.", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);

    }

}