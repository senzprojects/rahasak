package com.score.chatz.ui;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.hardware.Camera;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.google.android.gms.vision.CameraSource;
import com.score.chatz.R;
import com.score.chatz.db.SenzorsDbSource;
import com.score.chatz.pojo.Secret;
import com.score.chatz.pojo.SenzStream;
import com.score.senz.ISenzService;
import com.score.senzc.enums.SenzTypeEnum;
import com.score.senzc.pojos.Senz;

import java.util.ArrayList;
import java.util.HashMap;

public class PhotoActivity extends AppCompatActivity {
    protected static final String TAG = PhotoActivity.class.getName();

    private android.hardware.Camera mCamera;
    private CameraPreview mCameraPreview;
    private ImageView closeBtn;
    private boolean isServiceBound = false;
    private PhotoActivity instnce;
    private SenzorsDbSource dbSource;
    private Senz originalSenz;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo);
        instnce = this;
        mCameraPreview = new CameraPreview(this, mCamera, (SenzStream.SENZ_STEAM_TYPE) getIntent().getExtras().get("StreamType"));

        FrameLayout preview = (FrameLayout) findViewById(R.id.photo);
        preview.addView(mCameraPreview);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setDisplayOptions(android.support.v7.app.ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(getLayoutInflater().inflate(R.layout.camera_action_bar, null));
        setupCloseBtn();
        dbSource = new SenzorsDbSource(this);
        originalSenz = (Senz)getIntent().getParcelableExtra("Senz");
        startCountdownToPhoto();
    }

    @Override
    public void onPause() {
        super.onPause();

        if (mCamera != null) {
            mCamera.setPreviewCallback(null);
            mCameraPreview.getHolder().removeCallback(mCameraPreview);
            mCamera.release();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        // Get the Camera instance as the activity achieves full user focus
        if (mCamera == null) {
            //initializeCamera(); // Local method to handle camera initialization
            //openFrontFacingCameraGingerbread();
        }
    }

    private void startCountdownToPhoto(){
        new CountDownTimer(5000,1000){
            @Override
            public void onFinish() {
                //initializeCamera();
                mCameraPreview.takePhoto(instnce, originalSenz);
            }
            @Override
            public void onTick(long millisUntilFinished) {
                Log.i(TAG, "Time in count down -" + millisUntilFinished);

            }
        }.start();

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

    public void onStart() {
        super.onStart();
        // bind to senz service
        if (!isServiceBound) {
            Intent intent = new Intent();
            intent.setClassName("com.score.chatz", "com.score.chatz.services.RemoteSenzService");
            this.bindService(intent, senzServiceConnection, Context.BIND_AUTO_CREATE);
            isServiceBound = true;
        }
    }

    @Override
    public void onStop() {
        super.onStop();

        // Unbind from the service
        if (isServiceBound) {
            this.unbindService(senzServiceConnection);
            isServiceBound = false;
        }
    }

    private void setupCloseBtn() {
        closeBtn = (ImageView) findViewById(R.id.close_btn);
        closeBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                goBack();

            }
        });
    }

    private void goBack(){
        this.finish();
    }

    /**
     * Helper method to access the camera returns null if
     * it cannot get the camera or does not exist
     * @return
     */
    private Camera getCameraInstant(){
        Camera camera = null;

        try{
            camera=Camera.open(Camera.CameraInfo.CAMERA_FACING_FRONT);
        }catch (Exception e){
            // cannot get camera or does not exist
        }
        return camera;
    }
}