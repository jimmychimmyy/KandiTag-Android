package com.jimchen.kanditag;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

/**
 * Created by Jim on 6/20/15.
 */
public class NavDrawerItem {

    public Drawable icon;
    public String name;

    public NavDrawerItem(Drawable icon, String name) {
        this.icon = icon;
        this.name = name;
    }
}
