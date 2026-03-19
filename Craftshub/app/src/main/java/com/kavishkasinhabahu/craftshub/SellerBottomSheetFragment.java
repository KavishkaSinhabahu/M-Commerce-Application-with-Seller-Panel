package com.kavishkasinhabahu.craftshub;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.firestore.FirebaseFirestore;

public class SellerBottomSheetFragment extends BottomSheetDialogFragment {

    private static final String TAG = "SellerBottomSheet";
    private GoogleMap mMap;
    private String sellerId;
    private TextView snameTxt, smobileTxt, saddressTxt;
    private ImageView sellerImage;
    private static final int REQUEST_CALL_PERMISSION = 1;

    public static SellerBottomSheetFragment newInstance(String sellerId) {
        SellerBottomSheetFragment fragment = new SellerBottomSheetFragment();
        Bundle args = new Bundle();
        args.putString("sellerId", sellerId);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_seller, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();

        View view = getView();
        if (view != null) {
            ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
            layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
            view.setLayoutParams(layoutParams);
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null) {
            sellerId = getArguments().getString("sellerId");
        }

        snameTxt = view.findViewById(R.id.textView49);
        smobileTxt = view.findViewById(R.id.textView51);
        saddressTxt = view.findViewById(R.id.textView52);
        sellerImage = view.findViewById(R.id.imageView13);

        smobileTxt.setOnClickListener(v -> makePhoneCall());

        loadProfileImage();

        Toolbar toolbar = view.findViewById(R.id.toolbar5);
        toolbar.setNavigationOnClickListener(v -> dismiss());

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(googleMap -> {
                Log.d("MAP_DEBUG", "Map is ready!");
                mMap = googleMap;
                fetchSellerLocation();
            });
        }
    }

    private void fetchSellerLocation() {
        if (sellerId == null) {
            Log.e(TAG, "Seller ID is null");
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("user").document(sellerId).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                String name = documentSnapshot.getString("name");
                String mobile = documentSnapshot.getString("mobile");
                String saddress = documentSnapshot.getString("storeAddress");
                Double lat = documentSnapshot.getDouble("latitude");
                Double lng = documentSnapshot.getDouble("longitude");

                snameTxt.setText(name);
                smobileTxt.setText(mobile);
                saddressTxt.setText(saddress);

                BitmapDescriptor customIcon = BitmapDescriptorFactory.fromResource(R.drawable.store1);

                if (lat != null && lng != null) {
                    LatLng sellerLocation = new LatLng(lat, lng);
                    mMap.addMarker(new MarkerOptions().position(sellerLocation).title("Seller Location").icon(customIcon));
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sellerLocation, 15f));
                    mMap.getUiSettings().setZoomControlsEnabled(true);
                } else {
                    Log.e(TAG, "Latitude or Longitude is null");
                }
            } else {
                Log.e(TAG, "Seller document does not exist");
            }
        }).addOnFailureListener(e -> Log.e(TAG, "Failed to fetch seller data", e));
    }

    private void loadProfileImage() {
        String imageUrl = "http://192.168.1.3:8080/Craftshub/profilepicture/" + sellerId + ".png";
        String cacheBuster = "?t=" + System.currentTimeMillis();
        Log.d("ProfileImage", "Loading Image URL: " + imageUrl);

        Glide.with(this)
                .load(imageUrl + cacheBuster)
                .placeholder(R.drawable.dppicker)
                .skipMemoryCache(true)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .error(R.drawable.agent)
                .into(sellerImage);
    }

    private void makePhoneCall() {
        if (smobileTxt == null) {
            return;
        }

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CALL_PHONE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(),
                    new String[]{Manifest.permission.CALL_PHONE}, REQUEST_CALL_PERMISSION);
        } else {
            startCall();
        }
    }

    private void startCall() {
        Intent intent = new Intent(Intent.ACTION_CALL);
        intent.setData(Uri.parse("tel:" + smobileTxt.getText()));
        startActivity(intent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CALL_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCall();
            }
        }
    }
}