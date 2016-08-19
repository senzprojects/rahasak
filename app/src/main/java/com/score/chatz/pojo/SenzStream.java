package com.score.chatz.pojo;

/**
 * Created by eranga on 8/19/16.
 */
public class SenzStream {
    private boolean isActive;
    private String user;
    private StringBuffer streamBuffer;

    public SenzStream(boolean isActive, String user, StringBuffer buffer) {
        this.isActive = isActive;
        this.user = user;
        this.streamBuffer = buffer;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setIsActive(boolean isActive) {
        this.isActive = isActive;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public void putStream(String stream) {
        this.streamBuffer.append(stream);
    }

    public String getStream() {
        return this.streamBuffer.toString();
    }
}
