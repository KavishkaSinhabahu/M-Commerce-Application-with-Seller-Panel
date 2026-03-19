package com.kavishkasinhabahu.craftshub;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class SignupActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private EditText usernameF, mobileF, emailF, passwordF;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_signup);

        db = FirebaseFirestore.getInstance();

        usernameF = findViewById(R.id.editUsernameSignup);
        mobileF = findViewById(R.id.editMobileSignup);
        emailF = findViewById(R.id.editEmailSignup);
        passwordF = findViewById(R.id.ediitPasswordSignup);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        findViewById(R.id.signupBtn).setOnClickListener(v -> registerUser());
    }

    private void registerUser() {
        String username = usernameF.getText().toString().trim();
        String mobile = mobileF.getText().toString().trim();
        String email = emailF.getText().toString().trim();
        String password = passwordF.getText().toString().trim();

        if (username.isEmpty() || mobile.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "All fields are required.", Toast.LENGTH_SHORT).show();
        } else if (!Pattern.compile("^[a-zA-Z0-9_!#$%&’*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$").matcher(email).matches()) {
            Toast.makeText(this, "Enter valid Email Address.", Toast.LENGTH_SHORT).show();
        } else if (!Pattern.compile("^07[01245678]{1}[0-9]{7}$").matcher(mobile).matches()) {
            Toast.makeText(this, "Enter valid Mobile Number.", Toast.LENGTH_SHORT).show();
        } else if (!Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@#$%^&+=]).{8,}$").matcher(password).matches()) {
            Toast.makeText(this, "Please check \n at least 1 digit \n at least 1 lower case letter \n at least 1 upper case letter \n any letter \n at least 1 special character \n no white spaces \n at least 8 characters \n in your Password.", Toast.LENGTH_LONG).show();
        } else {

            CollectionReference usersRef = db.collection("user");
            Query query = usersRef
                    .whereEqualTo("email", email);
            query.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    if (!task.getResult().isEmpty()) {
                        Toast.makeText(SignupActivity.this, "Email already registered!", Toast.LENGTH_SHORT).show();
                    } else {

                        saveUserData(username, mobile, email, password);
                    }
                } else {
                    Toast.makeText(SignupActivity.this, "Error checking email", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void saveUserData(String username, String mobile, String email, String password) {

        Map<String, Object> user = new HashMap<>();
        user.put("username", username);
        user.put("email", email);
        user.put("mobile", mobile);
        user.put("password", password);
        user.put("type", "user");

        db.collection("user")
                .add(user)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        saveDocumentIdLocally(documentReference.getId());
                        Log.i("Craftshub-Log", "Document Id : " + documentReference.getId());
                        Toast.makeText(SignupActivity.this, "Signup Successful!", Toast.LENGTH_SHORT).show();

                        Intent signinIntent = new Intent(SignupActivity.this, SigninActivity.class);
                        startActivity(signinIntent);
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(SignupActivity.this, "Failed to save user data", Toast.LENGTH_SHORT).show();
                        usernameF.setText("");
                        mobileF.setText("");
                        emailF.setText("");
                        passwordF.setText("");
                    }
                });

    }

    private void saveDocumentIdLocally(String documentId) {
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("documentId", documentId);
        editor.apply();
    }

}