package com.jimchen.kanditag;

import com.google.gson.JsonObject;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by Jim on 5/26/15.
 */
public class JsonFeedObject extends JSONObject {

    private String kt_id, caption;
    private ArrayList<String> tags;
    // TODO
    // will also need something to hold image/video

    public JsonFeedObject() {}

    // setters

    public void setKTID(String kt_id) {
        this.kt_id = kt_id;
        try {
            this.put("kt_id", kt_id);
        } catch (JSONException e) {}
    }

    public void setCaption(String caption) {
        this.caption = caption;
        try {
            this.put("caption", caption);
        } catch (JSONException e) {}
    }

    public void setTags(ArrayList<String> tags) {
        this.tags = tags;
        try {
            this.put("tags", tags);
        } catch (JSONException e) {}
    }

    // getters

    public String getKTID() {
        return kt_id;
    }

    public String getCaption() {
        return caption;
    }

    public ArrayList<String> getTags() {
        return tags;
    }

}
