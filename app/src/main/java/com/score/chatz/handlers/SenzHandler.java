package com.score.chatz.handlers;

import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteConstraintException;
import android.os.RemoteException;
import android.util.Base64;
import android.util.Log;

import com.score.chatz.R;
import com.score.chatz.db.SenzorsDbSource;
import com.score.chatz.pojo.Secret;
import com.score.chatz.pojo.SenzStream;
import com.score.chatz.services.LocationService;
import com.score.chatz.services.SenzServiceConnection;
import com.score.chatz.ui.PhotoActivity;
import com.score.chatz.utils.NotificationUtils;
import com.score.chatz.utils.SenzParser;
import com.score.senz.ISenzService;
import com.score.senzc.enums.SenzTypeEnum;
import com.score.senzc.pojos.Senz;
import com.score.senzc.pojos.User;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.util.ArrayList;
import java.util.HashMap;


/**
 * Handle All senz messages from here
 * <p/>
 * SENZ RECEIVERS
 * <p/>
 * 1. SENZ_SHARE
 */
public class SenzHandler {
    private static final String TAG = SenzHandler.class.getName();

    private static Context context;
    private static SenzHandler instance;
    private static SenzServiceConnection serviceConnection;
    private static SenzorsDbSource dbSource;

    private SenzStream senzStream;

    private SenzHandler() {
    }

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

