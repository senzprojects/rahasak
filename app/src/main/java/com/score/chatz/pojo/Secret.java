package com.score.chatz.pojo;

/**
 * Created by Lakmal on 7/31/16.
 */

import com.score.senzc.pojos.User;
public class Secret {
    private String text;
    private String image;
    private User sender;
    private User receiver;

    public Secret(String text, String image,User sender, User receiver) {
        this.text = text;
        this.sender = sender;
        this.receiver = receiver;
        this.image = image;
    }

    public String getText() {
        return text;
    }

    public User getSender() {
        return sender;
    }

    public User getReceiver() {
        return receiver;
    }

    public String getImage() {
        return image;
    }

}
