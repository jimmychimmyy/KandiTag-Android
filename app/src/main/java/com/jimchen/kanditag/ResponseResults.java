package com.jimchen.kanditag;

/**
 * Created by Jim on 6/9/15.
 */
public class ResponseResults {

    // KtUser
    private String kt_id, username, kandi_id;
    private int placement;

    public void setKt_id(String kt_id) {
        this.kt_id = kt_id;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setKandi_id(String kandi_id) {
        this.kandi_id = kandi_id;
    }

    public void setPlacement(int placement) {
        this.placement = placement;
    }

    public String getKt_id() {
        return kt_id;
    }

    public String getUsername() {
        return username;
    }

    public String getKandi_id() {
        return kandi_id;
    }

    public int getPlacement() {
        return placement;
    }

    // message && group message
    private String message, from_id, from_name, to_id, to_name, to_kandi_id, to_kandi_name, timestamp;

    public void setMessage(String message) {
        this.message = message;
    }

    public void setFrom_id(String from_id) {
        this.from_id = from_id;
    }

    public void setFrom_name(String from_name) {
        this.from_name = from_name;
    }

    public void setTo_id(String to_id) {
        this.to_id = to_id;
    }

    public void setTo_name(String to_name) {
        this.to_name = to_name;
    }

    public void setTo_kandi_id(String kandi_id) {
        this.to_kandi_id = kandi_id;
    }

    public void setTo_kandi_name(String kandi_name) {
        this.to_kandi_name = kandi_name;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getMessage() {
        return message;
    }

    public String getFrom_id() {
        return from_id;
    }

    public String getFrom_name() {
        return from_name;
    }

    public String getTo_id() {
        return to_id;
    }

    public String getTo_name() {
        return to_name;
    }

    public String getTo_kandi_id() {
        return to_kandi_id;
    }

    public String getTo_kandi_name() {
        return to_kandi_name;
    }

    public String getTimestamp() {
        return timestamp;
    }
}
