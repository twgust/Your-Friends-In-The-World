package com.example.assignment1.ViewModel;

import android.util.Log;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.assignment1.controller.entity.Location;

import java.util.ArrayList;
import java.util.List;

public class UserData extends ViewModel {
    private MutableLiveData<String> userID;
    private MutableLiveData<Location> userLocation;

    private MutableLiveData<List<String>> userGroups;
    private ArrayList<String> userGroups_arrayList;

    private MutableLiveData<List<String>> userID_MutableList;
    private ArrayList<String> userID_arrayList;

    public void addGroup(String groupName){
        if(userGroups == null){
            userGroups = new MutableLiveData<>();

        }
        if (userGroups_arrayList == null ){
            userGroups_arrayList = new ArrayList<>();
        }
        userGroups_arrayList.add(groupName);
        userGroups.setValue(userGroups_arrayList);
    }

    public void addUserID(String userid){
        if(userID_arrayList == null){
            userID_arrayList = new ArrayList<>();
        }
        if(userID_MutableList == null){
            userID_MutableList = new MutableLiveData<>();
        }
        userID_arrayList.add(userid);
        userID_MutableList.setValue(userID_arrayList);
    }
    public MutableLiveData<List<String>> getUserGroups(){
        if(userGroups == null){
            userGroups = new MutableLiveData<>();
        }
        return userGroups;
    }

    public MutableLiveData<List<String>> getUserIDList(){
        return userID_MutableList;
    }

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
