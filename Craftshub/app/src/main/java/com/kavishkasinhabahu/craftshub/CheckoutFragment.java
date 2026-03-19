package com.kavishkasinhabahu.craftshub;

import static androidx.fragment.app.FragmentManager.TAG;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import lk.payhere.androidsdk.*;
import lk.payhere.androidsdk.model.InitRequest;
import lk.payhere.androidsdk.model.StatusResponse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CheckoutFragment extends Fragment {
    private TextView txtTotalPrice, txtTotalQuantity, txtDeliveryFee, txtUserName, txtUserAddress, txtGrandTotal;
    private Button btnPlaceOrder;
    private FirebaseFirestore db;
    private String userId;
    private ArrayList<String> productIds;
    private double totalAmount;
    private double grandTotal;
    private static final int PAYHERE_REQUEST = 11001;
    private String orderId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_checkout, container, false);

        txtTotalPrice = view.findViewById(R.id.textView37);
        txtTotalQuantity = view.findViewById(R.id.textView35);
        txtDeliveryFee = view.findViewById(R.id.textView39);
        txtUserName = view.findViewById(R.id.textView17);
        txtUserAddress = view.findViewById(R.id.textView29);
        txtGrandTotal = view.findViewById(R.id.textView41);
        btnPlaceOrder = view.findViewById(R.id.button6);

        db = FirebaseFirestore.getInstance();
        userId = getUserIdFromSharedPreferences();

        if (getArguments() != null) {
            totalAmount = Double.parseDouble(getArguments().getString("totalAmount", "0"));
            String totalQuantity = getArguments().getString("totalQuantity", "0");
            productIds = getArguments().getStringArrayList("productIds");

            grandTotal = totalAmount + 550;

            txtTotalPrice.setText("Rs. " + String.format("%.0f", totalAmount));
            txtTotalQuantity.setText(totalQuantity + " item(s)");
            txtDeliveryFee.setText("Rs. 550");
            txtGrandTotal.setText("Rs. " + String.format("%.0f", grandTotal));
        }

        loadUserDetails();

        btnPlaceOrder.setOnClickListener(v -> processOrder());

        return view;
    }

    private String getUserIdFromSharedPreferences() {
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        return sharedPreferences.getString("documentId", "null");
    }

    private void loadUserDetails() {
        if (userId.equals("null")) {
            Log.e("CheckoutFragment", "User ID is null");
            return;
        }

        db.collection("user").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String name = documentSnapshot.getString("name");
                        String address = documentSnapshot.getString("address");

                        if (name == null || name.isEmpty() || address == null || address.isEmpty()) {
                            Toast.makeText(getContext(), "Please add your Name and Address!", Toast.LENGTH_SHORT).show();
                            navigateToMyAccount();
                        } else {
                            txtUserName.setText(name);
                            txtUserAddress.setText(address);
                        }
                    } else {
                        Log.e("CheckoutFragment", "User document does not exist");
                    }
                })
                .addOnFailureListener(e -> Log.e("CheckoutFragment", "Error loading user details", e));
    }

    private void navigateToMyAccount() {

        BottomNavigationView bottomNavigationView = getActivity().findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setVisibility(View.VISIBLE);
        bottomNavigationView.setSelectedItemId(R.id.nav_profile);
        MyaccountFragment myAccountFragment = new MyaccountFragment();
        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, myAccountFragment);
        transaction.commit();
    }

    private void processOrder() {
        if (txtUserName.getText().toString().isEmpty() || txtUserAddress.getText().toString().isEmpty()) {
            Toast.makeText(getContext(), "Please complete your profile before ordering!", Toast.LENGTH_SHORT).show();
            navigateToMyAccount();
            return;
        }

        Map<String, Object> orderData = new HashMap<>();
        orderData.put("userId", userId);
        orderData.put("productIds", productIds);
        orderData.put("fullAmount", grandTotal);
        orderData.put("orderAddedDatetime", Timestamp.now());
        orderData.put("status", "unpaid");

        db.collection("order").add(orderData)
                .addOnSuccessListener(documentReference -> {
                    orderId = documentReference.getId();
                    Log.d("CheckoutFragment", "Order saved with ID: " + orderId);
                    updateSellQuantities();
                    clearCart();
                    startPayHerePayment(orderId);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Order failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e("CheckoutFragment", "Error saving order", e);
                });
    }

    @SuppressLint("RestrictedApi")
    private void startPayHerePayment(String orderId) {
        InitRequest req = new InitRequest();
        req.setMerchantId("1221124");
        req.setCurrency("LKR");
        req.setAmount(grandTotal);
        req.setOrderId(orderId);
        req.setItemsDescription("Door bell wireless");
        req.setCustom1(orderId);
        req.setCustom2("This is the custom message 2");
        req.getCustomer().setFirstName("Saman");
        req.getCustomer().setLastName("Perera");
        req.getCustomer().setEmail("samanp@gmail.com");
        req.getCustomer().setPhone("+94771234567");
        req.getCustomer().getAddress().setAddress("No.1, Galle Road");
        req.getCustomer().getAddress().setCity("Colombo");
        req.getCustomer().getAddress().setCountry("Sri Lanka");

        Intent intent = new Intent(getContext(), PHMainActivity.class);
        intent.putExtra(PHConstants.INTENT_EXTRA_DATA, req);
        PHConfigs.setBaseUrl(PHConfigs.SANDBOX_URL);
        startActivityForResult(intent, PAYHERE_REQUEST);
    }

    @SuppressLint("RestrictedApi")
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PAYHERE_REQUEST) {
            if (data != null && data.hasExtra(PHConstants.INTENT_EXTRA_RESULT)) {
                PHResponse<StatusResponse> response = (PHResponse<StatusResponse>) data.getSerializableExtra(PHConstants.INTENT_EXTRA_RESULT);

                if (resultCode == Activity.RESULT_OK) {
                    if (response != null && response.isSuccess()) {
                        Log.d(TAG, "Payment successful: " + response.getData().toString());
                        // Proceed with your order confirmation
                        updateOrderStatusToPaid(orderId);
                        navigateToOrderConfirm();
                    } else {
                        Log.e(TAG, "Payment failed: " + (response != null ? response.toString() : "No response data"));
                        Toast.makeText(getContext(), "Payment failed: " + (response != null ? response.toString() : "Unknown error"), Toast.LENGTH_SHORT).show();
                    }
                } else if (resultCode == Activity.RESULT_CANCELED) {
                    // Payment canceled
                    Log.e(TAG, "Payment was canceled by the user.");
                    Toast.makeText(getContext(), "Payment canceled. Please try again.", Toast.LENGTH_SHORT).show();
                } else {
                    // Unexpected result code
                    Log.e(TAG, "Unexpected result code: " + resultCode);
                    Toast.makeText(getContext(), "Unexpected result code: " + resultCode, Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void updateOrderStatusToPaid(String orderId) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("status", "paid");

        db.collection("order").document(orderId)
                .update(updates)
                .addOnSuccessListener(aVoid -> Log.d("CheckoutFragment", "Order status updated to 'paid'"))
                .addOnFailureListener(e -> Log.e("CheckoutFragment", "Error updating order status", e));
    }

    private void navigateToOrderConfirm() {
        OrderconfirmFragment orderConfirmFragment = new OrderconfirmFragment();
        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, orderConfirmFragment);
        transaction.commit();
    }

    private void clearCart() {
        db.collection("cart").whereEqualTo("userId", userId).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                        db.collection("cart").document(document.getId()).delete();
                    }
                    Log.d("CheckoutFragment", "Cart cleared after order placement");
                })
                .addOnFailureListener(e -> Log.e("CheckoutFragment", "Error clearing cart", e));
    }

    private void updateSellQuantities() {
        for (String productId : productIds) {
            db.collection("product").document(productId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            Long currentSellQuantity = documentSnapshot.getLong("sellQuantity");
                            if (currentSellQuantity == null) {
                                currentSellQuantity = 0L;
                            }

                            Long newSellQuantity = currentSellQuantity + 1; // Increase by 1 per order

                            db.collection("products").document(productId)
                                    .update("sellQuantity", newSellQuantity)
                                    .addOnSuccessListener(aVoid -> Log.d("CheckoutFragment", "Sell quantity updated for " + productId))
                                    .addOnFailureListener(e -> Log.e("CheckoutFragment", "Error updating sell quantity", e));
                        }
                    })
                    .addOnFailureListener(e -> Log.e("CheckoutFragment", "Error fetching product sell quantity", e));
        }
    }

}
