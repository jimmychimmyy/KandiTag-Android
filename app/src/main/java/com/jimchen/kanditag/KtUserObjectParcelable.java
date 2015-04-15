package com.jimchen.kanditag;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Jim on 3/6/15.
 */
public class KtUserObjectParcelable implements Parcelable {

    private String kt_id, fb_id, user_name, qrCode;
    private int placement;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(kt_id);
        parcel.writeString(fb_id);
        parcel.writeString(user_name);
        parcel.writeString(qrCode);
        parcel.writeInt(placement);
    }

    public KtUserObjectParcelable() {}

    public KtUserObjectParcelable(String kt_id, String fb_id, String user_name, String qrCode, int placement) {
        this.kt_id = kt_id;
        this.fb_id = fb_id;
        this.user_name = user_name;
        this.qrCode = qrCode;
        this.placement = placement;
    }

    public void setFb_id(String fb) {
        this.fb_id = fb;
    }

    public void setUser_name(String name) {
        this.user_name = name;
    }

    public void setKt_id(String kt) {
        this.kt_id = kt;
    }

    public void setQrCode(String qr) {
        this.qrCode = qr;
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

    public String getKt_id() {
        return kt_id;
    }

    public String getQrCode() {
        return qrCode;
    }

    public int getPlacement() {
        return placement;
    }
}
