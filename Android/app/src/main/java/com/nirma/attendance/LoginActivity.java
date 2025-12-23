package com.nirma.attendance;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView; // Import TextView
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    private EditText etRollNo, etName;
    private Button btnLogin;
    private Switch switchRole;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 1. SMART AUTO-LOGIN
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        if (prefs.contains("uid")) {
            String role = prefs.getString("role", "STUDENT");
            if (role.equals("PROFESSOR")) {
                startActivity(new Intent(this, ProfessorActivity.class));
            } else {
                startActivity(new Intent(this, DashboardActivity.class));
            }
            finish();
            return;
        }

        setContentView(R.layout.activity_login);

        // 2. Setup Views
        etRollNo = findViewById(R.id.etRollNo);
        etName = findViewById(R.id.etName);
        btnLogin = findViewById(R.id.btnLogin);
        switchRole = findViewById(R.id.switchRole);
        TextView tvTitle = findViewById(R.id.tvTitle); // Find the Title Text

        // 3. UI TOGGLE LOGIC (The part you were missing!)
        switchRole.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                // Professor Mode
                tvTitle.setText("Professor Login");
                etRollNo.setHint("Enter Employee ID");
                btnLogin.setText("Login as Professor");
            } else {
                // Student Mode
                tvTitle.setText("Student Login");
                etRollNo.setHint("Enter Roll No (e.g. 21BCE001)");
                btnLogin.setText("Continue");
            }
        });

        // 4. Handle Login Button Click
        btnLogin.setOnClickListener(v -> {
            String uid = etRollNo.getText().toString().trim();
            String name = etName.getText().toString().trim();

            if (uid.isEmpty() || name.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            boolean isProfessor = switchRole.isChecked();
            String role = isProfessor ? "PROFESSOR" : "STUDENT";

            // Save User Data
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("uid", uid);
            editor.putString("name", name);
            editor.putString("role", role);
            editor.apply();

            Toast.makeText(this, "Welcome " + name, Toast.LENGTH_SHORT).show();

            // Navigate
            if (isProfessor) {
                startActivity(new Intent(this, ProfessorActivity.class));
            } else {
                startActivity(new Intent(this, DashboardActivity.class));
            }
            finish();
        });
    }
}