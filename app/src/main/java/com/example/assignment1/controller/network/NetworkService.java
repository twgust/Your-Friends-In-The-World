package com.example.assignment1.controller.network;

import com.example.assignment1.R;
import com.example.assignment1.View.Fragment_Maps;
import com.example.assignment1.controller.Controller;
import com.example.assignment1.controller.entity.MemberLocation;
import com.example.assignment1.controller.json_utility.JSONMessageWriter;
import com.example.assignment1.controller.json_utility.JSONParser;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApi;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.app.Service;

import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.location.LocationManager;
import android.media.Image;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;

import android.util.JsonReader;
import android.util.Log;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.StringReader;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import java.util.ArrayList;
import java.util.HashMap;
import java.net.Socket;


@SuppressWarnings("ALL")
public class NetworkService extends Service implements LocationListener {
    private static final String TAG = "NetworkService";
    private final IBinder binder = new LocalService();

    // streams
    private volatile Socket socket;
    private volatile DataInputStream dis;
    private volatile InputStream is;
    private volatile DataOutputStream dos;
    private volatile OutputStream os;

    // thread variables
    private ExecutorService threadPool;
    private ResponseHandler responseHandler;
    private ConnectThread connectThread;
    private Receiver receive;
    private Buffer responseBuffer;
    private boolean isRegistered;

    // local data
    private ArrayList<String> membersInGroups;
    private ArrayList<String> currentGroups;
    private ArrayList<String> currentLocations;
    public static String userID = "";
    public static String group = "";
    private LocationManager locationManager;
    private FusedLocationProviderClient fusedLocationClient;
    private Controller controller;

    // 30 sec .sleep duration (30k milliseconds)
    private final static int pauseDuration = 20000;


    @Override
    public void onLocationChanged(@NonNull Location location) {
        System.out.println(location.toString());
    }


    public class LocalService extends Binder {
        public NetworkService getService() {
            return NetworkService.this;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand: Service started successfully");
        return Service.START_STICKY;
    }

    @Override
    public void onDestroy() {
        disconnectFromServer();
        Log.d(TAG, "onDestroy: Invoked");
        super.onDestroy();
    }


    @Override
    public IBinder onBind(Intent intent) {
        connectThread = new ConnectThread();
        receive = new Receiver();
        responseHandler = new ResponseHandler(this);
        responseBuffer = new Buffer();


        // System.out.println("THREAD COUNT" + Runtime.getRuntime().availableProcessors());
        // setup a future object, waits until a connection to the server has been established
        threadPool = Executors.newFixedThreadPool(4);


        Future<?> awaitConnection = threadPool.submit(connectThread);
        try {
            awaitConnection.get();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
        setupContainers();
        setupSocketAndStreams();

        threadPool.submit(new Send(JSONMessageWriter.getGroups()));
        threadPool.submit(responseHandler);
        threadPool.submit(receive);

        Log.d(TAG, "onBind: Bound to Service 'NetworkService'");
        return binder;
    }


    private void setupSocketAndStreams() {
        try {
            socket = connectThread.getSocket();
            os = socket.getOutputStream();
            dos = new DataOutputStream(os);
            is = socket.getInputStream();
            dis = new DataInputStream(is);
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void setupContainers() {
        currentGroups = new ArrayList<>();
        currentLocations = new ArrayList<>();
        membersInGroups = new ArrayList<>();
    }

    public void serverRequest(String requestString) {
        threadPool.submit(new Send(requestString));

        Log.d(TAG, "serverRequest: Invoked, submitting Send Thread to threadpool...\n" +
                "Sending " + requestString + " to " + socket.getRemoteSocketAddress() +
                "...");
    }


    @SuppressWarnings("BusyWait")
    private void updatePositions()  {
        System.out.println(isRegistered);
        ExecutorService singleThread = Executors.newSingleThreadExecutor();
        singleThread.submit(() -> {
            try{
                while (!isRegistered) { Thread.sleep(500); }

                while (isRegistered) {
                {
                    fusedLocationClient.getLastLocation().addOnSuccessListener((Executor) Executors.newSingleThreadExecutor(), new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {

                            String longtitude = String.valueOf(location.getLongitude());
                            String latitude = String.valueOf(location.getLatitude());
                            Send send = new Send(JSONMessageWriter.setPosition
                                    (userID, longtitude, latitude));
                            send.run();
                        }
                        else if (location == null) {
                            Log.d(TAG, "onSuccess: LOCATION == NULL");
                        }
                    }});
                    Thread.sleep(10000); } } }
            catch (Exception e) { e.printStackTrace(); }
        });
    }
    private synchronized void setRegistered(boolean isRegistered){
        this.isRegistered = isRegistered;
    }

    public void registerRequest(String registerRequest) {
        Future<?> awaitRegistration;
        awaitRegistration = threadPool.submit(new Send(registerRequest));
        try {
            awaitRegistration.get();
            if (awaitRegistration.isDone()) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                        && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    fusedLocationClient = LocationServices.getFusedLocationProviderClient(NetworkService.this);
                    updatePositions();
                } else { System.out.println("NETWORK SERVICE, LOCATIONS Disabled"); }
            }
        }catch(Exception e){

        }
    }

