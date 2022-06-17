package com.example.assignment1.ViewModel;

import android.util.Log;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.assignment1.controller.entity.Group;
import com.example.assignment1.controller.entity.MemberLocation;

import java.util.ArrayList;
import java.util.List;

public class MemberData extends ViewModel {
    private static final String TAG = "ViewModel:MemberData";

    private MutableLiveData<List<String>> members;
    private MutableLiveData<List<String>> groups;

    private MutableLiveData<Group> group;
    private MutableLiveData<List<Group>> groupList;
    private MutableLiveData<List<MemberLocation>> memberLocationsList;




    public MemberData(){
        if(groups == null){
            groups = new MutableLiveData<>();
        }
        if (members == null) {
            members = new MutableLiveData<>();
        }
        if (group == null) {
            group = new MutableLiveData<>();
        }
    }

    public void loadMemberLocations(ArrayList<String> locations){

        if(this.memberLocationsList == null){
            this.memberLocationsList = new MutableLiveData<>();
        }

        ArrayList<MemberLocation> memberLocations = new ArrayList<>();
        for (String s: locations) {
            String[] arr = s.split(",");
            String groupName = arr[0];
            String memberName = arr[1];
            String longitude = arr[2];
            String latitude = arr[3];
            double longitudeDbl = Double.parseDouble(longitude);
            double latitudeDbl = Double.parseDouble(latitude);
            memberLocations.add(new MemberLocation(groupName, memberName, longitudeDbl, latitudeDbl));
        }
        this.memberLocationsList.setValue(memberLocations);
    }

    public MutableLiveData<List<MemberLocation>> getMemberLocationsList(){
        if(memberLocationsList == null){
            memberLocationsList = new MutableLiveData<>();

        }
        return this.memberLocationsList;
    }

    public void loadMembersInAGroup(ArrayList<String> members){
        try{
            Group group = new Group(members.get(0));
            members.remove(0);
            group.setMembersCount(members.size());
            group.setMembers(members);
            this.group.setValue(group);
            Log.d(TAG, "ViewModel.LoadMembers: Members updated in ViewModel");
        }catch (Exception e){e.printStackTrace(); }
    }

    public MutableLiveData<Group> getMembersForGroup(){
        if(group == null){
            group = new MutableLiveData<>();
        }
        return group;
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
