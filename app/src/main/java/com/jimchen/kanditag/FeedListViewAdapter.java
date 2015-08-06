package com.jimchen.kanditag;

import android.content.Context;
import android.content.Intent;
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
import android.support.v4.content.LocalBroadcastManager;
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
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import org.apache.http.HttpConnection;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.zip.Inflater;

/**
 * Created by Jim on 5/26/15.
 */
public class FeedListViewAdapter extends ArrayAdapter {

    int PROGRESS_BAR_MAX = 100;

    private static final String TAG = "FeedListViewAdapter";

    private SharedPreferences sharedPreferences;
    public static final String USER_PREFERENCES = "com.jimchen.kanditag.extra.PREFERENCES";
    public static final String KTID = "com.jimchen.kanditag.extra.KTID";
    private String MY_KT_ID;

    private Context context;
    private ArrayList<String> _ids = new ArrayList<String>();
    private LruCache<String, Bitmap> mMemoryCache;

    private static final String URL = "http://kandi.jit.su/kt_media/";
    //private ImageLoader imageLoader;
    private ViewHolder viewHolder;

    // socket variables
    private static com.github.nkzawa.socketio.client.Socket socket;
    private final String HOST = "http://www.kandi.jit.su/";
    private final int portNumber = 3000;

    public FeedListViewAdapter(Context context, int layoutResourceId, ArrayList<String> _ids) {
        super(context, layoutResourceId);
        this.context = context;
        this._ids = _ids;

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

        //imageLoader = ImageLoader.getInstance();
        //imageLoader.init(ImageLoaderConfiguration.createDefault(context));
    }

