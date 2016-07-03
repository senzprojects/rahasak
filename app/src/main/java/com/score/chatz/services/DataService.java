package com.score.chatz.services;

import com.score.senzc.pojos.ChatMessage;

import java.util.ArrayList;

/**
 * Created by Lakmal on 7/2/16.
 */
public class DataService {
    private static DataService ourInstance = new DataService();

    public static DataService getInstance() {
        return ourInstance;
    }

    private DataService() {
    }

    private ArrayList<ChatMessage> list;

    public ArrayList<ChatMessage> getAllMessages(){
        //Pretend we just downloaded featured stations from Internet

        list = new ArrayList<>();
        list.add(new ChatMessage("Hi, How are you... its me Lakmal", "Lakmal", true));
        list.add(new ChatMessage("I;m doing fine.. its me Eranga lol", "Eranga", false));
        list.add(new ChatMessage("NIce to hear from you mate .. :)", "Lakmal", true));

        return list;
    }

    public void addNewItem(ChatMessage message){
        list.add(message);

    }
}