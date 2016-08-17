package com.score.chatz.services;

/**
 * Created by Lakmal on 8/4/16.
 */
import android.util.Log;

import com.score.chatz.utils.SenzParser;
import com.score.senzc.pojos.Senz;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;

public class TCPClient {

    private static final String TAG = TCPClient.class.getName();
    private String serverMessage;
    //public static final String SERVERIP = "52.5.201.100";
    private static final String SERVERIP = "192.168.1.102";
    public static final int SERVERPORT = 7070;
    private OnMessageReceived mMessageListener = null;
    private boolean mRun = false;
    private boolean isStreaming = false;
    private StringBuilder bufferedData = null;


    PrintWriter out;
    BufferedReader in;
    Socket socket;

    /**
     *  Constructor of the class. OnMessagedReceived listens for the messages received from server
     */
    public TCPClient(OnMessageReceived listener) {
        mMessageListener = listener;
    }

    /**
     * Sends the message entered by client to the server
     * @param message text entered by client
     */
    public void sendMessage(String message){
        if (out != null && !out.checkError() && message != null) {

            Log.i(TAG, "MESSAGE : " + message);

            out.println(message);
            out.flush();

        }
    }


    public void run() {

        mRun = true;

        try {
            //here you must put your computer's IP address.
            InetAddress serverAddr = InetAddress.getByName(SERVERIP);

            Log.e(TAG, "C: Connecting...");

            //create a socket to make the connection with the server
            socket = new Socket(serverAddr, SERVERPORT);
            try {

                //send the message to the server
                out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
                Log.e(TAG, "Client: Sent.");
                Log.e(TAG, "Client: Done.");
                mMessageListener.onConnectionEstablished();

                //receive the message which the server sends back
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                //in this while the client listens for the messages sent by the server

                while (mRun) {
                    //serverMessage = in.readLine().replaceAll("\n", "").replaceAll("\n", "");
                    serverMessage = in.readLine();

                    //Log.i(TAG, serverMessage);
                    //Log.i(TAG, "----------");

                    Senz senz = null;
                    try {
                        senz = SenzParser.parse(serverMessage);
                    }catch(Exception ex){
                        //parse error
                        // streaming
                    }

                    if(senz != null && senz.getAttributes().containsKey("stream") && senz.getAttributes().get("stream").equalsIgnoreCase("on")){
                        isStreaming = true;
                        bufferedData = new StringBuilder();
                    }else if(senz != null && (senz.getAttributes().containsKey("stream") && senz.getAttributes().get("stream").equalsIgnoreCase("off"))){
                        //Continue flow, end of stream
                        isStreaming = false;
                        serverMessage = bufferedData.toString();
                    }


                    if(isStreaming == true){
                        if(serverMessage != null && !serverMessage.isEmpty())
                            bufferedData.append(serverMessage);
                        continue;
                    }



                    if (serverMessage != null && !serverMessage.isEmpty() && mMessageListener != null) {
                        //call the method messageReceived from MyActivity class

                        mMessageListener.onMessageReceived(serverMessage);
                    }
                    serverMessage = null;

                }

                Log.e(TAG, "Server: Received Message: '" + serverMessage + "'");

            } catch (Exception e) {

                Log.e(TAG, "Server: Error", e);


            } finally {
                //the socket must be closed. It is not possible to reconnect to this socket
                // after it is closed, which means a new socket instance has to be created.
                socket.close();
                mMessageListener.onConnectionDropped();
            }

        } catch (Exception e) {
            mMessageListener.onConnectionDropped();
            Log.e(TAG, "Client: Error", e);

        }

    }

    public void closeSocketAndClean(){
        mRun = false;
        if(socket != null && !socket.isClosed()){
            try {
                socket.close();
                in.close();
                out.close();
            }catch (IOException e){
                Log.e(TAG, "Client: Socket closing Error: ", e);
            }
        }
    }

    //Declare the interface. The method messageReceived(String message) will must be implemented in the MyActivity
    //class at on asynckTask doInBackground
    public interface OnMessageReceived {
        public void onMessageReceived(String message);
        public void onConnectionDropped();
        public void onConnectionEstablished();
    }
}