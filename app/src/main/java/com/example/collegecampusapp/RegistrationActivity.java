package com.example.collegecampusapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;


public class RegistrationActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;

    private TextInputLayout nameInputLayout, registrationNumberInputLayout, courseInputLayout, yearInputLayout, mobileInputLayout, emailInputLayout, passwordInputLayout, confirmPasswordInputLayout;
    private EditText editTextName, editTextRegistrationNumber, editTextCourse, editTextYear, editTextMobile, editTextEmail, editTextPassword, editTextConfirmPassword;
    private Button buttonSubmit;
    private static final String TAG = "RegistrationActivity";
    private FirebaseFirestore db;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
        mAuth = FirebaseAuth.getInstance();

        nameInputLayout = findViewById(R.id.nameInputLayout);
        editTextName = findViewById(R.id.nameEditText);

        registrationNumberInputLayout = findViewById(R.id.registration_numberInputLayout);
        editTextRegistrationNumber = findViewById(R.id.registration_numberEditText);

        courseInputLayout = findViewById(R.id.courseInputLayout);
        editTextCourse = findViewById(R.id.courseEditText);

        yearInputLayout = findViewById(R.id.yearInputLayout);
        editTextYear = findViewById(R.id.yearEditText);

        mobileInputLayout = findViewById(R.id.mobileInputLayout);
        editTextMobile = findViewById(R.id.mobileEditText);

        emailInputLayout = findViewById(R.id.emailInputLayout);
        editTextEmail = findViewById(R.id.emailEditText);

        passwordInputLayout = findViewById(R.id.passwordInputLayout);
        editTextPassword = findViewById(R.id.passwordEditText);

        confirmPasswordInputLayout = findViewById(R.id.confirmPasswordInputLayout);
        editTextConfirmPassword = findViewById(R.id.confirmPasswordEditText);

        db = FirebaseFirestore.getInstance();
        buttonSubmit = findViewById(R.id.submitButton);

        buttonSubmit.setOnClickListener(v -> {
            // Handle registration logic here
            String Name = editTextName.getText().toString().trim();
            String RegistrationNumber= editTextRegistrationNumber.getText().toString().trim();
            String Course = editTextCourse.getText().toString().trim();
            String Year = editTextYear.getText().toString().trim();
            String Mobile_Number = editTextMobile.getText().toString().trim();
            String Email = editTextEmail.getText().toString().trim();
            String password = editTextPassword.getText().toString().trim();
            String confirmPassword = editTextConfirmPassword.getText().toString().trim();

            // **Input Validation (including empty checks and password match)**
            if (TextUtils.isEmpty(Name)) {
                nameInputLayout.setError("Name is required"); // Use the TextInputLayout variable
                return;
            } else {
                nameInputLayout.setError(null);
            }
            // ... add validation for other fields (registration number, course, etc.)
            if (TextUtils.isEmpty(RegistrationNumber)) {
                registrationNumberInputLayout.setError("Registration Number is required");
                return;
            } else {
                try {
                    Long.parseLong(RegistrationNumber);
                    registrationNumberInputLayout.setError(null);
                } catch (NumberFormatException e) {
                    registrationNumberInputLayout.setError("Registration Number must be a valid number");
                    return;
                }
            }
            if (TextUtils.isEmpty(Course)) {
                courseInputLayout.setError("Course is required");
                return;
            } else {
                courseInputLayout.setError(null);
            }
            if (TextUtils.isEmpty(Year)) {
                yearInputLayout.setError("Year is required");
                return;
            } else {
                yearInputLayout.setError(null);
            }
            if (TextUtils.isEmpty(Mobile_Number)) {
                mobileInputLayout.setError("Mobile Number is required");
                return;
            } else {
                try {
                    Long.parseLong(Mobile_Number);
                    if (Mobile_Number.length() != 10) {
                        mobileInputLayout.setError("Mobile Number must be 10 digits");
                        return;
                    }
                    mobileInputLayout.setError(null);
                } catch (NumberFormatException e) {
                    mobileInputLayout.setError("Mobile Number must be a valid number");
                    return;
                }
            }

            if (TextUtils.isEmpty(Email)) {
                emailInputLayout.setError("Email is required");
                return;
            } else if (!Patterns.EMAIL_ADDRESS.matcher(Email).matches()) {
                emailInputLayout.setError("Invalid email format");
                return;
            } else {
                emailInputLayout.setError(null);
            }

            if (TextUtils.isEmpty(password)) {
                passwordInputLayout.setError("Password is required");
                return;
            } else if (password.length() < 6) {
                passwordInputLayout.setError("Password must be at least 6 characters");
                return;
            } else {
                passwordInputLayout.setError(null);
            }

            if (TextUtils.isEmpty(confirmPassword)) {
                confirmPasswordInputLayout.setError("Confirm Password is required");
                return;
            } else {
                confirmPasswordInputLayout.setError(null);
            }

            if (!password.equals(confirmPassword)) {
                confirmPasswordInputLayout.setError("Passwords do not match");
                return;
            } else {
                confirmPasswordInputLayout.setError(null);
            }
            mAuth.createUserWithEmailAndPassword(Email, password)
                    .addOnCompleteListener(this, task -> {
                        Toast.makeText(RegistrationActivity.this, "Auth listener executed", Toast.LENGTH_SHORT).show(); // Added Toast
                        Log.d(TAG, "createUserWithEmail:onComplete - isSuccessful: " + task.isSuccessful());
                        if (task.isSuccessful()) {
                            // User created successfully in Firebase Authentication
                            com.google.firebase.auth.FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                String uid = user.getUid();
                                Log.d(TAG, "User created in Auth with UID: " + uid);

                                // **Firestore - Save User Data**
                                Map<String, Object> userData = new HashMap<>();
                                userData.put("Name", Name);
                                userData.put("RegistrationNumber", RegistrationNumber);
                                userData.put("Course", Course);
                                userData.put("Year", Year);
                                userData.put("Mobile_Number", Mobile_Number);
                                userData.put("Email", Email);// You might store email here too if needed
                                Log.d(TAG, "User data to be saved: " + userData);


                                db.collection("Registration_userdata").document(uid) // Use UID as document ID
                                        .set(userData)
                                        .addOnSuccessListener(aVoid -> {
                                            // Data successfully written to Firestore
                                            Log.d(TAG, "User data added to Firestore for UID: " + uid);
                                            Toast.makeText(RegistrationActivity.this, "Registration successful!", Toast.LENGTH_SHORT).show();
                                            Toast.makeText(RegistrationActivity.this, "Your data is sent to the department for validation. Please wait for validation.", Toast.LENGTH_LONG).show();

                                            // Move the Toast and Intent inside onSuccessListener
                                            android.widget.Toast.makeText(RegistrationActivity.this, "your data is send to the department for validation wait for validation.", android.widget.Toast.LENGTH_LONG).show();
                                            // Navigate to the next activity (e.g., pending approval status page or homepage)
                                            Intent intent = new Intent(RegistrationActivity.this, LoginActivity.class); // Navigating to Login after successful registration
                                            startActivity(intent);
                                            finish(); // Close RegistrationActivity
                                        })
                                        .addOnFailureListener(e -> {
                                            // Handle errors writing to Firestore
                                            Log.w(TAG, "Error adding user data to Firestore for UID: " + uid, e);
                                            Toast.makeText(RegistrationActivity.this, "Registration failed (data save error)."+ e.getMessage(),
                                                    Toast.LENGTH_LONG).show();

                                            // **Important:** If Firestore write fails, you might want to delete the Firebase Auth user
                                            // to avoid orphaned accounts.
                                            user.delete().addOnCompleteListener(deleteTask -> {
                                                if (deleteTask.isSuccessful()) {
                                                    Log.d(TAG, "Firebase Auth user deleted due to Firestore write failure.");
                                                } else {
                                                    Log.w(TAG, "Failed to delete Firebase Auth user after Firestore write failure.", deleteTask.getException());
                                                }
                                            });
                                        });

                            } else {
                                Log.w(TAG, "createUserWithEmail:success, but FirebaseUser is null.");
                                Toast.makeText(RegistrationActivity.this, "Registration failed (user creation error).",
                                        Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            // If user creation fails in Firebase Authentication
                            String errorMessage = "Registration failed.";
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                                errorMessage = "Email address is already registered.";
                            } else {
                                Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            }
                            Toast.makeText(RegistrationActivity.this, errorMessage,
                                    Toast.LENGTH_SHORT).show();
                        }
                    });


        });
    }
}