    public static int getSizeInBytes(Bitmap bitmap) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            return bitmap.getByteCount();
        } else {
            return bitmap.getRowBytes() * bitmap.getHeight();
        }
    }

    public void addBitmapToMemoryCache(String key, Bitmap bitmap) {
        if (getBitmapFromMemCache(key) == null) {
            mMemoryCache.put(key, bitmap);
        }
    }

    /**

    public void loadBitmap(int resId, ImageView imageView) {
        final String imageKey = String.valueOf(resId);

        final Bitmap bitmap = getBitmapFromMemCache(imageKey);
        if (bitmap != null) {
            imageView.setImageBitmap(bitmap);
        } else {
            imageView.setImageResource(R.drawable.feedlistitem_empty_background);
            // start task to download image

        }
    }

     **/

    public Bitmap getBitmapFromMemCache(String key) {
        return mMemoryCache.get(key);
    }


    // adapter should take list of image filenames as a parameter
    // using an async task, download the image from the server
    // create an optimized bitmap from the image
    // display the image


    static class ViewHolder {
        public DynamicImageView media;
        public TextView _id;
        public TextView username;
        public TextView caption;
        public ProgressBar progress;
        public LinearLayout layout;
        public boolean isOpen = false;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        String _id = getItem(position);

        View rowView = convertView;
        viewHolder = new ViewHolder();

        if (rowView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            rowView = inflater.inflate(R.layout.feed_list_item, null, false);
            viewHolder.media = (DynamicImageView) rowView.findViewById(R.id.FeedListItem_mediaContainer);
            viewHolder._id = (TextView) rowView.findViewById(R.id.FeedListItem_URI);
            viewHolder.username = (TextView) rowView.findViewById(R.id.FeedListItem_Username);
            viewHolder.caption = (TextView) rowView.findViewById(R.id.FeedListItem_Caption);
            viewHolder.progress = (ProgressBar) rowView.findViewById(R.id.FeedListItem_ProgressBar);
            viewHolder.layout = (LinearLayout) rowView.findViewById(R.id.FeedListItem_LinearLayout);
            rowView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }


        viewHolder.media.setTag(position);
        //viewHolder._id.setText(uri);

        if (_id != null) {

            String uri = URL + _id;

            if (uri != null) {

                Bitmap bm = getBitmapFromMemCache(uri);

                if (bm == null) {

                    //iewHolder.media.setImageResource(R.drawable.feedlistitem_empty_background);
                    new ImageLoadTask(viewHolder).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, uri);

                } else {

                    viewHolder.media.setImageBitmap(bm);
                    viewHolder.progress.setVisibility(View.GONE);

                }

            } else {

                viewHolder.media.setImageResource(R.drawable.feedlistitem_empty_background);

            }
        }
        //viewHolder.username.setText();
        //viewHolder.caption.setText();


        /**

        if (_ids.get(position) == null) {

            try {

                imageLoader.displayImage(uri, viewHolder.media, new ImageLoadingListener() {
                    @Override
                    public void onLoadingStarted(String imageUri, View view) {
                        viewHolder.progress.setVisibility(View.VISIBLE);

                    }

                    @Override
                    public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                        viewHolder.progress.setVisibility(View.GONE);
                    }

                    @Override
                    public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                        viewHolder.progress.setVisibility(View.GONE);
                    }

                    @Override
                    public void onLoadingCancelled(String imageUri, View view) {
                        viewHolder.progress.setVisibility(View.GONE);
                    }
                });

            } catch (Exception e) {

            }

        } **/

        return rowView;
    }

    public AbsListView.RecyclerListener mRecycleListener = new AbsListView.RecyclerListener() {
        @Override
        public void onMovedToScrapHeap(View view) {
            ViewHolder holder = (ViewHolder) view.getTag();
            /**
            DownloadFeedTask downloadTask = holder.downloadFeedTask;

            if (downloadTask != null) {
                downloadTask.cancel(true);
            } **/

        }
    };

    private class ImageLoadTask extends AsyncTask<String, Integer, Bitmap> {

        private String url;
        private final WeakReference<ViewHolder> holderWeakReference;
        //private final WeakReference<DynamicImageView> imageViewWeakReference;

        int progress;

        public ImageLoadTask(ViewHolder holder) {
            holderWeakReference = new WeakReference<ViewHolder>(holder);
            //imageViewWeakReference = new WeakReference<DynamicImageView>(imageView);
        }

        @Override
        protected Bitmap doInBackground(String... params) {

            Bitmap bm;

            url = params[0];

            Log.i(TAG, "attempting to download image with URL: " + url);

            //return  downloadBitmap(url);

            HttpURLConnection connection = null;

            try {

                java.net.URL reqURL = new URL(url);

                connection = (HttpURLConnection) reqURL.openConnection();

                int length = connection.getContentLength();

                InputStream inputStream = (InputStream) reqURL.getContent();

                byte[] imageData = new byte[length];

                int bufferSize = (int) Math.ceil(length / (double) PROGRESS_BAR_MAX);

                int downloaded = 0;

                for (int i = 1; i < PROGRESS_BAR_MAX; i++) {

                    int read = inputStream.read(imageData, downloaded, bufferSize);

                    downloaded += read;

                    publishProgress(i);
                }

                BufferedInputStream bufferedInputStream = new BufferedInputStream(connection.getInputStream());

                bm = BitmapFactory.decodeStream(bufferedInputStream);

                int data = inputStream.read();

                while (data != -1) {
                    data = inputStream.read();
                }
            } catch (Exception e) {

                Log.e(TAG, "error loading: " + e);
                return null;

            } finally {

                if (connection == null) {
                    connection.disconnect();
                }

                publishProgress(PROGRESS_BAR_MAX);
            }

            return bm;

        }

        @Override
        protected void onPreExecute() {
            holderWeakReference.get().progress.setMax(PROGRESS_BAR_MAX);
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            super.onProgressUpdate(progress);
            holderWeakReference.get().progress.setProgress(progress[0]);
        }


        @Override
        protected void onPostExecute(final Bitmap bm) {
            addBitmapToMemoryCache(url, bm);
            if (bm == null) {
                //holderWeakReference.get().media.setImageResource(R.drawable.feedlistitem_empty_background);
                holderWeakReference.get().layout.setBackgroundColor(R.color.red);
                holderWeakReference.get().progress.setVisibility(View.GONE);
                //imageViewWeakReference.get().setImageResource(R.drawable.feedlistitem_empty_background);
            } else {
                holderWeakReference.get().layout.setBackgroundColor(R.color.gold);
                holderWeakReference.get().media.setImageBitmap(bm);
                holderWeakReference.get().media.setVisibility(View.GONE);
                holderWeakReference.get().layout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        if (!holderWeakReference.get().isOpen) {
                            holderWeakReference.get().media.setVisibility(View.VISIBLE);
                            holderWeakReference.get().isOpen = true;
                        } else if (holderWeakReference.get().isOpen) {
                            holderWeakReference.get().media.setVisibility(View.GONE);
                            holderWeakReference.get().isOpen = false;
                        }
                    }
                });
                holderWeakReference.get().progress.setVisibility(View.GONE);
                //imageViewWeakReference.get().setImageBitmap(bm);
            }
        }

        private Bitmap downloadBitmap(String url) {

            final DefaultHttpClient client = new DefaultHttpClient();

            Log.i(TAG, url);

            final HttpGet httpGet = new HttpGet(url);

            try {

                HttpResponse response = client.execute(httpGet);

                final int statusCode = response.getStatusLine().getStatusCode();

                if (statusCode != HttpStatus.SC_OK) {

                    Log.w(TAG, "Error " + statusCode + " while getting bitmap from " + url);
                    return null;

                }

                final HttpEntity entity = response.getEntity();

                if (entity != null) {

                    InputStream inputStream = null;
                    try {

                        inputStream = entity.getContent();
                        final Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

                        return bitmap;

                    } finally {

                        if (inputStream != null) {
                            inputStream.close();
                        }

                        entity.consumeContent();
                    }
                }
            } catch (Exception e) {

                Log.e(TAG, "something went wrong: " + e);
            }

            return null;

        }
    }


    class DownloadFeedTask extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... params) {

            String _id = params[0];

            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);

            BufferedReader in = null;

            String URL = "http://kandi.jit.su/kt_media/";

            try {

                HttpClient httpClient = new DefaultHttpClient();

                HttpGet req = new HttpGet();

                URI dest = new URI(URL + _id);

                req.setURI(dest);

                HttpResponse res = httpClient.execute(req);

                InputStream data = res.getEntity().getContent();


            } catch (URISyntaxException e) {

            } catch (IOException e) {

            }

            return null;
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
        return _ids.size();
    }

    @Override
    public String getItem(int position) {
        return _ids.get(position);
    }

    // not sure if this is needed
    @Override
    public long getItemId(int arg0) {
        return arg0;
    }

    public void setArrayList(ArrayList<String> list) {
        this._ids = list;
        notifyDataSetChanged();
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
