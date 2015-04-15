package com.jimchen.kanditag;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Jim on 2/27/15.
 */
public class KtUserObject implements Parcelable {

    private String name, kt_id, fb_id, qrCode;
    private int placement;

    public KtUserObject() {}

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeString(name);
        parcel.writeString(kt_id);
        parcel.writeString(fb_id);
        parcel.writeString(qrCode);
        parcel.writeInt(placement);
    }

    public static final Creator <KtUserObject> CREATOR = new Parcelable.Creator<KtUserObject>() {

        public KtUserObject createFromParcel(Parcel in) {
            return new KtUserObject(in);
        }

        public KtUserObject[] newArray(int size) {
            return new KtUserObject[size];
        }
    };

    private KtUserObject(Parcel parcel) {
        name = parcel.readString();
        kt_id = parcel.readString();
        fb_id = parcel.readString();
        qrCode = parcel.readString();
        placement = parcel.readInt();
    }

    public KtUserObject(String nameString, String ktString, String fbString, String qrCode, int placement) {
        this.name = nameString;
        this.kt_id = ktString;
        this.fb_id = fbString;
        this.qrCode = qrCode;
        this.placement = placement;
    }


    public void setName(String name) {
        this.name = name;
    }

    public void setKt_id(String kt_id) {
        this.kt_id = kt_id;
    }

    public void setFb_id(String fb_id) {
        this.fb_id = fb_id;
    }


    public void setQrCode(String kandi) {
        this.qrCode = kandi;
    }

    public void setPlacement(int placement) {
        this.placement = placement;
    }



    public String getName() {
        return name;
    }

    public String getKt_id() {
        return kt_id;
    }

    public String getFb_id() {
        return fb_id;
    }

    public String getQrCode() {
        return qrCode;
    }

    public int getPlacement() {
        return placement;
    }
}
