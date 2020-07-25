package com.araditc.chat.core.Repository.Xmpp;

import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;

import androidx.annotation.NonNull;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.StanzaListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.chat.ChatMessageListener;
import org.jivesoftware.smack.filter.AndFilter;
import org.jivesoftware.smack.filter.FromMatchesFilter;
import org.jivesoftware.smack.filter.StanzaFilter;
import org.jivesoftware.smack.packet.Stanza;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jivesoftware.smackx.ping.PingManager;
import org.jxmpp.jid.Jid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.stringprep.XmppStringprepException;

import java.io.IOException;

public class Connection implements ConnectionListener, StanzaListener {

    private final Context mApplicationContext;
    private String mServiceName;
    private String mUsername;
    private String mPassword;
    private String mResource;
    private XMPPTCPConnection mConnection;
    private int mResumptionTime = 5;
    private Jid[] mAllowedSenderJid;
    private int mPingInterval = 10;
    private int mDefaultFixedDelay = 10;
    private boolean isConnected;
    private boolean isAuthenticated;
    private ConnectionListener connectionListener;
    private Handler mWorkerHandler;
    private Handler mMainHandler;
    private HandlerThread mHandlerThread;

    private ChatMessageListener mChatMessageListener = (chat, packet) -> {

    };

    private class MainHandler extends Handler {
        private MainHandler(Looper looper) {
            super(looper);
        }

        private static final int MSG_RECEIVED = 1;

        @Override
        public void handleMessage(android.os.Message msg) {
            if (connectionListener != null) {
                if (msg.what == MSG_RECEIVED) {
                    connectionListener.onStanzaReceived((Stanza) msg.obj);
                }
            }
        }
    }

    public Connection(Context mApplicationContext, ConnectionListener connectionListener) {
        this.mApplicationContext = mApplicationContext;
        this.connectionListener = connectionListener;
        mHandlerThread = new HandlerThread("XMPPConnection");
        mHandlerThread.start();
        mWorkerHandler = new Handler(mHandlerThread.getLooper());
        mMainHandler = new MainHandler(Looper.getMainLooper());
    }

    public void setPingInterval(int pingInterval) {
        this.mPingInterval = pingInterval;
    }

    public void setDefaultFixedDelay(int defaultFixedDelay) {
        this.mDefaultFixedDelay = defaultFixedDelay;
    }

    public void setCredential(@NonNull String JID, @NonNull String password) {
        this.mPassword = password;
        mUsername = JID.split("@")[0];
        mServiceName = JID.split("@")[1];
    }

    public void setResource(String resource) {
        this.mResource = resource;
    }

    public void enableFilteringSenders(String[] allowedSenders) throws XmppStringprepException {
        this.mAllowedSenderJid = convertAllowedSendersToJid(allowedSenders);
    }

    private Jid[] convertAllowedSendersToJid(String[] allowedSenders) throws XmppStringprepException {
        Jid[] jids = new Jid[allowedSenders.length];
        for (int i = 0; i < allowedSenders.length; i++) {
            jids[i] = JidCreate.bareFrom(allowedSenders[i]);
        }
        return jids;
    }

    public void connect() throws IOException, XMPPException, SmackException {
        XMPPTCPConnectionConfiguration conf = XMPPTCPConnectionConfiguration.builder()
                .setXmppDomain(mServiceName)
                .setHost(mServiceName)
                .setResource(mResource)
                .setSecurityMode(ConnectionConfiguration.SecurityMode.required)
                .setKeystoreType(null)
                .setSendPresence(true)
                .setCompressionEnabled(true).build();
        XMPPTCPConnection.setUseStreamManagementResumptionDefault(true);
        XMPPTCPConnection.setUseStreamManagementDefault(true);

        mConnection = new XMPPTCPConnection(conf);
        mConnection.setUseStreamManagement(true);
        mConnection.setUseStreamManagementResumption(true);
        mConnection.setPreferredResumptionTime(mResumptionTime);
        mConnection.addConnectionListener(this);

        if (mConnection != null && !mConnection.isConnected()) {
            mWorkerHandler.post(() -> {
                try {
                    mConnection.connect();
                } catch (SmackException | IOException | XMPPException | InterruptedException e) {
                    e.printStackTrace();
                }
            });
        }
    }

    public void disconnect() {
        if (mConnection != null && mConnection.isConnected()) {
            mWorkerHandler.post(() -> mConnection.disconnect());
        }
    }

    public boolean isConnectedUsage() {
        return mConnection != null && mConnection.isAuthenticated();
    }

    @Override
    public void connected(XMPPConnection connection) {
        this.connectionListener.onConnected();
        if (!connection.isAuthenticated()) {
            try {
                mConnection.login(mUsername, mPassword);
                isAuthenticated = true;
            } catch (XMPPException | SmackException | IOException | InterruptedException e) {
                if (e instanceof SmackException.AlreadyLoggedInException) {
                    isAuthenticated = true;
                }
                e.printStackTrace();
            }
        }
    }

    @Override
    public void authenticated(XMPPConnection connection, boolean resumed) {
        PingManager pingManager = PingManager.getInstanceFor(mConnection);
        pingManager.setPingInterval(mPingInterval);
        if (mAllowedSenderJid != null && mAllowedSenderJid.length != 0) {
            mConnection.addAsyncStanzaListener(this, addFilters(mAllowedSenderJid));
        } else {
            mConnection.addAsyncStanzaListener(this, null);
        }
    }

    @Override
    public void connectionClosed() {
        connectionListener.onDisconnected();
    }

    @Override
    public void connectionClosedOnError(Exception e) {
        connectionListener.onDisconnected();
    }

    @Override
    public void reconnectionSuccessful() {

    }

    @Override
    public void reconnectingIn(int seconds) {

    }

    @Override
    public void reconnectionFailed(Exception e) {

    }

    @Override
    public void processStanza(Stanza stanza) {
        Message message = mMainHandler.obtainMessage(MainHandler.MSG_RECEIVED);
        message.obj = stanza;
        mMainHandler.sendMessage(message);
    }

    public void setResumptionTime(int resumptionTime) {
        this.mResumptionTime = resumptionTime;
    }

    private StanzaFilter addFilters(Jid[] allowedSenders) {
        if (allowedSenders.length == 0) {
            return null;
        }
        StanzaFilter[] stanzaFilters = new StanzaFilter[allowedSenders.length];
        for (int i = 0; i < allowedSenders.length; i++) {
            stanzaFilters[i] = FromMatchesFilter.create(allowedSenders[i]);
        }
        return new AndFilter(stanzaFilters);
    }


    public void destroy() {
        disconnect();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            mHandlerThread.quitSafely();
        } else {
            mHandlerThread.quit();
        }
    }

    public interface ConnectionListener {
        void onConnected();

        void onAuthenticated();

        void onDisconnected();

        void onStanzaReceived(Stanza stanza);
    }
}