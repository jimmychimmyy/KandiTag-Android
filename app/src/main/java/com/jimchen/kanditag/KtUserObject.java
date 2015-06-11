package com.jimchen.kanditag;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Jim on 2/27/15.
 */
public class KtUserObject implements Parcelable {

    private String name, kt_id, kandi_id;
    public int placement;

    public KtUserObject() {}

    //TODO describeContents() always returns zero?
    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeString(name);
        parcel.writeString(kt_id);
        parcel.writeString(kandi_id);
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
        kandi_id = parcel.readString();
        placement = parcel.readInt();
    }

    public KtUserObject(String username, String kt_id, String kandi_id, int placement) {
        this.name = username;
        this.kt_id = kt_id;
        this.kandi_id = kandi_id;
        this.placement = placement;
    }


    public void setUsername(String name) {
        this.name = name;
    }

    public void setKt_id(String kt_id) {
        this.kt_id = kt_id;
    }

    public void setKandiId(String kandi) {
        this.kandi_id = kandi;
    }

    public void setPlacement(int placement) {
        this.placement = placement;
    }



    public String getUsername() {
        return name;
    }

    public String getKt_id() {
        return kt_id;
    }

    public String getKandiId() {
        return kandi_id;
    }

    public int getPlacement() {
        return placement;
    }
}
