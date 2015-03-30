package com.kanditag.kanditag;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Jim on 3/18/15.
 */
public class GroupKandiObjectListAdapter extends ArrayAdapter<KtUserObjectParcelable> {

    private Context context;

    private ArrayList<KtUserObjectParcelable> ktUserObjectParcelableArrayList;

    public GroupKandiObjectListAdapter(Context context, int textViewResourceId, ArrayList<KtUserObjectParcelable> list) {
        super(context, textViewResourceId);
        this.context = context;
        this.ktUserObjectParcelableArrayList = list;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Holder holder = new Holder();
        View convertView1 = convertView;

        if (convertView1 == null) {
            holder = new Holder();
            LayoutInflater vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView1 = vi.inflate(R.layout.kandi_group_list_item, null);
            holder.kandi_name = (TextView) convertView1.findViewById(R.id.KandiGroupListItem_KandiName);
            holder.pImage0 = (ImageView) convertView1.findViewById(R.id.KandiGroupListItem_0);
            holder.pImage1 = (ImageView) convertView1.findViewById(R.id.KandiGroupListItem_1);
            holder.pImage2 = (ImageView) convertView1.findViewById(R.id.KandiGroupListItem_2);
            holder.pImage3 = (ImageView) convertView1.findViewById(R.id.KandiGroupListItem_3);
            holder.pImage4 = (ImageView) convertView1.findViewById(R.id.KandiGroupListItem_4);
            holder.pImage5 = (ImageView) convertView1.findViewById(R.id.KandiGroupListItem_5);
            holder.pImage6 = (ImageView) convertView1.findViewById(R.id.KandiGroupListItem_6);
            holder.pImage7 = (ImageView) convertView1.findViewById(R.id.KandiGroupListItem_7);
            convertView1.setTag(holder);
        } else {
            holder = (Holder) convertView1.getTag();
        }

        KtUserObjectParcelable item = getItem(position);

        holder.kandi_name.setText(item.getUser_name());

        return convertView1;
    }

    @Override
    public int getCount() {
        return ktUserObjectParcelableArrayList.size();
    }

    @Override
    public KtUserObjectParcelable getItem(int position) {
        return ktUserObjectParcelableArrayList.get(position);
    }

    public void setArrayList(ArrayList<KtUserObjectParcelable> list) {
        this.ktUserObjectParcelableArrayList = list;
        notifyDataSetChanged();
    }

    private class Holder {
        public TextView kandiQr, kandi_name, user0, user1, user2, user3, user4, user5, user6, user7;
        public ImageView pImage0, pImage1, pImage2, pImage3, pImage4, pImage5, pImage6, pImage7;
    }
}
