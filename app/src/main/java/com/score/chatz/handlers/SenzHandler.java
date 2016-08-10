package com.score.chatz.handlers;

import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteConstraintException;
import android.os.RemoteException;
import android.util.Log;

import com.score.chatz.R;
import com.score.chatz.db.SenzorsDbSource;
import com.score.chatz.listeners.ShareSenzListener;
import com.score.chatz.services.LocationService;
import com.score.chatz.services.SenzServiceConnection;
import com.score.chatz.utils.NotificationUtils;
import com.score.chatz.utils.SenzParser;
import com.score.senz.ISenzService;
import com.score.senzc.enums.SenzTypeEnum;
import com.score.senzc.pojos.Senz;
import com.score.senzc.pojos.User;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.util.HashMap;


/**
 * Handle All senz messages from here
 * <p>
 * SENZ RECEIVERS
 * <p>
 * 1. SENZ_SHARE
 */
public class SenzHandler {
    private static final String TAG = SenzHandler.class.getName();

    private static Context context;
    private static ShareSenzListener listener;

    private static SenzHandler instance;

    private static SenzServiceConnection serviceConnection;
    private static SenzorsDbSource dbSource;

    private SenzHandler() {
    }

    /*
    public static SenzHandler getInstance(Context context, ShareSenzListener listener) {
        if (instance == null) {
            instance = new SenzHandler();
            SenzHandler.context = context;
            SenzHandler.listener = listener;
        }
        return instance;
    }*/


    public static SenzHandler getInstance(Context context) {
        if (instance == null) {
            instance = new SenzHandler();
            SenzHandler.context = context.getApplicationContext();

            serviceConnection = new SenzServiceConnection(context);
            dbSource = new SenzorsDbSource(context);

            // bind to senz service
            Intent serviceIntent = new Intent();
            //serviceIntent.setClassName("com.score.senz", "com.score.senz.services.RemoteSenzService");
            serviceIntent.setClassName("com.score.chatz", "com.score.chatz.services.RemoteSenzService");
            SenzHandler.context.bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
        }

        return instance;
    }

    public void handleSenz(String senzMessage) {
        // parse and verify senz
        try {
            Senz senz = SenzParser.parse(senzMessage);
            verifySenz(senz);
            switch (senz.getSenzType()) {
                case PING:
                    Log.d(TAG, "PING received");
                    break;
                case SHARE:
                    Log.d(TAG, "SHARE received");
                        Log.d(TAG, "#lat #lon SHARE received");
                        handleShareSenz(senz);
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
        } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
            e.printStackTrace();
        }
    }

    private static void verifySenz(Senz senz) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        senz.getSender();

        // TODO get public key of sender
        // TODO verify signature of the senz
        //RSAUtils.verifyDigitalSignature(senz.getPayload(), senz.getSignature(), null);
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
                dbSource.createPermissionsForUser(senz);
                senz.setSender(sender);

                Log.d(TAG, "save senz");

