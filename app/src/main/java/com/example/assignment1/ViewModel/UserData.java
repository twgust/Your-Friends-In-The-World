package com.example.assignment1.ViewModel;

import android.util.Log;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.assignment1.controller.entity.Location;

public class UserData extends ViewModel {
    private MutableLiveData<String> userID;
    private MutableLiveData<Location> userLocation;

    public void setUserID(String user_ID) {
        if(userID == null){
            userID= new MutableLiveData<String>();
        }

        Log.d("UserData.ViewModel", "setUserID: attempting to set data " + user_ID);
        userID.setValue(user_ID);
        Log.d("UserData.ViewModel", "setUserID: set value "  + userID.getValue());

    }
    public void setUserLocation(String longitude, String latitude){
        if(userLocation == null){
            userLocation = new MutableLiveData<>();
        }
        Log.d("UserData.ViewModel", "setUserLocation: attempting to set data " +
                "Lng:" + longitude + " " +
                "Lat:" + latitude);
        userLocation.setValue(new Location(longitude, latitude));
        Log.d("UserData.ViewModel", "setUserLocation: Successfully  set data " +
                "Lng:" + longitude + " " +
                "Lat:" + latitude);
    }

    public MutableLiveData<Location> getLocation(){
        if(userLocation == null){
            userLocation = new MutableLiveData<>();
        }
        return userLocation;
    }

    public MutableLiveData<String> getUserID(){
        if(userID == null){
            userID = new MutableLiveData<String>();
            userID.setValue("");
        }
        return userID;
    }
}
