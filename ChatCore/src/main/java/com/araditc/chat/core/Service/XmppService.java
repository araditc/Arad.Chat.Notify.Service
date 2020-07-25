package com.araditc.chat.core.Service;

import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.araditc.chat.core.Repository.Xmpp.Connection;
import com.araditc.chat.core.Util.EasyPreference;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Stanza;

import java.io.IOException;
import java.util.Calendar;

public class XmppService extends Service implements Connection.ConnectionListener {

    private Connection connection;
    private String NOTICATION_TITLE = "سرویس پیام آراد";
    private String NOTICATION_DESCRIPTION = "در حال دریافت اطلاعات";
    private int NOTICATION_ID = 1105;

    @Override
    public void onConnected() {

    }

    @Override
    public void onAuthenticated() {

    }

    @Override
    public void onDisconnected() {

    }

    @Override
    public void onStanzaReceived(Stanza stanza) {
        Intent intent = new Intent("xmpp");
        intent.putExtra("stanza", String.valueOf(stanza.toXML()));
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        new Handler().postDelayed(this::stopSelf, 2000);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            setupNotification();
        }
        initConnection();
    }

    private void setupNotification() {
        NotificationCompat.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder = new NotificationCompat.Builder(this, "AradCore");
        } else {
            builder = new NotificationCompat.Builder(this);
        }
        builder.setOngoing(true)
                .setSmallIcon(android.R.drawable.stat_notify_chat)
                .setContentTitle(NOTICATION_TITLE)
                .setContentText(NOTICATION_DESCRIPTION);
        startForeground(NOTICATION_ID, builder.build());
    }

    private void initConnection() {
        if (connection == null) {
            connection = new Connection(this, this);
            String Username = EasyPreference.with(this).getString("xmpp_username" , "");
            String Password = EasyPreference.with(this).getString("xmpp_password" , "");
            connection.setCredential(Username, Password);
            connection.setResource(String.valueOf(Calendar.getInstance().getTimeInMillis()));
        }
        try {
            connection.connect();
        } catch (IOException | XMPPException | SmackException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onUnbind(Intent intent) {
        connection.disconnect();
        connection.destroy();
        connection = null;
        return false;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        connection.disconnect();
        connection.destroy();
        connection = null;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
    }
}
