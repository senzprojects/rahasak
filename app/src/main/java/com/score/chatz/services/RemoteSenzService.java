package com.score.chatz.services;


import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.score.chatz.exceptions.NoUserException;
import com.score.chatz.handlers.SenzHandler;
import com.score.chatz.receivers.AlarmReceiver;
import com.score.chatz.utils.PreferenceUtils;
import com.score.chatz.utils.RSAUtils;
import com.score.chatz.utils.SenzParser;
import com.score.chatz.utils.SenzUtils;
import com.score.senz.ISenzService;
import com.score.senzc.pojos.Senz;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.util.List;


public class RemoteSenzService extends Service {

    private static final String TAG = RemoteSenzService.class.getName();

    // socket host, port
    public static final String SENZ_HOST = "192.168.1.102";
    //public static final String SENZ_HOST = "udp.mysensors.info";
    public static final int SENZ_PORT = 7070;

    // senz socket
    private Socket socket;
    private BufferedReader reader;
    private PrintWriter writer;

    // API end point of this service, we expose the endpoints define in ISenzService.aidl
    private final ISenzService.Stub apiEndPoints = new ISenzService.Stub() {
        @Override
        public String getUser() throws RemoteException {
            return null;
        }

        @Override
        public void send(Senz senz) throws RemoteException {
            Log.d(TAG, "Senz service call with senz " + senz.getId());
            writeSenz(senz);
        }

        @Override
        public void sendInOrder(List<Senz> senzList) throws RemoteException {
            wirteSenzList(senzList);
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        return apiEndPoints;
    }

    // broadcast receiver to check network status changes
    private final BroadcastReceiver networkStatusReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo netInfo = cm.getActiveNetworkInfo();

            Log.d(TAG, "Network status changed");

            //should check null because in air plan mode it will be null
            if (netInfo != null && netInfo.isConnected()) {
                // send ping
                sendPing();
            }
        }
    };

    // broadcast receiver to send ping message
    private final BroadcastReceiver pingAlarmReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "Ping alarm received in senz service");

            // ping senz
            sendPing();
        }
    };

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate called");

        registerReceivers();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand executed");

        initComm();

        return Service.START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");

        unRegisterReceivers();

        // restart service again
        // its done via broadcast receiver
        Intent intent = new Intent("senz.action.RESTART");
        LocalBroadcastManager.getInstance(RemoteSenzService.this).sendBroadcast(intent);
    }

    private void registerReceivers() {
        // Register network status receiver
        IntentFilter filter = new IntentFilter();
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        LocalBroadcastManager.getInstance(RemoteSenzService.this).registerReceiver(networkStatusReceiver, filter);

        // register local ping alarm receiver
        IntentFilter alarmFilter = new IntentFilter();
        alarmFilter.addAction("PING_ALARM");
        LocalBroadcastManager.getInstance(RemoteSenzService.this).registerReceiver(pingAlarmReceiver, filter);
    }

    private void unRegisterReceivers() {
        // un register receivers
        LocalBroadcastManager.getInstance(RemoteSenzService.this).unregisterReceiver(networkStatusReceiver);
        LocalBroadcastManager.getInstance(RemoteSenzService.this).unregisterReceiver(pingAlarmReceiver);
    }

    private void initPing() {
        // register ping alarm receiver
        Intent intentAlarm = new Intent(this, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intentAlarm, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 1000 * 5 * 60, pendingIntent);
    }

    private void initComm() {
        new Thread(new Runnable() {
            public void run() {
                if (socket == null || !socket.isConnected()) {
                    initSoc();
                    initPing();
                    sendPing();
                    initReader();
                } else {
                    sendPing();
                }
            }
        }).start();
    }

    private boolean initSoc() {
        try {
            socket = new Socket(InetAddress.getByName(SENZ_HOST), SENZ_PORT);
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);

            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }

    private void initReader() {
        try {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.isEmpty()) {
                    String senz = line.replaceAll("\n", "").replaceAll("\r", "");

                    Log.d(TAG, "Senz received " + senz);

                    // handle senz
                    SenzHandler.getInstance(RemoteSenzService.this).handleSenz(senz);
                } else {
                    Log.e(TAG, "empty senz recived");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendPing() {
        Senz senz = SenzUtils.getPingSenz(RemoteSenzService.this);
        if (senz != null) writeSenz(senz);
    }

    private void writeSenz(final Senz senz) {
        new Thread(new Runnable() {
            public void run() {
                //if (socket == null || !socket.isConnected()) initSoc();

                // sign and write senz
                try {
                    PrivateKey privateKey = RSAUtils.getPrivateKey(RemoteSenzService.this);

                    // if sender not already set find user(sender) and set it to senz first
                    if (senz.getSender() == null || senz.getSender().toString().isEmpty())
                        senz.setSender(PreferenceUtils.getUser(getBaseContext()));

                    // get digital signature of the senz
                    String senzPayload = SenzParser.getSenzPayload(senz);
                    String senzSignature = RSAUtils.getDigitalSignature(senzPayload.replaceAll(" ", ""), privateKey);
                    String message = SenzParser.getSenzMessage(senzPayload, senzSignature);
                    Log.d(TAG, "Senz to be send: " + message);

                    writer.println(message);
                    writer.flush();
                } catch (NoSuchAlgorithmException | InvalidKeySpecException | InvalidKeyException | SignatureException | NoUserException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void wirteSenzList(final List<Senz> senzList) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    PrivateKey privateKey = RSAUtils.getPrivateKey(RemoteSenzService.this);

                    for (Senz senz : senzList) {
                        // if sender not already set find user(sender) and set it to senz first
                        if (senz.getSender() == null || senz.getSender().toString().isEmpty())
                            senz.setSender(PreferenceUtils.getUser(getBaseContext()));

                        // get digital signature of the senz
                        String senzPayload = SenzParser.getSenzPayload(senz);
                        String senzSignature;
                        if (senz.getAttributes().containsKey("stream")) {
                            senzSignature = RSAUtils.getDigitalSignature(senzPayload.replaceAll(" ", ""), privateKey);
                        } else {
                            senzSignature = "SIGNATURE";
                        }
                        String message = SenzParser.getSenzMessage(senzPayload, senzSignature);

                        Log.d(TAG, "Senz to be send: " + message);

                        //  sends the message to the server
                        if (socket == null || !socket.isConnected()) initSoc();
                        writer.println(message);
                        writer.flush();

                        Thread.currentThread().sleep(10);
                    }
                } catch (NoSuchAlgorithmException | NoUserException | InvalidKeySpecException | SignatureException | InvalidKeyException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

}


