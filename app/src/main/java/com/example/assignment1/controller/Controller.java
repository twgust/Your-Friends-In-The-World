package com.example.assignment1.controller;

import android.content.ComponentName;

import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.example.assignment1.View.MainActivity;
import com.example.assignment1.controller.network.NetworkService;
import com.example.assignment1.controller.json_utility.JSONMessageWriter;


import java.io.IOException;

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
     * @throws IOException Exception
     */

    public void registerToGroup(String groupName, String username) throws IOException {
        Log.d(TAG, "onClickSubmit: RegisterToGroup(groupName, username)");
        networkService.registerRequest(JSONMessageWriter.registerToGroup(groupName, username));

    }

    /**
     * Tells NetworkService to request the members for groupName
     * @param groupName the group of which the members are in
     * @throws IOException Exception
     */

    public void getMembersInGroup(String groupName) throws IOException{
        Log.d(TAG, "onClickUserIcon: getMembersForGroup(groupName)");
        networkService.serverRequest(JSONMessageWriter.getMembersForGroup(groupName));
    }

    /**
     * Tells NetworkService to request the registered groups from the server
     * @throws IOException Exception
     */
    public void getRegisteredGroups() throws IOException{
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
}
