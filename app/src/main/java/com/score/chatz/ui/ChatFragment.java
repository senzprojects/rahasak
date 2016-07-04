package com.score.chatz.ui;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import com.score.chatz.R;
import com.score.chatz.db.SenzorsDbSource;
import com.score.chatz.exceptions.NoUserException;
import com.score.chatz.receivers.DatabaseChangedReceiver;
import com.score.chatz.services.DataService;
import com.score.chatz.utils.PreferenceUtils;
import com.score.chatz.viewholders.Message;
import com.score.senzc.pojos.User;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ChatFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ChatFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private EditText text_message;
    private Button sendBtn;
    SenzorsDbSource dbSource;
    User currentUser;


    public ChatFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ChatFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ChatFragment newInstance(String param1, String param2) {
        ChatFragment fragment = new ChatFragment();
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
        View view = inflater.inflate(R.layout.fragment_chat, container, false);
        FragmentManager fm = getActivity().getSupportFragmentManager();
        ChatMessagesFragment chatMessagesFragment = ChatMessagesFragment.newInstance("", "");
        fm.beginTransaction().add(R.id.text_messages_container, chatMessagesFragment).commit();

        dbSource = new SenzorsDbSource(getContext());
        text_message = (EditText) view.findViewById(R.id.text_message);
        sendBtn = (Button) view.findViewById(R.id.sendBtn);
        try {
            currentUser = PreferenceUtils.getUser(getContext());
        }catch (NoUserException e) {
            e.printStackTrace();
        }



        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Message newMessage = new Message(text_message.getText().toString(), currentUser, true);
                //DataService.getInstance().addNewItem(newMessage);
                dbSource.createChatz(newMessage);

                text_message.setText("");
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(getContext().INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(text_message.getWindowToken(), 0);
            }
        });


        return view;
    }


}
