package com.jimchen.kanditag;

/**
 * Created by Jim on 4/10/15.
 */
public class UserAndGroupListItem {

    private String sender_name, sender_id, group_id, group_name, message;

    public UserAndGroupListItem() {

    }

    public UserAndGroupListItem(String s_name, String s_id, String g_name, String g_id, String message) {
        this.sender_name = s_name;
        this.sender_id = s_id;
        this.group_id = g_id;
        this.group_name = g_name;
        this.message = message;
    }

    public void setSender_name(String name) {
        this.sender_name = name;
    }

    public void setSender_id(String id) {
        this.sender_id = id;
    }

    public void setGroup_id(String id) {
        this.group_id = id;
    }

    public void setGroup_name(String name) {
        this.group_name = name;
    }

    public void setMessage(String msg) {
        this.message = msg;
    }

    public String getSender_name() {
        return sender_name;
    }

    public String getSender_id() {
        return sender_id;
    }

    public String getGroup_id() {
        return group_id;
    }

    public String getGroup_name() {
        return group_name;
    }

    public String getMessage() {
        return message;
    }
}
