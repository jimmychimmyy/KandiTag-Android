package com.kanditag.kanditag;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

/**
 * Created by Jim on 3/11/15.
 */
public class KandiGroupObjectParcelable implements Parcelable{

    private String qrCode, groupName;

    private ArrayList<KtUserObject> users;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeString(qrCode);
        parcel.writeString(groupName);
        parcel.writeList(users);
    }

    public KandiGroupObjectParcelable() {}

    public void setListOfUsers(ArrayList<KtUserObject> users) {
        this.users = users;
    }

    public ArrayList<KtUserObject> getListOfUsers() {
        return users;
    }

    public KandiGroupObjectParcelable(String qrCode, String groupName) {
        this.qrCode = qrCode;
        this.groupName = groupName;
    }

    public void setQrCode(String qrCode) {
        this.qrCode = qrCode;
    }

    public void setGroupName(String name) {
        this.groupName = name;
    }

    public String getQrCode() {
        return qrCode;
    }

    public String getGroupName() {
        return groupName;
    }
}
