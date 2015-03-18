package com.kanditag.kanditag;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Shader;
import android.os.StrictMode;
import android.widget.ImageView;

import java.net.URL;

/**
 * Created by Jim on 2/18/15.
 */
public class MessageListItem {

    public String sender, description, name;
    public ImageView profilePicture;

    public MessageListItem() {}

    public MessageListItem(String sender, String description, String name) {
        this.sender = sender;
        this.description = description;
        this.name = name;
    }

    public String getSender() {
        return sender;
    }

    public String getDescription() {
        return description;
    }

    public String getName() {
        return name;
    }

    public boolean setSender(String sender) {
        this.sender = sender;
        return true;
    }

    public boolean setDescription(String description) {
        this.description = description;
        return true;
    }

    public boolean setName(String name) {
        this.name = name;
        return true;
    }
}
