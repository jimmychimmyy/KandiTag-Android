package com.jimchen.kanditag;

import android.os.Parcel;
import android.os.Parcelable;

import java.lang.reflect.Array;
import java.util.ArrayList;

/**
 * Created by Jim on 5/26/15.
 */
public class FeedObjectParcelable implements Parcelable {

    // id of whoever the image/video belongs to
    private String kt_id;
    private String user_name;

    // string representation of media
    private String media;

    // caption
    private String caption;

    // tags
    private ArrayList<String> tags;

    // comments
    private ArrayList<String> comments;

    // tagged users
    private ArrayList<String> tagged_users;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(kt_id);
        parcel.writeString(user_name);
        parcel.writeString(media);
        parcel.writeString(caption);
        parcel.writeList(tags);
        parcel.writeList(comments);
        parcel.writeList(tagged_users);
    }

    // constructor
    public FeedObjectParcelable() {}

    // setters
    public void setKTID(String kt_id) {
        this.kt_id = kt_id;
    }

    public void setUserName(String name) {
        this.user_name = name;
    }

    public void setMedia(String media) {
        this.media = media;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public void setTags(ArrayList<String> tags) {
        this.tags = tags;
    }

    public void setComments(ArrayList<String> comments) {
        this.comments = comments;
    }


    public void setTaggedUsers(ArrayList<String> ktids) {
        this.tagged_users = ktids;
    }

    // getters

    public String getKTID() {
        return kt_id;
    }

    public String getUserName() {
        return user_name;
    }

    public String getMedia() {
        return media;
    }

    public String getCaption() {
        return caption;
    }

    public ArrayList<String> getTags() {
        return tags;
    }

    public ArrayList<String> getComments() {
        return comments;
    }

    public ArrayList<String> getTagged_users() {
        return tagged_users;
    }
}
