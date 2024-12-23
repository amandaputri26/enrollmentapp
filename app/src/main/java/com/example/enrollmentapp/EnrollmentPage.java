package com.example.enrollmentapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EnrollmentPage extends AppCompatActivity {
    private TableLayout subjectTable;
    private TextView creditSummary;
    private Button submitButton;
    private long totalCredits = 0;
    private static final long MAX_CREDITS = 18;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enrollment_page);

        subjectTable = findViewById(R.id.subjectTable);
        creditSummary = findViewById(R.id.creditSummary);
        submitButton = findViewById(R.id.submitButton);

        loadSubjectsFromFirestore();

        submitButton.setOnClickListener(v -> {
            StringBuilder selectedSubjects = new StringBuilder("Selected Subjects:\n");
            List<Map<String, Object>> selectedSubjectsData = new ArrayList<>();

            for (int i = 1; i < subjectTable.getChildCount(); i++) {
                TableRow row = (TableRow) subjectTable.getChildAt(i);
                CheckBox checkBox = (CheckBox) row.getChildAt(2);

                if (checkBox.isChecked()) {
                    TextView subjectTextView = (TextView) row.getChildAt(0);
                    TextView creditsTextView = (TextView) row.getChildAt(1);
                    long credits = Long.parseLong(creditsTextView.getText().toString());

                    selectedSubjects.append(subjectTextView.getText().toString()).append("\n");

                    Map<String, Object> subjectData = new HashMap<>();
                    subjectData.put("subjectName", subjectTextView.getText().toString());
                    subjectData.put("credits", credits);
                    selectedSubjectsData.add(subjectData);
                }
            }
            saveEnrollmentDataToFirestore(selectedSubjectsData);

            Intent intent = new Intent(EnrollmentPage.this, EnrollmentSummaryPage.class);
            intent.putExtra("totalCredits", totalCredits);
            startActivity(intent);
        });
    }

    private void loadSubjectsFromFirestore() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("subjects")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String subjectName = document.getString("name");
                        Object creditsObj = document.get("credits");
                        long credits = creditsObj instanceof Number ? ((Number) creditsObj).longValue() : 0;

                        if (subjectName != null && credits > 0) {
                            addSubjectRow(subjectName, credits);
                        }
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error loading subjects: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void addSubjectRow(String subjectName, long credits) {
        TableRow row = new TableRow(this);

        row.setBackgroundResource(R.drawable.row_border);
        TextView subjectTextView = new TextView(this);
        subjectTextView.setText(subjectName);
        subjectTextView.setPadding(8, 8, 8, 8);
        subjectTextView.setBackgroundResource(R.drawable.columns_border);
        row.addView(subjectTextView);

        TextView creditsTextView = new TextView(this);
        creditsTextView.setText(String.valueOf(credits));
        creditsTextView.setPadding(8, 8, 8, 8);
        creditsTextView.setBackgroundResource(R.drawable.columns_border);
        row.addView(creditsTextView);

        CheckBox selectCheckbox = new CheckBox(this);
        selectCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                if (totalCredits + credits > MAX_CREDITS) {
                    Toast.makeText(EnrollmentPage.this, "Cannot pick more than " + MAX_CREDITS + " credits.", Toast.LENGTH_SHORT).show();
                    selectCheckbox.setChecked(false);
                    return;
                }
                totalCredits += credits;
            } else {
                totalCredits -= credits;
            }
            creditSummary.setText("Total Credits: " + totalCredits);
        });
        selectCheckbox.setBackgroundResource(R.drawable.columns_border);
        row.addView(selectCheckbox);

        subjectTable.addView(row);
    }


    private void saveEnrollmentDataToFirestore(List<Map<String, Object>> selectedSubjectsData) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String userEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();

        if (userEmail == null) {
            Toast.makeText(this, "User email not found!", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> enrollmentData = new HashMap<>();
        enrollmentData.put("selectedSubjects", selectedSubjectsData);
        enrollmentData.put("totalCredits", totalCredits);

        db.collection("enrollments")
                .document(userEmail)
                .set(enrollmentData)
                .addOnSuccessListener(aVoid -> Toast.makeText(this, "Enrollment data saved!", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this, "Error saving data: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
