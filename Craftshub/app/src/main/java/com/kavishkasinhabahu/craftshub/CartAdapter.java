package com.kavishkasinhabahu.craftshub;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.ViewHolder> {
    private List<Product> cartProductList;
    private List<String> productIds;
    private List<Integer> quantities;
    private String userId;
    private FirebaseFirestore db;
    private CartListener cartListener;
    private Context context;

    public interface CartListener {
        void onCartUpdated(int itemCount, double totalAmount);
    }

    public CartAdapter(Context context, List<Product> cartProductList, List<String> productIds, List<Integer> quantities, String userId, CartListener listener) {
        this.context = context;
        this.cartProductList = cartProductList;
        this.productIds = productIds;
        this.quantities = quantities;
        this.userId = userId;
        this.cartListener = listener;
        db = FirebaseFirestore.getInstance();
        calculateTotalAmount();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cart, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Product product = cartProductList.get(position);
        String productId = productIds.get(position);
        int quantity = quantities.get(position);

        holder.txtProductName.setText(product.getName());
        holder.txtProductPrice.setText("Rs. " + product.getPrice());
        holder.txtProductQuantity.setText("Quantity: " + quantity);

        Glide.with(holder.itemView.getContext()).load(product.getImage1()).into(holder.imgProduct);

        holder.btnRemove.setOnClickListener(v -> removeItem(productId, position));
    }

    @Override
    public int getItemCount() {
        return cartProductList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtProductName, txtProductPrice, txtProductQuantity;
        ImageView imgProduct;
        ImageButton btnRemove;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtProductName = itemView.findViewById(R.id.txtProductName);
            txtProductPrice = itemView.findViewById(R.id.txtProductPrice);
            txtProductQuantity = itemView.findViewById(R.id.crtQty);
            imgProduct = itemView.findViewById(R.id.imgProduct);
            btnRemove = itemView.findViewById(R.id.imageButton4);
        }
    }

    private void removeItem(String productId, int position) {
        db.collection("cart")
                .whereEqualTo("userId", userId)
                .whereEqualTo("productId", productId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                        db.collection("cart").document(document.getId()).delete()
                                .addOnSuccessListener(aVoid -> {
                                    cartProductList.remove(position);
                                    productIds.remove(position);
                                    quantities.remove(position);
                                    notifyItemRemoved(position);
                                    notifyItemRangeChanged(position, cartProductList.size());

                                    Toast.makeText(context, "Item deleted successfully", Toast.LENGTH_SHORT).show();

                                    calculateTotalAmount();

                                    if (cartListener != null) {
                                        cartListener.onCartUpdated(cartProductList.size(), calculateTotalAmount());
                                    }
                                })
                                .addOnFailureListener(e -> Log.e("Firestore", "Error deleting cart item", e));
                    }
                })
                .addOnFailureListener(e -> Log.e("Firestore", "Error finding cart item", e));
    }

    double calculateTotalAmount() {
        double total = 0;
        for (int i = 0; i < cartProductList.size(); i++) {
            total += Double.parseDouble(cartProductList.get(i).getPrice()) * quantities.get(i);
        }
        return total;
    }
}
