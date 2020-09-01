package com.araditc.chat.core.Service;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.NonNull;

import com.araditc.chat.core.Repository.Network.ApiService.TokenSubmiterService;
import com.araditc.chat.core.Repository.Network.APIClient;
import com.araditc.chat.core.Repository.Network.RequestBody.RequestTokenSubmiter;
import com.araditc.chat.core.Repository.Network.Response.ResponseTokenSubmiter;
import com.araditc.chat.core.Util.Utility;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@SuppressLint("MissingFirebaseInstanceTokenRefresh")
public class NotificationService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        if (!Utility.isServiceRunning(this, XmppService.class)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(new Intent(this, XmppService.class));
            } else {
                startService(new Intent(this, XmppService.class));
            }
        }
    }
}
