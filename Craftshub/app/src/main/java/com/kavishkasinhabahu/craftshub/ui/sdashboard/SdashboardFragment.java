package com.kavishkasinhabahu.craftshub.ui.sdashboard;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.kavishkasinhabahu.craftshub.databinding.FragmentSdashboardBinding;

import java.util.ArrayList;
import java.util.List;

public class SdashboardFragment extends Fragment {

    private FragmentSdashboardBinding binding;
    private FirebaseFirestore db;
    private String userId;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        SdashboardViewModel homeViewModel =
                new ViewModelProvider(this).get(SdashboardViewModel.class);

        binding = FragmentSdashboardBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        db = FirebaseFirestore.getInstance();

        // Get user ID from SharedPreferences
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        userId = sharedPreferences.getString("documentId", null);

        loadSellQuantityData();

        return root;
    }

    private void loadSellQuantityData() {
        if (userId == null) return;

        db.collection("product")
                .whereEqualTo("seller", userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<PieEntry> entries = new ArrayList<>();

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String productName = document.getString("name");
                            Long sellQuantity = document.contains("sellQuantity") ? document.getLong("sellQuantity") : 0;

                            if (sellQuantity != null && sellQuantity >= 0) {
                                entries.add(new PieEntry(sellQuantity, productName));
                            }
                        }

                        updatePieChart(entries);
                    } else {
                        Log.e("Firestore", "Error loading sellQuantity data", task.getException());
                    }
                });
    }

    private void updatePieChart(List<PieEntry> entries) {
        if (entries.isEmpty()) {
            binding.pieChart.setVisibility(View.GONE);
            return;
        }

        binding.pieChart.setVisibility(View.VISIBLE);
        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        dataSet.setValueTextSize(14f);
        dataSet.setValueTextColor(Color.WHITE);

        PieData data = new PieData(dataSet);
        data.setValueFormatter(new IntegerValueFormatter());
        binding.pieChart.setData(data);
        binding.pieChart.setDrawHoleEnabled(true);
        binding.pieChart.getDescription().setEnabled(false);
        binding.pieChart.setHoleRadius(40f);
        binding.pieChart.setTransparentCircleRadius(45f);
        binding.pieChart.animateY(1000);
        binding.pieChart.invalidate(); // Refresh chart
    }

    public class IntegerValueFormatter extends ValueFormatter {
        @Override
        public String getFormattedValue(float value) {
            return String.valueOf((int) value); // Convert float to int for whole number display
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}