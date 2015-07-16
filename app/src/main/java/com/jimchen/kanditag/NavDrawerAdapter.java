package com.jimchen.kanditag;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Jim on 6/20/15.
 */
public class NavDrawerAdapter extends ArrayAdapter<NavDrawerItem> {

    private final Context context;
    private final int layoutResourceId;
    private ArrayList<NavDrawerItem> data = null;

    public NavDrawerAdapter(Context context, int layoutResourceId, ArrayList<NavDrawerItem> data) {
        super(context, layoutResourceId);
        this.context = context;
        this.layoutResourceId = layoutResourceId;
        this.data = data;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        Holder holder;
        View row = convertView;

        if (row == null) {
            holder = new Holder();
            LayoutInflater vi = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = vi.inflate(R.layout.nav_drawer_layout, null);
            holder.icon = (ImageView) row.findViewById(R.id.NavDrawerLayout_IconContainer);
            holder.title = (TextView) row.findViewById(R.id.NavDrawerLayout_TitleContainer);
            holder.selected = (View) row.findViewById(R.id.NavDrawerLayout_Selected);
            row.setTag(holder);
        } else {

            holder = (Holder) row.getTag();

        }

        NavDrawerItem item = getItem(position);
        holder.title.setText(item.name);

        return row;
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public NavDrawerItem getItem(int position) {
        return data.get(position);
    }

    public void setArrayList(ArrayList<NavDrawerItem> followList) {
        this.data = followList;
        notifyDataSetChanged();
    }

    private class Holder {
        public View selected;
        public TextView title;
        public ImageView icon;
    }

}
