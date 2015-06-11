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
public class DownloadArchivedIntentService extends IntentService {

    private String TAG = "DownloadArchivedIntentService";

    private static com.github.nkzawa.socketio.client.Socket socket;
    private final String HOST = "http://kandi.jit.su/";
    private final int portNumber = 3000;

    // TODO: Rename actions, choose action names that describe tasks that this
    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    private static final String ACTION_DOWNLOAD_KANDITAGS = "com.jimchen.kanditag.action.DOWNLOAD_KANDITAGS";
    private static final String ACTION_DOWNLOAD_FRIENDS = "com.jimchen.kanditag.action.DOWNLOAD_FRIENDS";
    private static final String ACTION_DOWNLOAD_MESSAGES = "com.jimchen.kanditag.action.DOWNLOAD_MESSAGES";
    private static final String ACTION_DOWNLOAD_GROUP_MESSAGES = "com.jimchen.kanditag.action.DOWNLOAD_GROUP_MESSAGES";
    private static final String ACTION_BAZ = "com.jimchen.kanditag.action.BAZ";

    // TODO: Rename parameters
    private static final String KT_ID = "com.jimchen.kanditag.extra.KTID";
    private static final String KANDI_ID = "com.jimchen.kanditag.extra.KANDI_ID";

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startDownloadingKandiTags(Context context, String kt_id) {
        Intent intent = new Intent(context, DownloadArchivedIntentService.class);
        intent.setAction(ACTION_DOWNLOAD_KANDITAGS);
        intent.putExtra(KT_ID, kt_id);
        context.startService(intent);
    }

    public static void startDownloadingFriends(Context context, String kt_id, String kandi_id) {
        Intent intent = new Intent(context, DownloadArchivedIntentService.class);
        intent.setAction(ACTION_DOWNLOAD_FRIENDS);
        intent.putExtra(KT_ID, kt_id);
        intent.putExtra(KANDI_ID, kandi_id);
        context.startService(intent);
    }

    public static void startDownloadingMessages(Context context, String kt_id) {
        Intent intent = new Intent(context, DownloadArchivedIntentService.class);
        intent.setAction(ACTION_DOWNLOAD_MESSAGES);
        intent.putExtra(KT_ID, kt_id);
        context.startService(intent);
    }

    public static void startDownloadingGroupMessages(Context context, String kt_id) {
        Intent intent = new Intent(context, DownloadArchivedIntentService.class);
        intent.setAction(ACTION_DOWNLOAD_GROUP_MESSAGES);
        intent.putExtra(KT_ID, kt_id);
        context.startService(intent);
    }

    public DownloadArchivedIntentService() {
        super("DownloadArchivedIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_DOWNLOAD_KANDITAGS.equals(action)) {
                final String kt_id = intent.getStringExtra(KT_ID);
                handleActionDownloadKandiTags(kt_id);
            } else if (ACTION_DOWNLOAD_FRIENDS.equals(action)) {
                final String kt_id = intent.getStringExtra(KT_ID);
                final String kandi_id = intent.getStringExtra(KANDI_ID);
                handleActionDownloadFriends(kt_id, kandi_id);
            } else if (ACTION_DOWNLOAD_MESSAGES.equals(action)) {
                final String kt_id = intent.getStringExtra(KT_ID);
                handleActionDownloadMessages(kt_id);
            } else if (ACTION_DOWNLOAD_GROUP_MESSAGES.equals(action)) {
                final String kt_id = intent.getStringExtra(KT_ID);
                handleActionDownloadGroupMessages(kt_id);
            }
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */

    private void handleActionDownloadKandiTags(String kt_id) {

        try {
            IO.Options options = new IO.Options();
            options.forceNew = true;
            socket = IO.socket(HOST, options);
            socket.on("download_archived_kanditags", onDownloadArchivedKandiTags);
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
        socket.emit("download_archived_kanditags", kt_id);
    }

    private void handleActionDownloadFriends(String kt_id, String kandi_id) {

        try {
            IO.Options options = new IO.Options();
            options.forceNew = true;
            socket = IO.socket(HOST, options);
            socket.on("download_archived_friends", onDownloadArchivedFriends);
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
        socket.emit("download_archived_friends", kt_id, kandi_id);
    }

    private void handleActionDownloadMessages(String kt_id) {

        try {
            IO.Options options = new IO.Options();
            options.forceNew = true;
            socket = IO.socket(HOST, options);
            socket.on("download_archived_messages", onDownloadArchivedMessages);
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
        socket.emit("download_archived_messages", kt_id);
    }

    private void handleActionDownloadGroupMessages(String kt_id) {

        try {
            IO.Options options = new IO.Options();
            options.forceNew = true;
            socket = IO.socket(HOST, options);
            socket.on("download_archived_group_messages", onDownloadArchivedGroupMessages);
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
        socket.emit("download_archived_group_messages", kt_id);
    }

    private Emitter.Listener onDownloadArchivedKandiTags = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            // TODO save entries into db
            Log.d(TAG, "downloaded something onDownloadArchivedKandiTags");
        }
    };

    private Emitter.Listener onDownloadArchivedFriends = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            // TODO save entries into db
            Log.d(TAG, "downloaded something onDownloadArchivedFriends");
        }
    };

    private Emitter.Listener onDownloadArchivedMessages = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            // TODO save entries into db
            Log.d(TAG, "downloaded something onDownloadArchivedMessages");
        }
    };

    private Emitter.Listener onDownloadArchivedGroupMessages = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            // TODO save entries into db
            Log.d(TAG, "downloaded something onDownloadArchivedGroupMessages");
        }
    };

}
