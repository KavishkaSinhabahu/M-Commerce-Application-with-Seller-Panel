package com.kavishkasinhabahu.craftshub;

import static android.content.Context.MODE_PRIVATE;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.Target;

public class ProfileFragment extends Fragment {

    private String userId;
    private ImageView imageView;

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        view.findViewById(R.id.logoutBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                logout();
            }
        });

        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        userId = sharedPreferences.getString("documentId", null);

        view.findViewById(R.id.myaccountCard).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Fragment myaccountFragment = new MyaccountFragment();
                FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
                transaction.replace(R.id.fragment_container, myaccountFragment);
                transaction.commit();
            }
        });

        view.findViewById(R.id.myorderCard).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Fragment myaccountFragment = new OrderFragment();
                FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
                transaction.replace(R.id.fragment_container, myaccountFragment);
                transaction.commit();
            }
        });

        imageView = view.findViewById(R.id.imageView14);
        loadProfileImage();

        view.findViewById(R.id.sellerCard).setOnClickListener(v -> showConfirmationDialog());

        return view;
    }

    private void showConfirmationDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Confirmation")
                .setMessage("Are you sure you want to become a seller?")
                .setPositiveButton("Yes", (dialog, which) -> navigateToBecomeSeller())
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void navigateToBecomeSeller() {
        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, new BecomesellerFragment());
        transaction.commit();
        getActivity().findViewById(R.id.bottomNavigationView).setVisibility(View.GONE);
    }

    private void loadProfileImage() {

        String imageUrl = "http://192.168.1.3:8080/Craftshub/profilepicture/" + userId + ".png";
        String cacheBuster = "?t=" + System.currentTimeMillis();
        Log.d("ProfileImage", "Loading Image URL: " + imageUrl);

        Glide.with(this)
                .load(imageUrl + cacheBuster)
                .placeholder(R.drawable.dppicker)
                .skipMemoryCache(true)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .error(R.drawable.user)
                .into(imageView);
    }

    private void logout() {
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("UserPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.clear();
        editor.apply();

        Intent signinIntent = new Intent(getActivity(), SigninActivity.class);
        startActivity(signinIntent);
        getActivity().finish();
    }
}