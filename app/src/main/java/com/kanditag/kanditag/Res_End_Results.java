package com.kanditag.kanditag;

/**
 * Created by Jim on 3/1/15.
 */
public class Res_End_Results {

    private Boolean success;
    private String error;

    private int previous_userCount;
    private Boolean previous_user, sent;

    private String qrCode, kt_id, fb_id, user_name, _id, kandiName, message, date;
    private int placement;

    private String gmMessage, gmFrom_id, gmFrom_name, gmKandi_group, gmKandi_name, gmTime;

    private Records[] records;

    public Res_End_Results() {}

    public String getGmMessage() {
        return gmMessage;
    }

    public String getGmFrom_name() {
        return gmFrom_name;
    }

    public String getGmFrom_id() {
        return gmFrom_id;
    }

    public String getGmKandi_group() {
        return gmKandi_group;
    }

    public String getGmKandi_name() {
        return gmKandi_name;
    }

    public String getGmTime() {
        return gmTime;
    }

    public void setGmMessage(String message) {
        this.gmMessage = message;
    }

    public void setGmFrom_id(String id) {
        this.gmFrom_id = id;
    }

    public void setGmFrom_name(String name) {
        this.gmFrom_name = name;
    }

    public void setGmKandi_group(String qr) {
        this.gmKandi_group = qr;
    }

    public void setGmKandi_name(String name) {
        this.gmKandi_name = name;
    }

    public void setGmTime(String time) {
        this.gmTime = time;
    }

    public void setError(String err) {
        this.error = err;
    }

    public String getError() {
        return error;
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

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setPrevious_userCount(int count) {
        this.previous_userCount = count;
    }

    public int getPrevious_userCount() {
        return previous_userCount;
    }

    public void setPrevious_user(Boolean user) {
        this.previous_user = user;
    }

    public Boolean getPrevious_user() {
        return previous_user;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public Boolean getSuccess() {
        return success;
    }

    public void setKandiName(String kandiName) {
        this.kandiName = kandiName;
    }

    public String getKandiName() {
        return kandiName;
    }

    public void setRecords(Records[] records) {
        this.records = records;
    }

    public Records[] getRecords() {
        return records;
    }

    //These are for kt_qrcode_save

    public void setQrCode(String qr) {
        this.qrCode = qr;
    }

    public void setKt_id(String kt) {
        this.kt_id = kt;
    }

    public void setFb_id(String fb) {
        this.fb_id = fb;
    }

    public void setUser_name(String name) {
        this.user_name = name;
    }

    public void setPlacement(int place) {
        this.placement = place;
    }

    public String getQrCode() {
        return qrCode;
    }

    public String getKt_id() {
        return kt_id;
    }

    public String getFb_id() {
        return fb_id;
    }

    public String getUser_name() {
        return user_name;
    }

    public int getPlacement() {
        return placement;
    }

    public void set_id(String id) {
        this._id = id;
    }

    public String get_id() {
        return _id;
    }

}
