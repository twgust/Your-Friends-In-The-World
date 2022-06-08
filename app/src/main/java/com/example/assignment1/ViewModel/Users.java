package com.example.assignment1.ViewModel;

import android.util.Log;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

public class Users extends ViewModel {
    private static final String TAG = "ViewModel:Users";
    private MutableLiveData<List<String>> groups;
    private MutableLiveData<List<String>> members;

    public Users(){
        if(groups == null){
            groups = new MutableLiveData<>();
        }
        if (members == null) {
            members = new MutableLiveData<>();
        }
    }

    public MutableLiveData<List<String>> getGroups(){
        if(groups == null){
            groups = new MutableLiveData<>();
        }
        return groups;
    }

    public void loadGroups(List<String> groupsList){
        if(groupsList == null || groupsList.isEmpty()){
            Log.d(TAG, "loadGroups: No groups to add");
            groups.setValue(groupsList);
        }
        else{
            Log.d(TAG, "loadGroups: attempting to set groups...");
            groups.setValue(groupsList);
            Log.d(TAG, "loadGroups: Successfully set groups! " + groupsList.size());
        }
    }

}
