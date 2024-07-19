package com.example.imu;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.Arrays;

public class ViewModel extends AndroidViewModel {
    private MutableLiveData<short[]> data1 = new MutableLiveData<>();

    public ViewModel(@NonNull Application application) {
        super(application);
    }


    public LiveData<short[]> getData1() {
        System.out.println("getdata1");
        return data1;

    }

    public void setData1(short[] data) {
//       System.out.println("setdata1"+ Arrays.toString(data));
        data1.postValue(data);
    }


}
