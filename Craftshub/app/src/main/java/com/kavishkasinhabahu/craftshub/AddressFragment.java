package com.kavishkasinhabahu.craftshub;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddressFragment extends Fragment {

    private Spinner spinnerProvinces;
    private FirebaseFirestore db;
    private List<String> provinceList;
    private ArrayAdapter<String> adapter;
    private String selectedProvince = "";
    private EditText etLine1, etLine2, etCity, etPostalCode;
    private Button btnSave;
    private String userDocumentId;

    public void MyAccountFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_address, container, false);

        etLine1 = view.findViewById(R.id.editTextText3);
        etLine2 = view.findViewById(R.id.editTextText4);
        etCity = view.findViewById(R.id.editTextText5);
        etPostalCode = view.findViewById(R.id.editTextNumber);
        btnSave = view.findViewById(R.id.button9);

        spinnerProvinces = view.findViewById(R.id.spinner);
        db = FirebaseFirestore.getInstance();
        provinceList = new ArrayList<>();

        provinceList.add("Select a Province");

        adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, provinceList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerProvinces.setAdapter(adapter);

        loadProvinces();

        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        userDocumentId = sharedPreferences.getString("documentId", null);

        if (userDocumentId == null) {
            Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_SHORT).show();
            return view;
        }

        btnSave.setOnClickListener(v -> saveAddress());

        spinnerProvinces.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0) {
                    selectedProvince = provinceList.get(position);
                    Toast.makeText(requireContext(), "Selected: " + selectedProvince, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedProvince = "";
            }
        });

        return view;
    }

    private void loadProvinces() {
        db.collection("province").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                provinceList.clear();
                provinceList.add("Select a Province");

                for (QueryDocumentSnapshot document : task.getResult()) {
                    String provinceName = document.getString("name");
                    if (provinceName != null) {
                        provinceList.add(provinceName);
                    }
                }

                adapter.notifyDataSetChanged();
            } else {
                Toast.makeText(requireContext(), "Failed to load provinces", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveAddress() {

        BottomNavigationView bottomNavigationView = getActivity().findViewById(R.id.bottomNavigationView);

        String line1 = etLine1.getText().toString().trim();
        String line2 = etLine2.getText().toString().trim();
        String state = spinnerProvinces.getSelectedItem().toString();
        String city = etCity.getText().toString().trim();
        String postalCode = etPostalCode.getText().toString().trim();

        if (line1.isEmpty() || line2.isEmpty() || state.equals("Select a Province") || city.isEmpty() || postalCode.isEmpty()) {
            Toast.makeText(requireContext(), "Please fill all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> address = new HashMap<>();
        address.put("address", line1 + " " + line2 + " " + city + " " + postalCode + " " + state);

        DocumentReference userRef = db.collection("user").document(userDocumentId);

        userRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists() && document.contains("address")) {
                    userRef.update(address)
                            .addOnSuccessListener(aVoid -> {
                                Log.d("Firestore", "Address updated successfully");

                                FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
                                MyaccountFragment myAccountFragment = new MyaccountFragment();
                                fragmentTransaction.replace(R.id.fragment_container, myAccountFragment);
                                fragmentTransaction.commit();
                                bottomNavigationView.setVisibility(View.VISIBLE);
                            })
                            .addOnFailureListener(e -> {
                                Log.e("Firestore", "Error updating address", e);
                            });
                } else {
                    userRef.set(address, SetOptions.merge())
                            .addOnSuccessListener(aVoid -> {
                                Log.d("Firestore", "Address added successfully");

                                FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
                                MyaccountFragment myAccountFragment = new MyaccountFragment();
                                fragmentTransaction.replace(R.id.fragment_container, myAccountFragment);
                                fragmentTransaction.commit();
                                bottomNavigationView.setVisibility(View.VISIBLE);
                            })
                            .addOnFailureListener(e -> {
                                Log.e("Firestore", "Error adding address", e);
                            });
                }
            } else {
                Log.e("Firestore", "Error getting document", task.getException());
            }
        });
    }


}