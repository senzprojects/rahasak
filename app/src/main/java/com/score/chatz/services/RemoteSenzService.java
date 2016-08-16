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
import android.os.AsyncTask;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.score.senz.ISenzService;
import com.score.senzc.enums.SenzTypeEnum;
import com.score.senzc.pojos.Senz;
import com.score.senzc.pojos.User;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.util.HashMap;

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
import android.util.Log;
import android.widget.Toast;

import com.score.chatz.exceptions.NoUserException;
import com.score.chatz.handlers.SenzHandler;
import com.score.chatz.listeners.ShareSenzListener;
//import com.score.chatz.receivers.AlarmReceiver;
import com.score.chatz.utils.NetworkUtil;
import com.score.chatz.utils.PreferenceUtils;
import com.score.chatz.utils.RSAUtils;
import com.score.chatz.utils.SenzParser;
import com.score.senz.ISenzService;
import com.score.senzc.enums.SenzTypeEnum;
import com.score.senzc.pojos.Senz;
import com.score.senzc.pojos.User;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.util.HashMap;
import java.util.List;

/**
 * Remote service which handles UDP related functions
 *
 * @author eranga herath(erangaeb@gamil.com)
 */
public class RemoteSenzService extends Service implements ShareSenzListener {

    private static final String TAG = RemoteSenzService.class.getName();

    // we are listing for UDP socket
    //private DatagramSocket socket;
    private Socket socket;

    // server address
    private InetAddress address;


