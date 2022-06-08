package com.example.assignment1.controller.json_utility;

import android.util.JsonReader;
import android.util.JsonWriter;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

/**
 * Class for creating requests to send to server
 */
public class JSONMessageWriter {
    public JSONMessageWriter(){

    }
    public static String imageMessage(String userID, String text, String longitude, String latitude){
        try{
            StringWriter stringWriter = new StringWriter();
            JsonWriter writer = new JsonWriter( stringWriter );
            writer.beginObject().name("type").value("imagechat")
                                .name("id").value(userID)
                                .name("text").value(text)
                                .name("longitude").value(longitude)
                                .name("latitude").value(latitude).endObject();
            return stringWriter.toString();
        }catch (IOException e){
            e.printStackTrace();
            return "Exception";
        }
    }
    public static String getGroups() {
        try{
            StringWriter stringWriter = new StringWriter();
            JsonWriter writer = new JsonWriter( stringWriter );
            writer.beginObject().name("type").value("groups").endObject();
            return stringWriter.toString();
        }catch (IOException e){
            e.printStackTrace();
            return "Exception";
        }
    }

    public static String getMembersForGroup(String groupName){
        try{
            StringWriter stringWriter = new StringWriter();
            JsonWriter writer = new JsonWriter( stringWriter );
            writer.beginObject().name("type").value("members")
                                .name("group").value(groupName)
                                .endObject();
            return stringWriter.toString();
        }catch (IOException e){
            e.printStackTrace();
            return "Exception";
        }
    }
    public static String jsonReader(String jsonmessage){
        try{
            StringReader stringReader = new StringReader(jsonmessage);
            JsonReader reader = new JsonReader(stringReader);
            reader.beginObject();
            while(reader.hasNext()){
                String name = reader.nextName();
                String value = reader.nextString();
                if(name.equals("id")){
                    System.out.println(value);
                    reader.endObject();
                    return value;
                    }
                }
            } catch (Exception e) {
            e.printStackTrace();
        }
        return "no id";
    }

    public static String setPosition(String ID,String longitude, String latitude){
        try{
            StringWriter stringWriter = new StringWriter();
            JsonWriter writer = new JsonWriter( stringWriter );
            writer.beginObject().name("type").value("location")
                                .name("id").value(ID)
                                .name("longitude").value(longitude)
                                .name("latitude").value(latitude)
                                .endObject();
            return stringWriter.toString();
        }catch (IOException e){
            e.printStackTrace();
            return "Exception";
        }
    }
    public static String registerToGroup(String groupName, String userName){
        try{
            StringWriter stringWriter = new StringWriter();
            JsonWriter writer = new JsonWriter( stringWriter );
            writer.beginObject().name("type").value("register")
                    .name("group").value(groupName)
                    .name("member").value(userName)
                    .endObject();
            return stringWriter.toString();
        }catch (Exception e){
            e.printStackTrace();
            return "bad formatting";

        }
    }
    public static String writeTextMessage(String id, String text){
        try{
            StringWriter stringWriter = new StringWriter();
            JsonWriter writer = new JsonWriter(stringWriter);
            writer.beginObject().name("type").value("textchat")
                    .name("id").value(id)
                    .name("text").value(text)
                    .endObject();
            return stringWriter.toString();
        }catch (Exception e){
            e.printStackTrace();
        }
        return "bad formatting";
    }
}
