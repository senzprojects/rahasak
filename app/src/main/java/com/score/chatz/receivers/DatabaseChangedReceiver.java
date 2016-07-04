package com.score.chatz.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.score.chatz.ui.ChatMessagesFragment;

/**
 * Created by Lakmal on 7/4/16.
 */
public class DatabaseChangedReceiver extends BroadcastReceiver{
    public static String ACTION_DATABASE_CHANGED = "com.score.chatz.DATABASE_CHANGED";

    @Override
    public void onReceive(Context context, Intent intent) {
        ChatMessagesFragment.msAdapter.notifyDataSetChanged();

    }
}
