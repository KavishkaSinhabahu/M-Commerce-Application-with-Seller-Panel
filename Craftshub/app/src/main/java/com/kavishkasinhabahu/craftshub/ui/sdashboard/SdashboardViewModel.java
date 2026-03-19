package com.kavishkasinhabahu.craftshub.ui.sdashboard;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class SdashboardViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public SdashboardViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is home fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}