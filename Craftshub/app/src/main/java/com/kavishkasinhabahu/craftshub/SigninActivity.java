package com.kavishkasinhabahu.craftshub;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.FragmentTransaction;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

public class SigninActivity extends AppCompatActivity {

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_signin);

        checkLoginStatus();

        db = FirebaseFirestore.getInstance();

        @SuppressLint({"MissingInflatedId", "LocalSuppress"})
        TextView textView = findViewById(R.id.signupLink);
        String text = "Sign Up Here ...";
        SpannableString spannable = new SpannableString(text);
        spannable.setSpan(new UnderlineSpan(), 0, text.length(), 0);
        textView.setText(spannable);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        findViewById(R.id.signinBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText emailA = findViewById(R.id.editSigninEmail);
                EditText pwd = findViewById(R.id.editSigninPassword);

                String email = emailA.getText().toString().trim();
                String password = pwd.getText().toString().trim();

                if(email.isEmpty()) {
                    Toast.makeText(SigninActivity.this, "Please enter your Email Address.", Toast.LENGTH_SHORT).show();
                } else if (password.isEmpty()) {
                    Toast.makeText(SigninActivity.this, "Please enter your Password", Toast.LENGTH_SHORT).show();
                } else {

                    checkUserCredentials(email, password);
                }
            }
        });

        TextView signupLink = findViewById(R.id.signupLink);
        signupLink.setOnClickListener(v -> {
            Intent intent = new Intent(SigninActivity.this, SignupActivity.class);
            startActivity(intent);
        });
    }

    private void checkUserCredentials(String email, String password) {
        CollectionReference usersRef = db.collection("user");

        Query query = usersRef
                .whereEqualTo("email", email)
                .whereEqualTo("password", password);

        query.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (!task.getResult().isEmpty()) {

                    String documentId = task.getResult().getDocuments().get(0).getId();
                    String userType = task.getResult().getDocuments().get(0).getString("type");
                    String name = task.getResult().getDocuments().get(0).getString("username");

                    // Login successful
                    Toast.makeText(SigninActivity.this, "Login Successfully", Toast.LENGTH_SHORT).show();
                    saveLoginDetails(email, password, name, documentId, userType);

                    Log.d("Craftshub-Log", "User authenticated: " + email);
                } else {

                    // Invalid credentials
                    Toast.makeText(SigninActivity.this, "Invalid username or password", Toast.LENGTH_SHORT).show();
                    Log.d("Craftshub-Log", "Authentication failed for: " + email);
                }
            } else {

                // Error handling
                Toast.makeText(SigninActivity.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("Craftshub-Log", "Error getting user", task.getException());
            }
        });
    }

    private void checkLoginStatus() {
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        boolean isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false);

        if (isLoggedIn) {
            Intent intent = new Intent(SigninActivity.this, MainActivity.class);
            startActivity(intent);
        }
    }

    private void saveLoginDetails(String email, String password, String name, String documentId, String userType) {
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString("username", email);
        editor.putString("password", password);
        editor.putString("name", name);
        editor.putString("documentId", documentId);
        editor.putString("userType", userType);
        editor.putBoolean("isLoggedIn", true);  // Save login status

        editor.apply();

        Intent intent = new Intent(SigninActivity.this, MainActivity.class);
        startActivity(intent);
    }
}