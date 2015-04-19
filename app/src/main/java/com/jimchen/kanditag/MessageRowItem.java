package com.jimchen.kanditag;

/**
 * Created by Jim on 4/17/15.
 */
public class MessageRowItem {

    //vars for all messages
    private String message_text, message_sender, message_senderID, message_timestamp;
    private String message_recipient, message_recipientID;
    //vars for group messages
    private String message_kandiID, message_kandiName;

    public MessageRowItem() {}

    public void setMessageText(String message_text) {
        this.message_text = message_text;
    }

    public void setMessageSender(String sender) {
        this.message_sender = sender;
    }

    public void setMessageSenderID(String id) {
        this.message_senderID = id;
    }

    public void setMessageRecipient(String recipient) {
        this.message_recipient = recipient;
    }

    public void setMessageRecipientID(String id) {
        this.message_recipientID = id;
    }

    public void setMessageTimestamp(String timestamp) {
        this.message_timestamp = timestamp;
    }

    public void setMessageKandiID(String kandiID) {
        this.message_kandiID = kandiID;
    }

    public void setMessageKandiName(String kandiName) {
        this.message_kandiName = kandiName;
    }

    public String getMessageText() {
        return message_text;
    }

    public String getMessageSender() {
        return message_sender;
    }

    public String getMessageSenderID() {
        return message_senderID;
    }

    public String getMessageRecipient() {
        return message_recipient;
    }

    public String getMessageRecipientID() {
        return message_recipientID;
    }

    public String getMessageTimeStamp() {
        return message_timestamp;
    }

    public String getMessageKandiID() {
        return message_kandiID;
    }

    public String getKandiName() {
        return message_kandiName;
    }
}
