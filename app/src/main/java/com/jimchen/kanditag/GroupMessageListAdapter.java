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
 * Created by Jim on 3/16/15.
 */
public class GroupMessageListAdapter extends ArrayAdapter<GroupMessageItem> {

    private Context context;
    private String myFacebookID;
    private ArrayList<GroupMessageItem> groupMessageItemArrayList;

    private KtDatabase myDatabase;
    private ArrayList<KandiObject> kandiObjects;

    public GroupMessageListAdapter(Context context, int textViewResourceId,
                                   ArrayList<GroupMessageItem> list, String myFacebookID) {
        super(context, textViewResourceId);
        this.groupMessageItemArrayList = list;
        this.context = context;
        this.myFacebookID = myFacebookID;
        myDatabase = new KtDatabase(context);
        kandiObjects = myDatabase.getKandi();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        Holder holder;
        View convertView1 = convertView;

        if (convertView1 == null) {
            holder = new Holder();
            LayoutInflater vi = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView1 = vi.inflate(R.layout.group_message_list_item, null);
            holder.myMessage = (TextView) convertView1.findViewById(R.id.GroupMessageListItem_MyMessage);
            holder.message = (TextView) convertView1.findViewById(R.id.GroupMessageListItem_TheirMessage);
            holder.from_name = (TextView) convertView1.findViewById(R.id.GroupMessageListItem_Bubble);
            convertView1.setTag(holder);
        } else {
            holder = (Holder) convertView1.getTag();
        }

        GroupMessageItem item = getItem(position);

        //TODO probably use a different method to get the kandi name or save the kandi name into the database for faster access

        for (int i = 0; i < kandiObjects.size(); i++) {
            if (kandiObjects.get(i).getQrCode().equals(item.getQrCode())) {
                holder.from_name.setText(kandiObjects.get(i).getKandi_name());
            }
        }

        holder.message.setText(item.getMessage());

        //holder.message.setText(conversationItem.message);
        //holder.senderName.setText(conversationItem.senderName);
        //holder.recipientName.setText(conversationItem.recipientName);
        //holder.time.setText(conversationItem.time);

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
        return groupMessageItemArrayList.size();
    }

    @Override
    public GroupMessageItem getItem(int position) {
        return groupMessageItemArrayList.get(position);
    }

    public void setArrayList(ArrayList<GroupMessageItem> messageList) {
        this.groupMessageItemArrayList = messageList;
        notifyDataSetChanged();
    }

    private class Holder {
        public TextView message, myMessage, from_id, from_name, qrCode, time;
        public ImageView profilePic;
    }

}
