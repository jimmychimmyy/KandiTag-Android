package com.jimchen.kanditag;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;

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

    // socket io variables
    private static com.github.nkzawa.socketio.client.Socket socket;
    private final String HOST = "http://kandi.jit.su/";
    private final int portNumber = 3000;

    // TODO: Rename actions, choose action names that describe tasks that this
    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    public static final String ACTION_DOWNLOAD_FEED = "com.jimchen.kanditag.action.DOWNLOAD_FEED";

    // parameters
    private static final String MY_KT_ID = "com.jimchen.kanditag.extra.MYKTID";

    // data to send with the broadcast intent
    public static final String IMAGE_DATA = "com.jimchen.kanditag.data.IMAGE";

    /**
     * Starts this service to perform action Baz with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // download feed customized helper method
    public static void startDownloadingFeed(Context context, String kt_id) {
        Intent intent = new Intent(context, IntentServiceDownloadFeed.class);
        intent.setAction(ACTION_DOWNLOAD_FEED);
        intent.putExtra(MY_KT_ID, kt_id);
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
                final String my_kt_id = intent.getStringExtra(MY_KT_ID);
                Log.d(TAG, my_kt_id);
                handleDownloadingFeed(my_kt_id);
            }
        }
    }


    /**
     * Handle action download feed in the provided background thread with the provided
     * parameters.
     */
    private void handleDownloadingFeed(String kt_id) {
        // set up socket
        try {
            IO.Options options = new IO.Options();
            options.forceNew = true;
            socket = IO.socket(HOST, options);
            socket.on("test_download_my_own_feed", onDownloadFeed);
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
        socket.emit("test_download_my_own_feed", kt_id);
    }

    private Emitter.Listener onDownloadFeed = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            byte[] img = (byte[]) args[0];
            Log.d(TAG, "downloaded something");
            Intent localIntent = new Intent(ACTION_DOWNLOAD_FEED).putExtra(IMAGE_DATA, img);
            LocalBroadcastManager.getInstance(IntentServiceDownloadFeed.this).sendBroadcast(localIntent);
        }
    };

}
