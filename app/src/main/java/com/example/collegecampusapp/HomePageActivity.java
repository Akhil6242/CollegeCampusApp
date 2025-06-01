package com.example.collegecampusapp;

import android.os.Bundle;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class HomePageActivity extends AppCompatActivity {
    private TextView studentName;
    //private ImageView profilePhoto; // Keep this for later use, but we won't fetch the image now
    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        ImageView collegeLogo = findViewById(R.id.collegeLogo); //keep
        ImageView profilePhoto = findViewById(R.id.profilePhoto); //keep
        studentName = findViewById(R.id.studentName);//keep
        GridLayout mainGrid = findViewById(R.id.mainGrid); //keep

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        fetchStudentName();

//        CardView timetableCard = (CardView) mainGrid.getChildAt(0);
//        timetableCard.setOnClickListener(v -> {
//            Intent intent = new Intent(HomePageActivity.this, timetableActivity.class);
//            startActivity(intent);
//        });
//        CardView resultCard = (CardView) mainGrid.getChildAt(1);
//        resultCard.setOnClickListener(v -> {
//            Intent intent = new Intent(HomePageActivity.this, ResultActivity.class);
//            startActivity(intent);
//        });




    }
    private void fetchStudentName() {
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            db.collection("verifiedUser").document(userId).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String name = documentSnapshot.getString("Name");
                            if (name != null) {
                                studentName.setText(name);
                            } else {
                                studentName.setText(R.string.student_name); // Fallback to default
                            }
                        } else {
                            studentName.setText(R.string.student_name); // Fallback to default
                        }
                    })
                    .addOnFailureListener(e -> {
                        studentName.setText(R.string.student_name); // Fallback to default
                        // Optionally log the error for debugging
                        Toast.makeText(this, "Failed to fetch name: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } else {
            studentName.setText(R.string.student_name); // Fallback if user is not logged in
        }
    }
}












