package com.kanditag.kanditag;

import android.app.Activity;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.StrictMode;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.lang.reflect.Array;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by Jim on 1/29/15.
 */
public class FollowGridViewAdapter extends ArrayAdapter<FollowGridItem> {
    private Context context;
    //private int layoutResourceId;
    private ArrayList<FollowGridItem> data = new ArrayList();

    private ArrayList<Bitmap> imageArrayList_follower;

    public FollowGridViewAdapter(Context context, int layoutResourceId, ArrayList<FollowGridItem> data) {
        super(context, layoutResourceId);
        this.data = data;
        this.context = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Holder holder;
        View row = convertView;

        if (row == null) {
            holder = new Holder();
            LayoutInflater vi = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = vi.inflate(R.layout.follow_grid_item, null);
            holder.fb_id = (TextView) row.findViewById(R.id.followGridItem_FBID);
            holder.kt_id = (TextView) row.findViewById(R.id.followGridItem_KTID);
            holder.user_name = (TextView) row.findViewById(R.id.followGridItem_NAME);
            holder.profilePicture = (ImageView) row.findViewById(R.id.followGridItem_PICTURE);
            row.setTag(holder);
        } else {

            holder = (Holder) row.getTag();

        }

        FollowGridItem followGridItem = getItem(position);

        holder.fb_id.setText(followGridItem.fb_id);
        holder.kt_id.setText(followGridItem.kt_id);
        holder.user_name.setText(followGridItem.user_name);

        URL img_value = null;
            try {
                StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                StrictMode.setThreadPolicy(policy);
                img_value = new URL("https://graph.facebook.com/" + holder.fb_id.getText() + "/picture?width=150&height=150");
                Bitmap mIcon = BitmapFactory.decodeStream(img_value.openConnection().getInputStream());
                holder.profilePicture.setImageBitmap(mIcon);
                //holder.profilePicture.setPadding(6, 6, 6, 6);
            } catch (Exception e) {
                e.printStackTrace();
            }

    return row;

    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public FollowGridItem getItem(int position) {
        return data.get(position);
    }

    public void setArrayList(ArrayList<FollowGridItem> followList) {
        this.data = followList;
        notifyDataSetChanged();
    }

    private class Holder {
        public TextView kt_id, fb_id, user_name;
        public ImageView profilePicture;
    }

}
