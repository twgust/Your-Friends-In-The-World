package com.example.assignment1.controller.entity;

import java.util.ArrayList;

public class Group {
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getMembersCount() {
        return membersCount;
    }

    public void setMembersCount(int membersCount) {
        this.membersCount = membersCount;
    }

    private String name;

    public ArrayList<String> getMembers() {
        return members;
    }

    private int membersCount;

    public void setMembers(ArrayList<String> members) {
        this.members = members;
    }

    private ArrayList<String> members;

    public Group(String groupName){
        this.name = groupName;
    }
}
