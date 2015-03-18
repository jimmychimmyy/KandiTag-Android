package com.kanditag.kanditag;

/**
 * Created by Jim on 3/1/15.
 */
public class Records {

    private String qrcode, kt_id, fb_id, username, _id, group;
    private int placement;
    private String msg, date, from_id, to_id;
    private boolean sent;
    private String fromName, toName;
    private String time;
    private String kandiName;

    public void setKandiName(String name) {
        this.kandiName = name;
    }

    public String getKandiName() {
        return kandiName;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getTime() {
        return time;
    }

    public void setFromName(String name) {
        this.fromName = name;
    }

    public void setToName(String name) {
        this.toName = name;
    }

    public String getFromName() {
        return fromName;
    }

    public String getToName() {
        return toName;
    }

    public void setFID(String id) {
        this.from_id = id;
    }

    public void setTID(String id) {
        this.to_id = id;
    }

    public String getFID() {
        return from_id;
    }

    public String getTID() {
        return to_id;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getMsg() {
        return msg;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDate() {
        return date;
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
