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
 * Created by Jim on 1/29/15.
 */
public class FriendsGridViewAdapter extends ArrayAdapter<KtUserObjectParcelable> {
    private Context context;
    //private int layoutResourceId;
    private ArrayList<KtUserObjectParcelable> data = new ArrayList();

    private ArrayList<Bitmap> imageArrayList_follower;

    public FriendsGridViewAdapter(Context context, int layoutResourceId, ArrayList<KtUserObjectParcelable> data) {
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
            row = vi.inflate(R.layout.friends_grid_item, null);
            holder.fb_id = (TextView) row.findViewById(R.id.FriendsGridItem_Fbid);
            holder.kt_id = (TextView) row.findViewById(R.id.FriendsGridItem_Ktid);
            holder.user_name = (TextView) row.findViewById(R.id.FriendsGridItem_Name);
            holder.profilePicture = (ImageView) row.findViewById(R.id.FriendsGridItem_Picture);
            row.setTag(holder);
        } else {

            holder = (Holder) row.getTag();

        }

        KtUserObjectParcelable ktUserObjectParcelable = getItem(position);

        holder.fb_id.setText(ktUserObjectParcelable.getFb_id());
        holder.kt_id.setText(ktUserObjectParcelable.getKt_id());
        holder.user_name.setText(ktUserObjectParcelable.getUser_name());

        URL img_value = null;
            try {
                StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                StrictMode.setThreadPolicy(policy);
                img_value = new URL("https://graph.facebook.com/" + holder.fb_id.getText() + "/picture?width=150&height=150");
                Bitmap mIcon = BitmapFactory.decodeStream(img_value.openConnection().getInputStream());
                //holder.profilePicture.setImageBitmap(mIcon);
                //holder.profilePicture.setPadding(6, 6, 6, 6);

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
    public KtUserObjectParcelable getItem(int position) {
        return data.get(position);
    }

    public void setArrayList(ArrayList<KtUserObjectParcelable> followList) {
        this.data = followList;
        notifyDataSetChanged();
    }

    private class Holder {
        public TextView kt_id, fb_id, user_name;
        public ImageView profilePicture;
    }

}