    // broadcast receiver to check network status changes
    private final BroadcastReceiver networkStatusReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //should check null because in air plan mode it will be null
            if (NetworkUtil.isAvailableNetwork(getApplicationContext())) {
                // send ping from here
                Log.d(TAG, "Network status changed: AVAILABLE");
                reinitiateTCP();
            }else{

                Log.d(TAG, "Network status changed: NOT AVAILABLE");
            }
        }
    };

    /*
    // broadcast receiver to send ping message
    private final BroadcastReceiver pingAlarmReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "Ping alarm received in senz service");
            sendPingMessage();
        }
    };
    */


    // API end point of this service, we expose the endpoints define in ISenzService.aidl
    private final ISenzService.Stub apiEndPoints = new ISenzService.Stub() {
        @Override
        public String getUser() throws RemoteException {
            return null;
        }

        @Override
        public void send(Senz senz) throws RemoteException {
            Log.d(TAG, "Senz service call with senz " + senz.getId());
            sendSenzMessage(senz);
        }

        @Override
        public void sendInOrder(List<Senz> senzList) throws RemoteException {
            sendSenz(senzList);
        }
    };

    /**
     * {@inheritDoc}
     */
    @Override
    public IBinder onBind(Intent intent) {
        return apiEndPoints;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreate() {
        // Register network status receiver
        IntentFilter filter = new IntentFilter();
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(networkStatusReceiver, filter);

        /*
        // register ping alarm receiver
        IntentFilter alarmFilter = new IntentFilter();
        alarmFilter.addAction("PING_ALARM");
        registerReceiver(pingAlarmReceiver, alarmFilter);
        */
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Start service");
        //initUdpSocket();
        //initPingSender();
        //initUdpListener();
        //initTCPConnection();
        if(NetworkUtil.isAvailableNetwork(getApplicationContext())) {
            if (conn == null || mTcpClient == null) {
                reinitiateTCP();
            }
        }else{
            Toast.makeText(getApplicationContext(), "No network connection available", Toast.LENGTH_LONG).show();
        }


        // If we get killed, after returning from here, restart
        return START_STICKY;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onDestroy() {
        Log.d(TAG, "Destroyed");

        // unregister connectivity listener
        unregisterReceiver(networkStatusReceiver);

        // un register ping receiver
        //unregisterReceiver(pingAlarmReceiver);

        // restart service again
        // its done via broadcast receiver
        Intent intent = new Intent("com.score.senz.senzservice");
        sendBroadcast(intent);
    }



    /**
     * Start thread to send initial PING messages to server
     * We initialize UDP connection from here
    private void initUdpSender() {
        if (socket != null) {
            new Thread(new Runnable() {
                public void run() {
                    sendPingMessage();
                }
            }).start();
        } else {
            Log.e(TAG, "Socket not connected");
        }
    }
     */



    /**
     * Start thread to send PING message to server in every 28 minutes

    private void initPingSender() {
        // register ping alarm receiver
        Intent intentAlarm = new Intent(this, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intentAlarm, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), AlarmManager.INTERVAL_FIFTEEN_MINUTES, pendingIntent);
    }
     */




    /**
     * Send ping message to server
     */
    private void sendPingMessage() {
        new Thread(new Runnable() {
            public void run() {
                if (NetworkUtil.isAvailableNetwork(RemoteSenzService.this)) {


                    try {
                        PreferenceUtils.getUser(getApplicationContext());
                    }catch (NoUserException e){
                        e.printStackTrace();
                        Log.e(TAG, "Cannot send ping, No Registered User");
                        return;
                    }


                    try {
                        String message;
                        PrivateKey privateKey = RSAUtils.getPrivateKey(RemoteSenzService.this);
                        User user = PreferenceUtils.getUser(RemoteSenzService.this);
                        HashMap<String, String> senzAttributes = new HashMap<>();
                        senzAttributes.put("time", ((Long) (System.currentTimeMillis() / 1000)).toString());
                        senzAttributes.put("pubkey", PreferenceUtils.getRsaKey(RemoteSenzService.this, RSAUtils.PUBLIC_KEY));

                        // new senz
                        String id = "_ID";
                        String signature = "";
                        SenzTypeEnum senzType = SenzTypeEnum.SHARE;
                        User sender = new User("", user.getUsername());
                        User receiver = new User("", "senzswitch");
                        Senz senz = new Senz(id, signature, senzType, sender, receiver, senzAttributes);

                        String senzPayload = SenzParser.getSenzPayload(senz);
                        String senzSignature = RSAUtils.getDigitalSignature(senzPayload.replaceAll(" ", ""), privateKey);
                        message = SenzParser.getSenzMessage(senzPayload, senzSignature);



                        Log.d(TAG, "Ping(#SHARE) to be send: " + message);

                        //sends the message to the server
                        if (mTcpClient == null) {
                            reinitiateTCP();
                        }
                        mTcpClient.sendMessage(message);
                    } catch ( NoSuchAlgorithmException | NoUserException | SignatureException | InvalidKeyException | InvalidKeySpecException e) {
                        e.printStackTrace();
                    }
                } else {
                    Log.e(TAG, "Cannot send ping, No connection available");
                }
            }
        }).start();
    }


    private void reinitiateTCP(){
        if (NetworkUtil.isAvailableNetwork(getApplicationContext())) {
            if (mTcpClient != null)
                mTcpClient.closeSocketAndClean();


            conn = new ConnectTask();
            conn.execute("");
        }

    }


    /**
     * Send SenZ message to SenZ service via UDP socket
     * Separate thread used to send messages asynchronously
     *
     * @param senz senz message
     */
    public void sendSenzMessage(final Senz senz) {
        if (NetworkUtil.isAvailableNetwork(RemoteSenzService.this)) {
            new Thread(new Runnable() {
                public void run() {
                    try {
                        /*if(senz.getSenzType() != SenzTypeEnum.SHARE){
                            sendPingMessage();
                        }*/
                        PrivateKey privateKey = RSAUtils.getPrivateKey(RemoteSenzService.this);

                        // if sender not already set find user(sender) and set it to senz first
                        if (senz.getSender() == null || senz.getSender().toString().isEmpty())
                            senz.setSender(PreferenceUtils.getUser(getBaseContext()));

                        // get digital signature of the senz
                        String senzPayload = SenzParser.getSenzPayload(senz);
                        String senzSignature = RSAUtils.getDigitalSignature(senzPayload.replaceAll(" ", ""), privateKey);
                        String message = SenzParser.getSenzMessage(senzPayload, senzSignature);
                        Log.d(TAG, "Senz to be send: " + message);


                        //sends the message to the server
                        if (mTcpClient == null) {
                            reinitiateTCP();
                        }
                        mTcpClient.sendMessage(message);


                    } catch (NoSuchAlgorithmException | InvalidKeySpecException | InvalidKeyException | SignatureException | NoUserException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        } else {
            Log.e(TAG, "Cannot send senz, No connection available");
        }
    }


    public void sendSenz(final List<Senz> senzList) {
        if (NetworkUtil.isAvailableNetwork(RemoteSenzService.this)) {
            new Thread(new Runnable() {
                public void run() {
                    try {

                        PrivateKey privateKey = RSAUtils.getPrivateKey(RemoteSenzService.this);


                        for(Senz senz : senzList) {

                            // if sender not already set find user(sender) and set it to senz first
                            if (senz.getSender() == null || senz.getSender().toString().isEmpty())
                                senz.setSender(PreferenceUtils.getUser(getBaseContext()));

                            // get digital signature of the senz
                            String senzPayload = SenzParser.getSenzPayload(senz);
                            String senzSignature = RSAUtils.getDigitalSignature(senzPayload.replaceAll(" ", ""), privateKey);
                            String message = SenzParser.getSenzMessage(senzPayload, senzSignature);
                            Log.d(TAG, "Senz to be send: " + message);


                            //sends the message to the server
                            if (mTcpClient == null) {
                                reinitiateTCP();
                            }
                            mTcpClient.sendMessage(message);
                            Thread.sleep(2000);
                        }


                    } catch (InterruptedException | NoSuchAlgorithmException | InvalidKeySpecException | InvalidKeyException | SignatureException | NoUserException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        } else {
            Log.e(TAG, "Cannot send senz, No connection available");
        }
    }

    @Override
    public void onShareSenz(Senz senz) {
        sendSenzMessage(senz);
    }



    private TCPClient mTcpClient;
    private ConnectTask conn;
    public class ConnectTask extends AsyncTask<String,String,TCPClient> {


        @Override
        protected TCPClient doInBackground(String... message) {

            //we create a TCPClient object and
            mTcpClient = new TCPClient(new TCPClient.OnMessageReceived() {
                @Override
                //here the messageReceived method is implemented
                public void onMessageReceived(String senz) {
                    Log.e(TAG, "Message from TCP " + senz);
                    SenzHandler.getInstance(getApplicationContext()).handleSenz(senz);
                }

                @Override
                //here the messageReceived method is implemented
                public void onConnectionDropped() {
                    Log.e(TAG, "Connection Dropped. Re-initiating socket.");
                    reinitiateTCP();
                }

                @Override
                //here the messageReceived method is implemented
                public void onConnectionEstablished() {
                    sendPingMessage();
                }


            });
            mTcpClient.run();


            return null;
        }

    }





}






