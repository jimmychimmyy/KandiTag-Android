package com.jimchen.kanditag;

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
 * Created by Jim on 3/6/15.
 */
public class KtUserObjectListAdapter extends ArrayAdapter<KtUserObjectParcelable> {

    private Context context;

    public ArrayList<KtUserObjectParcelable> ktUserObjectArrayList;

    public KtUserObjectListAdapter(Context context, int textViewResourceId, ArrayList<KtUserObjectParcelable> list) {
        super(context, textViewResourceId);
        this.ktUserObjectArrayList = list;
        this.context = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        Holder holder;
        View convertView1 = convertView;

        if (convertView1 == null) {
            holder = new Holder();
            LayoutInflater vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView1 = vi.inflate(R.layout.users_groups_for_new_message_list_item, null);
            holder.kt_id = (TextView) convertView1.findViewById(R.id.usersGroupsForNewMessageListItem_ktid);
            holder.fb_id = (TextView) convertView1.findViewById(R.id.GroupsForNewMessageListItem_qrCode);
            holder.user_name = (TextView) convertView1.findViewById(R.id.GroupsForNewMessageListItem_KandiName);
            holder.profileImage = (ImageView) convertView1.findViewById(R.id.GroupsForNewMessageListItem_ImageView);
            convertView1.setTag(holder);
        } else {
            holder = (Holder) convertView1.getTag();
        }

        KtUserObjectParcelable user = getItem(position);

        holder.user_name.setText(user.getUser_name());
        holder.kt_id.setText(user.getKt_id());
        holder.fb_id.setText(user.getFb_id());

        URL img_value = null;

        try {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
            img_value = new URL("https://graph.facebook.com/" + user.getFb_id() + "/picture?width=160&height=160");
            Bitmap mIcon = BitmapFactory.decodeStream(img_value.openConnection().getInputStream());

            //make bitmap circular
            Bitmap circleBitmap = Bitmap.createBitmap(mIcon.getWidth(), mIcon.getHeight(), Bitmap.Config.ARGB_8888);
            BitmapShader shader = new BitmapShader(mIcon, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
            Paint paint = new Paint();
            paint.setShader(shader);
            Canvas canvas = new Canvas(circleBitmap);
            canvas.drawCircle(mIcon.getWidth()/2, mIcon.getHeight()/2, mIcon.getWidth()/2, paint);

            holder.profileImage.setImageBitmap(circleBitmap);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return convertView1;

    }

    @Override
    public int getCount() {
        return ktUserObjectArrayList.size();
    }

    @Override
    public KtUserObjectParcelable getItem(int position) {
        return ktUserObjectArrayList.get(position);
    }

    public void setArrayList(ArrayList<KtUserObjectParcelable> list) {
        this.ktUserObjectArrayList = list;
        notifyDataSetChanged();
    }

    private class Holder {
        public TextView kt_id, fb_id, user_name;
        public ImageView profileImage;
    }
}
