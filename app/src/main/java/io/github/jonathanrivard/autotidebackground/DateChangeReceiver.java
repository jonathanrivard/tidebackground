package io.github.jonathanrivard.autotidebackground;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.Toast;

public class DateChangeReceiver extends BroadcastReceiver {
    SharedPreferences prefs;

    @Override
    public void onReceive(Context context, Intent intent) {
        prefs = context.getSharedPreferences("TideAppSettings", Context.MODE_PRIVATE);
        Boolean shouldUpdate = prefs.getBoolean("autoChange", false);
        if(shouldUpdate){
            context.sendBroadcast(new Intent("UPDATE_TIDE_BACKGROUND_JR"));
        }
    }
}
