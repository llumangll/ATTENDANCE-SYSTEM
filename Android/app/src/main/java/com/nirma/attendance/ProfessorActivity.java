package com.nirma.attendance;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class ProfessorActivity extends AppCompatActivity {

    // REPLACE WITH YOUR REAL IP!
    private static final String BASE_URL = "http://10.82.33.138:8080/api";

    private EditText etSubject;
    private Button btnStartSession;
    private TextView tvStatus;
    private String profName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_professor);

        // 1. Get Professor Name from Login
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        profName = prefs.getString("name", "Professor");

        etSubject = findViewById(R.id.etSubject);
        btnStartSession = findViewById(R.id.btnStartSession);
        tvStatus = findViewById(R.id.tvStatus);

        // 2. Button Click -> Start Class
        btnStartSession.setOnClickListener(v -> {
            String subject = etSubject.getText().toString().trim();
            if (!subject.isEmpty()) {
                startSession(subject);
            } else {
                Toast.makeText(this, "Enter a subject first", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void startSession(String subject) {
        new Thread(() -> {
            try {
                // Call API: /session/start?profName=...&subject=...
                String link = BASE_URL + "/session/start?profName=" + profName + "&subject=" + subject;
                URL url = new URL(link);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET"); // We used GET for easy testing

                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) response.append(line);

                // Parse Result to get Session ID
                JSONObject json = new JSONObject(response.toString());
                long sessionId = json.getLong("id");

                // Update UI
                new Handler(Looper.getMainLooper()).post(() -> {
                    tvStatus.setText("🔴 LIVE: " + subject + " (ID: " + sessionId + ")");
                    tvStatus.setTextColor(0xFFFF0000); // Red Color
                    Toast.makeText(this, "Session Started!", Toast.LENGTH_SHORT).show();
                });

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        }).start();
    }
}