package com.example.enrollmentapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class EnrollmentPage extends AppCompatActivity {
    private ArrayList<EnrollmentData> selectedSubjects = new ArrayList<>();
    private int totalCredits = 0;
    private TextView creditSummary;
    private TableLayout subjectTable;
    private Button submitButton;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enrollment_page);

        db = FirebaseFirestore.getInstance();
        creditSummary = findViewById(R.id.creditSummary);
        subjectTable = findViewById(R.id.subjectTable);
        submitButton = findViewById(R.id.submitButton);

        populateSubjectTable();

        submitButton.setOnClickListener(this::submitEnrollment);
    }

    private void populateSubjectTable() {
        String[] subjects = {
                "Object Oriented Visual Programming",
                "Web Programming",
                "Discrete Mathematics",
                "Numerical Methods",
                "Calculus",
                "Wireless Programming",
                "3D Animation",
                "Network Security",
                "Software Engineering",
                "Computer Network",
                "Database System",
                "Artificial Intelligence"
        };
        int[] credits = {3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3};

        for (int i = 0; i < subjects.length; i++) {
            TableRow row = new TableRow(this);
            row.setBackgroundResource(R.drawable.row_border); // Apply row border

            TextView subjectName = new TextView(this);
            subjectName.setText(subjects[i]);
            subjectName.setPadding(16, 16, 16, 16);
            subjectName.setBackgroundResource(R.drawable.columns_border); // Apply column border

            TextView creditView = new TextView(this);
            creditView.setText(String.valueOf(credits[i]));
            creditView.setPadding(16, 16, 16, 16);
            creditView.setBackgroundResource(R.drawable.columns_border); // Apply column border

            CheckBox checkBox = new CheckBox(this);
            checkBox.setBackgroundResource(R.drawable.columns_border); // Apply column border

            int index = i;
            checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    if (totalCredits + credits[index] <= 18) {
                        selectedSubjects.add(new EnrollmentData(subjects[index], credits[index]));
                        totalCredits += credits[index];
                    } else {
                        buttonView.setChecked(false);
                        Toast.makeText(this, "Maximum 18 credits allowed!", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    selectedSubjects.removeIf(sub -> sub.getSubjectName().equals(subjects[index]));
                    totalCredits -= credits[index];
                }
                updateCreditSummary();
            });

            row.addView(subjectName);
            row.addView(creditView);
            row.addView(checkBox);

            subjectTable.addView(row);
        }
    }

    private void updateCreditSummary() {
        creditSummary.setText("Total Credits: " + totalCredits);
    }

    public void submitEnrollment(View view) {
        if (selectedSubjects.isEmpty()) {
            Toast.makeText(this, "Select at least one subject!", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "User not logged in!", Toast.LENGTH_SHORT).show();
            return;
        }

        String userEmail = user.getEmail();

        Map<String, Object> enrollmentData = new HashMap<>();
        enrollmentData.put("selectedSubjects", selectedSubjects);
        enrollmentData.put("totalCredits", totalCredits);

        db.collection("enrollments")
                .document(userEmail)
                .set(enrollmentData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Enrollment saved successfully!", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(EnrollmentPage.this, EnrollmentSummaryPage.class);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to save enrollment: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
