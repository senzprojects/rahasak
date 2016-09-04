package com.score.chatz.ui;

import android.app.NotificationManager;
import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaPlayer;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.content.Intent;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.Buffer;
import java.nio.ShortBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;
import com.score.chatz.R;
import com.score.chatz.db.SenzorsDbSource;
import com.score.chatz.exceptions.NoUserException;
import com.score.chatz.handlers.SenzHandler;
import com.score.chatz.pojo.Secret;
import com.score.chatz.utils.AudioRecorder;
import com.score.chatz.utils.AudioUtils;
import com.score.chatz.utils.PreferenceUtils;
import com.score.senzc.pojos.User;

public class RecordingActivity extends AppCompatActivity {

    private static final String TAG = RecordingActivity.class.getName();
    private TextView mTimerTextView;
    private long mStartTime = 10;
    private Thread ticker;

    private User sender;
    private User receiver;

    private boolean isRecordingDone;

    SenzorsDbSource dbSource;

    AudioRecorder audioRecorder;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recording);

        setButtonHandlers();
        enableButtons(false);

        this.mTimerTextView = (TextView) this.findViewById(R.id.timer);
        this.mTimerTextView.setText(mStartTime + "");

        Intent intent = getIntent();
        try {
            receiver = PreferenceUtils.getUser(getApplicationContext());
        } catch (NoUserException e) {
            e.printStackTrace();
        }
        String senderString = intent.getStringExtra("SENDER");
        sender = new User("", senderString);

        dbSource = new SenzorsDbSource(this);

        audioRecorder = new AudioRecorder();

        isRecordingDone = false;

        Log.i(TAG, "SENDER FROM RECORDING ACTIVITY - " + sender.getUsername());
        Log.i(TAG, "RECEIVERE FROM RECORDING ACTIVITY - " + receiver.getUsername());
    }

    private void setButtonHandlers() {
        ((Button) findViewById(R.id.btnStart)).setOnClickListener(btnClick);
        ((Button) findViewById(R.id.btnStop)).setOnClickListener(btnClick);
    }

    private void enableButton(int id, boolean isEnable) {
        ((Button) findViewById(id)).setEnabled(isEnable);
    }

    private void enableButtons(boolean isRecording) {
        enableButton(R.id.btnStart, !isRecording);
        enableButton(R.id.btnStop, isRecording);
    }

    private void startRecording() {
        audioRecorder.startRecording(getApplicationContext());
        ticker = new Thread(new Runnable() {
            @Override
            public void run() {
                while (mStartTime > 0) {
                    tick();
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ex) {
                        Log.e(TAG, "Ticker thread interrupted");
                    }
                }
            }
        });
        ticker.start();
    }

    private void stopRecording() {
        // stops the recording activity
        if(isRecordingDone == false) {
            isRecordingDone = true;
            audioRecorder.stopRecording();
            if (audioRecorder.getRecording() != null) {
                Secret secret = getSoundSecret(receiver, sender, Base64.encodeToString(audioRecorder.getRecording().toByteArray(), 0));
                dbSource.createSecret(secret);
                sendSecret(secret);
            }
        }
        this.finish();
    }

    private void sendSecret(Secret secret) {
        SenzHandler.getInstance(this).sendSound(secret);
    }

    private Secret getSoundSecret(User sender, User receiver, String sound) {
        //Swapping receiever and sender here cause we need to send the secret out
        Secret secret = new Secret(null, null, null, receiver, sender);
        secret.setSound(sound);
        return secret;
    }

    private View.OnClickListener btnClick = new View.OnClickListener() {
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btnStart: {
                    enableButtons(true);
                    startRecording();
                    break;
                }
                case R.id.btnStop: {
                    enableButtons(false);
                    stopRecording();
                    break;
                }
            }
        }
    };

    private void tick() {
        try {
            if (mStartTime > 0) {
                mStartTime--;
                this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mTimerTextView.setText(mStartTime + "");
                    }
                });
                if (mStartTime == 0) {
                    stopRecording();
                }
            } else {
                stopRecording();
            }
        } catch (Exception ex) {
            Log.e(TAG, "Tick method exceptop - " + ex);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }
}







