package com.jimchen.kanditag;

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
 * Created by Jim on 4/19/15.
 */
public class KtGroupListViewAdapter extends ArrayAdapter<KandiGroupObjectParcelable> {


    private Context context;

    public ArrayList<KandiGroupObjectParcelable> kandiGroupObjectList;

    public KtGroupListViewAdapter(Context context, int textViewResourceId, ArrayList<KandiGroupObjectParcelable> list) {
        super(context, textViewResourceId);
        this.kandiGroupObjectList = list;
        this.context = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        Holder holder;
        View convertView1 = convertView;

        if (convertView1 == null) {
            holder = new Holder();
            LayoutInflater vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView1 = vi.inflate(R.layout.users_groups_for_new_message_list_item, null);
            holder.kandi_id = (TextView) convertView1.findViewById(R.id.UsersAndGroupsForNewMessageListItem_kandiId);
            holder.kandi_name = (TextView) convertView1.findViewById(R.id.UsersAndGroupsForNewMessageListItem_KandiName);
            holder.profileImage = (ImageView) convertView1.findViewById(R.id.UsersAndGroupsForNewMessageListItem_ImageView);
            convertView1.setTag(holder);
        } else {
            holder = (Holder) convertView1.getTag();
        }

        KandiGroupObjectParcelable kandiObject = getItem(position);

        holder.kandi_id.setText(kandiObject.getKandi_id());
        holder.kandi_name.setText(kandiObject.getKandi_name());

        return convertView1;

    }

    @Override
    public int getCount() {
        return kandiGroupObjectList.size();
    }

    @Override
    public KandiGroupObjectParcelable getItem(int position) {
        return kandiGroupObjectList.get(position);
    }

    public void setArrayList(ArrayList<KandiGroupObjectParcelable> list) {
        this.kandiGroupObjectList = list;
        notifyDataSetChanged();
    }

    private class Holder {
        public TextView kandi_id, kandi_name;
        public ArrayList<KtUserObjectParcelable> users;
        public ImageView profileImage;
    }
}
