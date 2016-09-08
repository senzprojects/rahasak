package com.score.chatz.ui;

import android.content.ClipData;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.RippleDrawable;
import android.hardware.Camera;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.RemoteException;
import android.support.v4.view.WindowCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.DragEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.github.siyamed.shapeimageview.CircularImageView;
import com.google.android.gms.vision.CameraSource;
import com.score.chatz.R;
import com.score.chatz.db.SenzorsDbSource;
import com.score.chatz.pojo.Secret;
import com.score.chatz.pojo.SenzStream;
import com.score.chatz.utils.AnimationUtils;
import com.score.chatz.utils.CameraUtils;
import com.score.chatz.utils.VibrationUtils;
import com.score.senz.ISenzService;
import com.score.senzc.enums.SenzTypeEnum;
import com.score.senzc.pojos.Senz;
import com.skyfishjy.library.RippleBackground;

import java.util.ArrayList;
import java.util.HashMap;

public class PhotoActivity extends AppCompatActivity implements View.OnTouchListener {
    protected static final String TAG = PhotoActivity.class.getName();

    private android.hardware.Camera mCamera;
    private CameraPreview mCameraPreview;
    private boolean isPhotoTaken;

    private ImageView closeBtn;
    private PhotoActivity instnce;
    private SenzorsDbSource dbSource;
    private Senz originalSenz;
    private CircularImageView cancelBtn;
    private ImageView startBtn;
    private RippleBackground goRipple;
    private PowerManager.WakeLock wakeLock;

    private boolean isServiceBound = false;

    private float dX, dY, startX, startY;


    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
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
        try {
            originalSenz = (Senz) getIntent().getParcelableExtra("Senz");
        }catch (Exception ex){
            ex.printStackTrace();
        }
        //startCountdownToPhoto();

        setupSwipeBtns();
        startBtnAnimations();
        startVibrations();
        setupHandlesForSwipeBtnContainers();

        PowerManager pm = (PowerManager) getApplicationContext().getSystemService(Context.POWER_SERVICE);
        wakeLock = pm.newWakeLock((PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP), "TAG");
        wakeLock.acquire();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //Release screen lock, so the phone can go back to sleep
        wakeLock.release();
    }

    private void setupSwipeBtns(){
        cancelBtn = (CircularImageView) findViewById(R.id.cancel);
        cancelBtn.setTag(FLOATING_BTN_TYPES.RED_BTN);
        startBtn = (ImageView) findViewById(R.id.start);
        startBtn.setTag(FLOATING_BTN_TYPES.GREEN_BTN);
    }

    private void startBtnAnimations(){
        Animation anim = android.view.animation.AnimationUtils.loadAnimation(this, R.anim.shake);
        //cancelBtn.startAnimation(anim);
        //startBtn.startAnimation(anim);

        //AnimationUtils.pulseImage(cancelBtn);
        //AnimationUtils.pulseImage(startBtn);
        //stopRipple=(RippleBackground)findViewById(R.id.stop_ripple);
        goRipple=(RippleBackground)findViewById(R.id.go_ripple);
        //stopRipple.startRippleAnimation();
        goRipple.startRippleAnimation();
        goRipple.startAnimation(anim);


    }

    private void startVibrations(){
        VibrationUtils.startVibrationForPhoto(VibrationUtils.getVibratorPatterIncomingPhotoRequest(), this);
    }

    private void stopVibrations(){
        VibrationUtils.stopVibration(this);
    }


    ArrayList<View> targets = new ArrayList<View>();

    private void setupHandlesForSwipeBtnContainers() {
        goRipple.setOnTouchListener(this);
        targets.add(startBtn);
        targets.add(cancelBtn);
    }

    public boolean onTouch(View v, MotionEvent event) {
        int x = (int)event.getRawX();
        int y = (int)event.getRawY();
        switch (event.getAction()) {
            case (MotionEvent.ACTION_DOWN):
                v.clearAnimation();
                startX = v.getX();
                startY = v.getY();
                dX = v.getX() - event.getRawX();
                dY = v.getY() - event.getRawY();

                break;

            case (MotionEvent.ACTION_MOVE):
                v.animate()
                        .x(event.getRawX() + dX)
                        .y(event.getRawY() + dY)
                        .setDuration(0)
                        .start();
                Integer viewPos = findViewPos(event.getX() + dX, event.getY()+ dY);
                if(v.getX() > startX + 10 && v.getY() > startY - 5 && v.getY() > startY + 5){
                    stopVibrations();
                    CameraUtils.shootSound(this);
                    if(isPhotoTaken == false) {
                        mCameraPreview.takePhotoManually(instnce, originalSenz);
                        isPhotoTaken = true;
                    }
                }else if(v.getX() < startX - 10 && v.getY() > startY - 5 && v.getY() > startY + 5){
                    stopVibrations();
                    this.finish();
                }
                break;

            case (MotionEvent.ACTION_UP):
                v.animate()
                        .x(startX)
                        .y(startY)
                        .setDuration(0)
                        .start();
                Animation anim = android.view.animation.AnimationUtils.loadAnimation(this, R.anim.shake);
                v.startAnimation(anim);
                break;
        }
        return true;
    }

    private Integer findViewPos(float x, float y)
    {
        final int count = targets.size();
        for (int i = 0; i < count; i++) {
            final View target = targets.get(i);
            if (target.getRight() > x && target.getTop() < y
                    && target.getBottom() > y && target.getLeft() < x) {
                return i;
            }
        }
        return null;
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

    enum FLOATING_BTN_TYPES{
        GREEN_BTN, RED_BTN
    }
}