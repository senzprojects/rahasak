package com.score.senzc.pojos;

/**
 * Created by Lakmal on 7/2/16.
 */
public class ChatMessage {

    private String text;
    private String sender;
    private boolean mine;
    final String myMessageBg = "drawable/my_message_box";
    final String notMyMessageBg = "drawable/not_my_message_box";

    public ChatMessage(String text, String sender, boolean mine) {
        this.text = text;
        this.sender = sender;
        this.mine = mine;
    }

    public String getText() {
        return text;
    }

    public String getSender() {
        return sender;
    }

    public boolean getMine() {
        return mine;
    }


    /*
     * Following getting feed the alignment to the view
     */
    public boolean getAlignLeft(){
        if(mine == true){
            return true;
        }else{
            return false;
        }
    }

    public boolean getAligRight(){
        if(mine == true){
            return false;
        }else{
            return true;
        }
    }

    public String getBackgroundImage(){
        if(mine == true) {
            return myMessageBg;
        }else{
            return notMyMessageBg;
        }
    }

}

