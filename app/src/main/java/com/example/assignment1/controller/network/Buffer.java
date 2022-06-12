package com.example.assignment1.controller.network;

import android.util.Log;

import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicBoolean;

public class Buffer {
    private LinkedList<String> responseBuffer;
    private Boolean registration  ;

    public Buffer(){
        responseBuffer = new LinkedList<>();
    }
    protected synchronized void setRegistered(Boolean registered){
        this.registration = registered;
        notifyAll();
    }
    protected synchronized Boolean getRegistered() throws InterruptedException {
        while(!registration){
            wait();
        }
        return registration;
    }
    protected synchronized void put(String response){
        responseBuffer.addLast(response);
        Log.d("Buffer", "put: Element added to buffer!");
        notifyAll();

    }
    protected synchronized String get() throws InterruptedException {
        while(responseBuffer.isEmpty()){
            Log.d("Buffer", "get: Waiting for element to be added...");
            wait();
        }
        Log.d("Buffer", "get: Element removed from buffer!");
        return responseBuffer.removeFirst();
    }
}
