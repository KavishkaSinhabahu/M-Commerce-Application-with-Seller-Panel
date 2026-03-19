package com.kavishkasinhabahu.craftshub;

import androidx.lifecycle.ViewModel;
import androidx.lifecycle.MutableLiveData;

public class SearchViewModel extends ViewModel {
    private MutableLiveData<String> selectedCategory = new MutableLiveData<>();

    public MutableLiveData<String> getCategory() {
        return selectedCategory;
    }

    public void setCategory(String category) {
        selectedCategory.setValue(category);
    }

    public void resetCategory() {
        this.selectedCategory.setValue(null);
    }
}

