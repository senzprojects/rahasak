package com.score.chatz.pojo;

import com.score.senzc.pojos.User;

/**
 * Created by Lakmal on 8/6/16.
 */
public class UserPermission {
    private User user;
    private boolean cameraPermission;
    private boolean locationPermission;

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


}
