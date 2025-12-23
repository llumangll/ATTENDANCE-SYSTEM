package com.nirma.attendance;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.net.HttpURLConnection;
import java.net.URL;

public class ProfessorActivity extends AppCompatActivity {

    // ⚠️ Updated IP to match your Student Dashboard
    private static final String BASE_URL = "http://10.82.33.138:8080/api";

    private EditText etSubject;
    private Button btnStartSession, btnLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_professor);

        etSubject = findViewById(R.id.etSubject);
        btnStartSession = findViewById(R.id.btnStartSession);
        btnLogout = findViewById(R.id.btnLogout); // Find the new Logout button

        // 1. LOGOUT LOGIC
        btnLogout.setOnClickListener(v -> {
            SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.clear(); // 🗑️ Wipe login data
            editor.apply();

            // Go back to Login Screen
            Intent intent = new Intent(ProfessorActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });

        // 2. BROADCAST LOGIC
        btnStartSession.setOnClickListener(v -> {
            String subject = etSubject.getText().toString().trim();
            if (subject.isEmpty()) {
                Toast.makeText(this, "Enter a subject first!", Toast.LENGTH_SHORT).show();
                return;
            }
            startSession(subject);
        });
    }

    private void startSession(String subject) {
        new Thread(() -> {
            try {
                // Get Professor Name from Prefs
                SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                String professorName = prefs.getString("name", "Unknown Professor");

                // Call Server: /api/create?subject=...&professorName=...
                String link = BASE_URL + "/create?subject=" + subject + "&professorName=" + professorName;
                URL url = new URL(link);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");

                int responseCode = conn.getResponseCode();

                runOnUiThread(() -> {
                    if (responseCode == 200) {
                        Toast.makeText(this, "🔴 LIVE: " + subject, Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(this, "❌ Server Error: " + responseCode, Toast.LENGTH_SHORT).show();
                    }
                });

            } catch (Exception e) {
                runOnUiThread(() ->
                        Toast.makeText(this, "Connection Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
            }
        }).start();
    }
}