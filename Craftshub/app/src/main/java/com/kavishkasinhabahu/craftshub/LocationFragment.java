package com.kavishkasinhabahu.craftshub;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class LocationFragment extends Fragment implements OnMapReadyCallback {

    private GoogleMap mMap;
    private FirebaseFirestore db;

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_location, container, false);

        db = FirebaseFirestore.getInstance();
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.mapfrgment);

        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        view.findViewById(R.id.imageButton3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
                transaction.replace(R.id.fragment_container, new HomeFragment());
                transaction.commit();
                BottomNavigationView bottomNavigationView = requireActivity().findViewById(R.id.bottomNavigationView);
                bottomNavigationView.setVisibility(View.VISIBLE);
            }
        });

        return view;
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        loadStoreLocations();
    }

    private void loadStoreLocations() {
        db.collection("stores").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        double lat = document.getDouble("latitude");
                        double lng = document.getDouble("longitude");

                        BitmapDescriptor customIcon = BitmapDescriptorFactory.fromResource(R.drawable.store1);

                        LatLng location = new LatLng(lat, lng);
                        mMap.addMarker(new MarkerOptions().position(location).title("Seller Location").icon(customIcon));
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 8));
                        mMap.getUiSettings().setZoomControlsEnabled(true);
                    }

                    if (!queryDocumentSnapshots.isEmpty()) {
                        QueryDocumentSnapshot firstStore = (QueryDocumentSnapshot) queryDocumentSnapshots.getDocuments().get(0);
                        double lat = firstStore.getDouble("latitude");
                        double lng = firstStore.getDouble("longitude");
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lng), 8));
                    }
                })
                .addOnFailureListener(e -> Log.e("Firestore", "Error loading locations", e));
    }
}