package com.kavishkasinhabahu.craftshub;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.bumptech.glide.Glide;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MyaccountFragment extends Fragment {

    private ImageButton imageButton;
    private Toolbar toolbar;
    private EditText nameEditText, mobileEditText, usernameEditText, emailEditText;
    private TextView addressEditText;
    private String userId;
    private Uri selectedImageUri;

    @SuppressLint({"MissingInflatedId"})
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_myaccount, container, false);
        BottomNavigationView bottomNavigationView = getActivity().findViewById(R.id.bottomNavigationView);

        nameEditText = view.findViewById(R.id.acnametxt);
        mobileEditText = view.findViewById(R.id.acmobileTxt);
        usernameEditText = view.findViewById(R.id.acusernameTxt);
        emailEditText = view.findViewById(R.id.acemailTxt);
        addressEditText = view.findViewById(R.id.acAddress);
        imageButton = view.findViewById(R.id.dpPicker);

        SharedPreferences sharedPreferences = getContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        userId = sharedPreferences.getString("documentId", null);

        if (userId != null) {
            loadUserData(userId);
            loadProfileImage();
        }

        imageButton.setOnClickListener(v -> {
            ImagePicker.with(this)
                    .cropSquare()
                    .galleryOnly()
                    .compress(1024)
                    .maxResultSize(400, 400)
                    .start();
        });

        toolbar = view.findViewById(R.id.myaccountToolbar);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);

        toolbar.setNavigationOnClickListener(v -> {
            FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, new ProfileFragment());
            transaction.commit();
        });

        view.findViewById(R.id.acAddress).setOnClickListener(v -> {
            Fragment addressFragment = new AddressFragment();
            FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, addressFragment);
            transaction.commit();
            bottomNavigationView.setVisibility(View.GONE);
        });

        view.findViewById(R.id.button2).setOnClickListener(v -> saveUserData());

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == getActivity().RESULT_OK && data != null) {
            selectedImageUri = data.getData();
            imageButton.setImageURI(selectedImageUri);

            if (selectedImageUri != null) {
                uploadImageToServer(selectedImageUri);
            }
        } else if (resultCode == ImagePicker.RESULT_ERROR) {
            Toast.makeText(getActivity(), ImagePicker.getError(data), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getActivity(), "Image selection canceled", Toast.LENGTH_SHORT).show();
        }
    }

    private void uploadImageToServer(Uri imageUri) {
        new Thread(() -> {
            try {
                if (imageUri == null) {
                    Log.e("UploadError", "Image URI is null");
                    return;
                }

                InputStream inputStream = getActivity().getContentResolver().openInputStream(imageUri);
                if (inputStream == null) {
                    Log.e("UploadError", "Failed to get InputStream from URI");
                    getActivity().runOnUiThread(() ->
                            Toast.makeText(getContext(), "Failed to read image data", Toast.LENGTH_SHORT).show()
                    );
                    return;
                }

                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    byteArrayOutputStream.write(buffer, 0, bytesRead);
                }
                inputStream.close();
                byte[] imageBytes = byteArrayOutputStream.toByteArray();

                Log.d("UploadDebug", "Image size in bytes: " + imageBytes.length);

                RequestBody requestBody = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("file", userId + ".png",
                                RequestBody.create(MediaType.parse("image/png"), imageBytes))
                        .build();

                Request request = new Request.Builder()
                        .url("http://192.168.1.3:8080/Craftshub/upload")
                        .post(requestBody)
                        .build();

                OkHttpClient client = new OkHttpClient();
                Response response = client.newCall(request).execute();

                if (response.isSuccessful()) {
                    Log.d("UploadSuccess", "Image uploaded successfully!");
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), "Profile picture uploaded!", Toast.LENGTH_SHORT).show();
                        loadProfileImage();
                    });
                } else {
                    Log.e("UploadError", "Server Response Code: " + response.code());
                    Log.e("UploadError", "Server Response: " + response.body().string());

                    getActivity().runOnUiThread(() ->
                            Toast.makeText(getContext(), "Upload failed! Server response: " + response.code(), Toast.LENGTH_SHORT).show()
                    );
                }
            } catch (IOException e) {
                Log.e("UploadError", "IOException: " + e.getMessage());
                e.printStackTrace();
                getActivity().runOnUiThread(() -> {
                    Toast.makeText(getContext(), "Error uploading image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.i("Error uploading image:", e.getMessage());
                });
            }
        }).start();
    }


    private void loadUserData(String documentId) {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();

        firestore.collection("user").document(documentId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String nameUser = documentSnapshot.getString("name");
                        String userMobile = documentSnapshot.getString("mobile");
                        String username = documentSnapshot.getString("username");
                        String email = documentSnapshot.getString("email");
                        String address = documentSnapshot.getString("address");

                        updateUI(nameUser != null ? nameUser : "", userMobile != null ? userMobile : "", address != null ? address : "", username != null ? username : "", email != null ? email : "");
                    } else {
                        Toast.makeText(getContext(), "No user data found!", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Error loading user data: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void updateUI(String nameUser, String userMobile, String address, String username, String email) {
        nameEditText.setText(nameUser);
        mobileEditText.setText(userMobile);
        addressEditText.setText(address);
        usernameEditText.setText(username);
        emailEditText.setText(email);
    }

    private void saveUserData() {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();

        String name = nameEditText.getText().toString().trim();
        String mobile = mobileEditText.getText().toString().trim();

        if (name.isEmpty() || mobile.isEmpty()) {
            Toast.makeText(getContext(), "Name and Mobile cannot be empty!", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> userData = new HashMap<>();
        userData.put("name", name);
        userData.put("mobile", mobile);

        firestore.collection("user").document(userId)
                .update(userData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Profile Updated!", Toast.LENGTH_SHORT).show();
                    navigateToProfile();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Error updating profile: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void navigateToProfile() {
        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, new ProfileFragment());
        transaction.commit();
    }

    private void loadProfileImage() {
        if (imageButton == null) return;

        String imageUrl = "http://192.168.1.3:8080/Craftshub/profilepicture/" + userId + ".png";
        String cacheBuster = "?t=" + System.currentTimeMillis();
        Log.d("ProfileImage", "Loading Image URL: " + imageUrl); // Debugging

        Glide.with(this)
                .load(imageUrl + cacheBuster)
                .placeholder(R.drawable.dppicker)
                .skipMemoryCache(true)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .error(R.drawable.user)
                .into(imageButton);
    }

}