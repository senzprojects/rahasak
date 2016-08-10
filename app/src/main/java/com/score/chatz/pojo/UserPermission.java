package com.score.chatz.pojo;

import com.score.senzc.pojos.User;

/**
 * Created by Lakmal on 8/6/16.
 */
public class UserPermission {
    private User user;
    private boolean cameraPermission;
    private boolean locationPermission;
    /*final String activeLocationDrawable = "drawable/my_message_box";
    final String deactiveLocationDrawable = "drawable/not_my_message_box";
    final String activeCameraDrawable = "drawable/my_message_box";
    final String deactiveLocationDrawable = "drawable/not_my_message_box";*/

    public UserPermission(User user, boolean cameraPermission, boolean locationPermission) {
        this.user = user;
        this.cameraPermission = cameraPermission;
        this.locationPermission = locationPermission;
    }

    public User getUser() {
        return user;
    }

    public boolean getCamPerm() {
        return cameraPermission;
    }

    public boolean getLocPerm() {
        return locationPermission;
    }


    /*
     * Following getting feed the alignment to the view

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
        /*if(mine == true) {
            return myMessageBg;
        }else{
            return notMyMessageBg;
        }
        return null;
    }*/
}
