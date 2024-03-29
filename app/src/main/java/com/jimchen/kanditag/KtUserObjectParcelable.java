package com.jimchen.kanditag;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Jim on 3/6/15.
 */
//TODO this whole entire class could be shared with KtUserObject; almost identical methods and variables
public class KtUserObjectParcelable implements Parcelable {

    private String kt_id, fb_id, username, kandi_id;
    private int placement;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(kt_id);
        parcel.writeString(fb_id);
        parcel.writeString(username);
        parcel.writeString(kandi_id);
        parcel.writeInt(placement);
    }

    public KtUserObjectParcelable() {}

    public KtUserObjectParcelable(String kt_id, String fb_id, String username, String kandi_id, int placement) {
        this.kt_id = kt_id;
        this.fb_id = fb_id;
        this.username = username;
        this.kandi_id = kandi_id;
        this.placement = placement;
    }

    public void setFb_id(String fb) {
        this.fb_id = fb;
    }

    public void setUsername(String name) {
        this.username = name;
    }

    public void setKt_id(String kt) {
        this.kt_id = kt;
    }

    public void setKandi_id(String qr) {
        this.kandi_id = qr;
    }

    public void setPlacement(int placement) {
        this.placement = placement;
    }

    public String getFb_id() {
        return fb_id;
    }

    public String getUsername() {
        return username;
    }

    public String getKt_id() {
        return kt_id;
    }

    public String getKandi_id() {
        return kandi_id;
    }

    public int getPlacement() {
        return placement;
    }
}
