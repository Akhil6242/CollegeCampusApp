package com.example.collegecampusapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;

public class LoginActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private TextInputLayout usernameInputLayout, passwordInputLayout;
    private TextInputEditText usernameEditText, passwordEditText;
    private static final String TAG = "LoginActivity";

    @SuppressLint("CutPasteId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        usernameInputLayout = findViewById(R.id.usernameInputLayout);
        passwordInputLayout = findViewById(R.id.passwordInputLayout);
        usernameEditText = findViewById(R.id.username);
        passwordEditText = findViewById(R.id.password);
        Button loginButton = findViewById(R.id.loginButton);
        TextView registerTextView = findViewById(R.id.registerLink);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();


        // Handle login logic here
        loginButton.setOnClickListener((View v) -> {
            String username = Objects.requireNonNull(usernameEditText.getText()).toString();
            String password = Objects.requireNonNull(passwordEditText.getText()).toString();
            if (TextUtils.isEmpty(username)) {
                usernameInputLayout.setError("Username is required");
                return;
            } else {
                usernameInputLayout.setError(null);
            }
            if (TextUtils.isEmpty(password)) {
                passwordInputLayout.setError("Password is required");
                return;
            } else {
                passwordInputLayout.setError(null);
            }
            // **Firebase Authentication Logic**
            mAuth.signInWithEmailAndPassword(username, password)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                // Now it's safe to access user properties like getUid()
                                String uid = user.getUid();
                                db.collection("verifiedUser").document(uid).get()
                                        .addOnSuccessListener(documentSnapshot -> {
                                            if (documentSnapshot.exists()) {
                                                if (documentSnapshot.contains("username")) {
                                                    String storedUsername = documentSnapshot.getString("username");
                                                    if (storedUsername != null && storedUsername.equals(username)) {
                                                        // User is authenticated and username matches in 'verifiedUser'
                                                        Log.d(TAG, "User verified successfully!");
                                                        Log.d(TAG, "User UID: " + uid);
                                                        Log.d(TAG, "Stored Username: " + storedUsername);
                                                        // Navigate to the next screen (e.g., homepage)
                                                        Intent intent = new Intent(LoginActivity.this, HomePageActivity.class);
                                                        startActivity(intent);
                                                        finish(); // Close LoginActivity
                                                    } else {
                                                        // Username in 'verifiedUser' doesn't match entered username
                                                        Log.w(TAG, "Username mismatch in 'verifiedUser' for UID: " + uid);
                                                        Toast.makeText(LoginActivity.this, "Incorrect username.", Toast.LENGTH_SHORT).show();
                                                    }
                                                } else {
                                                    // 'username' field missing in 'verifiedUser' document
                                                    Log.w(TAG, "'username' field missing in 'verifiedUser' for UID: " + uid);
                                                    Toast.makeText(LoginActivity.this, "User not verified.", Toast.LENGTH_SHORT).show();
                                                }
                                            } else {
                                                // Document does not exist in 'verifiedUser'
                                                Log.w(TAG, "Document not found in 'verifiedUser' for UID: " + uid);
                                                Toast.makeText(LoginActivity.this, "User not verified.", Toast.LENGTH_SHORT).show();
                                            }
                                        })
                                        .addOnFailureListener(e -> {
                                            // Handle errors fetching document from Firestore
                                            Log.w(TAG, "Error fetching 'verifiedUser' document for UID: " + uid, e);
                                            Toast.makeText(LoginActivity.this, "Login failed (verification error).", Toast.LENGTH_SHORT).show();
                                        });

                            } else {
                                // This case should be rare after task.isSuccessful(),
                                // but it's good to handle defensively.
                                Log.w(TAG, "Authentication task successful, but FirebaseUser is null.");
                                Toast.makeText(LoginActivity.this, "Authentication failed (user not found).",
                                        Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithUsername:failure", task.getException());
                            String errorMessage;
                            if (task.getException() instanceof FirebaseAuthInvalidUserException) {
                                errorMessage = "Invalid username.";
                                Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                            } else if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                errorMessage = "Invalid password.";
                                Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                            } else {
                                Log.w(TAG, "signInWithUsername:failure", task.getException());
                            }
                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });


        });
        registerTextView.setOnClickListener(v -> startActivity(new Intent(LoginActivity.this, RegistrationActivity.class)));
    }
}