    public void imageMessageRequest(String imageMessageRequest) {
        Future <?> awaitImageResponse = threadPool.submit(new Send(imageMessageRequest));
        try{
            awaitImageResponse.get();
            if(awaitImageResponse.isDone()){
            }
        }

        catch (Exception e){

        }
    }


    public void uploadImage(String imageID, String portStr) {
        int port = Integer.parseInt(portStr);
        byte[] image = null;
        ConnectThread imageConnection = new ConnectThread(port);
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
            Bitmap bm = BitmapFactory.decodeFile("/storage/emulated/0/Pictures/IMG_20220610_155101.jpg");
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bm.compress(Bitmap.CompressFormat.JPEG, 100, baos); //bm is the bitmap object
            bm.recycle();
            image = baos.toByteArray();
            System.out.println("complete");
        }
        else{
            System.out.println("no storage permissions");
        }
        Future<?> awaitConnection = threadPool.submit(imageConnection);
        try {
            awaitConnection.get();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
        try {
            OutputStream os = imageConnection.getSocket().getOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream
                    (os);
            oos.writeUTF(imageID);
            oos.flush();
            oos.writeObject(image);
            oos.flush();
            oos.close();

        }
        // oos.writeObject(uploadArray);

        catch (IOException e) { e.printStackTrace(); }
        // byte array containing the image data
    }
    public static Bitmap getBitmapFromLocalPath(String path, int sampleSize)
    {
        try
        {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = sampleSize;
            return BitmapFactory.decodeFile(path, options);
        }
        catch(Exception e)
        {
            //  Logger.e(e.toString());
        }

        return null;
    }

    private void disconnectFromServer() {
        threadPool.submit(new NetworkDisconnect());
    }

