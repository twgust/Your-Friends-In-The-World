package com.example.assignment1.controller.json_utility;

import android.util.JsonReader;
import android.util.Log;

import com.example.assignment1.controller.entity.MemberLocation;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

/**
 * Class for parsing the responses from server to avoid cluttering the NetworkService class
 * It is in no way independent of that class. It is simply for readability.
 */
public class JSONParser {

    private static final String TAG = "Utilities:JSONParser";

    /**
     * Reads and parses the registration response from the server
     * @return returns the User ID formatted as String
     */
    public static String[] parseRegistrationResponse(String JSONMessage) {
        String[] arr = new String[2];
        try {
            JSONObject response = new JSONObject(JSONMessage);
            String group = response.getString("group");
            String id = response.getString("id");
            arr[0] = group;
            arr[1] = id;
            return arr;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return arr;
    }

    public static String parserDeregistrationResponse(String JSONMessage) {

        try{
            JSONObject response = new JSONObject(JSONMessage);
            String typeStr = response.getString("type");
            String responseStr = response.getString("id");
            return typeStr + "," + responseStr;
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }


    public static List<String> parseMembersInGroup(String JSONMessage) {
        try {
            List<String> groupMembers = new ArrayList<>();
            JSONObject response = new JSONObject(JSONMessage);
            String name = response.getString("group");
            groupMembers.add(name);
            JSONArray members = response.getJSONArray("members");
            for (int i = 0; i <members.length() ; i++) {
                groupMembers.add(members.getJSONObject(i).getString("member"));
                System.out.println(members.getJSONObject(i).getString("member"));
            }
            return groupMembers;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static ArrayList<String> parseGroupsResponse(String JSONMessage) {
        ArrayList<String> groups = new ArrayList<>();
        try {

            JSONObject response = new JSONObject(JSONMessage);
            JSONArray jsonGroups = response.getJSONArray("groups");
            for (int i = 0; i < jsonGroups.length() ; i++) {
                String groupName = jsonGroups.getJSONObject(i).getString("group");
                groups.add(groupName);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return groups;
    }

    /**
     * Reads and parses the locations response from the server
     *
     * @return returns a list of location Objects formatted as Strings
     */
    public static ArrayList<String> parseLocationsResponse(String JSONMessage) {

        try {
            ArrayList<String> list = new ArrayList<>();
            JSONObject responseObject = new JSONObject(JSONMessage);
            String group = responseObject.getString("group");
            JSONArray locations = responseObject.getJSONArray("location");

            for (int i = 0; i < locations.length(); i++) {
                String memberName = locations.getJSONObject(i).getString("member");
                String longitude = locations.getJSONObject(i).getString("longitude");
                String latitude = locations.getJSONObject(i).getString("latitude");
                list.add(group + ","+ memberName + "," + longitude + "," + latitude);
            }
            return list;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static HashMap<String, String> parseLocationResponse(String JSONMessage) {
        HashMap<String, String> map = new HashMap<>();
        try {
            JSONObject responseObject = new JSONObject(JSONMessage);

            String longitude = responseObject.getString("longitude");
            String latitude = responseObject.getString("latitude");
            map.put("longitude", longitude);
            map.put("latitude", latitude);
            return map;
        } catch (JSONException e) {
            Log.d(TAG, "parseLocationsResponse: bad response from server");
            e.printStackTrace();
            return null;
        }
    }



    public static HashMap<String, String> parseTextMessageResponse(String JSONMessage) {
        try {
            HashMap<String, String> map = new HashMap<>();
            JSONObject responseObject = new JSONObject(JSONMessage);
            System.out.println("Parsing TextMessageResponse from server...");
            responseObject.getString("type");

            // if response contains ID it's from the client
            if (responseObject.has("id")) {
                String id = responseObject.getString("id");
                String text = responseObject.getString("text");

                map.put("type", "outbound");
                map.put("id", id);
                map.put("text", text);
            }
            // if it contains group it's a message from the server
            else if (responseObject.has("group")) {
                String group = responseObject.getString("group");
                String member = responseObject.getString("member");
                String text = responseObject.getString("text");

                map.put("type", "inbound");
                map.put("group", group);
                map.put("member", member);
                map.put("text", text);
            }
            return map;
        } catch (JSONException e) {

            e.printStackTrace();
            return null;
        }
    }

    public static HashMap<String, String> parseUploadImageResponse(String JSONMessage) {
        System.out.println("Parsing UploadImageResponse from server...");
        HashMap<String, String> map = new HashMap<>();
        try {
            JSONObject response = new JSONObject(JSONMessage);
            String imageID = response.getString("imageid");
            String port = response.getString("port");
            map.put("port", port);
            map.put("imageID", imageID);
            return map;
        } catch (JSONException e) {
            Log.d(TAG, "parseUploadImageResponse: bad response from server");
            e.printStackTrace();
            return null;
        }
    }

    public static HashMap<String, String> parseImageMessageResponse(String JSONMessage) {
        HashMap<String, String> map = new HashMap<>();
        try {
            JSONObject imageResponse = new JSONObject(JSONMessage);
            String group = imageResponse.getString("group");
            String member = imageResponse.getString("member");
            String text = imageResponse.getString("text");
            String longitude = imageResponse.getString("longitude");
            String latitude = imageResponse.getString("latitude");


        } catch (Exception e) {
            e.printStackTrace();
        }
        return map;

    }

}
