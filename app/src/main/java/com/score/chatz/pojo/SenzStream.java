package com.score.chatz.pojo;

import android.util.Log;

import com.score.senzc.pojos.Senz;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by eranga on 8/19/16.
 */
public class SenzStream {
    private boolean isActive;
    private String user;
    private StringBuilder streamBuffer;
    private String image;
    private SENZ_STEAM_TYPE streamType;

    public SenzStream(boolean isActive, String user, StringBuilder buffer) {
        this.isActive = isActive;
        this.user = user;
        this.streamBuffer = buffer;
        this.image = "";
    }

    public void setStreamType(SENZ_STEAM_TYPE type){
        streamType = type;
    }

    public SENZ_STEAM_TYPE getStreamType(){
        return streamType;
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

       // this.streamBuffer.append(stream); This doesn't work for some reason ??
        //TODO integarate a string buffer to improve performance
        image += stream;
    }

    public String getStream() {
        return image;
    }

    /**
     * Return a sinle senz of the stream
     * @return
     */
    public String getSenzString(){
        String senzString = getStartOfStream(streamType);
        senzString += getImageFromStream();
        senzString += getEndOfStream();
        return senzString;
    }

    /**
     * Get only the image part of the stream
     * @return
     */
    private String getImageFromStream(){
        String imageString = "";
        Pattern pattern = getRegexPattern(streamType);
        Matcher matcher = pattern.matcher(image);
        while (matcher.find())
        {
            imageString += matcher.group(1);
        }
        return imageString;
    }

    /**
     * Return regex pattern based on type, to extract either the chatphoto or profilez photo
     * @param type
     * @return
     */
    private Pattern getRegexPattern(SENZ_STEAM_TYPE type){
        Pattern regex = null;

        switch (type){
            case CHATZPHOTO:
                regex = Pattern.compile("#chatzphoto\\s(.*?)\\s#time|(^[^D][^A][^T][^A].*?)\\s#time");
                break;
            case PROFILEZPHOTO:
                regex = Pattern.compile("#profilezphoto\\s(.*?)\\s#time");
                break;
        }

        return regex;
    }

    /**
     * Return the start of the stream based on which type of stream
     * @param type
     * @return
     */
    private String getStartOfStream(SENZ_STEAM_TYPE type){
        String startOfStream = null;

        switch (type){
            case CHATZPHOTO:
                startOfStream = "DATA #chatzphoto ";
                break;
            case PROFILEZPHOTO:
                startOfStream = "DATA #profilezphoto ";
            break;
        }

        return startOfStream;
    }

    /**
     * Extract the last part of the stream which is common to all streaming packets
     * @return
     */
    private String getEndOfStream(){
        String lastSection = "";

        Pattern pattern = Pattern.compile("(\\s#time\\s\\d+?\\s@[a-zA-Z0-9]+?\\s\\^[a-zA-Z0-9]+?\\sSIGNATURE)$");
        Matcher matcher = pattern.matcher(image);

        if (matcher.find())
        {
            lastSection = matcher.group(1);
        }

        return lastSection;
    }

    public enum SENZ_STEAM_TYPE{
        CHATZPHOTO, PROFILEZPHOTO
    }
}