    /**
     * Closes the socket and all streams, invoked when service is destroyed.
     */
    private class NetworkDisconnect implements Runnable {
        public void run() {
            try {
                if (is != null)
                    is.close();
                if (dis != null)
                    dis.close();
                if (os != null)
                    os.close();
                if (dos != null)
                    dos.close();
                if (socket != null) {
                    socket.close();
                    // receive = null;
                    responseHandler = null;
                    Log.d(TAG, "NetworkDisconnect run(): connection to server terminated");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private class Receiver implements Runnable {
        @Override
        public void run() {
            while (receive != null) {
                try {
                    String str = dis.readUTF();
                    Thread.sleep(250);
                    responseBuffer.put(str);
                } catch (IOException | InterruptedException e) {
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException interruptedException) {
                        interruptedException.printStackTrace();
                    }
                    e.printStackTrace();
                }
            }
        }
    }


    /**
     * Class which sends requests to server
     * Pass JSON-formatted String as request to server in Constructor
     * Tested and works properly.
     */
    private class Send implements Runnable {
        private String serverRequest;

        public Send(String request) {
            this.serverRequest = request;
        }

        public void run() {
            String result;
            try {
                Log.d(TAG, "Send.run(): sending request,  THREAD = <" + Thread.currentThread() + ">");
                Log.d(TAG, "run: " + serverRequest);
                dos.writeUTF(serverRequest);
                dos.flush();
            } catch (Exception e) {
                Log.d(TAG, "Communication with server failed");
                e.printStackTrace();
            }
        }
    }

    /**
     * Thread which fetches an element from the buffer (responseBuffer) and
     * processes the contents of the element
     * <p>
     * this way one Request can be sent to the server while another request (element in the buffer)
     * is simultaneously being processed.
     * <p>
     * This design made the most sense to me as the cost of handling and parsing an element is a
     * much more expensive operation than just writing a string to an OutputStream.
     * Another consequence of separating the sending of requests from the receiving and handling of
     * responses is that of more readable code.
     * <p>
     * Lastly it also promotes Reusability.
     */
    private class ResponseHandler implements Runnable {
        private final static String TAG = "ResponseHandler";
        private final Context serviceContext;

        /**
         * Sets context to service so Broadcasts can be sent
         *
         * @param context Service Context
         */
        public ResponseHandler(Context context) {
            this.serviceContext = context;
        }

        public void run() {
            while (responseBuffer != null) {
                try {
                    String responseBody = responseBuffer.get();
                    Log.d(TAG, "Handling JSON response from server: "
                            + responseBody + "\n"
                            + "THREAD = <" + Thread.currentThread() + ">");
                    JSONObject serverResponse = new JSONObject(responseBody);
                    String responseType = serverResponse.getString("type");
                    switch (responseType) {
                        case "register":
                            String[] registerResponse = JSONParser.parseRegistrationResponse(responseBody);
                            userID = registerResponse[1];
                            group = registerResponse[0];
                            setRegistered(true);
                            Intent registerIntent = new Intent("REGISTER");
                            registerIntent.putExtra("group", registerResponse[0]);
                            registerIntent.putExtra("id", registerResponse[1]);
                            LocalBroadcastManager.getInstance(serviceContext)
                                    .sendBroadcast(registerIntent);
                            threadPool.submit(new Send(JSONMessageWriter.getMembersForGroup(group)));
                            break;
                        case "unregister":
                            String unregisterResponse = JSONParser.parserDeregistrationResponse(responseBody);
                            Intent unregisterIntent = new Intent("UNREGISTER");
                            unregisterIntent.putExtra("unregister", unregisterResponse);
                            LocalBroadcastManager.getInstance(serviceContext)
                                    .sendBroadcast(unregisterIntent);
                            setRegistered(false);
                            break;

                        case "members":
                            ArrayList<String> membersInGroupResponse = (ArrayList<String>)
                                    JSONParser.parseMembersInGroup(responseBody);
                            Intent membersIntent = new Intent("MEMBERS");
                            membersIntent.putStringArrayListExtra("name", membersInGroupResponse);
                            LocalBroadcastManager.getInstance(serviceContext)
                                    .sendBroadcast(membersIntent);
                            break;

                        case "groups":
                            currentGroups = JSONParser.parseGroupsResponse(responseBody);
                            Intent groupsIntent = new Intent("GROUPS");
                            groupsIntent.putExtra("groupsArrayList", currentGroups);
                            LocalBroadcastManager.getInstance(serviceContext)
                                    .sendBroadcast(groupsIntent);

                            break;

                        case "locations":
                            // member name, long, lat
                            ArrayList<String> locationsResponse = new ArrayList<>();
                            locationsResponse = (ArrayList<String>) JSONParser.parseLocationsResponse(responseBody);
                            Intent locationsIntent = new Intent("LOCATIONS");
                            locationsIntent.putStringArrayListExtra("locations",locationsResponse);
                            LocalBroadcastManager.getInstance(serviceContext)
                                    .sendBroadcast(locationsIntent);
                            break;

                        case "location":
                            HashMap<String, String> locationResponse = JSONParser.parseLocationResponse(responseBody);
                            Intent locationIntent = new Intent("LOCATION");
                            locationIntent.putExtra("longitude", locationResponse.get("longitude"));
                            locationIntent.putExtra("latitude", locationResponse.get("latitude"));
                            LocalBroadcastManager.getInstance(serviceContext)
                                    .sendBroadcast(locationIntent);
                            break;

                        case "textchat":
                            System.out.println(responseBody);
                            HashMap<String, String> textChatResponse =
                                    JSONParser.parseTextMessageResponse(responseBody);
                                    /*
                                      only send inbound messages to the broadcast receiver
                                      this way we don't have to deal with some Server-side redundancy
                                      explanation:
                                      if the client sends a text message,
                                      the server will send two responses

                                      Response one:
                                      (this is the response when u send a message,
                                      which is the same message u sent to the server
                                      when u send a textMessage Request):

                                       { ”type”:”textchat”,
                                       ”id”:”ID”,
                                       ”text”:”TEXT” }

                                      Response two:
                                      (this is the response when u send a message
                                      and receive a message from another user)

                                      { ”type”:”textchat”,
                                       ”group”:”NAME”,
                                       “member”:”NAME”,
                                       ”text”:”TEXT” }

                                       the second response is sent to you regardless of whether
                                       the client is the author of that message or not,
                                       so it is not useful to classify that response as solely inbound.

                                       but since both of the responses have the same type
                                       we hack the json in JsonParser by checking
                                       if the response contains ID, if it does then we class
                                       that as outbound and simply ignore it since the same data can
                                       be fetched from the other response.
                                       see JSONParser.parseTextMessageResponse(JsonReader Reader)

                                     */
                            if (Objects.equals(textChatResponse.get("type"), "inbound")) {
                                Intent textMessageIntent = new Intent("TEXTMESSAGE");
                                textMessageIntent.putExtra("type", textChatResponse.get("type"));
                                textMessageIntent.putExtra("group", textChatResponse.get("group"));
                                textMessageIntent.putExtra("member", textChatResponse.get("member"));
                                textMessageIntent.putExtra("text", textChatResponse.get("text"));
                                LocalBroadcastManager.getInstance(serviceContext)
                                        .sendBroadcast(textMessageIntent);
                            }
                            break;

                        case "upload":

                            System.out.println("<image upload, get your secrets>");
                            HashMap<String, String> uploadResponse =
                                    JSONParser.parseUploadImageResponse(responseBody);
                            Intent uploadImageIntent = new Intent("IMAGEUPLOAD");
                            uploadImageIntent.putExtra("port", uploadResponse.get("port"));
                            uploadImageIntent.putExtra("imageID", uploadResponse.get("imageID"));
                            LocalBroadcastManager.getInstance(serviceContext)
                                    .sendBroadcast(uploadImageIntent);
                            uploadImage(uploadResponse.get("imageID"),uploadResponse.get("port"));



                            break;
                        case "imagechat":

                            System.out.println("<image> chat>");
                            HashMap<String, String> imagechatResponse =
                                    JSONParser.parseImageMessageResponse(responseBody);

                            Intent imagechatIntent = new Intent("IMAGEMESSAGE");
                            imagechatIntent.putExtra("group", imagechatResponse.get("group")) ;
                            imagechatIntent.putExtra("member", imagechatResponse.get("member"));
                            imagechatIntent.putExtra("text",imagechatResponse.get("text"));
                            imagechatIntent.putExtra("longitude", imagechatResponse.get("longitude"));
                            imagechatIntent.putExtra("latitude", imagechatResponse.get("latitude"));
                            imagechatIntent.putExtra("imageID", imagechatResponse.get("imageID"));
                            imagechatIntent.putExtra("port", imagechatResponse.get("port"));
                            LocalBroadcastManager.getInstance(serviceContext)
                                    .sendBroadcast(imagechatIntent);
                            break;

                        default:
                            System.out.println("UNKNOWN RESPONSE: " + responseType);
                            System.out.println(responseBody);
                            break;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}