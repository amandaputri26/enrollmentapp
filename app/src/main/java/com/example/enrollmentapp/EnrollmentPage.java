package com.example.enrollmentapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class EnrollmentPage extends AppCompatActivity {
    private ArrayList<EnrollmentData> selectedSubjects = new ArrayList<>();
    private int totalCredits = 0;
    private TextView creditSummary;
    private Button submitButton;
    private FirebaseFirestore db;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enrollment_page);

        db = FirebaseFirestore.getInstance();
        creditSummary = findViewById(R.id.creditSummary);
        submitButton = findViewById(R.id.submitButton);

        setupSubjectSelection();

        submitButton.setOnClickListener(this::submitEnrollment);
    }

    private void setupSubjectSelection() {
        CheckBox[] subjectCheckboxes = {
                findViewById(R.id.checkboxSubject1),
                findViewById(R.id.checkboxSubject2),
                findViewById(R.id.checkboxSubject3),
                findViewById(R.id.checkboxSubject4),
                findViewById(R.id.checkboxSubject5),
                findViewById(R.id.checkboxSubject6),
                findViewById(R.id.checkboxSubject7),
                findViewById(R.id.checkboxSubject8),
                findViewById(R.id.checkboxSubject9),
                findViewById(R.id.checkboxSubject10),
                findViewById(R.id.checkboxSubject11),
                findViewById(R.id.checkboxSubject12),
        };

        int[] subjectCredits = {3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3};
        String[] subjectNames = {
                "Object Oriented Visual Programming",
                "Web Programming",
                "Discrete Mathematics",
                "Numerical Methods",
                "Calculus",
                "Wireless and Mobile Programming",
                "3D Computer Graphics and Animation",
                "Network Security",
                "Software Engineering",
                "Computer Network",
                "Database System",
                "Artificial Intelligence"
        };

        for (int i = 0; i < subjectCheckboxes.length; i++) {
            int index = i;
            subjectCheckboxes[i].setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    if (totalCredits + subjectCredits[index] <= 18) {
                        selectedSubjects.add(new EnrollmentData(subjectNames[index], subjectCredits[index]));
                        totalCredits += subjectCredits[index];
                    } else {
                        buttonView.setChecked(false);
                        Toast.makeText(this, "Maximum 18 credits allowed!", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    selectedSubjects.removeIf(sub -> sub.getSubjectName().equals(subjectNames[index]));
                    totalCredits -= subjectCredits[index];
                }
                updateCreditSummary();
            });
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
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Map<String, Object> enrollmentData = new HashMap<>();
        enrollmentData.put("selectedSubjects", selectedSubjects);
        enrollmentData.put("totalCredits", totalCredits);

        db.collection("enrollments")
                .document(userEmail)
                .set(enrollmentData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Enrollment saved successfully!", Toast.LENGTH_SHORT).show();

                    // Navigate to EnrollmentSummaryPage
                    Intent intent = new Intent(EnrollmentPage.this, EnrollmentSummaryPage.class);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to save enrollment: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private Map<String, Object> createEnrollmentData() {
        Map<String, Object> enrollmentData = new HashMap<>();
        List<Map<String, Object>> subjects = new ArrayList<>();

        for (EnrollmentData subject : selectedSubjects) {
            Map<String, Object> subjectMap = new HashMap<>();
            subjectMap.put("subjectName", subject.getSubjectName());
            subjectMap.put("credits", subject.getCredits());
            subjects.add(subjectMap);
        }

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String email = user.getEmail();
            enrollmentData.put("userId", email); // Store email as userId
        }
        enrollmentData.put("selectedSubjects", subjects);
        enrollmentData.put("totalCredits", totalCredits);
//        enrollmentData.put("timestamp", System.currentTimeMillis());

        return enrollmentData;
    }
}