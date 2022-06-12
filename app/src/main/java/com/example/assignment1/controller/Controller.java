package com.example.assignment1.controller;

import android.content.ComponentName;

import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;

import androidx.lifecycle.MutableLiveData;

import com.example.assignment1.View.MainActivity;
import com.example.assignment1.controller.network.NetworkService;
import com.example.assignment1.controller.json_utility.JSONMessageWriter;


import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Controller {
    private static final String TAG = "Controller";
    private NetworkService networkService;
    private final MainActivity mainActivity;
    private boolean isBound = false;

    /**
     * Starts Service NetworkService, invoking NetworkService.onStartCommand()
     */
    public Controller(MainActivity activity, Bundle savedInstanceState){
        this.mainActivity = activity;
        Intent intent = new Intent(activity, NetworkService.class);
        activity.bindService(intent, connection, Context.BIND_AUTO_CREATE);
        Log.d(TAG, "Controller: Service 'NetworkService.class' started");
    }

    /**
     * Binds to NetworkService
     */
    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            NetworkService.LocalService binder = (NetworkService.LocalService) iBinder;
            networkService = binder.getService();
            isBound = true;
            Log.d(TAG, "onServiceConnected: Invoked");
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            isBound = false;
            Log.d(TAG, "onServiceDisconnected: Invoked");
        }
    };

    public void onDestroy() {
        if (isBound) {
            networkService.onDestroy();
            mainActivity.unbindService(connection);
        }
    }

    /**
     * Tells NetworkService to register to a group
     * @param groupName The name of the group which the user wishes to register to
     * @param username The name of the user which is registering to a group
     */
    public void registerToGroup(String groupName, String username) {
        Log.d(TAG, "onClickSubmit: RegisterToGroup(groupName, username)");
        networkService.registerRequest(JSONMessageWriter.registerToGroup(groupName, username));
    }

    /**
     * Tells NetworkService to request the members for groupName
     * @param groupName the group of which the members are in
     */
    public void getMembersInGroup(String groupName)  {
        Log.d(TAG, "onClickUserIcon: getMembersForGroup(groupName)");
        try {    networkService.serverRequest(JSONMessageWriter.getMembersForGroup(groupName)); }
        catch (Exception e){ e.printStackTrace(); }
    }

    /**
     * Tells NetworkService to request the registered groups from the server
     */
    public void getRegisteredGroups() {
        Log.d(TAG, "onClickGroupIcon: getRegisteredGroups()");
        networkService.serverRequest(JSONMessageWriter.getGroups());

    }



    public void sendTextMessage(String id, String message){
        Log.d(TAG, "sendTextMessage: Data:" + id +
                ":" + message);
        networkService.serverRequest(JSONMessageWriter.writeTextMessage(id,message));
    }

    public void sendImageMessage(String id, String message, String longitude, String latitude){
        Log.d(TAG, "sendImageMessage: Data" + id
                + ":" + message
                + ": Longitude[" + longitude + "]"
                + ": Latitude[" + latitude + "]");
        networkService.imageMessageRequest(JSONMessageWriter.imageMessage(id,message,longitude,latitude));
    }
    public void uploadImage(String port, String imageID){

    }

    public void unregister(MutableLiveData<String> userID) {
        networkService.serverRequest(JSONMessageWriter.unregisterMessage(userID.getValue()));
    }
}
