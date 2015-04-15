package com.jimchen.kanditag;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Jim on 4/10/15.
 */
public class UserAndGroupsListAdapter extends ArrayAdapter<UserAndGroupListItem>{

    private Context context;
    private ArrayList<UserAndGroupListItem> data;

    public UserAndGroupsListAdapter(Context context, int layoutResourceId, ArrayList<UserAndGroupListItem> data) {
        super(context, layoutResourceId);
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
            row = vi.inflate(R.layout.user_and_group_list_item, null);
            holder.sender = (TextView) row.findViewById(R.id.UserAndGroupListItem_SenderNameTextView);
            holder.profilePicture = (ImageView) row.findViewById(R.id.UserAndGroupListItem_ProfilePictureImageView);
            holder.sender_id = (TextView) row.findViewById(R.id.UserAndGroupListItem_SenderId);
            holder.message = (TextView) row.findViewById(R.id.UserAndGroupListItem_MessageTextView);
            holder.group = (TextView) row.findViewById(R.id.UserAndGroupListItem_GroupName);
            holder.group_id = (TextView) row.findViewById(R.id.UserAndGroupListItem_GroupId);
        } else {
            holder = (Holder) row.getTag();
        }

        UserAndGroupListItem item = getItem(position);

        holder.message.setText(item.getMessage());
        holder.sender_id.setText(item.getSender_id());
        holder.sender.setText(item.getSender_name());

        try {
            holder.group_id.setText(item.getGroup_id());
        } catch (NullPointerException nullEx) {}

        try {
            holder.group.setText(item.getGroup_name());
        } catch (NullPointerException nullEx) {}

        //TODO need to set up the profile picture as well

        return row;
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public UserAndGroupListItem getItem(int position) {
        return data.get(position);
    }

    public void setArrayList(ArrayList<UserAndGroupListItem> followList) {
        this.data = followList;
        notifyDataSetChanged();
    }

    private class Holder {
        public TextView sender, sender_id, group, group_id, message;
        public ImageView profilePicture;
    }
}
