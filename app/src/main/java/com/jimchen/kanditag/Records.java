package com.jimchen.kanditag;

/**
 * Created by Jim on 3/1/15.
 */
public class Records {

    private String qrcode, kt_id, fb_id, username, _id, group;
    private int placement;
    private String message, timestamp, from_id, to_id;
    private boolean sent;
    private String from_name, to_name;

    //group message
    private String kandi_id;
    private String kandi_name;

    public void setKandiID(String id) {
        this.kandi_id = id;
    }

    public String getKandiID() {
        return kandi_id;
    }

    public void setKandi_name(String name) {
        this.kandi_name = name;
    }

    public String getKandi_name() {
        return kandi_name;
    }

    public void setFrom_name(String name) {
        this.from_name = name;
    }

    public void setTo_name(String name) {
        this.to_name = name;
    }

    public String getFrom_name() {
        return from_name;
    }

    public String getTo_name() {
        return to_name;
    }

    public void setFrom_id(String id) {
        this.from_id = id;
    }

    public void setTo_id(String id) {
        this.to_id = id;
    }

    public String getFrom_id() {
        return from_id;
    }

    public String getTo_id() {
        return to_id;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setSent(boolean sent) {
        this.sent = sent;
    }

    public boolean getSent() {
        return sent;
    }

    public void setQrcode(String qr) {
        this.qrcode = qr;
    }

    public String getQrcode() {
        return qrcode;
    }

    public void setKt_id(String kt) {
        this.kt_id = kt;
    }

    public String getKt_id() {
        return kt_id;
    }

    public void setFb_id(String fb) {
        this.fb_id = fb;
    }

    public void setGroup(String kandi) {
        this.group = kandi;
    }

    public String getFb_id() {
        return fb_id;
    }

    public void setUsername(String name) {
        this.username = name;
    }

    public String getUsername() {
        return username;
    }

    public void set_id(String id) {
        this._id = id;
    }

    public String get_id() {
        return _id;
    }

    public void setPlacement(int placement) {
        this.placement = placement;
    }

    public int getPlacement() {
        return placement;
    }

    public String getGroup() {
        return group;
    }
}
