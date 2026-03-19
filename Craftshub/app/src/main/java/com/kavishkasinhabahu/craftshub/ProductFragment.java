package com.kavishkasinhabahu.craftshub;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentTransaction;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class ProductFragment extends Fragment {

    private FirebaseFirestore db;
    private String productId, sellerId, userId;
    private TextView sellerNameText, sellerIntroText, sellerRegdateText;
    private Toolbar toolbar;
    private TextView productName, productPrice, productQuantity, productDescription, productCategory;
    private ImageView productImage;
    private ImageButton sellerImage, cartBtn;

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_product, container, false);

        toolbar = view.findViewById(R.id.toolbar);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
                transaction.replace(R.id.fragment_container, new HomeFragment());
                transaction.commit();
                BottomNavigationView bottomNavigationView = getActivity().findViewById(R.id.bottomNavigationView);
                bottomNavigationView.setVisibility(View.VISIBLE);
                bottomNavigationView.setSelectedItemId(R.id.nav_home);
            }
        });

        cartBtn = view.findViewById(R.id.imageButton7);
        cartBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
                transaction.replace(R.id.fragment_container, new CartFragment());
                transaction.commit();
                BottomNavigationView bottomNavigationView = getActivity().findViewById(R.id.bottomNavigationView);
                bottomNavigationView.setVisibility(View.VISIBLE);
                bottomNavigationView.setSelectedItemId(R.id.nav_cart);
            }
        });

        productName = view.findViewById(R.id.textView10);
        productPrice = view.findViewById(R.id.textView11);
        productQuantity = view.findViewById(R.id.textView8);
        productDescription = view.findViewById(R.id.textView9);
        productCategory = view.findViewById(R.id.textView16);
        productImage = view.findViewById(R.id.imageView10);
        sellerImage = view.findViewById(R.id.imageButton8);

        sellerNameText = view.findViewById(R.id.textView13);
        sellerIntroText = view.findViewById(R.id.textView15);
        sellerRegdateText = view.findViewById(R.id.textView14);

        if (getArguments() != null) {
            productId = getArguments().getString("productId");
        }

        db = FirebaseFirestore.getInstance();
        loadProductData();

//        view.findViewById(R.id.cardView).setOnClickListener(v -> {
//
//            SellerFragment sellerFragment = SellerFragment.newInstance(sellerId);
//            getParentFragmentManager().beginTransaction()
//                    .replace(R.id.fragment_container, sellerFragment)
//                    .addToBackStack("")
//                    .commit();
//        });

        view.findViewById(R.id.cardView).setOnClickListener(v -> {
            SellerBottomSheetFragment sellerBottomSheet = SellerBottomSheetFragment.newInstance(sellerId);
            sellerBottomSheet.show(getParentFragmentManager(), "SellerBottomSheet");
        });

        view.findViewById(R.id.button3).setOnClickListener(v -> addToCart());

        return view;
    }

    @SuppressLint("SetTextI18n")
    private void loadProductData() {
        if (productId != null) {
            db.collection("product")
                    .document(productId)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document != null && document.exists()) {
                                Product product = document.toObject(Product.class);

                                if (product != null) {
                                    productName.setText(product.getName());
                                    productPrice.setText("Rs. " + product.getPrice());
                                    productQuantity.setText(product.getQuantity() + " In Stock");
                                    productDescription.setText(product.getDescription());
                                    productCategory.setText("Category : " + product.getCategory());

                                    Glide.with(getContext())
                                            .load(product.getImage1())
                                            .placeholder(R.drawable.ic_menu_gallery)
                                            .into(productImage);

                                    sellerId = document.getString("seller");

                                    Log.i("SellerId", sellerId);

                                    if (sellerId != null && !sellerId.isEmpty()) {
                                        loadSellerDetails(sellerId);
                                        loadProfileImage();
                                    }
                                }
                            }
                        } else {
                            Log.e("Firestore", "Failed to get product details", task.getException());
                        }
                    });
        }
    }

    private void loadSellerDetails(String sellerId) {
        db.collection("user").document(sellerId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                DocumentSnapshot document = task.getResult();
                String sellerName = document.getString("name");
                String sellerIntro = document.getString("introduction");
                String sellerRegdate = document.getString("registeredDate");

                sellerNameText.setText((sellerName != null ? sellerName : "Seller 01"));
                sellerIntroText.setText((sellerIntro != null ? sellerIntro : "No Introduction"));
                sellerRegdateText.setText(sellerRegdate != null ? sellerRegdate : "No Registered Date");

                Log.d("Firestore", "Seller details loaded: " + sellerName);
            } else {
                Log.e("Firestore", "Failed to get seller details", task.getException());
            }
        });
    }

    private void addToCart() {
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        userId = sharedPreferences.getString("documentId", null);

        if (userId == null) {
            Intent intent = new Intent(getActivity(), SigninActivity.class);
            startActivity(intent);
        } else {
            db.collection("cart")
                    .whereEqualTo("userId", userId)
                    .whereEqualTo("productId", productId)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && task.getResult() != null) {
                            if (!task.getResult().isEmpty()) {
                                DocumentSnapshot document = task.getResult().getDocuments().get(0);
                                String cartItemId = document.getId();
                                String currentQuantityStr = document.getString("quantity");

                                int currentQuantity = (currentQuantityStr != null) ? Integer.parseInt(currentQuantityStr) : 0;
                                String newQuantity = String.valueOf(currentQuantity + 1);

                                db.collection("cart")
                                        .document(cartItemId)
                                        .update("quantity", newQuantity)
                                        .addOnSuccessListener(aVoid -> {
                                            updateProductQuantity();
                                            Toast.makeText(getContext(), "Cart updated!", Toast.LENGTH_SHORT).show();
                                        })
                                        .addOnFailureListener(e -> {
                                            Toast.makeText(getContext(), "Failed to update cart", Toast.LENGTH_SHORT).show();
                                            Log.e("Firestore", "Error updating cart", e);
                                        });
                            } else {
                                Map<String, Object> cartItem = new HashMap<>();
                                cartItem.put("userId", userId);
                                cartItem.put("productId", productId);
                                cartItem.put("quantity", "1"); // New product, set quantity to "1" as string

                                db.collection("cart").add(cartItem)
                                        .addOnSuccessListener(documentReference -> {
                                            updateProductQuantity();
                                            Toast.makeText(getContext(), "Added to cart!", Toast.LENGTH_SHORT).show();
                                        })
                                        .addOnFailureListener(e -> {
                                            Toast.makeText(getContext(), "Failed to add to cart", Toast.LENGTH_SHORT).show();
                                            Log.e("Firestore", "Error adding to cart", e);
                                        });
                            }
                        } else {
                            Log.e("Firestore", "Error checking cart", task.getException());
                        }
                    });
        }
    }

    private void updateProductQuantity() {
        db.collection("product").document(productId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        int currentQuantity = Integer.parseInt(documentSnapshot.getString("quantity"));

                        Map<String, Object> updates = new HashMap<>();
                        if (currentQuantity > 0) {
                            updates.put("quantity", String.valueOf(currentQuantity - 1));
                        }

                        db.collection("product").document(productId)
                                .update(updates)
                                .addOnSuccessListener(aVoid -> {
                                    Log.d("Firestore", "Product quantity and sellQuantity updated");
                                    productQuantity.setText(String.valueOf(currentQuantity - 1) + " In Stock");
                                })
                                .addOnFailureListener(e -> Log.e("Firestore", "Product quantity update failed", e));
                    }
                });
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

}