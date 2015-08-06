package com.jimchen.kanditag;

import java.util.ArrayList;

/**
 * Created by Jim on 7/31/15.
 */
public class FeedItem {

    private String _id;
    private String filename;
    private String uploadDate;
    private ArrayList<String> metadata;

    public void setID(String _id) {
        this._id = _id;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public void setUploadDate(String date) {
        this.uploadDate = date;
    }

    public void setMetadata(ArrayList<String> metadata){
        this.metadata = metadata;
    }

    public String getID() {
        return _id;
    }

    public String getFilename() {
        return filename;
    }

    public String getUploadDate() {
        return uploadDate;
    }

    public ArrayList<String> getMetadata() {
        return metadata;
    }

}
