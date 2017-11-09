package com.example.pc.bucketdrops.reciver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.pc.bucketdrops.extras.Util;

public class BootReceiver extends BroadcastReceiver {
    public BootReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Util.scheduleAlarm(context);
    }
}
