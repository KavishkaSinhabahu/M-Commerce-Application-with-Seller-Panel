package com.kavishkasinhabahu.craftshub.ui.addproduct;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class AddproductViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public AddproductViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is gallery fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}