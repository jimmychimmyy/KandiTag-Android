package com.jimchen.kanditag;

import java.net.URL;
import java.util.ArrayList;

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

/**
 * Created by Jim on 2/18/15.
 */
public class MessageListAdapter extends ArrayAdapter<MessageListItem> {
    private Context ctx;
    public ArrayList<MessageListItem> messageListArray;

    public MessageListAdapter(Context context, int textViewResourceId,
                              ArrayList<MessageListItem> messageListArray) {
        super(context, textViewResourceId);
        this.messageListArray = messageListArray;
        this.ctx = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        Holder holder;
        View convertView1 = convertView;

        if (convertView1 == null) {
            holder = new Holder();
            LayoutInflater vi = (LayoutInflater) ctx
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView1 = vi.inflate(R.layout.message_list_item, null);
            holder.messageTo = (TextView) convertView1.findViewById(R.id.MessageListItem_userfbid);
            holder.messageToName = (TextView) convertView1.findViewById(R.id.MessageListItem_name);
            holder.messageContent = (TextView) convertView1.findViewById(R.id.MessageListItem_description);
            holder.profileImage = (ImageView) convertView1.findViewById(R.id.MessageListItem_profilePic);
            convertView1.setTag(holder);
        } else {
            holder = (Holder) convertView1.getTag();
        }

        MessageListItem message = getItem(position);

        holder.messageTo.setText(message.sender);

        holder.messageToName.setText(message.name +" : ");

        holder.messageContent.setText(message.description);

        URL img_value = null;

        try {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
            img_value = new URL("https://graph.facebook.com/" + message.sender + "/picture?width=160&height=160");
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
        return messageListArray.size();
    }

    @Override
    public MessageListItem getItem(int position) {
        return messageListArray.get(position);
    }

    public void setArrayList(ArrayList<MessageListItem> messageList) {
        this.messageListArray = messageList;
        notifyDataSetChanged();
    }

    private class Holder {
        public TextView messageTo, messageContent, messageToName;
        public ImageView profileImage;
    }

}