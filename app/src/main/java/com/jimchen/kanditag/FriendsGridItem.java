package com.jimchen.kanditag;

import android.widget.ImageView;

/**
 * Created by Jim on 2/20/15.
 */
public class FriendsGridItem {

    public String fb_id, kt_id, user_name;
    public ImageView profilePicture;

    public FriendsGridItem() {}

    public FriendsGridItem(String fb, String kt, String name) {
        this.fb_id = fb;
        this.kt_id = kt;
        this.user_name = name;
    }

    public String getFB_ID() {
        return fb_id;
    }

    public String getKT_ID() {
        return kt_id;
    }

    public String getUSER_NAME() {
        return user_name;
    }

    public boolean setFB_ID(String fb) {
        this.fb_id = fb;
        return true;
    }

    public boolean setKT_ID(String kt) {
        this.kt_id = kt;
        return true;
    }

    public boolean setUSER_NAME(String name) {
        this.user_name = name;
        return true;
    }
}
