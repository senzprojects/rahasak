package com.score.chatz.ui;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.score.chatz.R;
import com.score.chatz.db.SenzorsDbSource;
import com.score.chatz.pojo.Secret;
import com.score.chatz.pojo.UserPermission;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ChatMessagesListFragment#newInstance} factory method to
 * create an instance of this fragment.
 *
 * NOT USED TOBE DELETED
 */
public class ChatMessagesListFragment extends ListFragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String TAG = ChatMessagesListFragment.class.getName();

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    SenzorsDbSource dbSource;
    private static ArrayList<Secret> messages;

    public static ChatMessagesAdapter msAdapter;


    public ChatMessagesListFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ChatMessagesListFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ChatMessagesListFragment newInstance(String param1, String param2) {
        ChatMessagesListFragment fragment = new ChatMessagesListFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        dbSource = new SenzorsDbSource(getContext());
        View v = inflater.inflate(R.layout.fragment_chat_messages, container, false);

        RecyclerView recyclerView = (RecyclerView)v.findViewById(R.id.recycler_messages);
        recyclerView.setHasFixedSize(true);


        //ChatMessagesAdapter adapter = new ChatMessagesAdapter(DataService.getInstance().getAllMessages());
        //messages = dbSource.getSecretz(getActivity().);
        ChatMessagesAdapter adapter = new ChatMessagesAdapter(messages);
        recyclerView.setAdapter(adapter);
        msAdapter = adapter;


        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);

        return v;

    }



    public static void addMessage(Secret msg){
        messages.add(msg);
        msAdapter.notifyDataSetChanged();
    }




















}
