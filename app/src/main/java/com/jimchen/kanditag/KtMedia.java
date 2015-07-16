package com.jimchen.kanditag;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

/**
 * Created by Jim on 6/25/15.
 */
public class KtMedia implements Parcelable, Comparable<KtMedia> {

    private String filename;
    private String uploadDate;
    private byte[] image;
    private int bytes_length;
    private ArrayList<String> metadata;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(filename);
        parcel.writeString(uploadDate);
        parcel.writeInt(image.length);
        parcel.writeByteArray(image);
        parcel.writeSerializable(metadata);
    }

    @Override
    public int compareTo(KtMedia obj) {
        return getUploadDate().compareTo(obj.getUploadDate());
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public void setUploadDate(String uploadDate) {
        this.uploadDate = uploadDate;
    }

    public void setImage(byte[] image, int bytes_length) {
        this.image = image;
        this.bytes_length = image.length;
    }

    public void setMetadata(ArrayList<String> metadata) {
        this.metadata = metadata;
    }

    public String getFilename() {
        return filename;
    }

    public String getUploadDate() {
        return uploadDate;
    }

    public byte[] getImage() {
        return image;
    }

    public int getBytesLength() {
        return bytes_length;
    }

    public ArrayList<String> getMetadata() {
        return metadata;
    }
}
