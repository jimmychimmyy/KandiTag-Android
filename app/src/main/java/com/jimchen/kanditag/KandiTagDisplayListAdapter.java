package com.jimchen.kanditag;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.net.URL;
import java.util.ArrayList;

/**
 * Created by Jim on 6/11/15.
 */
public class KandiTagDisplayListAdapter extends ArrayAdapter<KtUserObject> {

    private Context context;
    public ArrayList<KtUserObject> ktUserObjects;

    public KandiTagDisplayListAdapter(Context context, int textViewResourceId,
                                   ArrayList<KtUserObject> list) {
        super(context, textViewResourceId);
        this.ktUserObjects = list;
        this.context = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        Holder holder;
        View convertView1 = convertView;

        if (convertView1 == null) {
            holder = new Holder();
            LayoutInflater vi = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView1 = vi.inflate(R.layout.kanditag_display_row, null);
            holder.username = (TextView) convertView1.findViewById(R.id.KandiTagDisplayRow_UsernameTextView);
            holder.profileImage = (ImageView) convertView1.findViewById(R.id.KandiTagDisplayRow_ProfileImageContainer);
            convertView1.setTag(holder);
        } else {
            holder = (Holder) convertView1.getTag();
        }

        KtUserObject user = getItem(position);
        holder.username.setText(user.getUsername());


        URL img_value = null;
        URL imgVal = null;

        /**

         try {
         StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
         StrictMode.setThreadPolicy(policy);
         img_value = new URL("https://graph.facebook.com/" + conversationItem.senderID + "/picture?width=80&height=80");
         Bitmap mIcon = BitmapFactory.decodeStream(img_value.openConnection().getInputStream());

         //make bitmap circular
         Bitmap circleBitmap = Bitmap.createBitmap(mIcon.getWidth(), mIcon.getHeight(), Bitmap.Config.ARGB_8888);
         BitmapShader shader = new BitmapShader(mIcon, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
         Paint paint = new Paint();
         paint.setShader(shader);
         Canvas canvas = new Canvas(circleBitmap);
         canvas.drawCircle(mIcon.getWidth()/2, mIcon.getHeight()/2, mIcon.getWidth()/2, paint);

         imgVal = new URL("https://graph.facebook.com/" + conversationItem.recipientID + "/picture?width=80&height=80");
         Bitmap mIcon2 = BitmapFactory.decodeStream(imgVal.openConnection().getInputStream());

         //make bitmap circular
         Bitmap bitmapCircular = Bitmap.createBitmap(mIcon2.getWidth(), mIcon2.getHeight(), Bitmap.Config.ARGB_8888);
         BitmapShader shader2 = new BitmapShader(mIcon2, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
         Paint paint2 = new Paint();
         paint2.setShader(shader2);
         Canvas canvas2 = new Canvas(bitmapCircular);
         canvas2.drawCircle(mIcon.getWidth()/2, mIcon.getHeight()/2, mIcon.getWidth()/2, paint);

         if (myFacebookID.equals(conversationItem.senderID)) {
         //holder.theirProfileImage.setImageBitmap(bitmapCircular);
         holder.myProfileImage.setImageBitmap(circleBitmap);
         holder.theirProfileImage.setImageBitmap(null);
         } else if (myFacebookID.equals(conversationItem.recipientID)) {
         holder.theirProfileImage.setImageBitmap(circleBitmap);
         holder.myProfileImage.setImageBitmap(null);
         //holder.myProfileImage.setImageBitmap(bitmapCircular);
         }

         } catch (Exception e) {
         e.printStackTrace();
         }

         **/

        return convertView1;
    }

    @Override
    public int getCount() {
        return ktUserObjects.size();
    }

    @Override
    public KtUserObject getItem(int position) {
        return ktUserObjects.get(position);
    }

    public void setArrayList(ArrayList<KtUserObject> list) {
        this.ktUserObjects = list;
        notifyDataSetChanged();
    }

    private class Holder {
        public TextView username;
        public ImageView profileImage;
    }


}
