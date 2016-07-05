package com.score.chatz.ui;


import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.score.chatz.R;
import com.score.chatz.db.SenzorsDbSource;
import com.score.chatz.exceptions.NoUserException;
import com.score.chatz.services.LocationAddressReceiver;
import com.score.chatz.utils.ActivityUtils;
import com.score.chatz.utils.NetworkUtil;
import com.score.chatz.utils.PreferenceUtils;
import com.score.chatz.viewholders.Message;
import com.score.senz.ISenzService;
import com.score.senzc.enums.SenzTypeEnum;
import com.score.senzc.pojos.Senz;
import com.score.senzc.pojos.User;

import java.util.HashMap;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ChatFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ChatFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String TAG = ShareFragment.class.getName();
    private static final String RECEIVER = "RECEIVER";

    // TODO: Rename and change types of parameters
    private String receiver;

    private EditText text_message;
    private Button sendBtn;
    SenzorsDbSource dbSource;
    User currentUser;
    // use to track share timeout
    private SenzCountDownTimer senzCountDownTimer;
    private SenzCountDownTimerForLocation senzCountDownTimerForLocation;
    private boolean isResponseReceived;
    // custom font
    private Typeface typeface;
    boolean isServiceBound = false;

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


    public ChatFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param receiver receiver
     *
     * @return A new instance of fragment ChatFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ChatFragment newInstance(String receiver) {
        ChatFragment fragment = new ChatFragment();
        Bundle args = new Bundle();
        args.putString(RECEIVER, receiver);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            receiver = getArguments().getString(RECEIVER);
        }
        setHasOptionsMenu(true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.chatz_menu, menu);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_location_search:
                    if (NetworkUtil.isAvailableNetwork(getActivity())) {
                        ActivityUtils.showProgressDialog(getActivity(), "Please wait...");
                        senzCountDownTimerForLocation.start();
                    } else {
                        Toast.makeText(getActivity(), "No network connection available", Toast.LENGTH_LONG).show();
                    }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onStart() {
        super.onStart();

        // bind to senz service
        if (!isServiceBound) {
            Intent intent = new Intent();
            intent.setClassName("com.score.senz", "com.score.senz.services.RemoteSenzService");
            getActivity().bindService(intent, senzServiceConnection, Context.BIND_AUTO_CREATE);
            isServiceBound = true;
        }

        getActivity().registerReceiver(senzMessageReceiver, new IntentFilter("com.score.senz.DATA_SENZ"));
    }

    @Override
    public void onStop() {
        super.onStop();

        // Unbind from the service
        if (isServiceBound) {
            getActivity().unbindService(senzServiceConnection);
            isServiceBound = false;
        }

        getActivity().unregisterReceiver(senzMessageReceiver);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat, container, false);
        FragmentManager fm = getActivity().getSupportFragmentManager();
        ChatMessagesFragment chatMessagesFragment = ChatMessagesFragment.newInstance("", "");
        fm.beginTransaction().add(R.id.text_messages_container, chatMessagesFragment).commit();

        dbSource = new SenzorsDbSource(getContext());
        text_message = (EditText) view.findViewById(R.id.text_message);
        sendBtn = (Button) view.findViewById(R.id.sendBtn);
        senzCountDownTimer = new SenzCountDownTimer(16000, 5000);
        senzCountDownTimerForLocation = new SenzCountDownTimerForLocation(16000, 5000);
        isResponseReceived = false;
        typeface = Typeface.createFromAsset(getActivity().getAssets(), "fonts/vegur_2.otf");


        try {
            currentUser = PreferenceUtils.getUser(getContext());
        }catch (NoUserException e) {
            e.printStackTrace();
        }

        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (text_message.getText().toString().trim().isEmpty() != true) {
                    if (NetworkUtil.isAvailableNetwork(getActivity())) {
                    ActivityUtils.showProgressDialog(getActivity(), "Please wait...");
                    senzCountDownTimer.start();
                    InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(getContext().INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(text_message.getWindowToken(), 0);
                    } else {
                        Toast.makeText(getActivity(), "No network connection available", Toast.LENGTH_LONG).show();
                    }
                }
            }
        });

        return view;
    }

    private BroadcastReceiver senzMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "Got message from Senz service");
            handleMessage(intent);
        }
    };



    /**
     * Share current sensor
     * Need to send share query to server via web socket
     */
    private void sendMessage() {
        try {
            // create senz attributes
            HashMap<String, String> senzAttributes = new HashMap<>();
            senzAttributes.put("chatzmsg", text_message.getText().toString());
            senzAttributes.put("time", ((Long) (System.currentTimeMillis() / 1000)).toString());

            // new senz
            String id = "_ID";
            String signature = "_SIGNATURE";
            SenzTypeEnum senzType = SenzTypeEnum.DATA;
            User _receiver = new User("", receiver.trim());
            Senz senz = new Senz(id, signature, senzType, null, _receiver, senzAttributes);

            senzService.send(senz);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }




    /**
     * Keep track with share response timeout
     */
    private class SenzCountDownTimer extends CountDownTimer {

        public SenzCountDownTimer(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onTick(long millisUntilFinished) {
            // if response not received yet, resend share
            if (!isResponseReceived) {
                sendMessage();
                Log.d(TAG, "Response not received yet");
            }
        }

        @Override
        public void onFinish() {
            ActivityUtils.hideSoftKeyboard(getActivity());
            ActivityUtils.cancelProgressDialog();
            // display message dialog that we couldn't reach the user
            if (!isResponseReceived) {
                String message = "<font color=#000000>Seems we couldn't reach the user </font> <font color=#eada00>" + "<b>" + receiver + "</b>" + "</font> <font color=#000000> at this moment</font>";
                displayInformationMessageDialog("#MSG Sending Fail", message);
            }
        }
    }


    /**
     * Keep track with share response timeout
     */
    private class SenzCountDownTimerForLocation extends CountDownTimer {

        public SenzCountDownTimerForLocation(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onTick(long millisUntilFinished) {
            // if response not received yet, resend share
            if (!isResponseReceived) {
                getSenz(new User("", receiver));
                Log.d(TAG, "Response not received yet");
            }
        }

        @Override
        public void onFinish() {
            ActivityUtils.hideSoftKeyboard(getActivity());
            ActivityUtils.cancelProgressDialog();
            // display message dialog that we couldn't reach the user
            if (!isResponseReceived) {
                String message = "<font color=#000000>Seems we couldn't reach the user </font> <font color=#eada00>" + "<b>" + receiver + "</b>" + "</font> <font color=#000000> at this moment</font>";
                displayInformationMessageDialog("#Location Fetching Fail", message);
            }
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
    }


    /**
     * Handle broadcast message receives
     * Need to handle registration success failure here
     *
     * @param intent intent
     */
    private int counterForMSG = 0;
    private int counterForCHATZMSG = 0;
    private void handleMessage(Intent intent) {
        String action = intent.getAction();

        if (action.equalsIgnoreCase("com.score.senz.DATA_SENZ")) {
            Senz senz = intent.getExtras().getParcelable("SENZ");

            if (senz.getAttributes().containsKey("msg")) {
                counterForMSG++;
                if(counterForMSG == 1) {
                    // msg response received
                    ActivityUtils.cancelProgressDialog();
                    isResponseReceived = true;
                    senzCountDownTimer.cancel();
                    isResponseReceived = false;

                    String msg = senz.getAttributes().get("msg");
                    if (msg != null && msg.equalsIgnoreCase("MsgSent")) {
                        // save senz in db
                        Log.d(TAG, "save sent chatz");
                        Message newMessage = new Message(text_message.getText().toString(), currentUser, true);
                        dbSource.createChatz(newMessage);
                        ChatMessagesFragment.addMessage(newMessage);
                        text_message.setText("");
                    } else {
                        String message = "<font color=#000000>Seems we couldn't share the chatz with </font> <font color=#eada00>" + "<b>" + receiver + "</b>" + "</font>";
                        displayInformationMessageDialog("#Share Fail", message);
                    }
                }else if(counterForMSG == 4){
                    counterForMSG = 0;
                }
            }else if (senz.getAttributes().containsKey("chatzmsg")) {
                counterForCHATZMSG++;
                if(counterForCHATZMSG == 1) {
                    Log.d(TAG, "save incoming chatz");
                    String msg = senz.getAttributes().get("chatzmsg");
                    Message newMessage = new Message(msg, senz.getSender(), false);
                    dbSource.createChatz(newMessage);
                    ChatMessagesFragment.addMessage(newMessage);

                    try {
                        // create senz attributes
                        HashMap<String, String> senzAttributes = new HashMap<>();
                        senzAttributes.put("msg", "MsgSent");
                        senzAttributes.put("time", ((Long) (System.currentTimeMillis() / 1000)).toString());

                        // new senz
                        String id = "_ID";
                        String signature = "_SIGNATURE";
                        SenzTypeEnum senzType = SenzTypeEnum.DATA;
                        Senz _senz = new Senz(id, signature, senzType, null, senz.getSender(), senzAttributes);

                        senzService.send(_senz);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }else if(counterForCHATZMSG == 2){
                    counterForCHATZMSG = 0;
                }
            }else if (senz.getAttributes().containsKey("lat")) {
                // location response received
                ActivityUtils.cancelProgressDialog();
                isResponseReceived = true;
                senzCountDownTimerForLocation.cancel();
                isResponseReceived = false;

                double lat = Double.parseDouble(senz.getAttributes().get("lat"));
                double lan = Double.parseDouble(senz.getAttributes().get("lon"));
                LatLng latLng = new LatLng(lat, lan);

                // start location address receiver
                new LocationAddressReceiver(getActivity(), latLng, senz.getSender()).execute("PARAM");

                // start map activity
                Intent mapIntent = new Intent(getActivity(), SenzMapActivity.class);
                mapIntent.putExtra("extra", latLng);
                getActivity().startActivity(mapIntent);
                getActivity().overridePendingTransition(R.anim.right_in, R.anim.stay_in);
            }
        }
    }


    /**
     * Display message dialog when user request(click) to delete invoice
     *
     * @param message message to be display
     */
    public void displayInformationMessageDialog(String title, String message) {
        final Dialog dialog = new Dialog(getActivity());

        //set layout for dialog
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.information_message_dialog);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(true);

        // set dialog texts
        TextView messageHeaderTextView = (TextView) dialog.findViewById(R.id.information_message_dialog_layout_message_header_text);
        TextView messageTextView = (TextView) dialog.findViewById(R.id.information_message_dialog_layout_message_text);
        messageHeaderTextView.setText(title);
        messageTextView.setText(Html.fromHtml(message));

        // set custom font
        messageHeaderTextView.setTypeface(typeface);
        messageTextView.setTypeface(typeface);

        //set ok button
        Button okButton = (Button) dialog.findViewById(R.id.information_message_dialog_layout_ok_button);
        okButton.setTypeface(typeface);
        okButton.setTypeface(null, Typeface.BOLD);
        okButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                dialog.cancel();
            }
        });

        dialog.show();
    }


    /**
     * Send GET senz to service, actual senz sending task done by SenzService
     *
     * @param receiver senz receiver
     */
    private void getSenz(User receiver) {
        try {
            // create senz attributes
            HashMap<String, String> senzAttributes = new HashMap<>();
            senzAttributes.put("time", ((Long) (System.currentTimeMillis() / 1000)).toString());
            senzAttributes.put("lat", "lat");
            senzAttributes.put("lon", "lon");

            // new senz
            String id = "_ID";
            String signature = "_SIGNATURE";
            SenzTypeEnum senzType = SenzTypeEnum.GET;
            Senz senz = new Senz(id, signature, senzType, null, receiver, senzAttributes);

            senzService.send(senz);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}
