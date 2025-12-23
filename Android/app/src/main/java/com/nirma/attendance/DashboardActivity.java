package com.nirma.attendance;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import android.content.Intent;

// Firebase Imports
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.HashMap;
import java.util.Map;

public class DashboardActivity extends AppCompatActivity {

    // ⚠️ IP Updated: 10.82.33.138
    private static final String BASE_URL = "http://10.82.33.138:8080/api";

    private ListView sessionList;
    private Button btnRefresh;
    private TextView tvWelcome;
    private SessionAdapter adapter;
    private List<ClassSession> sessions = new ArrayList<>();

    private String currentUid;
    private String currentName;
    private DatabaseReference firebaseRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // 1. Initialize Firebase
        firebaseRef = FirebaseDatabase.getInstance().getReference("Attendance");

        // 2. Get User Info
        // We define 'prefs' here ONCE.
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        currentUid = prefs.getString("uid", "Unknown");
        currentName = prefs.getString("name", "Student");

        tvWelcome = findViewById(R.id.tvWelcome);
        sessionList = findViewById(R.id.sessionList);
        btnRefresh = findViewById(R.id.btnRefresh);
        Button btnLogout = findViewById(R.id.btnLogout); // Find Logout Button

        tvWelcome.setText("Welcome, " + currentName);

        adapter = new SessionAdapter(this, sessions);
        sessionList.setAdapter(adapter);

        fetchActiveSessions();
        btnRefresh.setOnClickListener(v -> fetchActiveSessions());

        sessionList.setOnItemClickListener((parent, view, position, id) -> {
            ClassSession selectedSession = sessions.get(position);
            markAttendance(selectedSession);
        });

        // 3. LOGOUT LOGIC (Fixed)
        btnLogout.setOnClickListener(v -> {
            // We use the 'prefs' variable that already exists.
            // We do NOT write "SharedPreferences prefs =" again.
            SharedPreferences.Editor editor = prefs.edit();
            editor.clear(); // Wipe data
            editor.apply();

            // Go back to Login
            Intent intent = new Intent(DashboardActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
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
                    newSessions.add(new ClassSession(
                            obj.getLong("id"),
                            obj.getString("professorName"),
                            obj.getString("subject")
                    ));
                }

                new Handler(Looper.getMainLooper()).post(() -> {
                    sessions.clear();
                    sessions.addAll(newSessions);
                    adapter.notifyDataSetChanged();
                    Toast.makeText(DashboardActivity.this, "List Updated", Toast.LENGTH_SHORT).show();
                });

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void markAttendance(ClassSession session) {
        // Part A: Spring Boot Server
        new Thread(() -> {
            try {
                String link = BASE_URL + "/mark?uid=" + currentUid + "&sessionId=" + session.getId();
                URL url = new URL(link);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                reader.readLine();

                runOnUiThread(() ->
                        Toast.makeText(this, "✅ Marked Present (Server & Cloud)", Toast.LENGTH_LONG).show()
                );

            } catch (Exception e) {
                runOnUiThread(() ->
                        Toast.makeText(this, "❌ Server Error: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
            }
        }).start();

        // Part B: Firebase Cloud
        String key = firebaseRef.push().getKey();
        Map<String, Object> data = new HashMap<>();
        data.put("studentName", currentName);
        data.put("rollNo", currentUid);
        data.put("subject", session.getSubject());
        data.put("date", new java.util.Date().toString());

        firebaseRef.child(key).setValue(data)
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Cloud Error: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }
}