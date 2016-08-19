package com.score.chatz.ui;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.score.chatz.R;

public class UserProfileActivity extends AppCompatActivity {
    private static final String TAG = UserProfileActivity.class.getName();
    private ImageView backBtn;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);


        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setCollapsible(false);
        toolbar.setOverScrollMode(Toolbar.OVER_SCROLL_NEVER);
        setSupportActionBar(toolbar);



        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        //displaying custom ActionBar
        getSupportActionBar().setCustomView(getLayoutInflater().inflate(R.layout.user_profile_action_bar, null));
        getSupportActionBar().setDisplayOptions(android.support.v7.app.ActionBar.DISPLAY_SHOW_CUSTOM);

        Toolbar toolbar=(Toolbar) getSupportActionBar().getCustomView().getParent();
        toolbar.setContentInsetsAbsolute(0, 0);
        toolbar.getContentInsetEnd();
        toolbar.setPadding(0, 0, 0, 0);

        setupBackBtn();

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
}
