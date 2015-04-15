package com.jimchen.kanditag;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Jim on 3/30/15.
 */
public class GalleryGridAdapter extends ArrayAdapter<GalleryGridItem> {

    private Context context;
    private ArrayList<GalleryGridItem> data;

    public GalleryGridAdapter(Context context, int layoutResourceId, ArrayList<GalleryGridItem> list) {
        super(context, layoutResourceId);
        this.context = context;
        this.data = list;
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

        GalleryGridItem galleryGridItem = getItem(position);
        //FriendsGridItem friendsGridItem = getItem(position);

        holder._id.setText(galleryGridItem._id);
        holder.image.setImageMatrix(galleryGridItem.getImageBitmap());

        /**
        URL img_value = null;
        try {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
            img_value = new URL("https://graph.facebook.com/" + holder.fb_id.getText() + "/picture?width=200&height=200");
            Bitmap mIcon = BitmapFactory.decodeStream(img_value.openConnection().getInputStream());
            holder.profilePicture.setImageBitmap(mIcon);
            //holder.profilePicture.setPadding(6, 6, 6, 6);
        } catch (Exception e) {
            e.printStackTrace();
        }
         **/

        return row;

    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public GalleryGridItem getItem(int position) {
        return data.get(position);
    }

    public void setArrayList(ArrayList<GalleryGridItem> followList) {
        this.data = followList;
        notifyDataSetChanged();
    }

    private class Holder {
        public ImageView image;
        public TextView _id;
    }

}
