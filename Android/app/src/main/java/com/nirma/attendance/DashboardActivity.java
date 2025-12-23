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

// 1. Firebase Imports
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.HashMap;
import java.util.Map;

public class DashboardActivity extends AppCompatActivity {

    // ⚠️ IMPORTANT: Check your IP Address!
    private static final String BASE_URL = "http://10.82.33.138:8080/api";

    private ListView sessionList;
    private Button btnRefresh;
    private TextView tvWelcome;
    private ArrayAdapter<ClassSession> adapter;
    private List<ClassSession> sessions = new ArrayList<>();

    private String currentUid;
    private String currentName;

    // 2. Firebase Reference
    private DatabaseReference firebaseRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // 3. Initialize Firebase (Creates a folder called "Attendance" in the cloud)
        firebaseRef = FirebaseDatabase.getInstance().getReference("Attendance");

        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        currentUid = prefs.getString("uid", "Unknown");
        currentName = prefs.getString("name", "Student");

        tvWelcome = findViewById(R.id.tvWelcome);
        sessionList = findViewById(R.id.sessionList);
        btnRefresh = findViewById(R.id.btnRefresh);

        tvWelcome.setText("Welcome, " + currentName);

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, sessions);
        sessionList.setAdapter(adapter);

        fetchActiveSessions();
        btnRefresh.setOnClickListener(v -> fetchActiveSessions());

        sessionList.setOnItemClickListener((parent, view, position, id) -> {
            ClassSession selectedSession = sessions.get(position);
            markAttendance(selectedSession);
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
        // --- PART A: Send to Spring Boot (Your Laptop) ---
        new Thread(() -> {
            try {
                String link = BASE_URL + "/mark?uid=" + currentUid + "&sessionId=" + session.getId();
                URL url = new URL(link);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                reader.readLine(); // Read response to trigger the request

                // If Spring Boot succeeds, we show the success message
                runOnUiThread(() ->
                        Toast.makeText(this, "✅ Marked Present (Server & Cloud)", Toast.LENGTH_LONG).show()
                );

            } catch (Exception e) {
                runOnUiThread(() ->
                        Toast.makeText(this, "❌ Server Error: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
            }
        }).start();

        // --- PART B: Send to Firebase (Google Cloud) ☁️ ---
        // This runs instantly alongside the Server code
        String key = firebaseRef.push().getKey(); // Create unique ID

        Map<String, Object> data = new HashMap<>();
        data.put("studentName", currentName);
        data.put("rollNo", currentUid);
        data.put("subject", session.getSubject());
        data.put("date", new java.util.Date().toString()); // Adds human-readable time

        firebaseRef.child(key).setValue(data)
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Cloud Error: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }
}