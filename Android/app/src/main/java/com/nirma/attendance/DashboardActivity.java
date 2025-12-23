package com.nirma.attendance;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
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

    // REPLACE THIS WITH YOUR COMPUTER'S IP ADDRESS!
    private static final String BASE_URL = "http://10.82.33.138:8080/api";

    private ListView sessionList;
    private Button btnRefresh;
    private ArrayAdapter<ClassSession> adapter;
    private List<ClassSession> sessions = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        sessionList = findViewById(R.id.sessionList);
        btnRefresh = findViewById(R.id.btnRefresh);

        // Setup the List Adapter
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, sessions);
        sessionList.setAdapter(adapter);

        // 1. Load classes when screen opens
        fetchActiveSessions();

        // 2. Refresh button click
        btnRefresh.setOnClickListener(v -> fetchActiveSessions());

        // 3. Handle clicking a class (Mark Attendance)
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

                // Parse JSON
                JSONArray jsonArray = new JSONArray(response.toString());
                final List<ClassSession> newSessions = new ArrayList<>();

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject obj = jsonArray.getJSONObject(i);
                    ClassSession session = new ClassSession(
                            obj.getLong("id"),
                            obj.getString("professorName"),
                            obj.getString("subject")
                    );
                    newSessions.add(session);
                }

                // Update UI on Main Thread
                new Handler(Looper.getMainLooper()).post(() -> {
                    sessions.clear();
                    sessions.addAll(newSessions);
                    adapter.notifyDataSetChanged();
                    Toast.makeText(DashboardActivity.this, "List Updated", Toast.LENGTH_SHORT).show();
                });

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(DashboardActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void markAttendance(ClassSession session) {
        // For now, let's just show a Toast.
        // Later we will connect this to your "Student ID" logic.
        Toast.makeText(this, "Selected: " + session.getSubject(), Toast.LENGTH_SHORT).show();

        // TODO: Send request to /api/mark?uid=101&sessionId=...
    }
}