        /*
         *  Is called when you receive an invitation/share from another person
         */

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
                    NotificationUtils.showNotification(context, context.getString(R.string.new_senz), "You have received an invitation from @" + senz.getSender().getUsername());
                    shareBackToUser(senzService, sender, true);
                    handleDataChanges(senz);
                } catch (SQLiteConstraintException e) {
                    shareBackToUser(senzService, sender, false);
                    Log.e(TAG, e.toString());
                }
            }
        });

    }

    private void shareBackToUser(ISenzService senzService, User receiver, boolean isDone) {
        Log.d(TAG, "send response");

        /*
         * After receive invitation from someone, you send back a share message to the user, with your permissions.
         */

        try {
            // create senz attributes
            HashMap<String, String> senzAttributes = new HashMap<>();
            senzAttributes.put("time", ((Long) (System.currentTimeMillis() / 1000)).toString());
            if (isDone) {
                senzAttributes.put("msg", "ShareDone");
                senzAttributes.put("lat", "lat");
                senzAttributes.put("lon", "lon");
                // Switch handles the sharing of all attributes
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

    private void handleGetSenz(final Senz senz) {
        Log.d(TAG, senz.getSender() + " : " + senz.getSenzType().toString());

        /*
         * Requesting your camra
         * Launch camera activity
         */


        if (senz.getAttributes().containsKey("chatzphoto")) {

            Intent intent = new Intent();
            intent.setClass(context, PhotoActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            //To pass:
            intent.putExtra("Senz", senz);
            context.startActivity(intent);

        } else if (senz.getAttributes().containsKey("lat") && senz.getAttributes().containsKey("lon")) {
            Intent serviceIntent = new Intent(context, LocationService.class);
            serviceIntent.putExtra("USER", senz.getSender());
            context.startService(serviceIntent);
        }
    }

    private void handleDataSenz(Senz senz) {

        if (senz.getAttributes().containsKey("msg") && senz.getAttributes().get("msg").equalsIgnoreCase("ShareDone")) {
            /*
             * Share message from other user, to whom you send a share to.
             */
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
            /*
             * New access permission you received from an a friend
             * Send permission accepted message to indicate to other user, update was succesfull
             */
            //Add New Permission
            handleSharedPermission(senz);
            Intent intent = new Intent("com.score.chatz.DATA_SENZ");
            intent.putExtra("SENZ", senz);
            context.sendBroadcast(intent);
        } else if (senz.getAttributes().containsKey("msg") && senz.getAttributes().get("msg").equalsIgnoreCase("sharePermDone")) {
            /*
             * New permission, set a user, is notified to be updated correctly, success message.
             */
            //Add New Permission
            if (senz.getAttributes().containsKey("msg") && senz.getAttributes().get("msg").equalsIgnoreCase("sharePermDone")) {
                // save senz in db
                User sender = dbSource.getOrCreateUser(senz.getSender().getUsername());
                senz.setSender(sender);
                Log.d(TAG, "save user permission");
                try {
                    dbSource.updateConfigurablePermissions(senz.getSender(), senz.getAttributes().get("camPerm"), senz.getAttributes().get("locPerm"));
                } catch (SQLiteConstraintException e) {
                    Log.e(TAG, e.toString());
                }
                //TODO Indicate to the user if success
            } else {
                //TODO Indicate to the user if fail
            }
        } else if (senz.getAttributes().containsKey("chatzmsg")) {
            /*
             * Any new chatz message, incoming, save straight to db
             */
            //Add chat message
            handleSharedMessages(senz);
            Intent intent = new Intent("com.score.chatz.DATA_SENZ");
            intent.putExtra("SENZ", senz);
            context.sendBroadcast(intent);

        } else if (senz.getAttributes().containsKey("stream")) {
            // handle streaming
            if (senz.getAttributes().get("stream").equalsIgnoreCase("ON")) {
                Log.d(TAG, "Stream ON from " + senz.getSender().getUsername());

                senzStream = new SenzStream(true, senz.getSender().getUsername(), new StringBuffer());
            } else if (senz.getAttributes().get("stream").equalsIgnoreCase("OFF")) {
                // stream off
                senzStream.setIsActive(false);

                Log.d(TAG, "Stream OFF from " + senz.getSender().getUsername());

                // save stream to db
                try {
                    Log.i(TAG, "SENDER OF PHOTO : " + senz.getSender());
                    dbSource.createSecret(new Secret(null, senzStream.getStream(), senz.getSender(), senz.getReceiver()));
                } catch (SQLiteConstraintException e) {
                    Log.e(TAG, e.toString());
                }

                // broadcast
                Intent intent = new Intent("com.score.chatz.DATA_SENZ");
                intent.putExtra("SENZ", senz);
                context.sendBroadcast(intent);
            }
        } else if (senz.getAttributes().containsKey("chatzphoto")) {
            if (senzStream != null && senzStream.isActive()) {
                // streaming ON
                Log.d(TAG, "Stream ON, chatzphoto received from " + senz.getSender().getUsername());
                senzStream.putStream(senz.getAttributes().get("chatzphoto"));
            } else {
                Log.e(TAG, "Stream OFF, chatzphoto received from " + senz.getSender().getUsername());
            }
        } else {
            /*
             * Default cases, handle such as registration success or, any other scenarios where need to specifically handle in the Activity.
             */
            Intent intent = new Intent("com.score.chatz.DATA_SENZ");
            intent.putExtra("SENZ", senz);
            context.sendBroadcast(intent);
        }

        /*
         * The following method is used to notify all list view to update.
         */
        handleDataChanges(senz);

    }

    private void handleSharedPermission(final Senz senz) {
        //call back after service bind
        serviceConnection.executeAfterServiceConnected(new Runnable() {
            @Override
            public void run() {
                // service instance
                ISenzService senzService = serviceConnection.getInterface();

                // save senz in db
                User sender = dbSource.getOrCreateUser(senz.getSender().getUsername());
                try {
                    SenzorsDbSource dbSource = new SenzorsDbSource(context);

                    if (senz.getAttributes().containsKey("locPerm")) {
                        dbSource.updatePermissions(senz.getSender(), null, senz.getAttributes().get("locPerm"));
                    }
                    if (senz.getAttributes().containsKey("camPerm")) {
                        dbSource.updatePermissions(senz.getSender(), senz.getAttributes().get("camPerm"), null);
                    }

                    senz.setSender(sender);
                    Log.d(TAG, "save permissions");
                    // if senz already exists in the db, SQLiteConstraintException should throw

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

    private void sharePermBackToUser(ISenzService senzService, Senz senz, User receiver, boolean isDone) {
        Log.d(TAG, "send response(shareback) for permission");
        try {
            // create senz attributes
            HashMap<String, String> senzAttributes = new HashMap<>();
            senzAttributes.put("time", ((Long) (System.currentTimeMillis() / 1000)).toString());
            if (isDone) {
                senzAttributes.put("msg", "SharePermDone");
                if (senz.getAttributes().containsKey("locPerm")) {
                    senzAttributes.put("locPerm", senz.getAttributes().get("locPerm"));
                }
                if (senz.getAttributes().containsKey("camPerm")) {
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

    private void handleSharedMessages(final Senz senz) {
        //call back after service bind
        serviceConnection.executeAfterServiceConnected(new Runnable() {
            @Override
            public void run() {
                // service instance
                ISenzService senzService = serviceConnection.getInterface();

                // save senz in db
                User sender = dbSource.getOrCreateUser(senz.getSender().getUsername());
                try {
                    SenzorsDbSource dbSource = new SenzorsDbSource(context);

                    Log.d(TAG, "save incoming chatz");
                    String msg = senz.getAttributes().get("chatzmsg");
                    Secret newSecret = new Secret(msg, null, senz.getSender(), senz.getReceiver());
                    dbSource.createSecret(newSecret);

                    senz.setSender(sender);
                    Log.d(TAG, "save messages");
                    // if senz already exists in the db, SQLiteConstraintException should throw

                    NotificationUtils.showNotification(context, context.getString(R.string.new_senz), "New message received from @" + senz.getSender().getUsername());
                    shareMessageBackToUser(senzService, senz, sender, senz.getReceiver(), true);
                    handleDataChanges(senz);
                } catch (SQLiteConstraintException e) {
                    shareMessageBackToUser(senzService, senz, sender, senz.getReceiver(), false);
                    Log.e(TAG, e.toString());
                }
            }
        });
    }

    private void shareMessageBackToUser(ISenzService senzService, Senz senz, User sender, User receiver, boolean isDone) {
        Log.d(TAG, "send response(share back) for message");

        try {
            // create senz attributes
            HashMap<String, String> senzAttributes = new HashMap<>();
            senzAttributes.put("time", ((Long) (System.currentTimeMillis() / 1000)).toString());
            if (isDone) {
                senzAttributes.put("msg", "MsgSent");
            } else {
                senzAttributes.put("msg", "MsgSentFail");
            }
            // new senz
            String id = "_ID";
            String signature = "_SIGNATURE";
            SenzTypeEnum senzType = SenzTypeEnum.DATA;
            Senz _senz = new Senz(id, signature, senzType, receiver, sender, senzAttributes);

            senzService.send(_senz);
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


    public void sendPhoto(final byte[] image, final Senz senz) {
        serviceConnection.executeAfterServiceConnected(new Runnable() {
            @Override
            public void run() {
                // service instance
                ISenzService senzService = serviceConnection.getInterface();
                Log.d(TAG, "send response(share back) for photo : " + image);

                Log.i(TAG, "USER INFO - senz.getSender() : " + senz.getSender().getUsername() + ", senz.getReceiver() : " + senz.getReceiver().getUsername());
                try {
//                    Senz _senz = getPhotoStreamingSenz(senz, image); //Swapped senz, passing original senz
//                    startPhotoSharing(senz, senzService);
//                    senzService.send(_senz);
//                    stopPhotoSharing(senz, senzService);

                    // compose senzes
                    Senz startSenz = getStartPhotoSharingSenze(senz);
                    //senzService.send(startSenz);

                    Senz photoSenz = getPhotoStreamingSenz(senz, image);
                    //senzService.send(photoSenz);

                    Senz stopSenz = getStopPhotoSharingSenz(senz);
                    //senzService.send(stopSenz);


                    ArrayList<Senz> senzList = new ArrayList<Senz>();
                    senzList.add(startSenz);
                    senzList.add(photoSenz);
                    senzList.add(stopSenz);
                    senzService.sendInOrder(senzList);


                } catch (RemoteException e) {
                    e.printStackTrace();
                }


            }
        });


    }


    private Senz getPhotoStreamingSenz(Senz senz, byte[] image) {
        // create senz attributes
        HashMap<String, String> senzAttributes = new HashMap<>();
        senzAttributes.put("time", ((Long) (System.currentTimeMillis() / 1000)).toString());
        String imageAsString = Base64.encodeToString(image, Base64.DEFAULT);
        senzAttributes.put("chatzphoto", imageAsString);


        //Save photo to db before sending
        new SenzorsDbSource(context).createSecret(new Secret(null, imageAsString, senz.getSender(), senz.getReceiver()));
        // new senz
        String id = "_ID";
        String signature = "_SIGNATURE";
        SenzTypeEnum senzType = SenzTypeEnum.DATA;
        Senz _senz = new Senz(id, signature, senzType, senz.getReceiver(), senz.getSender(), senzAttributes);
        return _senz;
    }


    private Senz getStartPhotoSharingSenze(Senz senz) {
        //senz is the original senz
        // create senz attributes
        HashMap<String, String> senzAttributes = new HashMap<>();
        senzAttributes.put("time", ((Long) (System.currentTimeMillis() / 1000)).toString());
        senzAttributes.put("stream", "on");

        // new senz
        String id = "_ID";
        String signature = "_SIGNATURE";
        SenzTypeEnum senzType = SenzTypeEnum.DATA;
        Log.i(TAG, "Senz receiver - " + senz.getReceiver());
        Log.i(TAG, "Senz sender - " + senz.getSender());
        Senz _senz = new Senz(id, signature, senzType, senz.getReceiver(), senz.getSender(), senzAttributes);
        return _senz;
    }


    private Senz getStopPhotoSharingSenz(Senz senz) {
        // create senz attributes
        //senz is the original senz
        HashMap<String, String> senzAttributes = new HashMap<>();
        senzAttributes.put("time", ((Long) (System.currentTimeMillis() / 1000)).toString());
        senzAttributes.put("stream", "off");

        // new senz
        String id = "_ID";
        String signature = "_SIGNATURE";
        SenzTypeEnum senzType = SenzTypeEnum.DATA;
        Senz _senz = new Senz(id, signature, senzType, senz.getReceiver(), senz.getSender(), senzAttributes);
        return _senz;
    }

}

