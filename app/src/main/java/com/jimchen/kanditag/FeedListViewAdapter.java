package com.jimchen.kanditag;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.zip.Inflater;

/**
 * Created by Jim on 5/26/15.
 */
public class FeedListViewAdapter extends ArrayAdapter {

    private Context context;
    private ArrayList<byte[]> images;

    private Bitmap final_image;

    public FeedListViewAdapter(Context context, int layoutResourceId, ArrayList<byte[]> images) {
        super(context, layoutResourceId);
        this.context = context;
        this.images = images;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final Holder holder;
        View row = convertView;

        byte[] image = decompressByteArray(getItem(position));
        Bitmap bitmap = BitmapFactory.decodeByteArray(image, 0, image.length);



        if (context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            Bitmap scaledB = Bitmap.createScaledBitmap(bitmap, bitmap.getWidth() / 2, bitmap.getHeight() / 2, true);
            int width = scaledB.getWidth();
            int height = scaledB.getHeight();

            //rotate 90 degrees with matrix
            Matrix matrix = new Matrix();
            matrix.postRotate(90);
            final_image = Bitmap.createBitmap(scaledB, 0, 0, width, height, matrix, true);
        }

        if (row == null) {

            holder = new Holder();
            LayoutInflater vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = vi.inflate(R.layout.feed_list_item, null, false);
            holder.user_name = (TextView) row.findViewById(R.id.FeedListItem_usernameContainer);
            holder.caption = (TextView) row.findViewById(R.id.FeedListItem_captionContainer);

            holder.media = (ImageView) row.findViewById(R.id.FeedListItem_mediaContainer);
            RelativeLayout relativeLayout = (RelativeLayout) row.findViewById(R.id.FeedListItemRelativeLayout);
            RelativeLayout.LayoutParams params=new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, bitmap.getHeight());
            holder.media.setLayoutParams(params);
            relativeLayout.removeView(holder.media);
            relativeLayout.addView(holder.media, params);
            row.setTag(holder);

        } else {

            holder = (Holder) row.getTag();

        }

        holder.media.setImageBitmap(final_image);


        /**

        URL img_value = null;
        try {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
            img_value = new URL("https://graph.facebook.com/" + holder.fb_id.getText() + "/picture?width=150&height=150");
            Bitmap mIcon = BitmapFactory.decodeStream(img_value.openConnection().getInputStream());
            //holder.profilePicture.setImageBitmap(mIcon);
            //holder.profilePicture.setPadding(6, 6, 6, 6);

            //make bitmap circular
            Bitmap circleBitmap = Bitmap.createBitmap(mIcon.getWidth(), mIcon.getHeight(), Bitmap.Config.ARGB_8888);
            BitmapShader shader = new BitmapShader(mIcon, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
            Paint paint = new Paint();
            paint.setShader(shader);
            Canvas canvas = new Canvas(circleBitmap);
            canvas.drawCircle(mIcon.getWidth()/2, mIcon.getHeight()/2, mIcon.getWidth()/2, paint);
            holder.profilePicture.setImageBitmap(circleBitmap);

        } catch (Exception e) {
            e.printStackTrace();
        }
         **/

        return row;

    }

    // method to decompress image before posting
    private byte[] decompressByteArray(byte[] bytes) {

        ByteArrayOutputStream baos = null;
        Inflater inflater = new Inflater();
        inflater.setInput(bytes);
        baos = new ByteArrayOutputStream();
        byte[] temp = new byte[4*1024];
        try {
            while (!inflater.finished()) {
                int size = inflater.inflate(temp);
                baos.write(temp, 0, size);
            }
        } catch (Exception e) {

        } finally {
            try {
                if (baos != null) baos.close();
            } catch (Exception e) {}
        }

        return baos.toByteArray();
    }

    @Override
    public int getCount() {
        return images.size();
    }

    @Override
    public byte[] getItem(int position) {
        return images.get(position);
    }

    public void setArrayList(ArrayList<byte[]> list) {
        this.images = list;
        notifyDataSetChanged();
    }

    private class Holder {
        public ImageView media;
        public TextView caption, user_name;
    }

    private void scaleImage(Bitmap image, ImageView container)
    {
        if (image == null) {
            return; // Checking for null & return, as suggested in comments
        }
        Bitmap bitmap = image;

        // Get current dimensions AND the desired bounding box
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int bounding = dpToPx(250);
        Log.i("Test", "original width = " + Integer.toString(width));
        Log.i("Test", "original height = " + Integer.toString(height));
        Log.i("Test", "bounding = " + Integer.toString(bounding));

        // Determine how much to scale: the dimension requiring less scaling is
        // closer to the its side. This way the image always stays inside your
        // bounding box AND either x/y axis touches it.
        float xScale = ((float) bounding) / width;
        float yScale = ((float) bounding) / height;
        float scale = (xScale <= yScale) ? xScale : yScale;
        Log.i("Test", "xScale = " + Float.toString(xScale));
        Log.i("Test", "yScale = " + Float.toString(yScale));
        Log.i("Test", "scale = " + Float.toString(scale));

        // Create a matrix for the scaling and add the scaling data
        Matrix matrix = new Matrix();
        matrix.postScale(scale, scale);

        // Create a new bitmap and convert it to a format understood by the ImageView
        Bitmap scaledBitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
        width = scaledBitmap.getWidth(); // re-use
        height = scaledBitmap.getHeight(); // re-use
        BitmapDrawable result = new BitmapDrawable(scaledBitmap);
        Log.i("Test", "scaled width = " + Integer.toString(width));
        Log.i("Test", "scaled height = " + Integer.toString(height));

        // Apply the scaled bitmap
        container.setImageDrawable(result);

        // Now change ImageView's dimensions to match the scaled image
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) container.getLayoutParams();
        params.width = width;
        params.height = height;
        container.setLayoutParams(params);

        Log.i("Test", "done");
    }

    private int dpToPx(int dp)
    {
        float density = context.getApplicationContext().getResources().getDisplayMetrics().density;
        return Math.round((float)dp * density);
    }
}
