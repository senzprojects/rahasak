package com.score.chatz.ui;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.os.AsyncTaskCompat;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.score.chatz.R;
import com.score.chatz.exceptions.NoUserException;
import com.score.chatz.services.RemoteSenzService;
import com.score.chatz.utils.PreferenceUtils;

/**
 * Splash activity, send login query from here
 *
 * @author eranga herath(erangaeb@gmail.com)
 */
public class SplashActivity extends Activity{
    private final int SPLASH_DISPLAY_LENGTH = 5000;
    private static final String TAG = SplashActivity.class.getName();

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.splash_layout);
        initSenzService();
        initNavigation();

    }

    /**
     * Determine where to go from here
     */
    private void initNavigation(){
        // determine where to go
        // start service
        try {
            PreferenceUtils.getUser(this);
            // have user, so move to home
            Intent intent = new Intent(this, HomeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            finish();
            startActivity(intent);
        } catch (NoUserException e) {
            e.printStackTrace();
            initLoader();
        }
    }

    private void navigateRegistration(){
        // no user, so move to registration
        Intent intent = new Intent(this, RegistrationActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        finish();
        startActivity(intent);
    }




    /*
     * Start stripe loader
     */

    private void initLoader(){
        new AsyncLoader().execute();
    }

    /**
     * {@inheritDoc}
     */
    protected void onResume() {
        super.onResume();
    }


    /*private void initNavigation() {
        PackageManager packageManager = this.getPackageManager();
        try {
            Log.i("MyApp", "package already installed! Awesome!");
            packageManager.getPackageInfo("com.score.senz", PackageManager.GET_ACTIVITIES);

            navigateToHome();
        } catch (PackageManager.NameNotFoundException e) {
            //App is not installed, launch Google Play Store
            displayConfirmDialog("In order to work with the application you have to install and register on SenZ service first" +
                    "Are you sure you want to install the SenZ service");
        }
    }8*/

    /**
     * Switch to home activity
     * This method will be call after successful login
     */
    private void navigateToHome() {
        Intent intent = new Intent(SplashActivity.this, HomeActivity.class);
        SplashActivity.this.startActivity(intent);
        SplashActivity.this.finish();

    }

    /**
     * Display message dialog when user request(click) to delete invoice
     *
     * @param message message to be display
     */
    public void displayConfirmDialog(String message) {
        final Dialog dialog = new Dialog(this);

        //set layout for dialog
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.share_confirm_message_dialog);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.setCancelable(true);

        // set dialog texts
        TextView messageHeaderTextView = (TextView) dialog.findViewById(R.id.information_message_dialog_layout_message_header_text);
        TextView messageTextView = (TextView) dialog.findViewById(R.id.information_message_dialog_layout_message_text);
        messageHeaderTextView.setText("#Share");
        messageTextView.setText(Html.fromHtml(message));

        // set custom font
        Typeface face = Typeface.createFromAsset(getAssets(), "fonts/vegur_2.otf");
        messageHeaderTextView.setTypeface(face);
        messageTextView.setTypeface(face);

        //set ok button
        Button okButton = (Button) dialog.findViewById(R.id.information_message_dialog_layout_ok_button);
        okButton.setTypeface(face);
        okButton.setTypeface(null, Typeface.BOLD);
        okButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                dialog.cancel();

                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("market://details?id=com.score.senz"));
                startActivity(intent);
            }
        });

        // cancel button
        Button cancelButton = (Button) dialog.findViewById(R.id.information_message_dialog_layout_cancel_button);
        cancelButton.setTypeface(face);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                dialog.cancel();
            }
        });

        dialog.show();
    }

    /*
     * Async Loader to show a loader to start the remote service
     */
    class AsyncLoader extends AsyncTask<Void, Integer, Void> {
        private ProgressBar progressBar;
        private final int SPLASH_PROGRESS_BAR_DISPLAY_LENGTH = 5; //Seconds
        int currentPosition;
        int total;

        @Override
        protected void onPreExecute() {
            progressBar = (ProgressBar) findViewById(R.id.progressBar);
            progressBar.setProgress(0);
            progressBar.setMax(SPLASH_PROGRESS_BAR_DISPLAY_LENGTH);
            progressBar.setVisibility(ProgressBar.VISIBLE);
            currentPosition= 0;
            total = SPLASH_PROGRESS_BAR_DISPLAY_LENGTH;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            progressBar.setVisibility(ProgressBar.GONE);
            navigateRegistration();
        }

        @Override
        protected void onProgressUpdate(Integer... value) {
            progressBar.setProgress(value[0]);
        }


        @Override
        protected Void doInBackground(Void... params) {
            while (currentPosition<total) {
                try {
                    currentPosition++;
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                publishProgress(currentPosition);
            }
            return null;
        }
    }


    /**
     * Initialize senz service
     */
    private void initSenzService() {
        // start service from here
        Intent serviceIntent = new Intent(this, RemoteSenzService.class);
        startService(serviceIntent);
    }


}