package com.kavishkasinhabahu.craftshub.ui.addproduct;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.kavishkasinhabahu.craftshub.CartFragment;
import com.kavishkasinhabahu.craftshub.R;
import com.kavishkasinhabahu.craftshub.databinding.FragmentAddproductBinding;
import com.kavishkasinhabahu.craftshub.ui.products.ProductsFragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddproductFragment extends Fragment {

    private Spinner categorySpinner;
    private FirebaseFirestore firestore;
    private FirebaseStorage storage;
    private List<String> categoryList = new ArrayList<>();
    private List<String> categoryIdList = new ArrayList<>();
    private FragmentAddproductBinding binding;
    private String selectedCategory = "";
    private Uri imageUri1, imageUri2;
    private SharedPreferences sharedPreferences;

    private final ActivityResultLauncher<Intent> imagePickerLauncher1 = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == getActivity().RESULT_OK && result.getData() != null) {
                    imageUri1 = result.getData().getData();
                    binding.imageButton5.setImageURI(imageUri1);
                }
            });

    private final ActivityResultLauncher<Intent> imagePickerLauncher2 = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == getActivity().RESULT_OK && result.getData() != null) {
                    imageUri2 = result.getData().getData();
                    binding.imageButton6.setImageURI(imageUri2);
                }
            });

    public AddproductFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentAddproductBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        categorySpinner = binding.spinner2;
        firestore = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        sharedPreferences = requireActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);

        loadCategories();
        binding.addProductBtn.setOnClickListener(v -> addProduct());
        binding.imageButton5.setOnClickListener(v -> pickImage(1));
        binding.imageButton6.setOnClickListener(v -> pickImage(2));

        return root;
    }

    private void pickImage(int imageNumber) {
        ImagePicker.with(this)
                .crop()
                .compress(1024)
                .maxResultSize(1080, 1080)
                .createIntent(intent -> {
                    if (imageNumber == 1) {
                        imagePickerLauncher1.launch(intent);
                    } else {
                        imagePickerLauncher2.launch(intent);
                    }
                    return null;
                });
    }

    private void loadCategories() {
        CollectionReference categoriesRef = firestore.collection("category");

        categoriesRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                categoryList.clear();
                categoryIdList.clear();
                categoryList.add("Select Category");
                categoryIdList.add(""); // Placeholder for default selection

                for (DocumentSnapshot document : task.getResult()) {
                    String categoryName = document.getString("name");
                    String categoryId = document.getId();

                    if (categoryName != null) {
                        categoryList.add(categoryName);
                        categoryIdList.add(categoryId);
                    }
                }

                setupSpinner();
            } else {
                Toast.makeText(getContext(), "Failed to load categories!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, categoryList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(adapter);

        categorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0) { // Ignore default selection
                    selectedCategory = categoryList.get(position);
                } else {
                    selectedCategory = ""; // Reset if "Select Category" is chosen
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });
    }

    private void addProduct() {
        String name = binding.editTextText2.getText().toString().trim();
        String description = binding.editTextTextMultiLine.getText().toString().trim();
        String price = binding.editTextNumber2.getText().toString().trim();
        String quantity = binding.editTextNumber3.getText().toString().trim();
        String sellerId = sharedPreferences.getString("documentId", "");

        if (name.isEmpty() || description.isEmpty() || price.isEmpty() || quantity.isEmpty() || selectedCategory.isEmpty() || sellerId.isEmpty()) {
            Toast.makeText(getContext(), "Please fill all fields and select a category!", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> product = new HashMap<>();
        product.put("name", name);
        product.put("description", description);
        product.put("price", price);
        product.put("quantity", quantity);
        product.put("category", selectedCategory);
        product.put("seller", sellerId);

        firestore.collection("product").add(product)
                .addOnSuccessListener(documentReference -> {
                    String productId = documentReference.getId();
                    uploadImages(productId);
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to add product!", Toast.LENGTH_SHORT).show());
    }

    private void uploadImages(String productId) {
        if (imageUri1 != null) {
            uploadImage(productId, imageUri1, "image1");
        }
        if (imageUri2 != null) {
            uploadImage(productId, imageUri2, "image2");
        }
    }

    private void uploadImage(String productId, Uri imageUri, String fieldName) {
        StorageReference imageRef = storage.getReference("product_images").child(productId + "_" + fieldName + ".jpg");
        imageRef.putFile(imageUri).addOnSuccessListener(taskSnapshot ->
                        imageRef
                                .getDownloadUrl()
                                .addOnSuccessListener(uri -> {
                                    firestore.collection("product").document(productId).update(fieldName, uri.toString());
                                    Toast.makeText(getContext(), "Product added Successfully.", Toast.LENGTH_SHORT).show();
                                }))
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to upload image!", Toast.LENGTH_SHORT).show());
    }

    public void setImageUri1(Uri uri) {
        this.imageUri1 = uri;
    }

    public void setImageUri2(Uri uri) {
        this.imageUri2 = uri;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
