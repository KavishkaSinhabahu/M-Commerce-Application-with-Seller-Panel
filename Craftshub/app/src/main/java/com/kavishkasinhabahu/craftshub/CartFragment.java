package com.kavishkasinhabahu.craftshub;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.cardview.widget.CardView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class CartFragment extends Fragment implements CartAdapter.CartListener {
    private RecyclerView recyclerView;
    private CartAdapter cartAdapter;
    private List<Product> cartProductList = new ArrayList<>();
    private List<String> productIds = new ArrayList<>();
    private List<Integer> quantities = new ArrayList<>();
    private FirebaseFirestore db;
    private String userId;
    private CardView crtCrd;
    private TextView amountCrt;

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cart, container, false);

        recyclerView = view.findViewById(R.id.crtRcycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        crtCrd = view.findViewById(R.id.crtCrd);
        amountCrt = view.findViewById(R.id.amountCrt);

        db = FirebaseFirestore.getInstance();
        userId = getUserIdFromSharedPreferences();

        cartAdapter = new CartAdapter(getContext(), cartProductList, productIds, quantities, userId, this);
        recyclerView.setAdapter(cartAdapter);

        crtCrd.setVisibility(View.GONE);

        loadCartItems();
        view.findViewById(R.id.button5).setOnClickListener(v -> navigateToCheckout());

        return view;
    }

    private void loadCartItems() {
        db.collection("cart")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    productIds.clear();
                    cartProductList.clear();
                    quantities.clear();

                    if (!queryDocumentSnapshots.isEmpty()) {
                        for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                            productIds.add(document.getString("productId"));
                            String quantityStr = document.getString("quantity");
                            int quantity = (quantityStr != null) ? Integer.parseInt(quantityStr) : 1;
                            quantities.add(quantity);
                        }
                        fetchProducts();
                    } else {
                        crtCrd.setVisibility(View.GONE);
                    }
                })
                .addOnFailureListener(e -> Log.e("Firestore", "Error getting cart items", e));
    }

    @SuppressLint("NotifyDataSetChanged")
    private void fetchProducts() {
        cartProductList.clear();
        for (String productId : productIds) {
            db.collection("product")
                    .document(productId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            Product product = documentSnapshot.toObject(Product.class);
                            cartProductList.add(product);
                            cartAdapter.notifyDataSetChanged();
                            crtCrd.setVisibility(View.VISIBLE);
                            updateTotalAmount();
                        }
                    })
                    .addOnFailureListener(e -> Log.e("Firestore", "Error getting product details", e));
        }
    }

    private void updateTotalAmount() {
        double total = 0;
        for (int i = 0; i < cartProductList.size(); i++) {
            total += Double.parseDouble(cartProductList.get(i).getPrice()) * quantities.get(i);
        }
        String formattedTotal = (total % 1 == 0) ? String.format("%.0f", total) : String.format("%.2f", total);
        amountCrt.setText("Rs. " + formattedTotal);
    }

    @Override
    public void onCartUpdated(int itemCount, double totalAmount) {
        if (itemCount == 0) {
            crtCrd.setVisibility(View.GONE);
            amountCrt.setText("Rs. 0");
        } else {
            String formattedTotal = (totalAmount % 1 == 0) ? String.format("%.0f", totalAmount) : String.format("%.2f", totalAmount);
            amountCrt.setText("Rs. " + formattedTotal);
        }
    }

    private String getUserIdFromSharedPreferences() {
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        return sharedPreferences.getString("documentId", "null");
    }

    @SuppressLint("DefaultLocale")
    private void navigateToCheckout() {
        CheckoutFragment checkoutFragment = new CheckoutFragment();
        Bundle bundle = new Bundle();

        int totalQuantity = 0;
        for (int quantity : quantities) {
            totalQuantity += quantity;
        }

        double totalAmount = cartAdapter.calculateTotalAmount();
        String formattedTotalAmount = (totalAmount % 1 == 0) ? String.format("%.0f", totalAmount) : String.format("%.2f", totalAmount);

        bundle.putString("totalAmount", formattedTotalAmount);
        bundle.putString("totalQuantity", String.valueOf(totalQuantity));
        bundle.putStringArrayList("productIds", new ArrayList<>(productIds));

        checkoutFragment.setArguments(bundle);

        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, checkoutFragment);
        transaction.addToBackStack(null);
        transaction.commit();
        getActivity().findViewById(R.id.bottomNavigationView).setVisibility(View.GONE);
    }
}