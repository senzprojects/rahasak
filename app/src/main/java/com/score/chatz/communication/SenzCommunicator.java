package com.score.chatz.communication;

import android.app.Dialog;
import android.content.ComponentName;
import android.content.ServiceConnection;
import android.graphics.Typeface;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.score.chatz.R;
import com.score.chatz.utils.ActivityUtils;
import com.score.senz.ISenzService;

/**
 * Created by lakmalcaldera on 7/3/16.
 */
public class SenzCommunicator {
    private static SenzCommunicator ourInstance = new SenzCommunicator();
    // use to track share timeout
    private SenzCountDownTimer senzCountDownTimer = new SenzCountDownTimer(16000, 5000);
    private boolean isResponseReceived = false;



    public static SenzCommunicator getInstance() {
        return ourInstance;
    }

    private SenzCommunicator() {
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
                //share();
                //Log.d(TAG, "Response not received yet");
            }
        }

        @Override
        public void onFinish() {
            // display message dialog that we couldn't reach the user
            if (!isResponseReceived) {
                //String user = usernameEditText.getText().toString().trim();
                //String message = "<font color=#000000>Seems we couldn't reach the user </font> <font color=#eada00>" + "<b>" + user + "</b>" + "</font> <font color=#000000> at this moment</font>";
                //displayInformationMessageDialog("#Share Fail", message);
            }
        }
    }



    /**
     * Display message dialog when user request(click) to delete invoice
     *
     * @param message message to be display
     */
    public void displayInformationMessageDialog(String title, String message) {
        /*final Dialog dialog = new Dialog(getActivity());

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

        dialog.show();*/
    }
}
