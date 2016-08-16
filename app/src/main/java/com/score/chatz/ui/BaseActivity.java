package com.score.chatz.ui;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.score.chatz.R;
import com.score.chatz.utils.ActivityUtils;

/**
 * Created by Lakmal on 8/7/16.
 */
public class BaseActivity extends AppCompatActivity {

    protected Typeface typeface;
    protected Typeface typefaceThin;
    protected Typeface typefaceUltraThin;


    @Override
    public void onCreate(Bundle savedInstanceState, PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);
        typeface = Typeface.createFromAsset(getAssets(), "fonts/vegur_2.otf");
        typefaceThin = Typeface.createFromAsset(getAssets(), "fonts/HelveticaNeue-Light.otf");
        typefaceUltraThin = Typeface.createFromAsset(getAssets(), "fonts/HelveticaNeue-UltraLight.otf");
    }



    /**
     * Display message dialog when user request(click) to register
     *
     * @param message message to be display
     */
    public void displayConfirmationMessageDialog(Context context, String message, String headerText, final View.OnClickListener onOKClickListener, final View.OnClickListener onCancelClickListenersage) {
    final Dialog confirmDialog = new Dialog(context);

        //set layout for dialog
        confirmDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        confirmDialog.setContentView(R.layout.share_confirm_message_dialog);
        confirmDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        confirmDialog.setCanceledOnTouchOutside(false);
        confirmDialog.setCancelable(true);

        // set dialog texts
        TextView messageHeaderTextView = (TextView) confirmDialog.findViewById(R.id.information_message_dialog_layout_message_header_text);
        TextView messageTextView = (TextView) confirmDialog.findViewById(R.id.information_message_dialog_layout_message_text);
        messageHeaderTextView.setText(headerText);
        messageTextView.setText(Html.fromHtml(message));

        // set custom font
        messageHeaderTextView.setTypeface(typeface);
        messageTextView.setTypeface(typeface);

        //set ok button
        Button okButton = (Button) confirmDialog.findViewById(R.id.information_message_dialog_layout_ok_button);
        okButton.setTypeface(typeface);
        okButton.setTypeface(null, Typeface.BOLD);
        okButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                confirmDialog.cancel();
                ActivityUtils.showProgressDialog(getApplicationContext(), "Please wait...");
                onOKClickListener.onClick(v);
            }
        });

        // cancel button
        Button cancelButton = (Button) confirmDialog.findViewById(R.id.information_message_dialog_layout_cancel_button);
        cancelButton.setTypeface(typeface);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                confirmDialog.cancel();
                onCancelClickListenersage.onClick(v);
            }
        });

        confirmDialog.show();
    }






    /**
     * Display message dialog with registration status
     *
     * @param message message to be display
     */
    public void displayInformationMessageDialog(String title, String message) {
        final Dialog dialog = new Dialog(this);

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


}



