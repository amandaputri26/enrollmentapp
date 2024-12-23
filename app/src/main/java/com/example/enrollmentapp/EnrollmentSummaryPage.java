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
        logoutButton = findViewById(R.id.logoutButton);
        firebaseAuth = FirebaseAuth.getInstance();

        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "You need to log in first!", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginPage.class));
            finish();
            return;
        }

        loadEnrollmentData();

        addEnrollmentButton.setOnClickListener(v -> startActivity(new Intent(this, EnrollmentPage.class)));
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
                        List<Map<String, Object>> subjects = (List<Map<String, Object>>) documentSnapshot.get("selectedSubjects");
                        Long totalCredits = documentSnapshot.getLong("totalCredits");

                        TableLayout tableLayout = findViewById(R.id.enrollmentTable);

                        tableLayout.setDividerDrawable(getResources().getDrawable(R.drawable.row_border));
                        tableLayout.setShowDividers(TableLayout.SHOW_DIVIDER_MIDDLE);

                        if (subjects != null && !subjects.isEmpty()) {
                            for (Map<String, Object> subject : subjects) {
                                String subjectName = (String) subject.get("subjectName");
                                Long credits = (Long) subject.get("credits");

                                if (subjectName != null && credits != null) {
                                    TableRow row = new TableRow(this);

                                    // Apply column divider to each TextView (columns)
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
                                    tableLayout.addView(row);
                                }
                            }
                            totalCreditsView.setText("Total Credits: " + totalCredits);
                        } else {
                            TableRow row = new TableRow(this);
                            TextView noDataTextView = new TextView(this);
                            noDataTextView.setText("No subjects found!");
                            noDataTextView.setPadding(8, 8, 8, 8);
                            row.addView(noDataTextView);
                            tableLayout.addView(row);
                        }
                    } else {
                        Toast.makeText(this, "No enrollment data found for this user!", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to load enrollment data: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
