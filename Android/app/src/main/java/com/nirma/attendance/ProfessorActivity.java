package com.nirma.attendance;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.auth.api.signin.GoogleSignIn; // 🆕
import com.google.android.gms.auth.api.signin.GoogleSignInClient; // 🆕
import com.google.android.gms.auth.api.signin.GoogleSignInOptions; // 🆕
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth; // 🆕

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
    private Button btnExport;
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
        btnExport = findViewById(R.id.btnExport);
        tvCode = findViewById(R.id.tvCode);
        tvProfWelcome = findViewById(R.id.tvProfWelcome);
        Button btnLogout = findViewById(R.id.btnLogoutProf);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        professorName = prefs.getString("name", "Professor");
        tvProfWelcome.setText("Welcome, " + professorName);

        // Start/Stop Button
        btnStartSession.setOnClickListener(v -> {
            if (!isSessionActive) {
                if (checkLocationPermission()) {
                    startSessionWithLocation();
                } else {
                    requestLocationPermission();
                }
            } else {
                stopSession();
            }
        });

        // Export Button
        btnExport.setOnClickListener(v -> {
            if (currentSessionId != -1) {
                String url = BASE_URL + "/attendance/export/" + currentSessionId;
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(url));
                startActivity(intent);
            } else {
                Toast.makeText(this, "No session to export!", Toast.LENGTH_SHORT).show();
            }
        });

        // 🆕 FIXED LOGOUT LOGIC
        btnLogout.setOnClickListener(v -> {
            // 1. Sign out from Firebase
            FirebaseAuth.getInstance().signOut();

            // 2. Sign out from Google Client
            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build();
            GoogleSignInClient googleClient = GoogleSignIn.getClient(this, gso);

            googleClient.signOut().addOnCompleteListener(this, task -> {
                // 3. Clear Local Data
                SharedPreferences.Editor editor = prefs.edit();
                editor.clear();
                editor.apply();

                // 4. Go back to Login
                Intent intent = new Intent(ProfessorActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            });
        });
    }

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
                Toast.makeText(this, "⚠️ Weak GPS. Starting anyway.", Toast.LENGTH_SHORT).show();
                sendStartRequest(0.0, 0.0);
            }
        });
    }

    private void sendStartRequest(double lat, double lon) {
        String subject = etSubject.getText().toString();
        if (subject.isEmpty()) {
            Toast.makeText(this, "Please enter a subject", Toast.LENGTH_SHORT).show();
            return;
        }

        String generatedPassword = String.format("%04d", new Random().nextInt(10000));

        new Thread(() -> {
            try {
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
                        btnExport.setVisibility(View.GONE);
                        Toast.makeText(this, "✅ Session Started!", Toast.LENGTH_SHORT).show();
                    });
                } else {
                    new Handler(Looper.getMainLooper()).post(() ->
                            Toast.makeText(this, "Server Error: " + responseCode, Toast.LENGTH_SHORT).show());
                }

            } catch (Exception e) {
                e.printStackTrace();
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
                    btnExport.setVisibility(View.VISIBLE);
                    Toast.makeText(this, "Session Stopped. Export Ready.", Toast.LENGTH_LONG).show();
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}