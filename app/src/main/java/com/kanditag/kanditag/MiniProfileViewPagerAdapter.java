package com.kanditag.kanditag;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Shader;
import android.os.StrictMode;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.net.URL;
import java.util.ArrayList;

/**
 * Created by Jim on 2/28/15.
 */
public class MiniProfileViewPagerAdapter extends ArrayAdapter<MiniProfileViewItem> {

    private Context context;
    private ArrayList<MiniProfileViewItem> data = new ArrayList<>();

    public MiniProfileViewPagerAdapter(Context context, int layoutResourceId, ArrayList<MiniProfileViewItem> data) {
        super (context, layoutResourceId);
        this.context = context;
        this.data = data;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Holder holder;
        View row = convertView;

        if (row == null) {
            holder = new Holder();
            LayoutInflater vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = vi.inflate(R.layout.mini_profile_view_item, null);
            holder.fb_id = (TextView) row.findViewById(R.id.miniProfileViewItem_facebookid);
            holder.user_name = (TextView) row.findViewById(R.id.miniProfileViewItem_name);
            holder.placement = (TextView) row.findViewById(R.id.miniProfileViewItem_placement);
            holder.profilePicture = (ImageView) row.findViewById(R.id.miniProfileViewItem_profilePic);
        } else {
            holder = (Holder) row.getTag();
        }

        URL img_value = null;

        try {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
            img_value = new URL("https://graph.facebook.com/" + holder.fb_id + "/picture?width=120&height=120");
            Bitmap mIcon = BitmapFactory.decodeStream(img_value.openConnection().getInputStream());

            //make bitmap circular
            Bitmap circleBitmap = Bitmap.createBitmap(mIcon.getWidth(), mIcon.getHeight(), Bitmap.Config.ARGB_8888);
            BitmapShader shader = new BitmapShader(mIcon, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
            Paint paint = new Paint();
            paint.setShader(shader);
            Canvas canvas = new Canvas(circleBitmap);
            canvas.drawCircle(mIcon.getWidth()/2, mIcon.getHeight()/2, mIcon.getWidth()/2, paint);

            holder.profilePicture.setImageBitmap(circleBitmap);

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
    public MiniProfileViewItem getItem(int position) {
        return data.get(position);
    }

    public void setArrayList(ArrayList<MiniProfileViewItem> followList) {
        this.data = followList;
        notifyDataSetChanged();
    }

    private class Holder {
        public TextView fb_id, user_name, placement;
        public ImageView profilePicture;
    }
}
