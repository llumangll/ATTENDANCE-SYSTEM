package com.nirma.attendance;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Random;

public class ProfessorActivity extends AppCompatActivity {

    private static final String BASE_URL = "http://10.82.33.138:8080/api";
    private static final int LOC_REQ_CODE = 1001;

    private EditText etSubject;
    private Button btnStartSession;
    private TextView tvCode, tvProfWelcome;
    private boolean isSessionActive = false;
    private long currentSessionId = -1;
    private String professorName;
    private FusedLocationProviderClient fusedLocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_professor);

        etSubject = findViewById(R.id.etSubject);
        btnStartSession = findViewById(R.id.btnStartSession);
        tvCode = findViewById(R.id.tvCode);
        tvProfWelcome = findViewById(R.id.tvProfWelcome);
        Button btnLogout = findViewById(R.id.btnLogoutProf);

        // 1. Initialize Location Client
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // 2. Get Professor Name
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        professorName = prefs.getString("name", "Professor");
        tvProfWelcome.setText("Welcome, " + professorName);

        // 3. Start/Stop Button Logic
        btnStartSession.setOnClickListener(v -> {
            if (!isSessionActive) {
                // Check Permission -> Get Location -> Start Session
                if (checkLocationPermission()) {
                    startSessionWithLocation();
                } else {
                    requestLocationPermission();
                }
            } else {
                stopSession();
            }
        });

        // 4. Logout Logic
        btnLogout.setOnClickListener(v -> {
            SharedPreferences.Editor editor = prefs.edit();
            editor.clear();
            editor.apply();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }

    // --- LOCATION HELPERS ---
    private boolean checkLocationPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOC_REQ_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOC_REQ_CODE && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startSessionWithLocation();
        } else {
            Toast.makeText(this, "⚠️ Location needed to prevent proxies!", Toast.LENGTH_SHORT).show();
        }
    }

    private void startSessionWithLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        Toast.makeText(this, "Fetching GPS...", Toast.LENGTH_SHORT).show();

        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null) {
                sendStartRequest(location.getLatitude(), location.getLongitude());
            } else {
                // Fallback if GPS is off/weak (sends 0.0)
                Toast.makeText(this, "⚠️ Weak GPS. Starting anyway.", Toast.LENGTH_SHORT).show();
                sendStartRequest(0.0, 0.0);
            }
        });
    }

    // --- NETWORK REQUESTS ---
    private void sendStartRequest(double lat, double lon) {
        String subject = etSubject.getText().toString();
        if (subject.isEmpty()) {
            Toast.makeText(this, "Please enter a subject", Toast.LENGTH_SHORT).show();
            return;
        }

        String generatedPassword = String.format("%04d", new Random().nextInt(10000));

        new Thread(() -> {
            try {
                // 🛠 FIX: Encode Name and Subject to handle spaces safely
                String safeName = java.net.URLEncoder.encode(professorName, "UTF-8");
                String safeSubject = java.net.URLEncoder.encode(subject, "UTF-8");

                String link = BASE_URL + "/sessions/start?professorName=" + safeName
                        + "&subject=" + safeSubject
                        + "&password=" + generatedPassword
                        + "&lat=" + lat + "&lon=" + lon;

                URL url = new URL(link);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");

                int responseCode = conn.getResponseCode();
                if (responseCode == 200) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    String response = reader.readLine();
                    JSONObject json = new JSONObject(response);
                    currentSessionId = json.getLong("id");

                    new Handler(Looper.getMainLooper()).post(() -> {
                        isSessionActive = true;
                        tvCode.setText("CODE: " + generatedPassword);
                        btnStartSession.setText("STOP SESSION");
                        btnStartSession.setBackgroundColor(android.graphics.Color.RED);
                        Toast.makeText(this, "✅ Session Started!", Toast.LENGTH_SHORT).show();
                    });
                } else {
                    new Handler(Looper.getMainLooper()).post(() ->
                            Toast.makeText(this, "Server Error: " + responseCode, Toast.LENGTH_SHORT).show());
                }

            } catch (Exception e) {
                e.printStackTrace();
                new Handler(Looper.getMainLooper()).post(() ->
                        Toast.makeText(this, "Connection Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void stopSession() {
        new Thread(() -> {
            try {
                URL url = new URL(BASE_URL + "/sessions/stop?sessionId=" + currentSessionId);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.getInputStream();

                new Handler(Looper.getMainLooper()).post(() -> {
                    isSessionActive = false;
                    tvCode.setText("");
                    btnStartSession.setText("Start Session");
                    btnStartSession.setBackgroundColor(android.graphics.Color.parseColor("#4CAF50"));
                    etSubject.setText("");
                    Toast.makeText(this, "Session Stopped", Toast.LENGTH_SHORT).show();
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}