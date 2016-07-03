package com.score.chatz.ui;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.score.chatz.R;

import com.score.senzc.pojos.ChatMessage;

import java.util.ArrayList;

/**
 * Created by Lakmal on 7/2/16.
 */
public class ChatMessagesAdapter  extends RecyclerView.Adapter<ChatMessageViewHolder> {

    private ArrayList<ChatMessage> messages;

    public ChatMessagesAdapter(ArrayList<ChatMessage> messages) {
        this.messages = messages;
    }

    @Override
    public void onBindViewHolder(ChatMessageViewHolder holder, int position) {
        ChatMessage message = messages.get(position);
        holder.updateUI(message);
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }


    @Override
    public ChatMessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View message = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_layout, parent, false);
        return new ChatMessageViewHolder(message);
    }




}
