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
 * Created by Jim on 2/23/15.
 */
public class ConversationListAdapter extends ArrayAdapter<ConversationListItem> {

    private Context ctx;
    private String myFacebookID;
    public ArrayList<ConversationListItem> conversationListArray;

    public ConversationListAdapter(Context context, int textViewResourceId,
                              ArrayList<ConversationListItem> conversationListArray, String myFacebookID) {
        super(context, textViewResourceId);
        this.conversationListArray = conversationListArray;
        this.ctx = context;
        this.myFacebookID = myFacebookID;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        Holder holder;
        View convertView1 = convertView;

        if (convertView1 == null) {
            holder = new Holder();
            LayoutInflater vi = (LayoutInflater) ctx
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView1 = vi.inflate(R.layout.conversation_list_item, null);
            //holder.message = (TextView) convertView1.findViewById(R.id.conversationListItem_bubble);
            //holder.senderName = (TextView) convertView1.findViewById(R.id.conversationListItem_myUserName);
            //holder.recipientName = (TextView) convertView1.findViewById(R.id.conversationListItem_otherUserName);
            holder.myProfileImage = (ImageView) convertView1.findViewById(R.id.conversationListItem_myPic);
            holder.theirProfileImage = (ImageView) convertView1.findViewById(R.id.conversationListItem_otherUserPic);
            holder.myMessage = (TextView) convertView1.findViewById(R.id.conversationListItem_myMessage);
            holder.theirMessage = (TextView) convertView1.findViewById(R.id.conversationListItem_otherUserMessage);
            convertView1.setTag(holder);
        } else {
            holder = (Holder) convertView1.getTag();
        }

        ConversationListItem conversationItem = getItem(position);

        //if the sender id is my facebook id, set the myMessage
        if (conversationItem.senderID.equals(myFacebookID)) {
            holder.myMessage.setText(conversationItem.message);
            holder.theirMessage.setText("");
        } else if (conversationItem.recipientID.equals(myFacebookID)) {
            holder.theirMessage.setText(conversationItem.message);
            holder.myMessage.setText("");
        }

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
        return conversationListArray.size();
    }

    @Override
    public ConversationListItem getItem(int position) {
        return conversationListArray.get(position);
    }

    public void setArrayList(ArrayList<ConversationListItem> messageList) {
        this.conversationListArray = messageList;
        notifyDataSetChanged();
    }

    private class Holder {
        public TextView message, senderID, recipientID, senderName, recipientName, time, myMessage, theirMessage;
        public ImageView myProfileImage, theirProfileImage;
    }


}
