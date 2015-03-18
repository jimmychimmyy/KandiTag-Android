package com.kanditag.kanditag;

/**
 * Created by Jim on 3/3/15.
 */
public class GroupMessageItem {

    private String _id, message, fromID, fromName, qrCode, time, kandi_name;

    public GroupMessageItem() {}

    public GroupMessageItem(String mssg, String f_id, String f_name, String kandi, String time) {
        this.message = mssg;
        this.fromID = f_id;
        this.fromName = f_name;
        this.qrCode = kandi;
        this.time = time;
    }

    public void setKandi_name(String name) {
        this.kandi_name = name;
    }

    public String getKandi_name() {
        return kandi_name;
    }

    public void setID(String pid) {
        this._id = pid;
    }

    public void setMessage(String mssg) {
        this.message = mssg;
    }

    public void setFromID(String f_id) {
        this.fromID = f_id;
    }

    public void setFromName(String name) {
        this.fromName = name;
    }

    public void setQrCode(String kandi) {
        this.qrCode = kandi;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getID() {
        return _id;
    }

    public String getMessage() {
        return message;
    }

    public String getFromID() {
        return fromID;
    }

    public String getFromName() {
        return fromName;
    }

    public String getQrCode() {
        return qrCode;
    }

    public String getTime() {
        return time;
    }
}
