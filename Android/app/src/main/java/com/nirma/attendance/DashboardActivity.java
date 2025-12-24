package com.nirma.attendance;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DashboardActivity extends AppCompatActivity {

    private static final String BASE_URL = "http://10.82.33.138:8080/api";
    private static final int LOC_REQ_CODE = 1002;

    private ListView sessionList;
    private Button btnRefresh;
    private TextView tvWelcome, tvRollNo, tvEmpty;
    private SessionAdapter adapter;
    private List<ClassSession> sessions = new ArrayList<>();

    private String currentUid;
    private String currentName;
    private DatabaseReference firebaseRef;
    private FusedLocationProviderClient fusedLocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        firebaseRef = FirebaseDatabase.getInstance().getReference("Attendance");
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        currentUid = prefs.getString("uid", "UnknownID");
        currentName = prefs.getString("name", "Student");

        tvWelcome = findViewById(R.id.tvWelcomeName);
        tvRollNo = findViewById(R.id.tvRollNo);
        sessionList = findViewById(R.id.sessionList);
        btnRefresh = findViewById(R.id.btnRefresh);
        tvEmpty = findViewById(R.id.tvEmpty);
        Button btnLogout = findViewById(R.id.btnLogout);

        if(tvWelcome != null) tvWelcome.setText("Hello, " + currentName);
        if(tvRollNo != null) tvRollNo.setText("ID: " + currentUid.toUpperCase());

        adapter = new SessionAdapter(this, sessions);
        sessionList.setAdapter(adapter);

        fetchActiveSessions();
        btnRefresh.setOnClickListener(v -> fetchActiveSessions());

        sessionList.setOnItemClickListener((parent, view, position, id) -> {
            ClassSession selectedSession = sessions.get(position);
            // 🔒 Step 1: Check Location Permission before showing dialog
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                showPasswordDialog(selectedSession);
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOC_REQ_CODE);
            }
        });

        btnLogout.setOnClickListener(v -> {
            SharedPreferences.Editor editor = prefs.edit();
            editor.clear();
            editor.apply();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }

    private void showPasswordDialog(ClassSession session) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter Class Code");
        builder.setMessage("Ask your professor for the 4-digit code.");

        final EditText input = new EditText(this);
        input.setHint("e.g. 1234");
        input.setTextColor(android.graphics.Color.BLACK);
        builder.setView(input);

        builder.setPositiveButton("Mark Present", (dialog, which) -> {
            String enteredCode = input.getText().toString().trim();
            if (enteredCode.equals(session.getPassword())) {
                // 🔒 Step 2: Verify Location
                verifyLocationAndMark(session);
            } else {
                Toast.makeText(this, "❌ Wrong Password!", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void verifyLocationAndMark(ClassSession session) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        Toast.makeText(this, "Checking Location...", Toast.LENGTH_SHORT).show();

        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null) {
                // 📏 Calculate Distance
                float[] results = new float[1];
                Location.distanceBetween(location.getLatitude(), location.getLongitude(),
                        session.getLat(), session.getLon(), results);
                float distanceInMeters = results[0];

                // 🛑 PROXY CHECK: 50 Meters Limit
                if (distanceInMeters <= 50) {
                    markAttendance(session);
                } else {
                    Toast.makeText(this, "❌ Proxy Alert! You are " + (int)distanceInMeters + "m away from class.", Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(this, "⚠️ Could not find your location. Turn on GPS.", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void fetchActiveSessions() {
        new Thread(() -> {
            try {
                URL url = new URL(BASE_URL + "/sessions/active");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) response.append(line);

                JSONArray jsonArray = new JSONArray(response.toString());
                final List<ClassSession> newSessions = new ArrayList<>();

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject obj = jsonArray.getJSONObject(i);
                    // 🆕 Parse Lat/Lon from JSON (handling if they are missing/0.0)
                    double sLat = obj.optDouble("lat", 0.0);
                    double sLon = obj.optDouble("lon", 0.0);

                    newSessions.add(new ClassSession(
                            obj.getLong("id"),
                            obj.getString("professorName"),
                            obj.getString("subject"),
                            obj.getString("password"),
                            sLat, sLon
                    ));
                }

                new Handler(Looper.getMainLooper()).post(() -> {
                    sessions.clear();
                    sessions.addAll(newSessions);
                    adapter.notifyDataSetChanged();

                    if (tvEmpty != null) {
                        tvEmpty.setVisibility(sessions.isEmpty() ? View.VISIBLE : View.GONE);
                        sessionList.setVisibility(sessions.isEmpty() ? View.GONE : View.VISIBLE);
                    }
                    Toast.makeText(DashboardActivity.this, "List Updated", Toast.LENGTH_SHORT).show();
                });

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void markAttendance(ClassSession session) {
        // Part A: Server
        new Thread(() -> {
            try {
                String link = BASE_URL + "/mark?uid=" + currentUid + "&sessionId=" + session.getId();
                URL url = new URL(link);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.getInputStream();

                runOnUiThread(() -> Toast.makeText(this, "✅ Marked Present! (Within Range)", Toast.LENGTH_LONG).show());
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(this, "❌ Server Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        }).start();

        // Part B: Firebase Backup
        String key = firebaseRef.push().getKey();
        Map<String, Object> data = new HashMap<>();
        data.put("studentName", currentName);
        data.put("rollNo", currentUid);
        data.put("subject", session.getSubject());
        data.put("date", new java.util.Date().toString());
        firebaseRef.child(key).setValue(data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOC_REQ_CODE && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Permission Granted. Try clicking the class again.", Toast.LENGTH_SHORT).show();
        }
    }
}