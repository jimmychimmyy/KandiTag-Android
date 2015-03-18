package com.kanditag.kanditag;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;


public class ConversationListItem  {

    //vars
    public String senderName, recipientName, senderID, recipientID, message, time;
    public ImageView senderPic, myPic;

    //constructors
    public ConversationListItem () {}

    public ConversationListItem (String message, String sender, String me, String senderID, String recipientID, String time) {
        this.message = message;
        this.senderName = sender;
        this.recipientName = me;
        this.senderID = senderID;
        this.recipientID = recipientID;
        this.time = time;
    }

    //setters
    public boolean setMessage(String msg) {
        this.message = msg;
        return true;
    }

    public boolean setSenderName(String name) {
        this.senderName = name;
        return true;
    }

    public boolean setMyName(String name) {
        this.recipientName = name;
        return true;
    }

    public boolean setSenderID(String id) {
        this.senderID = id;
        return true;
    }

    public boolean setRecipientID(String id) {
        this.recipientID = id;
        return true;
    }

    public boolean setRecipientName(String name) {
        this.recipientName = name;
        return true;
    }

    public boolean setTime(String time) {
        this.time = time;
        return true;
    }

    //getters
    public String getMessage() {
        return message;
    }

    public String getSenderName() {
        return senderName;
    }

    public String getRecipientName() {
        return recipientName;
    }

    public String getSenderID() {
        return senderID;
    }

    public String getRecipientID() {
        return recipientID;
    }

    public String getTime() { return time; }


}
