package com.example.assignment1.controller.network;

import com.example.assignment1.controller.json_utility.JSONMessageWriter;
import com.example.assignment1.controller.json_utility.JSONParser;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.content.Context;
import android.content.Intent;
import android.app.Service;

import android.os.Binder;
import android.os.IBinder;

import android.util.JsonReader;
import android.util.Log;

import org.json.JSONObject;

import java.io.DataOutputStream;
import java.io.DataInputStream;
import java.io.OutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.StringReader;

import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import java.util.ArrayList;
import java.util.HashMap;
import java.net.Socket;


@SuppressWarnings("ALL")
public class NetworkService extends Service {
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

    // 30 sec .sleep duration (30k milliseconds)
    private final static int pauseDuration = 30000;

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
        threadPool = Executors.newFixedThreadPool(3);


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
        } catch (IOException e) {
            e.printStackTrace();
        }
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
    private void updatePositions() {
        String TAG = "Location Thread";
        Log.d(TAG, "updatePositions: Entering update positions");
        System.out.println(isRegistered);
        ExecutorService singleThread = Executors.newSingleThreadExecutor();
        singleThread.submit(() -> {
            while (!isRegistered) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            while (isRegistered) {
                Log.d(TAG, "Refreshing groups = <" + Thread.currentThread() + ">");

                Send sendGroups = new Send(JSONMessageWriter.getGroups());
                sendGroups.run();
                try {
                    Thread.sleep(2000);
                    Log.d(TAG, "updating positions THREAD = <" + Thread.currentThread() + ">");
                    String setLocation = JSONMessageWriter.setPosition(userID, "12.979154", "55.613393");
                    Send send = new Send(setLocation);
                    send.run();
                    Thread.sleep(pauseDuration);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void registerRequest(String registerRequest) {
        Future<?> awaitRegistration;
        awaitRegistration = threadPool.submit(new Send(registerRequest));
        try {
            awaitRegistration.get();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (awaitRegistration.isDone()) {
            updatePositions();
        }
    }

    public void imageMessageRequest(String imageMessageRequest) {
        threadPool.submit(new Send(imageMessageRequest));
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
                            String[] groupAndID = JSONParser.parseRegistrationResponse(responseBody);
                            userID = groupAndID[1];
                            isRegistered = true;
                            // send broadcast (Group, ID) to activity
                            Intent registerIntent = new Intent("REGISTER");
                            registerIntent.putExtra("group", groupAndID[0]);
                            registerIntent.putExtra("id", groupAndID[1]);
                            LocalBroadcastManager.getInstance(serviceContext)
                                    .sendBroadcast(registerIntent);
                            break;
                        case "unregister":
                            isRegistered = false;
                            break;

                        case "members":
                            JSONParser.parseMembersInGroup(responseBody);
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
                            currentLocations = (ArrayList<String>)
                                    JSONParser.parseLocationsResponse(currentLocations, responseBody);
                            Intent locationsIntent = new Intent("LOCATIONS");
                            break;

                        case "location":
                            HashMap<String, String> response =
                                    JSONParser.parseLocationsResponse(responseBody);
                            Intent locationIntent = new Intent("LOCATION");
                            locationIntent.putExtra("longitude", response.get("longitude"));
                            locationIntent.putExtra("latitude", response.get("latitude"));
                            LocalBroadcastManager.getInstance(serviceContext)
                                    .sendBroadcast(locationIntent);
                            break;

                        case "textchat":
                            System.out.println(responseBody);
                            HashMap<String, String> text =
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
                            if (Objects.equals(text.get("type"), "inbound")) {
                                Intent textMessageIntent = new Intent("TEXTMESSAGE");
                                textMessageIntent.putExtra("type", text.get("type"));
                                textMessageIntent.putExtra("group", text.get("group"));
                                textMessageIntent.putExtra("member", text.get("member"));
                                textMessageIntent.putExtra("text", text.get("text"));
                                LocalBroadcastManager.getInstance(serviceContext)
                                        .sendBroadcast(textMessageIntent);
                            }
                            break;

                        case "upload":
                            System.out.println("<image upload>");
                            HashMap<String, String> responseData =
                                    JSONParser.parseUploadImageResponse(responseBody);

                            Intent uploadImageIntent = new Intent("IMAGEUPLOAD");
                            uploadImageIntent.putExtra("port", responseData.get("port"));
                            uploadImageIntent.putExtra("imageID", responseData.get("imageID"));
                            LocalBroadcastManager.getInstance(serviceContext)
                                    .sendBroadcast(uploadImageIntent);
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
