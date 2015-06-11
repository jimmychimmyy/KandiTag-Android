package com.jimchen.kanditag;

/**
 * Created by Jim on 4/17/15.
 */
public class MessageRowItem {

    //vars for all messages
    private String message, from_name, from_id, timestamp;
    private String to_name, to_id;
    //vars for group messages
    private String to_kandi_id, to_kandi_name;

    public MessageRowItem() {}

    public void setMessageContent(String message_text) {
        this.message = message_text;
    }

    public void setFrom_Name(String sender) {
        this.from_name = sender;
    }

    public void setFrom_Id(String id) {
        this.from_id = id;
    }

    public void setTo_Name(String recipient) {
        this.to_name = recipient;
    }

    public void setTo_Id(String id) {
        this.to_id = id;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public void setTo_Kandi_Id(String kandiID) {
        this.to_kandi_id = kandiID;
    }

    public void setTo_Kandi_Name(String kandiName) {
        this.to_kandi_name = kandiName;
    }

    public String getMessageContent() {
        return message;
    }

    public String getFrom_Name() {
        return from_name;
    }

    public String getFrom_Id() {
        return from_id;
    }

    public String getTo_Name() {
        return to_name;
    }

    public String getTo_Id() {
        return to_id;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getTo_Kandi_Id() {
        return to_kandi_id;
    }

    public String getTo_Kandi_Name() {
        return to_kandi_name;
    }
}
