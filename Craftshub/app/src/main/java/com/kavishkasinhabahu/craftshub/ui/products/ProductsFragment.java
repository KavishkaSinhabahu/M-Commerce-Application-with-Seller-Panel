package com.kavishkasinhabahu.craftshub.ui.products;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.kavishkasinhabahu.craftshub.Product;
import com.kavishkasinhabahu.craftshub.ProductsAdapter;
import com.kavishkasinhabahu.craftshub.databinding.FragmentProductsBinding;
import java.util.ArrayList;
import java.util.List;

public class ProductsFragment extends Fragment {

    private FragmentProductsBinding binding;
    private FirebaseFirestore db;
    private String userId;
    private ProductsAdapter adapter;
    private List<Product> productList;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentProductsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        db = FirebaseFirestore.getInstance();
        userId = getUserIdFromSharedPreferences();
        productList = new ArrayList<>();
        adapter = new ProductsAdapter(requireContext(), productList, this::deleteProduct);

        binding.recyclerViewProducts.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerViewProducts.setAdapter(adapter);

        loadProducts();

        return root;
    }

    private String getUserIdFromSharedPreferences() {
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        return sharedPreferences.getString("documentId", "null");
    }

    private void loadProducts() {
        if (userId.equals("null")) {
            Toast.makeText(getContext(), "User ID not found", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("product").whereEqualTo("seller", userId).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    productList.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Product product = document.toObject(Product.class);
                        product.setDocumentId(document.getId());
                        productList.add(product);
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to load products", Toast.LENGTH_SHORT).show());
    }

    private void deleteProduct(String productId) {
        db.collection("product").document(productId).delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Product deleted", Toast.LENGTH_SHORT).show();
                    loadProducts(); // Refresh the list
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to delete product", Toast.LENGTH_SHORT).show());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
