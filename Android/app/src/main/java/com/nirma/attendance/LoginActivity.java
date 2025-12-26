package com.nirma.attendance;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;

public class LoginActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 9001;
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private RadioGroup rgRole;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        rgRole = findViewById(R.id.rgRole);
        Button btnGoogleLogin = findViewById(R.id.btnGoogleSignIn);

        // 1. Configure Google Sign-In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // 2. Button Click Listener
        btnGoogleLogin.setOnClickListener(v -> signIn());
    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                Toast.makeText(this, "Google Sign-In Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    // 🚨 UPDATED SECURE AUTH METHOD
    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        String email = acct.getEmail();
        if (email == null) return;

        // 1. DOMAIN CHECK (Allowed: Nirma Emails OR your specific Gmail)
        if (!email.endsWith("@nirmauni.ac.in") && !email.equals("umangrabadiya18@gmail.com")) {
            Toast.makeText(this, "❌ Login Restricted: Use Nirma email.", Toast.LENGTH_LONG).show();
            mGoogleSignInClient.signOut();
            return;
        }

        // 2. PROFESSOR SECURITY CHECK
        boolean isProfessorRoleSelected = (rgRole.getCheckedRadioButtonId() == R.id.rbProfessor);

        if (isProfessorRoleSelected) {
            char firstChar = email.charAt(0);

            // Logic: If email starts with a Digit (Student) AND it is NOT Umang -> BLOCK
            if (Character.isDigit(firstChar) && !email.equals("umangrabadiya18@gmail.com")) {
                Toast.makeText(this, "⚠️ Access Denied: Students cannot login as Professor.", Toast.LENGTH_LONG).show();
                mGoogleSignInClient.signOut();
                return;
            }
        }

        // 3. PROCEED TO FIREBASE
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        saveUserToPrefs(acct);
                        navigateBasedOnRole();
                    } else {
                        Toast.makeText(this, "Authentication Failed.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void saveUserToPrefs(GoogleSignInAccount acct) {
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        String name = acct.getDisplayName();
        String uid = acct.getEmail().split("@")[0];

        editor.putString("name", name);
        editor.putString("uid", uid);
        editor.apply();

        Toast.makeText(this, "Welcome, " + name, Toast.LENGTH_SHORT).show();
    }

    private void navigateBasedOnRole() {
        int selectedId = rgRole.getCheckedRadioButtonId();
        boolean isProfessor = (selectedId == R.id.rbProfessor);

        if (isProfessor) {
            startActivity(new Intent(this, ProfessorActivity.class));
        } else {
            startActivity(new Intent(this, DashboardActivity.class));
        }
        finish();
    }
}