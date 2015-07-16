package com.jimchen.kanditag;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by Jim on 4/17/15.
 */
public class MessageListViewAdapter extends ArrayAdapter<MessageRowItem> {

    private Context context;
    //array for constructor
    private ArrayList<MessageRowItem> messageRowItems;

    private SharedPreferences sharedPreferences;
    private String MY_KT_ID, MY_FB_ID, MY_USER_NAME;
    public static final String USER_PREFERENCES = "com.jimchen.kanditag.extra.PREFERENCES";
    public static final String USERNAME = "com.jimchen.kanditag.extra.USERNAME";
    public static final String FBID = "com.jimchen.kanditag.extra.FBID";
    public static final String KTID = "com.jimchen.kanditag.extra.KTID";

    //constructor
    public MessageListViewAdapter(Context context, int layoutResourceId, ArrayList<MessageRowItem> messageRowItems) {
        super(context, layoutResourceId);
        this.context = context;
        this.messageRowItems = messageRowItems;
        sharedPreferences = context.getSharedPreferences(USER_PREFERENCES, Context.MODE_PRIVATE);
        MY_KT_ID = sharedPreferences.getString(KTID, "");
        MY_USER_NAME = sharedPreferences.getString(USERNAME, "");
        MY_FB_ID = sharedPreferences.getString(FBID, "");
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Holder holder;
        View convertToView = convertView;

        if (convertToView == null) {
            holder = new Holder();
            LayoutInflater vi = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertToView = vi.inflate(R.layout.message_fragment_row_item, null);
            holder.message_text = (TextView) convertToView.findViewById(R.id.MessageFragmentRowItem_MessageText);
            holder.message_sender = (TextView) convertToView.findViewById(R.id.MessageFragmentRowItem_SenderName);
            //holder.message_kandiname = (TextView) convertToView.findViewById(R.id.MessageFragmentRowItem_KandiName);
            holder.message_timestamp = (TextView) convertToView.findViewById(R.id.MessageFragmentRowItem_TimeStamp);
            holder.profileImage = (ImageView) convertToView.findViewById(R.id.MessageFragmentRowItem_SenderProfileImageContainer);
            convertToView.setTag(holder);
        } else {
            holder = (Holder) convertToView.getTag();
        }

        MessageRowItem rowItem = getItem(position);
        holder.message_text.setText(rowItem.getMessageContent());

        try {
            if (rowItem.getFrom_Id().equals(MY_KT_ID)) {
                holder.message_sender.setText(rowItem.getTo_Name());
            } else {
                holder.message_sender.setText(rowItem.getFrom_Name());
            }
        } catch (NullPointerException e) {}

            if (rowItem.getTo_Kandi_Name() != null) {
                //holder.message_kandiname.setText(rowItem.getTo_Kandi_Name());
            }


        //format the timestamp into actual time AM/PM
        try {
            Date d = new Date(Long.parseLong(rowItem.getTimestamp()));
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd " + "\n" + "hh:mm:ss");
            SimpleDateFormat dateOfMessage = new SimpleDateFormat("yyyy-MM-dd");
            SimpleDateFormat timeOfMessage = new SimpleDateFormat("hh:mm a");

            String date = dateFormat.format(d);
            String message_date = dateOfMessage.format(d);
            String message_time = timeOfMessage.format(d);

            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);

            // get today's date
            Date today = calendar.getTime();

            if (d.before(today)) {
                holder.message_timestamp.setText(message_date);
            } else {
                holder.message_timestamp.setText(message_time);
            }

            //holder.message_timestamp.setText(date);

        } catch (Exception e) {}

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
        public TextView message_text, message_sender, message_timestamp, message_kandiname;
        public ImageView profileImage;
    }

}
