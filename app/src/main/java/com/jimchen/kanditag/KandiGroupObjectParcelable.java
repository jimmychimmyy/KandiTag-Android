package com.jimchen.kanditag;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

/**
 * Created by Jim on 3/11/15.
 */
public class KandiGroupObjectParcelable implements Parcelable{

    private String kandi_id, kandi_name;

    private ArrayList<KtUserObjectParcelable> users;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeString(kandi_id);
        parcel.writeString(kandi_name);
        parcel.writeList(users);
    }

    public KandiGroupObjectParcelable() {}

    public void setListOfUsers(ArrayList<KtUserObjectParcelable> users) {
        this.users = users;
    }

    public ArrayList<KtUserObjectParcelable> getListOfUsers() {
        return users;
    }

    public KandiGroupObjectParcelable(String kandi_id, String kandi_name) {
        this.kandi_id = kandi_id;
        this.kandi_name = kandi_name;
    }

    public void setKandi_id(String kandi_id) {
        this.kandi_id = kandi_id;
    }

    public void setKandi_name(String name) {
        this.kandi_name = name;
    }

    public String getKandi_id() {
        return kandi_id;
    }

    public String getKandi_name() {
        return kandi_name;
    }
}
