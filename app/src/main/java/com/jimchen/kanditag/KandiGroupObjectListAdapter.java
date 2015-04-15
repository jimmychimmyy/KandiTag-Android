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
 * Created by Jim on 3/16/15.
 */
public class KandiGroupObjectListAdapter extends ArrayAdapter<KandiGroupObjectParcelable> {

    private Context context;

    private ArrayList<KandiGroupObjectParcelable> kandiGroupObjectArrayList;

    public KandiGroupObjectListAdapter(Context context, int textViewResourceId, ArrayList<KandiGroupObjectParcelable> list) {
        super(context, textViewResourceId);
        this.kandiGroupObjectArrayList = list;
        this.context = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        Holder holder;
        View convertView1 = convertView;

        if (convertView1 == null) {
            holder = new Holder();
            LayoutInflater vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView1 = vi.inflate(R.layout.groups_for_new_message_list_item, null);
            holder.kandi_name = (TextView) convertView1.findViewById(R.id.GroupsForNewMessageListItem_KandiGroupName);
            holder.qrCode = (TextView) convertView1.findViewById(R.id.GroupsForNewMessageListItem_qrCode);
            holder.groupImage = (ImageView) convertView1.findViewById(R.id.GroupsForNewMessageListItem_ImageView);
            convertView1.setTag(holder);
        } else {
            holder = (Holder) convertView1.getTag();
        }

        KandiGroupObjectParcelable object = getItem(position);

        holder.kandi_name.setText(object.getGroupName());
        holder.qrCode.setText(object.getQrCode());
        //holder.groupImage.setImageResource(R.drawable.splash_screen_kt_logo_universal);

        //change the layout of the xml file if there is under 5 kandi groups the user is in
        //keep the kandigroup icon centered at all times if kandigroupcount > 5

        /**

        URL img_value = null;

        try {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
            img_value = new URL("https://graph.facebook.com/" + user.getFb_id() + "/picture?width=160&height=160");
            Bitmap mIcon = BitmapFactory.decodeStream(img_value.openConnection().getInputStream());

            //make bitmap circular
            Bitmap circleBitmap = Bitmap.createBitmap(mIcon.getWidth(), mIcon.getHeight(), Bitmap.Config.ARGB_8888);
            BitmapShader shader = new BitmapShader(mIcon, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
            Paint paint = new Paint();
            paint.setShader(shader);
            Canvas canvas = new Canvas(circleBitmap);
            canvas.drawCircle(mIcon.getWidth()/2, mIcon.getHeight()/2, mIcon.getWidth()/2, paint);

            holder.groupImage.setImageBitmap(circleBitmap);

        } catch (Exception e) {
            e.printStackTrace();
        }

         **/

        return convertView1;

    }

    @Override
    public int getCount() {
        return kandiGroupObjectArrayList.size();
    }

    @Override
    public KandiGroupObjectParcelable getItem(int position) {
        return kandiGroupObjectArrayList.get(position);
    }

    public void setArrayList(ArrayList<KandiGroupObjectParcelable> list) {
        this.kandiGroupObjectArrayList = list;
        notifyDataSetChanged();
    }

    private class Holder {
        public TextView qrCode, kandi_name;
        public ImageView groupImage;
    }
}
