package com.kavishkasinhabahu.craftshub;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class SearchFragment extends Fragment {

    private RecyclerView recyclerView;
    private ProductAdapter productAdapter;
    private List<Product> productsList, allProducts;
    private FirebaseFirestore db;
    private EditText searchBar;

    public SearchFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_search, container, false);

        searchBar = view.findViewById(R.id.editTextText);
        recyclerView = view.findViewById(R.id.srchview);
        recyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 2));

        db = FirebaseFirestore.getInstance();
        productsList = new ArrayList<>();
        allProducts = new ArrayList<>();

        productAdapter = new ProductAdapter(getContext(), productsList, (productId, seller) -> {
            Bundle bundle = new Bundle();
            bundle.putString("productId", productId);
            bundle.putString("sellerId", seller);

            ProductFragment productFragment = new ProductFragment();
            productFragment.setArguments(bundle);

            FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, productFragment);
            transaction.commit();

        });
        recyclerView.setAdapter(productAdapter);

        SearchViewModel viewModel = new ViewModelProvider(requireActivity()).get(SearchViewModel.class);
        String category = viewModel.getCategory().getValue();

        if (category != null) {
            loadProductsByCategory(category);
        } else {
            loadAllProducts();
        }

        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterProducts(s.toString().trim());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        return view;
    }

    private void loadAllProducts() {
        db.collection("product")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        allProducts.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Product product = document.toObject(Product.class);
                            product.setDocumentId(document.getId());
                            product.setSeller(document.getString("seller"));
                            allProducts.add(product);
                        }
                        productAdapter.updateList(allProducts);
                    } else {
                        Log.w("TAG", "Error getting products: ", task.getException());
                    }
                });
    }

    @SuppressLint({"NotifyDataSetChanged", "RestrictedApi"})
    private void loadProductsByCategory(String category) {
        db.collection("product")
                .whereEqualTo("category", category)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        allProducts.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Product product = document.toObject(Product.class);
                            product.setDocumentId(document.getId());
                            product.setSeller(document.getString("seller"));
                            allProducts.add(product);
                        }
                        productAdapter.updateList(allProducts);
                    } else {
                        Log.e("Firestore", "Error getting category products: ", task.getException());
                    }
                });
    }

    private void filterProducts(@NonNull String searchText) {
        if (searchText.isEmpty()) {
            productAdapter.updateList(allProducts);
            return;
        }

        List<Product> filteredList = new ArrayList<>();
        for (Product product : allProducts) {
            if (product.getName().toLowerCase().contains(searchText.toLowerCase())) {
                filteredList.add(product);
            }
        }

        productAdapter.updateList(filteredList);
    }

}