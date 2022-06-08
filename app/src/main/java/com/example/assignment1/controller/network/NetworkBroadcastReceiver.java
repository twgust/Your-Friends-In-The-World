package com.example.assignment1.controller.network;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class NetworkBroadcastReceiver extends BroadcastReceiver {
    private static final String TAG = "NetworkBroadcastReceiver";
    @Override
    public void onReceive(Context context, Intent intent) {
        System.out.println(TAG);
    }
}