                // if senz already exists in the db, SQLiteConstraintException should throw
                try {
                    dbSource.createSenz(senz);
                    NotificationUtils.showNotification(context, context.getString(R.string.new_senz), "LocationZ received from @" + senz.getSender().getUsername());
                    shareBackToUser(senzService, sender, true);
                    handleDataChanges(senz);
                } catch (SQLiteConstraintException e) {
                    shareBackToUser(senzService, sender, false);
                    Log.e(TAG, e.toString());
                }
            }
        });


        /*Intent intent = new Intent("com.score.chatz.SENZ_SHARE");
        intent.putExtra("SENZ", senz);
        context.sendBroadcast(intent);*/
    }

    private void shareBackToUser(ISenzService senzService, User receiver, boolean isDone) {
        Log.d(TAG, "send response");
        try {
            // create senz attributes
            HashMap<String, String> senzAttributes = new HashMap<>();
            senzAttributes.put("time", ((Long) (System.currentTimeMillis() / 1000)).toString());
            if (isDone) {
                senzAttributes.put("msg", "ShareDone");
                senzAttributes.put("lat", "lat");
                senzAttributes.put("lon", "lon");
            } else {
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
        //Log.d(TAG, "Nothing to handler data senz from here, already broadcasted");
        //Specific case, when the app needs to be listening for chatz incoming messages
        /*if (senz.getAttributes().containsKey("chatzmsg")) {
            Log.d(TAG, "save incoming chatz");
            String msg = senz.getAttributes().get("chatzmsg");
            Message newMessage = new Message(msg, senz.getSender(), false);
            dbSource.createChatz(newMessage);
            ChatMessagesListFragment.addMessage(newMessage);

        }*/


        //Add permission to db


        //Add messages to db


        // we broadcast data senz



        if (senz.getAttributes().containsKey("msg") && senz.getAttributes().get("msg").equalsIgnoreCase("ShareDone")) {
            //Add New User
            // save senz in db
            User sender = dbSource.getOrCreateUser(senz.getSender().getUsername());
            dbSource.createPermissionsForUser(senz);
            senz.setSender(sender);
            Log.d(TAG, "save senz");
            // if senz already exists in the db, SQLiteConstraintException should throw
            try {
                dbSource.createSenz(senz);
                NotificationUtils.showNotification(context, context.getString(R.string.new_senz), "Permission to share with @" + senz.getSender().getUsername());
            } catch (SQLiteConstraintException e) {
                Log.e(TAG, e.toString());
            }
            Intent intent = new Intent("com.score.chatz.DATA_SENZ");
            intent.putExtra("SENZ", senz);
            context.sendBroadcast(intent);
        } else if (senz.getAttributes().containsKey("msg") && senz.getAttributes().get("msg").equalsIgnoreCase("newPerm")) {
            //Add New Permission
            handleSharedPermission(senz);
            Intent intent = new Intent("com.score.chatz.DATA_SENZ");
            intent.putExtra("SENZ", senz);
            context.sendBroadcast(intent);
        } else if (senz.getAttributes().containsKey("msg") && senz.getAttributes().get("msg").equalsIgnoreCase("sharePermDone")) {
            //Add New Permission

            if(senz.getAttributes().containsKey("msg") && senz.getAttributes().get("msg").equalsIgnoreCase("sharePermDone")){
                // save senz in db
                User sender = dbSource.getOrCreateUser(senz.getSender().getUsername());
                senz.setSender(sender);
                Log.d(TAG, "save user permission");
                try {
                    dbSource.updateConfigurablePermissions(senz.getSender(), senz.getAttributes().get("camPerm"), senz.getAttributes().get("locPerm"));
                } catch (SQLiteConstraintException e) {
                    Log.e(TAG, e.toString());
                }
                //Indicate to the user if success
            }else{
                //Indicate to the user if fail
            }

        } else{
            //Registration
            Intent intent = new Intent("com.score.chatz.DATA_SENZ");
            intent.putExtra("SENZ", senz);
            context.sendBroadcast(intent);
        }

        //Update app for availability of new data
        handleDataChanges(senz);

    }

    private void handleSharedPermission(final Senz senz){
        //call back after service bind
        serviceConnection.executeAfterServiceConnected(new Runnable() {
            @Override
            public void run() {
                // service instance
                ISenzService senzService = serviceConnection.getInterface();

                // save senz in db
                SenzorsDbSource dbSource = new SenzorsDbSource(context);
                User sender = dbSource.getOrCreateUser(senz.getSender().getUsername());

                if(senz.getAttributes().containsKey("locPerm")){
                    dbSource.updatePermissions(senz.getSender(), null, senz.getAttributes().get("locPerm"));
                }
                if(senz.getAttributes().containsKey("camPerm")) {
                    dbSource.updatePermissions(senz.getSender(), senz.getAttributes().get("camPerm"), null);
                }

                senz.setSender(sender);
                Log.d(TAG, "save permissions");
                // if senz already exists in the db, SQLiteConstraintException should throw
                try {
                    NotificationUtils.showNotification(context, context.getString(R.string.new_senz), "New permissions received from @" + senz.getSender().getUsername());
                    sharePermBackToUser(senzService, senz, sender, true);
                    handleDataChanges(senz);
                } catch (SQLiteConstraintException e) {
                    sharePermBackToUser(senzService, senz, sender, false);
                    Log.e(TAG, e.toString());
                }
            }
        });
    }

    private void sharePermBackToUser(ISenzService senzService, Senz senz, User receiver, boolean isDone){
        Log.d(TAG, "send response");
        try {
            // create senz attributes
            HashMap<String, String> senzAttributes = new HashMap<>();
            senzAttributes.put("time", ((Long) (System.currentTimeMillis() / 1000)).toString());
            if (isDone) {
                senzAttributes.put("msg", "SharePermDone");
                if(senz.getAttributes().containsKey("locPerm")){
                    senzAttributes.put("locPerm", senz.getAttributes().get("locPerm"));
                }
                if(senz.getAttributes().containsKey("camPerm")) {
                    senzAttributes.put("camPerm", senz.getAttributes().get("camPerm"));
                }

            } else {
                senzAttributes.put("msg", "ShareFail");
            }

            String id = "_ID";
            String signature = "";
            SenzTypeEnum senzType = SenzTypeEnum.DATA;
            Senz newSenz = new Senz(id, signature, senzType, null, receiver, senzAttributes);

            senzService.send(newSenz);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void handleDataChanges(Senz senz) {
        Intent intent = new Intent("com.score.chatz.USER_UPDATE");
        intent.putExtra("SENZ", senz);
        context.sendBroadcast(intent);
    }

    private void handleNewPermission(Senz senz) {

        // save senz in db
        SenzorsDbSource dbSource = new SenzorsDbSource(context);
        User sender = dbSource.getOrCreateUser(senz.getSender().getUsername());
        senz.setSender(sender);
        Log.d(TAG, "save senz");
        // if senz already exists in the db, SQLiteConstraintException should throw
        try {
            dbSource.updatePermissions(senz.getSender(), senz.getAttributes().get("camPerm"), senz.getAttributes().get("locPerm"));
            NotificationUtils.showNotification(context, context.getString(R.string.new_senz), "Permission updated by @" + senz.getSender().getUsername());
            //shareBackToUser(senzService, sender, true);
        } catch (SQLiteConstraintException e) {
            //shareBackToUser(senzService, sender, false);
            Log.e(TAG, e.toString());
        }

        handleDataChanges(senz);
    }


}

