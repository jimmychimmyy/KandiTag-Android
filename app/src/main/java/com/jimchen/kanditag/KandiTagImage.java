package com.jimchen.kanditag;

import java.util.ArrayList;

/**
 * Created by Jim on 6/18/15.
 */
public class KandiTagImage {

    private byte[] image;
    private ArrayList<String> tags;

    public void setImage(byte[] image) {
        this.image = image;
    }

    public void setTags(ArrayList<String> tags){
        this.tags = tags;
    }

    public byte[] getImage() {
        return image;
    }

    public ArrayList<String> getTags() {
        return tags;
    }
}
