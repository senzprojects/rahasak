package com.score.chatz.ui;

import android.content.Intent;
import android.media.Image;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;

import com.score.chatz.R;
import com.score.chatz.exceptions.NoUserException;
import com.score.chatz.utils.CameraUtils;
import com.score.chatz.utils.PreferenceUtils;

public class PhotoFullScreenActivity extends AppCompatActivity {

    ImageView imageView;
    String image;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_full_screen);

        Intent intent = getIntent();
        image = intent.getStringExtra("IMAGE");

        imageView = (ImageView) findViewById(R.id.imageView);
        imageView.setImageBitmap(CameraUtils.getRotatedImage(CameraUtils.getBitmapFromBytes(image.getBytes()), -90));
    }

    @Override
    protected void onResume() {
        super.onResume();

        //If the activity is destroy on rotation
        imageView.setImageBitmap(CameraUtils.getRotatedImage(CameraUtils.getBitmapFromBytes(image.getBytes()), -90));
    }
}
