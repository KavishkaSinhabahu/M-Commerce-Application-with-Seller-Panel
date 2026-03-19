package com.kavishkasinhabahu.craftshub;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.openlocationcode.OpenLocationCode;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class BecomesellerFragment extends Fragment {

    private EditText etName, etMobile, etEmail, etStoreAddress;
    private Button btnSave;
    private FirebaseFirestore db;
    private String userId;

    private static final String GOOGLE_MAPS_API_KEY = "AIzaSyAI5l6t6af7Wgdny50T8B9rln-PBbrtoVI";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_becomeseller, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        etName = view.findViewById(R.id.editTextText8);
        etMobile = view.findViewById(R.id.editTextPhone3);
        etEmail = view.findViewById(R.id.editTextTextEmailAddress3);
        etStoreAddress = view.findViewById(R.id.editTextTextMultiLine4);
        btnSave = view.findViewById(R.id.sellerbtn);

        db = FirebaseFirestore.getInstance();

        SharedPreferences prefs = requireActivity().getSharedPreferences("UserPrefs", MODE_PRIVATE);
        userId = prefs.getString("documentId", null);

        if (userId != null) {
            loadUserData();
        } else {
            Toast.makeText(getContext(), "User ID not found!", Toast.LENGTH_SHORT).show();
        }

        btnSave.setOnClickListener(v -> validateAndSaveData());
    }

    private void loadUserData() {
        db.collection("user").document(userId).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                etName.setText(documentSnapshot.getString("name"));
                etMobile.setText(documentSnapshot.getString("mobile"));
                etEmail.setText(documentSnapshot.getString("email"));
                etStoreAddress.setText(documentSnapshot.getString("storeAddress"));

                if (TextUtils.isEmpty(etName.getText())) etName.setHint("Enter Name");
                if (TextUtils.isEmpty(etMobile.getText())) etMobile.setHint("Enter Mobile");
                if (TextUtils.isEmpty(etEmail.getText())) etEmail.setHint("Enter Email");
                if (TextUtils.isEmpty(etStoreAddress.getText())) etStoreAddress.setHint("Enter Store Address");
            }
        }).addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to load user data", Toast.LENGTH_SHORT).show());
    }

    private void validateAndSaveData() {
        String name = etName.getText().toString().trim();
        String mobile = etMobile.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String storeAddress = etStoreAddress.getText().toString().trim();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(mobile) || TextUtils.isEmpty(email) || TextUtils.isEmpty(storeAddress)) {
            Toast.makeText(getContext(), "All fields are required!", Toast.LENGTH_SHORT).show();
            return;
        }

        checkMobileExists(mobile, name, email, storeAddress);
    }

    private void checkMobileExists(String mobile, String name, String email, String storeAddress) {
        db.collection("user").whereEqualTo("mobile", mobile).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && !task.getResult().isEmpty()) {
                for (DocumentSnapshot document : task.getResult()) {
                    if (!document.getId().equals(userId)) {
                        Toast.makeText(getContext(), "Mobile number already exists!", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
            }
            fetchLatLngAndSave(name, mobile, email, storeAddress);
        });
    }

    private void fetchLatLngAndSave(String name, String mobile, String email, String storeAddress) {
        new FetchLatLngTask((lat, lng) -> {
            String currentDate = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(new Date());

            Map<String, Object> userData = new HashMap<>();
            userData.put("name", name);
            userData.put("mobile", mobile);
            userData.put("email", email);
            userData.put("storeAddress", storeAddress);
            userData.put("latitude", lat);
            userData.put("longitude", lng);
            userData.put("registeredDate", currentDate);
            userData.put("type", "seller");

            db.collection("user").document(userId).update(userData)
                    .addOnSuccessListener(
                            aVoid -> {
                                Toast.makeText(getContext(), "Seller profile updated!", Toast.LENGTH_SHORT).show();
                                SharedPreferences sharedPreferences = getContext().getSharedPreferences("UserPrefs", MODE_PRIVATE);
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putString("userType", "seller");
                                editor.apply();
                                Intent intent = new Intent(getContext(), DashboardActivity.class);
                                startActivity(intent);
                            }
                    )
                    .addOnFailureListener(
                            e -> {
                                Toast.makeText(getContext(), "Error updating profile", Toast.LENGTH_SHORT).show();
                                FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
                                transaction.replace(R.id.fragment_container, new ProfileFragment());
                                transaction.commit();
                                getActivity().findViewById(R.id.bottomNavigationView).setVisibility(View.VISIBLE);
                            }
                    );
        }).execute(storeAddress);
    }

    private static class FetchLatLngTask extends AsyncTask<String, Void, LatLng> {
        private final LatLngCallback callback;

        FetchLatLngTask(LatLngCallback callback) {
            this.callback = callback;
        }

        @Override
        protected LatLng doInBackground(String... params) {
            String address = params[0];
            try {
                String encodedAddress = URLEncoder.encode(address, "UTF-8");
                String urlString = "https://maps.googleapis.com/maps/api/geocode/json?address=" +
                        encodedAddress + "&key=" + GOOGLE_MAPS_API_KEY;
                URL url = new URL(urlString);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

                InputStream inputStream = connection.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                Log.d("API_RESPONSE", response.toString());

                return parseLatLng(response.toString());

            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(LatLng latLng) {
            if (latLng != null) {
                callback.onLatLngReceived(latLng.latitude, latLng.longitude);
            } else {
                callback.onLatLngReceived(0.0, 0.0);
            }
        }

        private LatLng parseLatLng(String jsonResponse) {
            try {
                JSONObject jsonObject = new JSONObject(jsonResponse);
                JSONArray results = jsonObject.getJSONArray("results");

                if (results.length() > 0) {
                    JSONObject firstResult = results.getJSONObject(0);
                    JSONObject geometry = firstResult.getJSONObject("geometry");
                    JSONObject location = geometry.getJSONObject("location");

                    double lat = location.getDouble("lat");
                    double lng = location.getDouble("lng");

                    return new LatLng(lat, lng);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    public interface LatLngCallback {
        void onLatLngReceived(double lat, double lng);
    }

    public static class LatLng {
        public final double latitude;
        public final double longitude;

        public LatLng(double latitude, double longitude) {
            this.latitude = latitude;
            this.longitude = longitude;
        }
    }
}

