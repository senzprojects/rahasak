package com.score.chatz.ui;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.score.chatz.R;
import com.score.chatz.db.SenzorsDbSource;
import com.score.chatz.services.DataService;
import com.score.chatz.viewholders.Message;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ChatMessagesFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ChatMessagesFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    SenzorsDbSource dbSource;

    public static ChatMessagesAdapter msAdapter;


    public ChatMessagesFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ChatMessagesFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ChatMessagesFragment newInstance(String param1, String param2) {
        ChatMessagesFragment fragment = new ChatMessagesFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
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
        ChatMessagesAdapter adapter = new ChatMessagesAdapter(dbSource.getChatz());
        recyclerView.setAdapter(adapter);
        msAdapter = adapter;


        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);

        return v;

    }

}
