package com.nirma.attendance;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class ProfessorActivity extends AppCompatActivity {

    private static final String BASE_URL = "http://10.82.33.138:8080/api";

    private EditText etSubject;
    private Button btnStartSession, btnStopSession, btnLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_professor);

        etSubject = findViewById(R.id.etSubject);
        btnStartSession = findViewById(R.id.btnStartSession);
        btnStopSession = findViewById(R.id.btnStopSession); // 🆕 Find Stop Button
        btnLogout = findViewById(R.id.btnLogout);

        btnLogout.setOnClickListener(v -> {
            SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.clear();
            editor.apply();
            Intent intent = new Intent(ProfessorActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });

        // START CLICK
        btnStartSession.setOnClickListener(v -> {
            String subject = etSubject.getText().toString().trim();
            if (subject.isEmpty()) {
                Toast.makeText(this, "Enter a subject first!", Toast.LENGTH_SHORT).show();
                return;
            }
            startSession(subject);
        });

        // STOP CLICK 🆕
        btnStopSession.setOnClickListener(v -> {
            String subject = etSubject.getText().toString().replace("CLASS CODE: ", "").split("\n")[0].trim();
            // Note: We are being a bit lazy getting the subject back from the text box,
            // but for now let's just use a saved variable or the field if it wasn't overwritten.
            // BETTER WAY: Let's store the current subject in a variable.
            if (currentSubject != null) {
                stopSession(currentSubject);
            }
        });
    }

    private String currentSubject = null; // Store subject to stop it later

    private void startSession(String subject) {
        new Thread(() -> {
            try {
                SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                String professorName = prefs.getString("name", "Unknown Professor");

                String link = BASE_URL + "/create?subject=" + subject + "&professorName=" + professorName;
                URL url = new URL(link);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");

                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String otpCode = reader.readLine();

                runOnUiThread(() -> {
                    Toast.makeText(this, "Session Started!", Toast.LENGTH_SHORT).show();

                    // 1. Update UI to "Live Mode"
                    currentSubject = subject; // Save for later
                    etSubject.setText("CLASS CODE: " + otpCode + "\n(" + subject + ")");
                    etSubject.setEnabled(false); // Lock text

                    btnStartSession.setVisibility(View.GONE); // Hide Start
                    btnStopSession.setVisibility(View.VISIBLE); // Show Stop
                });

            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(this, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void stopSession(String subject) {
        new Thread(() -> {
            try {
                SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                String professorName = prefs.getString("name", "Unknown Professor");

                String link = BASE_URL + "/stop?subject=" + subject + "&professorName=" + professorName;
                URL url = new URL(link);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.getInputStream(); // Trigger request

                runOnUiThread(() -> {
                    Toast.makeText(this, "🛑 Session Stopped", Toast.LENGTH_SHORT).show();

                    // 2. Reset UI to "Ready Mode"
                    etSubject.setText("");
                    etSubject.setEnabled(true);
                    etSubject.setHint("Enter Subject (e.g. Java)");

                    btnStopSession.setVisibility(View.GONE); // Hide Stop
                    btnStartSession.setVisibility(View.VISIBLE); // Show Start
                    currentSubject = null;
                });

            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(this, "Failed to stop: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        }).start();
    }
}