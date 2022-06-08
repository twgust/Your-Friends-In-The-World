package com.example.assignment1.controller.network;

import android.util.Log;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;

/**
 * Experimented with making thread classes separate instead of a private inner class
 * Establishes connection to server by creating a socket(ip,port) and
 * initializes Input and Output-streams
 *
 * Works quite well for this Thread case as one can simply return the socket and thereby the
 * other threads gain access to the streams initialized here.
 *
 */
public class ConnectThread implements Runnable {
        private static final String TAG = ConnectThread.class.getName();
        private Socket socket;

        public void run() {
            try {
                Log.d(TAG, "ConnectThread.run: connecting to server.. THREAD = <" + Thread.currentThread()+">");
                int port = 7117;
                String ipString = "192.168.0.30";
                InetAddress address = InetAddress.getByName(ipString);

                socket = new Socket(address, port);
                OutputStream os = socket.getOutputStream();
                DataOutputStream dos = new DataOutputStream(os);
                InputStream is = socket.getInputStream();
                DataInputStream dis = new DataInputStream(is);

                Log.d(TAG, "NetworkConnect.run: client <" + socket.getLocalAddress()
                        + ">Connected to server @<" + socket.getRemoteSocketAddress() + ">");

            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("SUPER EXCEPTION");
            }

        }

    public Socket getSocket() {
        return socket;
    }
}
