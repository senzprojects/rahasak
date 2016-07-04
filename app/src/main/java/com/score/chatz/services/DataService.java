package com.score.chatz.services;

import com.score.chatz.viewholders.Message;

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

    private ArrayList<Message> list;

    public ArrayList<Message> getAllMessages(){
        //Pretend we just downloaded featured stations from Internet

        /*list = new ArrayList<>();
        list.add(new Message("Hi, How are you... its me Lakmal", "Lakmal", true));
        list.add(new Message("I;m doing fine.. its me Eranga lol", "Eranga", false));
        list.add(new Message("NIce to hear from you mate .. :)", "Lakmal", true));
*/
        return list;
    }

    public void addNewItem(Message message){
        list.add(message);

    }
}