package com.score.chatz.handlers;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.sqlite.SQLiteConstraintException;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.score.chatz.R;
import com.score.chatz.db.SenzorsDbSource;
import com.score.chatz.services.LocationService;
import com.score.chatz.services.SenzServiceConnection;
import com.score.chatz.utils.NotificationUtils;
import com.score.senz.ISenzService;
import com.score.senzc.enums.SenzTypeEnum;
import com.score.senzc.pojos.Senz;
import com.score.senzc.pojos.User;

import java.util.HashMap;

/**
 * Handle All senz messages from here
 */
public class SenzHandler {
    private static final String TAG = SenzHandler.class.getName();

    private static Context context;

    private static SenzHandler instance;

    private static SenzServiceConnection serviceConnection;

    private SenzHandler() {
    }

    // service interface
    private ISenzService senzService = null;

    // service connection
    private ServiceConnection senzServiceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            Log.d("TAG", "Connected with senz service");
            senzService = ISenzService.Stub.asInterface(service);
        }

        public void onServiceDisconnected(ComponentName className) {
            senzService = null;
            Log.d("TAG", "Disconnected from senz service");
        }
    };


    public static SenzHandler getInstance(Context context) {
        if (instance == null) {
            instance = new SenzHandler();
            SenzHandler.context = context.getApplicationContext();

            // bind to senz service
            Intent serviceIntent = new Intent();
            serviceIntent.setClassName("com.score.senz", "com.score.senz.services.RemoteSenzService");
            SenzHandler.context.bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
        }

        return instance;
    }

    public void handleSenz(Senz senz) {
        switch (senz.getSenzType()) {
            case PING:
                Log.d(TAG, "PING received");
                break;
            case SHARE:
                Log.d(TAG, "SHARE received");
                if (senz.getAttributes().containsKey("lat") || senz.getAttributes().containsKey("lon")) {
                    Log.d(TAG, "#lat #lon SHARE received");
                    handleShareSenz(senz);
                }
                break;
            case GET:
                Log.d(TAG, "GET received");
                handleGetSenz(senz);
                break;
            case DATA:
                Log.d(TAG, "DATA received");
                handleDataSenz(senz);
                break;
        }
    }

    private void handleShareSenz(final Senz senz) {
        Log.d("Tag", senz.getSender() + " : " + senz.getSenzType().toString());

        //call back after service bind
        serviceConnection.executeAfterServiceConnected(new Runnable() {
            @Override
            public void run() {
                // service instance
                ISenzService senzService = serviceConnection.getInterface();

                // save senz in db
                SenzorsDbSource dbSource = new SenzorsDbSource(context);
                User sender = dbSource.getOrCreateUser(senz.getSender().getUsername());
                senz.setSender(sender);

                Log.d(TAG, "save senz");

                // if senz already exists in the db, SQLiteConstraintException should throw
                try {
                    dbSource.createSenz(senz);

                    NotificationUtils.showNotification(context, context.getString(R.string.new_senz), "LocationZ received from @" + senz.getSender().getUsername());
                    sendResponse(senzService, sender, true);
                } catch (SQLiteConstraintException e) {
                    sendResponse(senzService, sender, false);
                    Log.e(TAG, e.toString());
                }
            }
        });
    }



    /**
     * Share current sensor
     * Need to send share query to server via web socket
     */
    private void shareBack(String sender) {
        try {
            // create senz attributes
            HashMap<String, String> senzAttributes = new HashMap<>();
            senzAttributes.put("lat", "lat");
            senzAttributes.put("lon", "lon");
            senzAttributes.put("msg", "msg");
            senzAttributes.put("time", ((Long) (System.currentTimeMillis() / 1000)).toString());

            // new senz
            String id = "_ID";
            String signature = "_SIGNATURE";
            SenzTypeEnum senzType = SenzTypeEnum.SHARE;
            User receiver = new User("", sender);
            Senz senz = new Senz(id, signature, senzType, null, receiver, senzAttributes);

            senzService.send(senz);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }




    private void sendResponse(ISenzService senzService, User receiver, boolean isDone) {
        Log.d(TAG, "send response");
        try {
            // create senz attributes
            HashMap<String, String> senzAttributes = new HashMap<>();
            senzAttributes.put("time", ((Long) (System.currentTimeMillis() / 1000)).toString());
            if (isDone){
                senzAttributes.put("msg", "ShareDone");
                senzAttributes.put("lat", "lat");
                senzAttributes.put("lon", "lon");
            }else {
                senzAttributes.put("msg", "ShareFail");
            }

            String id = "_ID";
            String signature = "";
            SenzTypeEnum senzType = SenzTypeEnum.DATA;
            Senz senz = new Senz(id, signature, senzType, null, receiver, senzAttributes);

            senzService.send(senz);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void handleGetSenz(Senz senz) {
        Log.d(TAG, senz.getSender() + " : " + senz.getSenzType().toString());

        Intent serviceIntent = new Intent(context, LocationService.class);
        serviceIntent.putExtra("USER", senz.getSender());

        context.startService(serviceIntent);
    }

    private void handleDataSenz(Senz senz) {
        // broadcast received senz
        Log.d(TAG, "Nothing to handler data senz from here, already broadcasted");
    }



}
