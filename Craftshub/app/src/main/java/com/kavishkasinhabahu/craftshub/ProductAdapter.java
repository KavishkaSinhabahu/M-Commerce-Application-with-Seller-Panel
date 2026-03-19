package com.kavishkasinhabahu.craftshub;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ViewHolder> {

    private Context context;
    private List<Product> productList;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(String productId, String seller);
    }

    public ProductAdapter(Context context, List<Product> productList, OnItemClickListener listener) {
        this.context = context;
        this.productList = productList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_product, parent, false));
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final int itemPosition = position;

        Product product = productList.get(itemPosition);
        holder.name.setText(product.getName());
        holder.price.setText("Rs. " + product.getPrice());
        holder.quantity.setText(product.getQuantity() + " In Stock");

        Glide.with(context)
                .load(product.getImage1())
                .placeholder(R.drawable.addproduct)
                .into(holder.image1);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(productList.get(itemPosition).getDocumentId(), productList.get(itemPosition).getSeller());
            }
        });

    }

    @Override
    public int getItemCount() {

        return productList.size();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void updateList(List<Product> newList) {
        productList.clear();
        productList.addAll(newList);
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView name, price, quantity;
        ImageView image1;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.product_name);
            price = itemView.findViewById(R.id.product_price);
            quantity = itemView.findViewById(R.id.product_quantity);
            image1 = itemView.findViewById(R.id.product_image);
        }
    }
}
