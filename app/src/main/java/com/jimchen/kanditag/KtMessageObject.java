package com.jimchen.kanditag;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Jim on 3/4/15.
 */
public class KtMessageObject implements Parcelable {

    private String message, from_id, from_name, timestamp;

    // for message
    private String to_id, to_name;

    // for group message
    private String to_kandi_id, to_kandi_name;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(message);
        parcel.writeString(from_id);
        parcel.writeString(from_name);
        parcel.writeString(timestamp);
        parcel.writeString(to_id);
        parcel.writeString(to_name);
        parcel.writeString(to_kandi_id);
        parcel.writeString(to_kandi_name);
    }


    public KtMessageObject() {}

    public void setTo_Kandi_Id(String id) {
        this.to_kandi_id = id;
    }

    public void setTo_Kandi_Name(String name) {
        this.to_kandi_name = name;
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

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
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
