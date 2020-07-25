package com.araditc.chat.core;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.araditc.chat.core.Interface.StanzaReceiveCallback;
import com.araditc.chat.core.Util.EasyPreference;
import com.google.firebase.FirebaseApp;

public class Core {

    private StanzaReceiveCallback _stanzaReceiveCallback;

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (_stanzaReceiveCallback != null) {
                _stanzaReceiveCallback.onReceiveMessage(String.valueOf(intent.getStringExtra("stanza")));
            }
        }
    };

    public Core() {
    }

    public Core(Context context, StanzaReceiveCallback stanzaReceiveCallback) {
        this._stanzaReceiveCallback = stanzaReceiveCallback;
        LocalBroadcastManager.getInstance(context).registerReceiver(mMessageReceiver, new IntentFilter("xmpp"));
    }

    public void init(Context context) {
        FirebaseApp.initializeApp(context);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(new NotificationChannel("AradCore",
                    "Service", NotificationManager.IMPORTANCE_HIGH));

        }
    }

    public void setXmpp(Context context, String username, String password) {
        EasyPreference.with(context).addString("xmpp_username", username);
        EasyPreference.with(context).addString("xmpp_password", password);
    }
}
