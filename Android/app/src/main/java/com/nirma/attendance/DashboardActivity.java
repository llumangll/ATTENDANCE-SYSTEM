package com.nirma.attendance; // Keep your package name!

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView; // Import added
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

public class DashboardActivity extends AppCompatActivity {

    // REPLACE WITH YOUR REAL IP AGAIN!
    private static final String BASE_URL = "http://192.168.1.5:8080/api";

    private ListView sessionList;
    private Button btnRefresh;
    private TextView tvWelcome; // To show "Welcome, Umang"
    private ArrayAdapter<ClassSession> adapter;
    private List<ClassSession> sessions = new ArrayList<>();

    // User Data
    private String currentUid;
    private String currentName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // 1. Get User Data from Login
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        currentUid = prefs.getString("uid", "Unknown");
        currentName = prefs.getString("name", "Student");

        // 2. Setup UI
        tvWelcome = findViewById(R.id.tvWelcome); // We will add this ID to XML next
        sessionList = findViewById(R.id.sessionList);
        btnRefresh = findViewById(R.id.btnRefresh);

        // Set the Welcome Message
        tvWelcome.setText("Welcome, " + currentName);

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, sessions);
        sessionList.setAdapter(adapter);

        fetchActiveSessions();

        btnRefresh.setOnClickListener(v -> fetchActiveSessions());

        // 3. Mark Attendance on Click
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
                    // Use the 3-argument constructor
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
        new Thread(() -> {
            try {
                // CALL THE SERVER: /api/mark?uid=...&sessionId=...
                String link = BASE_URL + "/mark?uid=" + currentUid + "&sessionId=" + session.getId();
                URL url = new URL(link);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String response = reader.readLine(); // "Success"

                runOnUiThread(() ->
                        Toast.makeText(this, "✅ Marked Present for " + session.getSubject(), Toast.LENGTH_LONG).show()
                );

            } catch (Exception e) {
                runOnUiThread(() ->
                        Toast.makeText(this, "❌ Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
            }
        }).start();
    }
}