package com.jimchen.kanditag;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.StrictMode;
import android.util.Log;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.zip.Inflater;

/**
 * Created by Jim on 5/26/15.
 */
public class FeedListViewAdapter extends ArrayAdapter {

    private final String TAG = "FeedListViewAdapter";

    private SharedPreferences sharedPreferences;
    public static final String USER_PREFERENCES = "com.jimchen.kanditag.extra.PREFERENCES";
    public static final String KTID = "com.jimchen.kanditag.extra.KTID";
    private String MY_KT_ID;

    private Context context;
    private ArrayList<byte[]> images;
    private ArrayList<KtMedia> files;
    private LruCache<String, Bitmap> mMemoryCache;

    //private ArrayList<String> filenames;

    //private ArrayList<String> inProcessFilenames = new ArrayList<>();

    private Bitmap final_image;

    private Bitmap[] bitmapList;
    private ArrayList<byte[]> bitmapPlaceholder;

    // socket variables
    private static com.github.nkzawa.socketio.client.Socket socket;
    private final String HOST = "http://kandi.jit.su/";
    private final int portNumber = 3000;

    public FeedListViewAdapter(Context context, int layoutResourceId, ArrayList<KtMedia> files) {
        super(context, layoutResourceId);
        this.context = context;
        this.files = files;

        sharedPreferences = context.getSharedPreferences(USER_PREFERENCES, Context.MODE_PRIVATE);
        MY_KT_ID = sharedPreferences.getString(KTID, "");

        // Bitmap cache
        final int maxMem = (int) (Runtime.getRuntime().maxMemory() / 1024);
        final int cacheSize = maxMem / 8;
        mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                return getSizeInBytes(bitmap) / 1024;
            }
        };

    }

    public static int getSizeInBytes(Bitmap bitmap) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            return bitmap.getByteCount();
        } else {
            return bitmap.getRowBytes() * bitmap.getHeight();
        }
    }

    public Bitmap getBitmapFromMemCache(String key) {
        return mMemoryCache.get(key);
    }


    // adapter should take list of image filenames as a parameter
    // using an async task, download the image from the server
    // create an optimized bitmap from the image
    // display the image

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {


        KtMedia file = getItem(position);
        //final String filename = getItem(position);

        //final byte[] img =  this.decompressByteArray(getItem(position));
        View rowView = convertView;
        ViewHolder viewHolder = new ViewHolder();

        if (rowView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            rowView = inflater.inflate(R.layout.feed_list_item, null, false);
            viewHolder.media = (ImageView) rowView.findViewById(R.id.FeedListItem_mediaContainer);
            viewHolder.caption = (TextView) rowView.findViewById(R.id.FeedListItem_captionContainer);
            rowView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.media.setTag(position);
        viewHolder.caption.setText(file.getFilename());
        viewHolder.position = position;
        viewHolder.downloadImage = new DownloadImage(position, viewHolder);
        if (!viewHolder.downloadImage.isCancelled()) {
            viewHolder.downloadImage.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, file.getFilename());
        }

        return rowView;
    }

    public AbsListView.RecyclerListener mRecycleListener = new AbsListView.RecyclerListener() {
        @Override
        public void onMovedToScrapHeap(View view) {
            ViewHolder holder = (ViewHolder) view.getTag();
            DownloadImage downloadImage = holder.downloadImage;

            /**
            if (downloadImage != null) {
                downloadImage.cancel(true);
            } **/

        }
    };

    private class DownloadImage extends AsyncTask<String, Void, Bitmap> {

        private int position;
        private ViewHolder holder;
        private Bitmap image;

        public DownloadImage(int position, ViewHolder holder) {
            this.position = position;
            this.holder = holder;
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            String filename = params[0];
            downloadImage(filename, this.position, this.holder);
            return image;
        }

        @Override
        protected void onPostExecute(Bitmap image) {
            super.onPostExecute(image);
            if (image != null && holder.position == this.position) {
                holder.media.setImageBitmap(image);
                holder.media.invalidate();
            }

        }
    }

    // when position and holder are final, the imageview container cannot be changed
    private void downloadImage(String filename, final int position, final ViewHolder holder) {

        // set up socket
        try {
            IO.Options options = new IO.Options();
            options.forceNew = true;
            socket = IO.socket(HOST, options);
            //socket.on("test_download_my_own_feed", onDownloadFeed);
            socket.on("download_image", new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    byte[] img = (byte[]) args[0];
                    // TODO call load image and convert the byte[] into a bitmap for display
                    byte[] image = decompressByteArray(img);
                    LoadImage loadImage = new LoadImage(position, holder);
                    loadImage.execute(image);
                    Log.d(TAG, "got a feed to display");
                }
            });
            socket.on(com.github.nkzawa.socketio.client.Socket.EVENT_CONNECT, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    Log.d(TAG, "socket connected");
                }
            }).on(com.github.nkzawa.socketio.client.Socket.EVENT_DISCONNECT, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    Log.d(TAG, "socket disconnected");
                }
            });
        } catch (URISyntaxException use) {
            use.printStackTrace();
        }

        // connect socket
        socket.connect();
        socket.emit("download_image", filename, MY_KT_ID);
    }

    private class LoadImage extends AsyncTask<byte[], Void, Bitmap> {

        private int position;
        private ViewHolder holder;

        public LoadImage(int position, ViewHolder holder) {
            this.position = position;
            this.holder = holder;
        }

        @Override
        protected Bitmap doInBackground(byte[]... params) {


            Log.d(TAG, "loadImage in background");
            byte[] bytes = params[0];

            // TODO then before calling this task, make sure that no task exists with the same filename

            Bitmap final_bitmap = null;
            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            if (context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT && bitmap != null) {
                Bitmap scaledB = Bitmap.createScaledBitmap(bitmap, bitmap.getWidth() / 5, bitmap.getHeight() / 5, true);
                int width = scaledB.getWidth();
                int height = scaledB.getHeight();
                //rotate 90 degrees with matrix
                Matrix matrix = new Matrix();
                matrix.postRotate(90);
                final_bitmap = Bitmap.createBitmap(scaledB, 0, 0, width, height, matrix, true);

                // TODO will need to scale the bitmap before returning it
            }

            return final_bitmap;
        };

        @Override
        protected void onPostExecute(Bitmap result) {
            super.onPostExecute(result);
            if (result != null && holder.position == this.position) {
                holder.media.setImageBitmap(result);
                holder.media.invalidate();
                Log.d(TAG, "Are we here yet?");
            }
        }
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
        return files.size();
    }

    @Override
    public KtMedia getItem(int position) {
        return files.get(position);
    }

    // not sure if this is needed
    @Override
    public long getItemId(int arg0) {
        return arg0;
    }

    public void setArrayList(ArrayList<KtMedia> list) {
        this.files = list;
        notifyDataSetChanged();
    }

    static class ViewHolder {
        public ImageView media;
        public TextView caption, user_name;
        public int position;
        public LoadImage loadTask;
        public DownloadImage downloadImage;
    }

    /**

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
    } **/

}
