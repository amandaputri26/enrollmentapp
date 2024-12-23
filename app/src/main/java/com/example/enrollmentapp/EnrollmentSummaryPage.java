package com.example.enrollmentapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.List;
import java.util.Map;

public class EnrollmentSummaryPage extends AppCompatActivity {
    private TextView totalCreditsView;
    private TableLayout enrollmentTable;
    private Button addEnrollmentButton, logoutButton;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enrollment_summary_page);

        totalCreditsView = findViewById(R.id.totalCredits);
        enrollmentTable = findViewById(R.id.enrollmentTable);
        addEnrollmentButton = findViewById(R.id.addEnrollmentButton);
        logoutButton = findViewById(R.id.logoutButton);

        firebaseAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Check if the user is logged in
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null) {
            redirectToLoginPage("You need to log in first!");
            return;
        }

        // Load enrollment data for the current user
        loadEnrollmentData();

        // Set up button listeners
        addEnrollmentButton.setOnClickListener(v -> startActivity(new Intent(this, EnrollmentPage.class)));
        logoutButton.setOnClickListener(v -> {
            firebaseAuth.signOut();
            Toast.makeText(this, "Logged out successfully!", Toast.LENGTH_SHORT).show();
            redirectToLoginPage(null);
        });
    }

    private void loadEnrollmentData() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user == null) {
            redirectToLoginPage("User not logged in!");
            return;
        }

        String userEmail = user.getEmail();
        if (userEmail == null) {
            Toast.makeText(this, "User email not found!", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("enrollments")
                .document(userEmail)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        List<Map<String, Object>> subjects = (List<Map<String, Object>>) documentSnapshot.get("selectedSubjects");
                        Long totalCredits = documentSnapshot.getLong("totalCredits");

                        if (subjects != null && !subjects.isEmpty()) {
                            populateEnrollmentTable(subjects);
                            totalCreditsView.setText("Total Credits: " + totalCredits);
                        } else {
                            displayNoSubjectsMessage();
                        }
                    } else {
                        Toast.makeText(this, "No enrollment data found for this user!", Toast.LENGTH_SHORT).show();
                        displayNoSubjectsMessage();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to load enrollment data: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void populateEnrollmentTable(List<Map<String, Object>> subjects) {
        enrollmentTable.removeAllViews();

        for (Map<String, Object> subject : subjects) {
            String subjectName = (String) subject.get("subjectName");
            Long credits = (Long) subject.get("credits");

            if (subjectName != null && credits != null) {
                TableRow row = new TableRow(this);

                TextView subjectTextView = new TextView(this);
                subjectTextView.setText(subjectName);
                subjectTextView.setPadding(8, 8, 8, 8);
                subjectTextView.setBackgroundResource(R.drawable.columns_border);

                TextView creditsTextView = new TextView(this);
                creditsTextView.setText(String.valueOf(credits));
                creditsTextView.setPadding(8, 8, 8, 8);
                creditsTextView.setBackgroundResource(R.drawable.columns_border);

                row.addView(subjectTextView);
                row.addView(creditsTextView);
                enrollmentTable.addView(row);
            }
        }
    }

    private void displayNoSubjectsMessage() {
        TableRow row = new TableRow(this);
        TextView noDataTextView = new TextView(this);
        noDataTextView.setText("No subjects found!");
        noDataTextView.setPadding(8, 8, 8, 8);
        row.addView(noDataTextView);
        enrollmentTable.addView(row);
    }

    private void redirectToLoginPage(String message) {
        if (message != null) {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        }
        startActivity(new Intent(this, LoginPage.class));
        finish();
    }
}
