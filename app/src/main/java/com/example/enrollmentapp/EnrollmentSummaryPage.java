package com.example.enrollmentapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.Map;

public class EnrollmentSummaryPage extends AppCompatActivity {
    private TextView enrollmentSummary, totalCreditsView;
    private Button addEnrollmentButton, logoutButton;
    private FirebaseAuth firebaseAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enrollment_summary_page);

        enrollmentSummary = findViewById(R.id.enrollmentSummary);
        totalCreditsView = findViewById(R.id.totalCredits);
        addEnrollmentButton = findViewById(R.id.addEnrollmentButton);
        firebaseAuth = FirebaseAuth.getInstance();
        logoutButton = findViewById(R.id.logoutButton);


        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "You need to log in first!", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginPage.class));
            finish();
            return;
        }

        loadEnrollmentData();

        addEnrollmentButton.setOnClickListener(v -> {
            startActivity(new Intent(this, EnrollmentPage.class));
        });
        logoutButton.setOnClickListener(v -> {
            firebaseAuth.signOut();
            Toast.makeText(this, "Logged out successfully!", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginPage.class));
            finish();
        });
    }

    private void loadEnrollmentData() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "User not logged in!", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginPage.class));
            finish();
            return;
        }

        String userEmail = user.getEmail();
        if (userEmail == null) {
            Toast.makeText(this, "User email not found!", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("enrollments")
                .document(userEmail)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        ArrayList<Map<String, Object>> subjects = (ArrayList<Map<String, Object>>) documentSnapshot.get("selectedSubjects");
                        Long totalCredits = documentSnapshot.getLong("totalCredits");

                        if (subjects != null && !subjects.isEmpty()) {
                            StringBuilder summaryBuilder = new StringBuilder();
                            for (Map<String, Object> subject : subjects) {
                                String subjectName = (String) subject.get("subjectName");
                                Long credits = (Long) subject.get("credits");
                                summaryBuilder.append(subjectName).append(" - ").append(credits).append(" credits\n");
                            }

                            enrollmentSummary.setText(summaryBuilder.toString());
                            totalCreditsView.setText("Total Credits: " + totalCredits);
                        } else {
                            enrollmentSummary.setText("No subjects found!");
                        }
                    } else {
                        Toast.makeText(this, "No enrollment data found for this user!", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load enrollment data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

}