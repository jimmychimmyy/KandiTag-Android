package com.kanditag.kanditag;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.widget.ImageView;

/**
 * Created by Jim on 3/30/15.
 */
public class GalleryGridItem {

    public String _id;
    public ImageView image;

    public GalleryGridItem() {}

    public void set_id(String id) {
        this._id = id;
    }

    public void setImage(Bitmap bitmap) {
        this.image.setImageBitmap(bitmap);
    }

    public String get_id() {
        return this._id;
    }

    public Matrix getImageBitmap() {
        return image.getImageMatrix();
    }

}
