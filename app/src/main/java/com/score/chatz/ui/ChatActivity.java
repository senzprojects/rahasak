package com.score.chatz.ui;

import android.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.ImageView;

import com.score.chatz.R;
import com.score.chatz.exceptions.NoUserException;
import com.score.chatz.utils.PreferenceUtils;
import com.score.senzc.pojos.User;


public class ChatActivity extends AppCompatActivity {

    //private Toolbar toolbar;
    private User receiver;
    private User sender;
    private ImageView backBtn;
    private static final String TAG = ChatActivity.class.getName();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        Intent intent = getIntent();
        try {
            receiver = PreferenceUtils.getUser(getApplicationContext());
        } catch (NoUserException e) {
            e.printStackTrace();
        }
        String senderString = intent.getStringExtra("SENDER");
        sender = new User("", senderString);
        FragmentManager fm = getSupportFragmentManager();
        ChatFragment mainFragment = (ChatFragment) fm.findFragmentById(R.id.container_main);
        /*toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setCollapsible(false);
        toolbar.setOverScrollMode(Toolbar.OVER_SCROLL_NEVER);
        setSupportActionBar(toolbar);*/
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setDisplayOptions(android.support.v7.app.ActionBar.DISPLAY_SHOW_CUSTOM);

        //displaying custom ActionBar
        getSupportActionBar().setCustomView(getLayoutInflater().inflate(R.layout.chat_action_bar, null));


        if (mainFragment == null) {
            mainFragment = ChatFragment.newInstance(sender, receiver);
            fm.beginTransaction().add(R.id.container_main, mainFragment).commit();
        }

        setupBackBtn();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // bind to senz service

    }


    @Override
    public void onStop() {
        super.onStop();


    }


    private void setupBackBtn() {
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


}
