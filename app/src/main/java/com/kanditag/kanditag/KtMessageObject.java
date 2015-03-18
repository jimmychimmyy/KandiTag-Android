package com.kanditag.kanditag;

/**
 * Created by Jim on 3/4/15.
 */
public class KtMessageObject {

    private String message, to_id, to_name, from_id, from_name, time;

    public KtMessageObject() {}

    public KtMessageObject(String mssg, String to_id, String to_name, String from_id, String from_name, String time) {
        this.message = mssg;
        this.to_id = to_id;
        this.to_name = to_name;
        this.from_id = from_id;
        this.from_name = from_name;
        this.time = time;
    }

    public void setMessage(String mssg) {
        this.message = mssg;
    }

    public void setTo_id(String id) {
        this.to_id = id;
    }

    public void setTo_name(String name) {
        this.to_name = name;
    }

    public void setFrom_id(String id) {
        this.from_id = id;
    }

    public void setFrom_name(String name) {
        this.from_name = name;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getMessage() {
        return message;
    }

    public String getTo_id() {
        return to_id;
    }

    public String getTo_name() {
        return to_name;
    }

    public String getFrom_id() {
        return from_id;
    }

    public String getFrom_name() {
        return from_name;
    }

    public String getTime() {
        return time;
    }

}
