package com.jimchen.kanditag;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.os.AsyncTask;
import android.os.StrictMode;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class IntentServiceDownloadFeed extends IntentService {

    private String TAG = "IntentServiceDownloadFeed";

    private final String URL = "http://kandi.jit.su/kt_media";

    // TODO: Rename actions, choose action names that describe tasks that this
    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    public static final String ACTION_DOWNLOAD_FEED = "com.jimchen.kanditag.action.DOWNLOAD_FEED";

    public static final String FILE_ID = "com.jimchen.kanditag.data.FILEID";

    // parameters
    //private static final String MY_KT_ID = "com.jimchen.kanditag.extra.MYKTID";

    // data to send with the broadcast intent
    //public static final String IMAGE_DATA = "com.jimchen.kanditag.data.IMAGE";

    /**
     * Starts this service to perform action Baz with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // download feed customized helper method
    public static void startDownloadingFeed(Context context) {
        Intent intent = new Intent(context, IntentServiceDownloadFeed.class);
        intent.setAction(ACTION_DOWNLOAD_FEED);
        context.startService(intent);
    }

    public IntentServiceDownloadFeed() {
        super("IntentServiceDownloadFeed");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_DOWNLOAD_FEED.equals(action)) {
                handleDownloadingFeed();
            }
        }
    }


    /**
     * Handle action download feed in the provided background thread with the provided
     * parameters.
     */
    private void handleDownloadingFeed() {

        DownloadFeedTask downloadFeedTask = new DownloadFeedTask();
        downloadFeedTask.execute();
    }

    class DownloadFeedTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {


            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);

            BufferedReader in = null;

            try {

                HttpClient httpClient = new DefaultHttpClient();

                HttpGet req = new HttpGet();

                URI dest = new URI(URL);

                req.setURI(dest);

                HttpResponse res = httpClient.execute(req);

                in = new BufferedReader(new InputStreamReader(res.getEntity().getContent()));

                String line = in.readLine();

                //System.out.println(line);

                line = line.replace("[", "");
                line = line.replace("]", "");
                line = line.replace("\"", "");

                String[] items = line.split(",");

                for (int i = 0; i < items.length; i++) {
                    System.out.println(items[i]);
                    Intent localIntent = new Intent(ACTION_DOWNLOAD_FEED).putExtra(FILE_ID, items[i]);
                    LocalBroadcastManager.getInstance(IntentServiceDownloadFeed.this).sendBroadcast(localIntent);
                }


            } catch (URISyntaxException e) {

            } catch (IOException e) {

            }

            return null;
        }
    }

    /**
    private Emitter.Listener onGetImageFilenames = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            final String response = (String) args[0];


            GsonBuilder gsonBuilder = new GsonBuilder();
            gsonBuilder.registerTypeAdapter(ResponseResults.class, new ResponseResultsDeserializer());
            Gson gson = gsonBuilder.create();

            ResponseResults results = gson.fromJson(response, ResponseResults.class);

            // TODO create ktmedia object and pass to feed fragment
            // also make sure that final does not change the way it is broadcasted

            final KtMedia media = new KtMedia();

            String filename = results.getFilename();
            filename = filename.replace("\"", "");

            String uploadDate = results.getUploadDate();
            uploadDate = uploadDate.replace("\"", "");

            media.setFilename(filename);
            media.setUploadDate(uploadDate);

            AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... voids) {
                    Intent localIntent = new Intent(ACTION_DOWNLOAD_FEED).putExtra(IMAGE_DATA, media);
                    LocalBroadcastManager.getInstance(IntentServiceDownloadFeed.this).sendBroadcast(localIntent);
                    return null;
                }
            };
            task.execute();
        }
    };
     **/

}
