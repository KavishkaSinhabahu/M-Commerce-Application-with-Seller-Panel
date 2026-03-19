package com.kavishkasinhabahu.craftshub;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private RecyclerView categoryView;
    private CategoryAdapter adapter;
    private List<Category> categoryList;
    private RecyclerView recyclerView;
    private ProductAdapter padapter;
    private List<Product> productList;
    private FirebaseFirestore db;
    private TextView homeuser;

    @SuppressLint({"MissingInflatedId", "SetTextI18n"})
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home, container, false);

        BottomNavigationView bottomNavigationView = getActivity().findViewById(R.id.bottomNavigationView);

        Window window = getActivity().getWindow();
        window.setNavigationBarColor(getResources().getColor(R.color.app_primary));

        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        String username = sharedPreferences.getString("name", "user");

        homeuser = view.findViewById(R.id.textView2);
        homeuser.setText("Hi " + username);

        categoryView = view.findViewById(R.id.categoryview);
        categoryView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

        categoryList = new ArrayList<>();
        adapter = new CategoryAdapter(getContext(), categoryList, this::navigateToSearchFragment);
        categoryView.setAdapter(adapter);

        recyclerView = view.findViewById(R.id.productView);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));

        productList = new ArrayList<>();
        padapter = new ProductAdapter(getContext(), productList, (productId, seller) -> {
            Bundle bundle = new Bundle();
            bundle.putString("productId", productId);
            bundle.putString("sellerId", seller);

            ProductFragment productFragment = new ProductFragment();
            productFragment.setArguments(bundle);

            FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, productFragment);
            transaction.commit();
            bottomNavigationView.setVisibility(View.GONE);
        });
        recyclerView.setAdapter(padapter);

        db = FirebaseFirestore.getInstance();
        loadCategoriesFromFirestore();
        loadProductsFromFirestore();

        view.findViewById(R.id.imageButton2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
                transaction.replace(R.id.fragment_container, new LocationFragment());
                transaction.commit();
                bottomNavigationView.setVisibility(View.GONE);
            }
        });

        view.findViewById(R.id.imageButton).setOnClickListener(view1 -> {
            FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, new CartFragment());
            transaction.commit();
            bottomNavigationView.setSelectedItemId(R.id.nav_cart);
        });

        return view;
    }

    @SuppressLint("NotifyDataSetChanged")
    private void loadCategoriesFromFirestore() {
        db.collection("category")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        categoryList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String categoryName = document.getString("name");
                            if (categoryName != null) {
                                categoryList.add(new Category(categoryName));
                            }
                        }
                        adapter.notifyDataSetChanged();
                    } else {
                        Log.e("Firestore", "Error getting categories: ", task.getException());
                    }
                });
    }

    @SuppressLint("NotifyDataSetChanged")
    private void loadProductsFromFirestore() {
        db.collection("product")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        productList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Product product = document.toObject(Product.class);
                            product.setDocumentId(document.getId());
                            product.setSeller(document.getString("seller"));
                            productList.add(product);
                        }
                        padapter.notifyDataSetChanged();

                        Log.d("Firestore", "Product list size: " + productList.size());
                    } else {
                        Log.e("Firestore", "Error getting documents: ", task.getException());
                    }
                });
    }

    private void navigateToSearchFragment(String categoryName) {
        SearchViewModel viewModel = new ViewModelProvider(requireActivity()).get(SearchViewModel.class);
        viewModel.setCategory(categoryName);

        SearchFragment searchFragment = new SearchFragment();
        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, searchFragment)
                .addToBackStack(null)
                .commit();

        BottomNavigationView bottomNavigationView = getActivity().findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setSelectedItemId(R.id.nav_search);

    }
}