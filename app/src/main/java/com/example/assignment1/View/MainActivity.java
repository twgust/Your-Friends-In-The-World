package com.example.assignment1.View;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import android.util.Log;
import android.view.MenuItem;


import com.example.assignment1.ViewModel.TextMessages;
import com.example.assignment1.ViewModel.UserData;
import com.example.assignment1.ViewModel.Users;
import com.example.assignment1.controller.Controller;
import com.example.assignment1.R;
import com.example.assignment1.controller.entity.TextMessage;
import com.example.assignment1.controller.network.NetworkBroadcastReceiver;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;

import java.io.IOException;
import java.util.Objects;


public class MainActivity extends AppCompatActivity implements
        NavigationView.OnNavigationItemSelectedListener,
        ActivityCompat.OnRequestPermissionsResultCallback,
        Fragment_LogIn.LoginListener,
        Fragment_LogIn.GroupSelectedListener,
        Fragment_Chat.SendMessageListener {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private static final String TAG = "MainActivity";
    private Controller controller;

    public static FragmentManager fragmentManager;
    private Fragment_LogIn fragment_logIn;
    private Fragment_Chat fragment_Chat;
    private Fragment_Maps fragmentMap;

    private TextMessages textMessageModel;
    private UserData userDataModel;
    private Users memberModel;

    private BottomNavigationView bottomNavigationView;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            fragmentMap = new Fragment_Maps(true);
            System.out.println("permission granted");
        }
        else {
            requestPermissionLauncher.launch(
                    Manifest.permission.ACCESS_FINE_LOCATION);
        }

        controller = new Controller(this, savedInstanceState);
        userDataModel = new ViewModelProvider(this).get(UserData.class);
        textMessageModel = new ViewModelProvider(this).get(TextMessages.class);
        memberModel = new ViewModelProvider(this).get(Users.class);

        setupNetworkBroadcastReceiver();
        // NavigationView navView = findViewById(R.id.navigationView);
        // navView.setNavigationItemSelectedListener(this);
        // setupDrawerLayout();

        // initialize the fragments
        fragmentManager = getSupportFragmentManager();

        fragment_logIn = new Fragment_LogIn();
        fragment_Chat = new Fragment_Chat();


        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.fragment_container,fragment_logIn );
        fragmentTransaction.commit();
    }
    @Override
    protected void onDestroy(){
        super.onDestroy();
        controller.onDestroy();
    }

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    fragmentMap = new Fragment_Maps(true);
                } else {
                    MapsPermissionUtils.RationaleDialog.newInstance(LOCATION_PERMISSION_REQUEST_CODE, false)
                            .show(this.getSupportFragmentManager(), "tag");
                    fragmentMap = new Fragment_Maps(false);

                    // Explain to the user that the feature is unavailable because the
                    // features requires a permission that the user has denied. At the
                    // same time, respect the user's decision. Don't link to system
                    // settings in an effort to convince the user to change their
                    // decision.
                }
            });

    private void setupBottomNavBar(){
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setSelected(false);
        bottomNavigationView.setOnItemSelectedListener(item -> {
           if(item.getItemId() == R.id.page_3_chat){
               System.out.println("chat item");
               switchFragment(fragment_Chat);
               return true;
           }
           else if(item.getItemId() == R.id.page_4_googleMaps){
               switchFragment(fragmentMap);
               return true;
           }
           else{
               return true;
           }
        });
    }

    private void switchFragment(Fragment fragment){
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, fragment, null);
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

                        // setup navigation when a user has registered
                        setupBottomNavBar();
                        break;

                    case "LOCATION":
                        Log.d(TAG, "onReceive: Location");
                        String longitude = intent.getStringExtra("longitude");
                        String latitude = intent.getStringExtra("latitude");
                        userDataModel.setUserLocation(longitude, latitude);
                        break;

                    case "MEMBERS":
                        Log.d(TAG, "onReceive: Members");
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
                                           "Client", TextMessage.TEXT_MESSAGE_OUTBOUND));
                        }
                        // else the user is receiving a message, set type to inbound
                        else{
                            Log.d(TAG, "onReceive.TextMessage: RECEIVED FROM SERVER");
                            textMessageModel.addMessage(new TextMessage(intent.getStringExtra("text"),
                                            intent.getStringExtra("member"), TextMessage.TEXT_MESSAGE_INBOUND));
                        }
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
                receiver, new IntentFilter("IMAGEUPLOAD"));
        LocalBroadcastManager.getInstance(this).registerReceiver(
                receiver, new IntentFilter("LOCATION"));
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id  = item.getItemId();
        System.out.println(id);

        if(id == R.id.useritem){
            try { controller.getMembersInGroup("group1"); }
            catch (IOException e) { e.printStackTrace(); }
        }

        else if (id == R.id.groupitem){
            try { controller.getRegisteredGroups(); }
            catch (IOException e){ e.printStackTrace(); }
        }

        else if (id == R.id.chatitem){
            if(userDataModel.getUserID() != null){
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.fragment_container, fragment_Chat, null);
            }
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.mainLayout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private static boolean isPermissionGranted(String[] grantPermissions, int[] grantResults, String permission){
        for (int i = 0; i < grantPermissions.length; i++) {
            if (permission.equals(grantPermissions[i])) {
                return grantResults[i] == PackageManager.PERMISSION_GRANTED;
            }
        }
        return false;
    }

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
    @Override
    public void onLoginButtonClicked(String groupname, String username) throws IOException {
        controller.registerToGroup(groupname,username);
        switchFragment(fragment_Chat);
    }
    @Override
    public void onSendMessageButtonClicked(String userID, String textMessage) {
        controller.sendTextMessage(userID, textMessage);
    }

    @Override
    public void onSendImageButtonClicked(String userID, String textMessage, String longitude, String latitude) {
        //controller.sendImageMessage(userID,textMessage,longitude,latitude);
    }

    @Override
    public void selectedGroupListener(int pos) {

    }

}



        /*
    private void setupTopAppBar() {
        MaterialToolbar topAppBar = (MaterialToolbar) findViewById(R.id.topAppBar);
        topAppBar.setOnMenuItemClickListener(item -> {

            return true;
        });
    }
         */