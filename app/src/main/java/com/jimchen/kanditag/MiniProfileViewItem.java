package com.jimchen.kanditag;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Jim on 2/28/15.
 */
public class MiniProfileViewItem implements Parcelable {

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(fb_id);
        parcel.writeString(user_name);
        parcel.writeInt(placement);
    }

    private String fb_id, user_name;
    private int placement;

    public MiniProfileViewItem() {

    }

    public MiniProfileViewItem(String fb, String user, int placement) {
        this.fb_id = fb;
        this.user_name = user;
        this.placement = placement;
    }

    public void setFb_id(String fb) {
        this.fb_id = fb;
    }

    public void setUser_name(String name) {
        this.user_name = name;
    }

    public void setPlacement(int placement) {
        this.placement = placement;
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
}
