package com.jimchen.kanditag;

import android.widget.ImageView;

/**
 * Created by Jim on 2/18/15.
 */
public class MessageListItem {

    public String sender, description, name, sender_id;
    public ImageView profilePicture;

    public MessageListItem() {}

    public MessageListItem(String sender_id, String sender, String description, String name) {
        this.sender_id = sender_id;
        this.sender = sender;
        this.description = description;
        this.name = name;
    }

    public String getSender_id() {
        return sender_id;
    }

    public String getSender() {
        return sender;
    }

    public String getDescription() {
        return description;
    }

    public String getName() {
        return name;
    }

    public boolean setSender_id(String id) {
        this.sender_id = id;
        return true;
    }

    public boolean setSender(String sender) {
        this.sender = sender;
        return true;
    }

    public boolean setDescription(String description) {
        this.description = description;
        return true;
    }

    public boolean setName(String name) {
        this.name = name;
        return true;
    }
}
