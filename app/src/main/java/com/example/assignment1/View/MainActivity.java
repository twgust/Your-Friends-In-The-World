package com.example.assignment1.View;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import android.util.Log;
import android.widget.Toast;


import com.example.assignment1.ViewModel.TextMessages;
import com.example.assignment1.ViewModel.UserData;
import com.example.assignment1.ViewModel.MemberData;
import com.example.assignment1.controller.Controller;
import com.example.assignment1.R;
import com.example.assignment1.controller.entity.TextMessage;
import com.example.assignment1.controller.network.NetworkBroadcastReceiver;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.Objects;


public class MainActivity extends AppCompatActivity implements
        ActivityCompat.OnRequestPermissionsResultCallback,
        Fragment_LogIn.LoginListener,
        Fragment_LogIn.GroupNameListener,
        Fragment_LogIn.GroupSelectedListener,
        Fragment_Chat.SendMessageListener,
        Fragment_Groups.GROUPS_ON_JOIN_CLICK,
        Fragment_Groups.GROUPS_REFRESH,
        Fragment_Groups.GROUP_REFRESH
{

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private static final int READ_PERMISSION_REQUEST_CODE = 2;
    private static final int WRITE_PERMISSION_REQUEST_CODE = 3;
    private static final String TAG = "MainActivity";
    private Controller controller;

    public static FragmentManager fragmentManager;
    private Fragment_LogIn fragment_logIn;
    private Fragment_Chat fragment_Chat;
    private Fragment_Maps fragment_Map;
    private Fragment_Groups fragment_Groups;

    private TextMessages textMessageModel;
    private UserData userDataModel;
    private MemberData memberModel;

    private BottomNavigationView bottomNavigationView;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        requestPermissions();
        setupNetworkBroadcastReceiver();

        controller = new Controller(this, savedInstanceState);
        userDataModel = new ViewModelProvider(this).get(UserData.class);
        textMessageModel = new ViewModelProvider(this).get(TextMessages.class);
        memberModel = new ViewModelProvider(this).get(MemberData.class);


        // initialize the fragments
        fragmentManager = getSupportFragmentManager();
        fragment_logIn = new Fragment_LogIn();
        fragment_Groups = new Fragment_Groups();
        fragment_Chat = new Fragment_Chat();


        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.fragment_container, fragment_logIn, "LOGIN");
        fragmentTransaction.commit();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        controller.onDestroy();
    }

    private void requestPermissions(){
        permissions.launch(new String[]{fineLocation(), coarseLocation(), writeExtStorage(), readExtStorage()});
    }
    private final ActivityResultLauncher<String[]> permissions = registerForActivityResult
            (new ActivityResultContracts.RequestMultiplePermissions(), result -> {

        if(result.get(fineLocation()) != null){
            Log.d(TAG, "Manifest.Permission.ACCESS_FINE_LOCATION: Should be granted.");
            if(ContextCompat.checkSelfPermission(this, fineLocation()) == PackageManager.PERMISSION_GRANTED ){
                fragment_Map = new Fragment_Maps(true);
                Log.d(TAG, "Manifest.Permission.ACCESS_FINE_LOCATION: Verified, Permission granted!"); }
            else{ fragment_Map = new Fragment_Maps(false);} }

        if(result.get(coarseLocation()) != null){
            Log.d(TAG, "Manifest.Permission.ACCESS_COARSE_LOCATION: Should be granted, verifying...");
            if(ContextCompat.checkSelfPermission(this, coarseLocation()) == PackageManager.PERMISSION_GRANTED){
                Log.d(TAG, "Manifest.Permission.ACCESS_COARSE_LOCATION: Verified, Permission granted!"); } }

        if (result.get(readExtStorage()) != null){
            Log.d(TAG, "Manifest.Permission.READ_EXTERNAL_STORAGE: Should be granted, verifying...");
            if(ContextCompat.checkSelfPermission(this, readExtStorage()) == PackageManager.PERMISSION_GRANTED){
                Log.d(TAG, "Manifest.Permission.READ_EXTERNAL_STORAGE: Verified, permission granted!"); } }

        if(result.get(writeExtStorage()) != null){
            Log.d(TAG, "Manifest.Permission.WRITE_EXTERNAL_STORAGE: Should be granted, verifying...");
            if(ContextCompat.checkSelfPermission(this, writeExtStorage()) == PackageManager.PERMISSION_GRANTED){
                Log.d(TAG, "Manifest.Permission.READ_EXTERNAL_STORAGE: Verified, permission granted!"); } }
    });




    private void bottomNavigation(){
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setSelected(false);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            if(item.getItemId() == R.id.page_2_groups){
                Log.d(TAG, "bottomNavigation: FRAGMENT: GROUPS");
                switchFragment(fragment_Groups);
                return true;
            }
           else if(item.getItemId() == R.id.page_3_chat){
               Log.d(TAG, "bottomNavigation: FRAGMENT: CHAT");
               switchFragment(fragment_Chat);
               bottomNavigationView.setSelected(true);
               return true;
           }
           else if(item.getItemId() == R.id.page_4_googleMaps){
               Log.d(TAG, "bottomNavigation: FRAGMENT: MAPS");
               switchFragment(fragment_Map);
               return true;
           }
           else{ return true; }
        });
    }

    private void switchFragment(Fragment fragment){
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        if(fragment instanceof Fragment_LogIn)
        { fragmentTransaction.replace(R.id.fragment_container, fragment, "LOGIN"); }
        else if (fragment instanceof Fragment_Maps)
        { fragmentTransaction.replace(R.id.fragment_container, fragment, "MAPS"); }
        else if (fragment instanceof Fragment_Chat)
        { fragmentTransaction.replace(R.id.fragment_container, fragment,"CHAT"); }
        else if (fragment instanceof Fragment_Groups)
        { fragmentTransaction.replace(R.id.fragment_container, fragment, "GROUPS"); }

        fragmentTransaction.commit();
    }

    /**
     * handles broadcasts from networkService
     */
    private void setupNetworkBroadcastReceiver(){
        NetworkBroadcastReceiver mMessageReceiver = new NetworkBroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction().toString();
                switch (action) {
                    case "REGISTER":
                        Log.d(TAG, "onReceive: Register");
                        String id = intent.getStringExtra("id");
                        userDataModel.setUserID(id);
                        Toast.makeText(getApplicationContext(), "Signed in as '" + id + "'", Toast.LENGTH_SHORT).show();
                        // setup navigation when a user has registered
                        bottomNavigation();
                        break;
                    case "UNREGISTER":
                        userDataModel.setUserID(null);
                    case "SETLOCATION":
                        Log.d(TAG, "onReceive: SETLOCATION");
                        String lng = intent.getStringExtra("longitude");
                        String lat = intent.getStringExtra("latitude");
                        System.out.println(lat);
                        System.out.println(lng);
                    case "LOCATION":
                        Log.d(TAG, "onReceive: Location");
                        String longitude = intent.getStringExtra("longitude");
                        String latitude = intent.getStringExtra("latitude");
                        userDataModel.setUserLocation(longitude, latitude);
                        break;
                    case "LOCATIONS":
                        Log.d(TAG, "onReceive: Locations");
                        ArrayList<String> locations = intent.getStringArrayListExtra("locations");
                        memberModel.loadMemberLocations(locations);
                        break;

                    case "MEMBERS":
                        Log.d(TAG, "onReceive: Members");
                        int count = 0;
                        memberModel.loadMembersInAGroup(intent.getStringArrayListExtra("name"));
                        //TODO UPDATE VIEWMODEL
                        break;

                    case "GROUPS":
                        Log.d(TAG, "onReceive: Groups");
                        memberModel.loadGroups(intent.getStringArrayListExtra("groupsArrayList"));
                        break;

                    case "TEXTMESSAGE":
                        Log.d(TAG, "onReceive: TextMessage");
                        // if the user is sending a message, set type to outbound
                        String userID = userDataModel.getUserID().getValue();
                        if (Objects.requireNonNull(userID).contains(intent.getStringExtra("member"))){
                            Log.d(TAG, "onReceive.TextMessage: SENT FROM CLIENT");
                            textMessageModel.addMessage(new TextMessage(intent.getStringExtra("text"),
                                           "Client", TextMessage.TEXT_MESSAGE_OUTBOUND)); }
                        // else the user is receiving a message, set type to inbound
                        else{
                            Log.d(TAG, "onReceive.TextMessage: RECEIVED FROM SERVER");
                            textMessageModel.addMessage(new TextMessage(intent.getStringExtra("text"),
                                            intent.getStringExtra("member"), TextMessage.TEXT_MESSAGE_INBOUND)); }
                        break;

                    case "IMAGEMESSAGE":
                        System.out.println("receiving an image message from the server");
                        break;
                    case "IMAGEUPLOAD":
                        String port = intent.getStringExtra("port");
                        String imageID = intent.getStringExtra("imageID");
                        System.out.println(port + imageID);
                        System.out.println("image upload");
                        break;
                    default:
                        System.out.println("unrecognised intent received from networkservice");
                }
            }};
        registerReceivers(mMessageReceiver);
    }

    private void registerReceivers(NetworkBroadcastReceiver receiver){
        LocalBroadcastManager.getInstance(this).registerReceiver(
                receiver, new IntentFilter("REGISTER"));
        LocalBroadcastManager.getInstance(this).registerReceiver(
                receiver, new IntentFilter("MEMBERS"));
        LocalBroadcastManager.getInstance(this).registerReceiver(
                receiver, new IntentFilter("GROUPS"));
        LocalBroadcastManager.getInstance(this).registerReceiver(
                receiver, new IntentFilter("TEXTMESSAGE"));
        LocalBroadcastManager.getInstance(this).registerReceiver(
                receiver, new IntentFilter("IMAGEMESSAGE"));
        LocalBroadcastManager.getInstance(this).registerReceiver(
                receiver, new IntentFilter("IMAGEUPLOAD"));
        LocalBroadcastManager.getInstance(this).registerReceiver(
                receiver, new IntentFilter("LOCATION"));
        LocalBroadcastManager.getInstance(this).registerReceiver(
                receiver, new IntentFilter("LOCATIONS"));
    }
    private String fineLocation(){ return Manifest.permission.ACCESS_FINE_LOCATION; }
    private String coarseLocation(){ return Manifest.permission.ACCESS_COARSE_LOCATION; }
    private String writeExtStorage(){ return Manifest.permission.WRITE_EXTERNAL_STORAGE; }
    private String readExtStorage(){ return Manifest.permission.READ_EXTERNAL_STORAGE; }

    /**
     * Fragment listeners
     *
     * onLoginButtonClicked(String groupname, String username) ==
     * submit button in Fragment_LogIn
     *
     * onSendMessageButtonClicked(String userID, String textMessage) ==
     * send button in [class: Fragment_Chat]
     *
     * selectedGroupListener(int pos) ==
     * the group items in the recyclerview [Class:Fragment_LogIn]
     */
    @SuppressLint("MissingPermission")
    @Override
    public void onLoginButtonClicked(String groupname, String username)  {
        controller.registerToGroup(groupname,username);
        switchFragment(fragment_Map);
    }
    @Override
    public void onSendMessageButtonClicked(String userID, String textMessage) {
        controller.sendTextMessage(userID, textMessage);
    }
    @Override
    public void onSendImageButtonClicked(String userID, String textMessage, String longitude, String latitude) {
        controller.sendImageMessage(userID,textMessage,longitude,latitude );
    }
    @Override
    public void LOGIN_onGroupClicked(String name) { controller.getMembersInGroup(name); }
    @Override
    public void LOGIN_onGroupClicked(int pos) {

    }


    @Override
    public void GROUPS_OnJoinClicked(String name) {
        switchFragment(fragment_logIn);
        controller.unregister(userDataModel.getUserID());
        //TODO
        // call unregister func in controller
        //
    }

    @Override
    public void GROUPS_refresh() { controller.getRegisteredGroups(); }

    @Override
    public void GROUP_byName(String name) {
        controller.getMembersInGroup(name);
    }
}