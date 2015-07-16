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
 * Created by Jim on 6/15/15.
 */
public class PostOptionsListAdapter extends ArrayAdapter<KtUserObjectParcelable> {

    private Context context;
    private ArrayList<KtUserObjectParcelable> ktUsersList;

    public PostOptionsListAdapter(Context context, int textViewResourceId, ArrayList<KtUserObjectParcelable> list) {
        super(context, textViewResourceId);
        this.context = context;
        this.ktUsersList = list;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        Holder holder;
        View convertView1 = convertView;

        if (convertView1 == null) {
            holder = new Holder();
            LayoutInflater vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView1 = vi.inflate(R.layout.post_options_row, null);
            holder.username = (TextView) convertView1.findViewById(R.id.PostOptionsRow_UsernameContainer);
            convertView1.setTag(holder);
        } else {
            holder = (Holder) convertView1.getTag();
        }

        KtUserObjectParcelable obj = getItem(position);

        holder.username.setText(obj.getUsername());

        return convertView1;

    }

    @Override
    public int getCount() {
        return ktUsersList.size();
    }

    @Override
    public KtUserObjectParcelable getItem(int position) {
        return ktUsersList.get(position);
    }

    public void setArrayList(ArrayList<KtUserObjectParcelable> list) {
        this.ktUsersList = list;
        notifyDataSetChanged();
    }

    private class Holder {
        private TextView username;
    }
}
