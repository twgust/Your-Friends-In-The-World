package com.example.assignment1.controller.entity;

public class MemberLocation {
    private String group;
    private String name;
    private double longitude;
    private double latitude;


    public MemberLocation(String group, String name, double longitude, double latitude) {
        this.group = group;
        this.name = name;
        this.longitude = longitude;
        this.latitude = latitude;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }


}