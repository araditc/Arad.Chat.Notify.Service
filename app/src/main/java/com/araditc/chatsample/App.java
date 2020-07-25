package com.araditc.chatsample;

import android.app.Application;
import android.widget.Toast;

import com.araditc.chat.core.Core;
import com.araditc.chat.core.Interface.StanzaReceiveCallback;

public class App extends Application implements StanzaReceiveCallback {

    @Override
    public void onCreate() {
        super.onCreate();
        new Core().init(this);
        new Core(this , this);
    }

    @Override
    public void onReceiveMessage(String stanza) {
        Toast.makeText(this, stanza, Toast.LENGTH_LONG).show();
    }
}
