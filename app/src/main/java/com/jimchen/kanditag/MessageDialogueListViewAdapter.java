package com.jimchen.kanditag;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by Jim on 4/18/15.
 */
public class MessageDialogueListViewAdapter extends ArrayAdapter<MessageRowItem> {
    private Context context;
    //array for constructor
    private ArrayList<MessageRowItem> messageRowItems;

    private SharedPreferences sharedPreferences;
    private String MY_KT_ID, MY_USER_NAME;
    public static final String USER_PREFERENCES = "com.jimchen.kanditag.extra.PREFERENCES";
    public static final String USERNAME = "com.jimchen.kanditag.extra.USERNAME";
    public static final String KTID = "com.jimchen.kanditag.extra.KTID";

    //constructor
    public MessageDialogueListViewAdapter(Context context, int layoutResourceId, ArrayList<MessageRowItem> messageRowItems) {
        super(context, layoutResourceId);
        this.context = context;
        this.messageRowItems = messageRowItems;
        sharedPreferences = context.getSharedPreferences(USER_PREFERENCES, Context.MODE_PRIVATE);
        MY_KT_ID = sharedPreferences.getString(KTID, "");
        MY_USER_NAME = sharedPreferences.getString(USERNAME, "");
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Holder holder;
        View convertToView = convertView;

        if (convertToView == null) {
            holder = new Holder();
            LayoutInflater vi = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertToView = vi.inflate(R.layout.message_dialogue_row_item, null);
            holder.message_text = (TextView) convertToView.findViewById(R.id.MessageDialogueRowItem_MessageText);
            //holder.message_sender = (TextView) convertToView.findViewById(R.id.MessageDialogueRowItem_SenderName);
            //holder.message_timestamp = (TextView) convertToView.findViewById(R.id.MessageDialogueRowItem_TimeStamp);
            holder.profileImage = (ImageView) convertToView.findViewById(R.id.MessageDialogueRowItem_SenderProfileImageContainer);
            convertToView.setTag(holder);
        } else {
            holder = (Holder) convertToView.getTag();
        }

        MessageRowItem rowItem = getItem(position);
        holder.message_text.setText(rowItem.getMessageContent());

        // TODO need to distinguish text between the two people

        if (rowItem.getFrom_Id().equals(MY_KT_ID)) {
            holder.message_text.setGravity(Gravity.RIGHT);
            holder.profileImage.setVisibility(View.GONE);

            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.MATCH_PARENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT);

            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);

            holder.message_text.setLayoutParams(layoutParams);

        } else if (!rowItem.getFrom_Id().equals(MY_KT_ID)) {
            holder.message_text.setGravity(Gravity.LEFT);
        }

        /**
        try {
            if (rowItem.getFrom_Name().equals(MY_USER_NAME)) {
                holder.message_sender.setText(rowItem.getFrom_Name());
            } else if (rowItem.getTo_Name().equals(MY_USER_NAME)) {
                holder.message_sender.setText(rowItem.getTo_Name());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } **/

        //format the timestamp into actual time AM/PM
        /**
        try {
            Date d = new Date(Long.parseLong(rowItem.getTimestamp()));
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd " + "\n" + "hh:mm:ss");
            String date = dateFormat.format(d);
            holder.message_timestamp.setText(date);
        } catch (Exception e) {} **/

        return convertToView;
    }

    @Override
    public int getCount() {
        return messageRowItems.size();
    }

    @Override
    public MessageRowItem getItem(int position) {
        return messageRowItems.get(position);
    }

    public void setArrayList(ArrayList<MessageRowItem> messageRowItems) {
        this.messageRowItems = messageRowItems;
        notifyDataSetChanged();
    }

    private class Holder {
        public TextView message_text, message_timestamp;
        public ImageView profileImage;
    }
}
