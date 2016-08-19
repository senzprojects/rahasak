package com.score.chatz.ui;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Typeface;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import com.score.chatz.R;
import com.score.chatz.db.SenzorsDbHelper;
import com.score.chatz.db.SenzorsDbSource;
import com.score.chatz.exceptions.NoUserException;
import com.score.chatz.pojo.UserPermission;
import com.score.chatz.utils.ActivityUtils;
import com.score.chatz.utils.PreferenceUtils;
import com.score.senz.ISenzService;
import com.score.senzc.enums.SenzTypeEnum;
import com.score.senzc.pojos.Senz;
import com.score.senzc.pojos.User;

import java.util.HashMap;

public class UserProfileActivity extends AppCompatActivity {
    private static final String TAG = UserProfileActivity.class.getName();
    private ImageView backBtn;
    private Toolbar toolbar;
    private User user;
    TextView username;
    Switch cameraSwitch;
    Switch locationSwitch;
    UserPermission userzPerm;
    Button shareSecretBtn;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        Intent intent = getIntent();
        String senderString = intent.getStringExtra("SENDER");
        user = new User("", senderString);


        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setCollapsible(false);
        toolbar.setOverScrollMode(Toolbar.OVER_SCROLL_NEVER);
        setSupportActionBar(toolbar);

        username = (TextView) findViewById(R.id.user_name);
        cameraSwitch = (Switch) findViewById(R.id.perm_camera_switch);
        locationSwitch = (Switch) findViewById(R.id.perm_location_switch);

        userzPerm = getUserPerm(user);



        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        //displaying custom ActionBar
        getSupportActionBar().setCustomView(getLayoutInflater().inflate(R.layout.user_profile_action_bar, null));
        getSupportActionBar().setDisplayOptions(android.support.v7.app.ActionBar.DISPLAY_SHOW_CUSTOM);

        Toolbar toolbar=(Toolbar) getSupportActionBar().getCustomView().getParent();
        toolbar.setContentInsetsAbsolute(0, 0);
        toolbar.getContentInsetEnd();
        toolbar.setPadding(0, 0, 0, 0);

        setupBackBtn();
        setupFontForActioBar();
        setupGoToChatViewBtn();
        setupUserPermissions();
        registerAllReceivers();

    }

    @Override
    public void onStop() {
        super.onStop();

        // Unbind from the service
        this.unbindService(senzServiceConnection);
    }


    @Override
    protected void onResume() {
        super.onResume();

        Intent intent = new Intent();
        intent.setClassName("com.score.chatz", "com.score.chatz.services.RemoteSenzService");
        bindService(intent, senzServiceConnection, Context.BIND_AUTO_CREATE);


    }

    private void setupUserPermissions(){
        username.setText(user.getUsername());
        cameraSwitch.setChecked(userzPerm.getCamPerm());
        locationSwitch.setChecked(userzPerm.getLocPerm());
        cameraSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked == true){
                    //Send permCam true to user
                    sendPermission(user, "true", null);
                }else{
                    //Send permCam false to user
                    sendPermission(user, "false", null);
                }
            }
        });
        locationSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked == true){
                    //Send permLoc true to user
                    sendPermission(user, null, "true");
                }else{
                    //Send permLoc false to user
                    sendPermission(user, null, "false");
                }
            }
        });
    }

    private UserPermission getUserPerm(User user){
        return new SenzorsDbSource(this).getUserPermission(user);
    }

    private void setupFontForActioBar(){
        TextView headerTitle = (TextView) findViewById(R.id.header_center_text);
        Typeface typefaceThin = Typeface.createFromAsset(getAssets(), "fonts/HelveticaNeue-Light.otf");
        headerTitle.setTypeface(typefaceThin, Typeface.NORMAL);
    }

    private void setupBackBtn(){
        backBtn = (ImageView) findViewById(R.id.goBackToHomeImg);
        backBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                goBackToHome();
            }
        });
    }

    private void goBackToHome(){
        Log.d(TAG, "go home clicked");
        this.finish();
    }

    private void setupGoToChatViewBtn(){

        shareSecretBtn = (Button) findViewById(R.id.share_secret_btn);
        shareSecretBtn.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
                intent.putExtra("SENDER", user.getUsername());
                startActivity(intent);
            }
        });
    }


    public void sendPermission(User receiver, String camPerm, String locPerm) {
        try {
            // create senz attributes
            HashMap<String, String> senzAttributes = new HashMap<>();
            senzAttributes.put("msg", "newPerm");
            if(camPerm != null) {
                senzAttributes.put("camPerm", camPerm); //Default Values, later in ui allow user to configure this on share
            }
            if(locPerm != null) {
                senzAttributes.put("locPerm", locPerm); //Dafault Values
            }
            senzAttributes.put("time", ((Long) (System.currentTimeMillis() / 1000)).toString());

            // new senz
            String id = "_ID";
            String signature = "_SIGNATURE";
            SenzTypeEnum senzType = SenzTypeEnum.DATA;
            Senz senz = new Senz(id, signature, senzType, null, receiver, senzAttributes);

            senzService.send(senz);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void registerAllReceivers(){
        registerReceiver(userSharedReceiver, new IntentFilter("com.score.chatz.USER_SHARED"));
        //Register for fail messages, incase other user is not online
        registerReceiver(senzDataReceiver, new IntentFilter("com.score.chatz.DATA_SENZ")); //Incoming data share
    }

    private void handleMessage(Intent intent) {
        String action = intent.getAction();
        if (action.equalsIgnoreCase("com.score.chatz.DATA_SENZ")) {
            Senz senz = intent.getExtras().getParcelable("SENZ");
            if (senz.getAttributes().containsKey("msg")) {
                // msg response received
                ActivityUtils.cancelProgressDialog();
                String msg = senz.getAttributes().get("msg");
                if (msg != null && msg.equalsIgnoreCase("USER_NOT_ONLINE")) {
                    //Reset display
                    setupUserPermissions();
                }
            }
        }
    }

    private void unregisterAllReceivers(){
        if (senzDataReceiver != null) unregisterReceiver(senzDataReceiver);
        if (userSharedReceiver != null) unregisterReceiver(userSharedReceiver);

    }


    private BroadcastReceiver userSharedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "Got user shared intent from Senz service");
            handleSharedUser(intent);
        }
    };

    private BroadcastReceiver senzDataReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "Got message from Senz service");
            handleMessage(intent);
        }
    };

    private void handleSharedUser(Intent intent) {
        setupUserPermissions();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterAllReceivers();
    }


}
