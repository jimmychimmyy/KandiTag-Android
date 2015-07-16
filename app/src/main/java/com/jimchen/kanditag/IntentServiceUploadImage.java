package com.jimchen.kanditag;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;

import java.net.URISyntaxException;
import java.util.ArrayList;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class IntentServiceUploadImage extends IntentService {

    private static final String TAG = "UploadImageService";

    // socket io variables
    private static com.github.nkzawa.socketio.client.Socket socket;
    private final String HOST = "http://kandi.jit.su/";
    private final int portNumber = 3000;

    // TODO: Rename actions, choose action names that describe tasks that this
    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    public static final String ACTION_UPLOAD_IMAGE = "com.jimchen.kanditag.action.UPLOAD_IMAGE";
    public static final String ACTION_ADD_METADATA = "com.jimchen.kanditag.action.ADD_METADATA";

    // parameters
    private static final String MY_KT_ID = "com.jimchen.kanditag.extra.MYKTID";
    private static final String IMAGE = "com.jimchen.kanditag.extra.IMAGE";
    private static final String IMAGE_FILENAME = "com.jimchen.kanditag.extra.IMAGE_FILENAME";
    private static final String IMAGE_TAGS = "com.jimchen.kanditag.extra.IMAGE_TAGS";

    /**
     * Starts this service to perform action with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // download feed customized helper method
    public static void startUploadingImage(Context context, String kt_id, byte[] image, String filename) {
        Intent intent = new Intent(context, IntentServiceUploadImage.class);
        intent.setAction(ACTION_UPLOAD_IMAGE);
        intent.putExtra(MY_KT_ID, kt_id);
        intent.putExtra(IMAGE, image);
        intent.putExtra(IMAGE_FILENAME, filename);
        context.startService(intent);
    }

    public static void startAddingMetadata(Context context, String kt_id, String filename, ArrayList<String> tags) {

    }

    public IntentServiceUploadImage() {
        super("IntentServiceUploadImage");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_UPLOAD_IMAGE.equals(action)) {
                final String my_kt_id = intent.getStringExtra(MY_KT_ID);
                final byte[] image_to_upload = intent.getByteArrayExtra(IMAGE);
                final String image_filename = intent.getStringExtra(IMAGE_FILENAME);
                handleUploadingImage(my_kt_id, image_to_upload, image_filename);
            } else if (ACTION_ADD_METADATA.equals(action)) {
                final String my_kt_id = intent.getStringExtra(MY_KT_ID);
                final String image_filename = intent.getStringExtra(IMAGE_FILENAME);
                final ArrayList<String> tags = intent.getStringArrayListExtra(IMAGE_TAGS);
                handleAddMetadata(my_kt_id, image_filename, tags);
            }
        }
    }


    /**
     * Handle action download feed in the provided background thread with the provided
     * parameters.
     */
    private void handleUploadingImage(String kt_id, byte[] image, String filename) {
        // set up socket
        try {
            IO.Options options = new IO.Options();
            options.forceNew = true;
            socket = IO.socket(HOST, options);
            socket.on("upload_image", onUploadImage);
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
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        // connect socket
        socket.connect();
        socket.emit("upload_image", kt_id, image, filename);
    }

    private void handleAddMetadata(String kt_id, String filename, ArrayList<String> tags) {

        // TODO will need to add the tags as part of the param

        try {
            IO.Options options = new IO.Options();
            options.forceNew = true;
            socket = IO.socket(HOST, options);
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
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        JsonQrObject obj = new JsonQrObject();
        obj.setKt_id(kt_id);
        obj.setFilename(filename);
        obj.setTags(tags);

        // TODO create json object with data

        socket.connect();
        socket.emit("add_image_meta", "json object");

    }

    private Emitter.Listener onUploadImage = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            String message = (String) args[0];
            Log.d(TAG, "onUploadImage: " + message);
        }
    };

    private Emitter.Listener onAddImageMeta = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            String res = (String) args[0];
            Log.d(TAG, res);

            // call socket disconnect after response
        }
    };

}
