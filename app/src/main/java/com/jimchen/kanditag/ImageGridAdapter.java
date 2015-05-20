package com.jimchen.kanditag;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

/**
 * Created by Jim on 5/5/15.
 */
public class ImageGridAdapter extends ArrayAdapter<byte[]> {

    private Context context;
    private ArrayList<byte[]> images;

    public ImageGridAdapter(Context context, int layoutResourceId, ArrayList<byte[]> data) {
        super(context, layoutResourceId);
        this.context = context;
        this.images = data;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Holder holder;
        View row = convertView;

        if (row == null) {
            holder = new Holder();
            LayoutInflater vi = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = vi.inflate(R.layout.gallery_grid_item, null);
            holder._id = (TextView) row.findViewById(R.id.GalleryGridItem_ImageId);
            holder.image = (ImageView) row.findViewById(R.id.GalleryGridItem_Image);
            row.setTag(holder);
        } else {

            holder = (Holder) row.getTag();

        }

        byte[] image = decompressByteArray(getItem(position));

        Bitmap bitmap = BitmapFactory.decodeByteArray(image, 0, image.length);

        if (context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            Bitmap scaledB = Bitmap.createScaledBitmap(bitmap, bitmap.getWidth() / 2, bitmap.getHeight() / 2, true);
            int width = scaledB.getWidth();
            int height = scaledB.getHeight();

            //rotate 90 degrees with matrix
            Matrix matrix = new Matrix();
            matrix.postRotate(90);
            bitmap = Bitmap.createBitmap(scaledB, 0, 0, width, height, matrix, true);

        }

        holder.image.setImageBitmap(bitmap);

        return row;

    }

    @Override
    public int getCount() {
        return images.size();
    }

    @Override
    public byte[] getItem(int position) {
        return images.get(position);
    }

    public void setArrayList(ArrayList<byte[]> data) {
        this.images = data;
        notifyDataSetChanged();
    }

    private class Holder {
        public ImageView image;
        public TextView _id;
    }

    // method to decompress image before posting
    private byte[] decompressByteArray(byte[] bytes) {

        ByteArrayOutputStream baos = null;
        Inflater inflater = new Inflater();
        inflater.setInput(bytes);
        baos = new ByteArrayOutputStream();
        byte[] temp = new byte[4*1024];
        try {
            while (!inflater.finished()) {
                int size = inflater.inflate(temp);
                baos.write(temp, 0, size);
            }
        } catch (Exception e) {

        } finally {
            try {
                if (baos != null) baos.close();
            } catch (Exception e) {}
        }

        return baos.toByteArray();
    }

}
