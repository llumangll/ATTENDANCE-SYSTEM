package com.nirma.attendance;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
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

// Firebase Imports
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.HashMap;
import java.util.Map;

public class DashboardActivity extends AppCompatActivity {

    private static final String BASE_URL = "http://10.82.33.138:8080/api";

    private ListView sessionList;
    private Button btnRefresh;
    private TextView tvWelcome;
    // Note: Use SessionAdapter if you want the Cards, or ArrayAdapter for simple list.
    // I am using SessionAdapter here since we added it earlier.
    private SessionAdapter adapter;
    private List<ClassSession> sessions = new ArrayList<>();

    private String currentUid;
    private String currentName;
    private DatabaseReference firebaseRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        firebaseRef = FirebaseDatabase.getInstance().getReference("Attendance");

        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        currentUid = prefs.getString("uid", "Unknown");
        currentName = prefs.getString("name", "Student");

        tvWelcome = findViewById(R.id.tvWelcome);
        sessionList = findViewById(R.id.sessionList);
        btnRefresh = findViewById(R.id.btnRefresh);
        Button btnLogout = findViewById(R.id.btnLogout);

        tvWelcome.setText("Welcome, " + currentName);

        adapter = new SessionAdapter(this, sessions);
        sessionList.setAdapter(adapter);

        fetchActiveSessions();
        btnRefresh.setOnClickListener(v -> fetchActiveSessions());

        // CLICK LISTENER: Ask for Password
        sessionList.setOnItemClickListener((parent, view, position, id) -> {
            ClassSession selectedSession = sessions.get(position);
            showPasswordDialog(selectedSession);
        });

        btnLogout.setOnClickListener(v -> {
            SharedPreferences.Editor editor = prefs.edit();
            editor.clear();
            editor.apply();
            Intent intent = new Intent(DashboardActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void showPasswordDialog(ClassSession session) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter Class Code");
        builder.setMessage("Ask your professor for the 4-digit code.");

        // Input Field
        final EditText input = new EditText(this);
        input.setHint("e.g. 1234");
        builder.setView(input);
        input.setTextColor(android.graphics.Color.BLACK);
        builder.setView(input);

        // Buttons
        builder.setPositiveButton("Mark Present", (dialog, which) -> {
            String enteredCode = input.getText().toString().trim();

            // SECURITY CHECK 🔒
            if (enteredCode.equals(session.getPassword())) {
                markAttendance(session);
            } else {
                Toast.makeText(this, "❌ Wrong Password!", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
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
                    // NOW READING PASSWORD FROM JSON
                    newSessions.add(new ClassSession(
                            obj.getLong("id"),
                            obj.getString("professorName"),
                            obj.getString("subject"),
                            obj.getString("password")
                    ));
                }

                new Handler(Looper.getMainLooper()).post(() -> {
                    sessions.clear();
                    sessions.addAll(newSessions);
                    adapter.notifyDataSetChanged();

                    TextView tvEmpty = findViewById(R.id.tvEmpty);
                    if (sessions.isEmpty()) {
                        sessionList.setVisibility(View.GONE);
                        tvEmpty.setVisibility(View.VISIBLE);
                    } else {
                        sessionList.setVisibility(View.VISIBLE);
                        tvEmpty.setVisibility(View.GONE);
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
                conn.getInputStream(); // Trigger request

                runOnUiThread(() ->
                        Toast.makeText(this, "✅ Marked Present (Server & Cloud)", Toast.LENGTH_LONG).show()
                );
            } catch (Exception e) {
                runOnUiThread(() ->
                        Toast.makeText(this, "❌ Server Error: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
            }
        }).start();

        // Part B: Firebase
        String key = firebaseRef.push().getKey();
        Map<String, Object> data = new HashMap<>();
        data.put("studentName", currentName);
        data.put("rollNo", currentUid);
        data.put("subject", session.getSubject());
        data.put("date", new java.util.Date().toString());

        firebaseRef.child(key).setValue(data);
    }
}