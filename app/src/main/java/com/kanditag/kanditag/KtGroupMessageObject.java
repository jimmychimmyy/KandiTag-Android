package com.kanditag.kanditag;

/**
 * Created by Jim on 3/4/15.
 */
public class KtGroupMessageObject {

    private String message, from_id, from_name, kt_group, time;

    public KtGroupMessageObject() {}

    public KtGroupMessageObject(String mssg, String from_id, String from_name, String kt_group, String time) {
        this.message = mssg;
        this.from_id = from_id;
        this.from_name = from_name;
        this.kt_group = kt_group;
        this.time = time;
    }

    public void setMessage(String mssg) {
        this.message = mssg;
    }

    public void setFrom_id(String id) {
        this.from_id = id;
    }

    public void setFrom_name(String name) {
        this.from_name = name;
    }

    public void setKt_group(String group) {
        this.kt_group = group;
    }

    public void setTime(String time) {
        this.time = time;
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

    public String getKt_group() {
        return kt_group;
    }

    public String getTime() {
        return time;
    }
}
