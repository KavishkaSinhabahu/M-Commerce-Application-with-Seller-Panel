package com.kavishkasinhabahu.craftshub;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.ViewHolder> {

    private Context context;
    private List<Category> categoryList;
    private OnCategoryClickListener listener;

    public interface OnCategoryClickListener {
        void onCategoryClick(String categoryName);
    }

    public CategoryAdapter(Context context, List<Category> categoryList, OnCategoryClickListener listener) {
        this.context = context;
        this.categoryList = categoryList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CategoryAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_category, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryAdapter.ViewHolder holder, int position) {
        Category category = categoryList.get(position);

        holder.chipGroup.removeAllViews();

        Chip chip = new Chip(context);
        chip.setText(category.getName());
        chip.setCheckable(true);
        chip.setClickable(true);
        chip.setTypeface(ResourcesCompat.getFont(context, R.font.montserrat_medium));

        chip.setOnClickListener(v -> listener.onCategoryClick(category.getName()));

        holder.chipGroup.addView(chip);
    }

    @Override
    public int getItemCount() {
        return categoryList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ChipGroup chipGroup;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            chipGroup = itemView.findViewById(R.id.chipGroup);
        }
    }
}