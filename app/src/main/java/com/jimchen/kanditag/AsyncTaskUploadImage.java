package com.jimchen.kanditag;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;

import java.net.URISyntaxException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by Jim on 6/4/15.
 */
public class AsyncTaskUploadImage extends AsyncTask<byte[], Void, String> {

    private String TAG = "AsyncTaskUploadImage";

    private String response = "";

    private static com.github.nkzawa.socketio.client.Socket socket;
    private final String HOST = "http://kandi.jit.su/";
    private final int portNumber = 3000;

    private Context context;
    SharedPreferences sharedPreferences;
    private String MY_KT_ID;
    public static final String MyPreferences = "MyPrefs";
    public static final String UserId = "userIdKey";

    private InterfaceStringResponse delegate = null;

    public AsyncTaskUploadImage(Context context, InterfaceStringResponse response) {
        this.context = context;
        this.delegate = response;
    }

    @Override
    protected String doInBackground(byte[]... param) {

        Log.d(TAG, "working in background");

        byte[] image = param[0];

        DateFormat dateFormat = new SimpleDateFormat("_yyyy-MM-dd_HH-mm-ss");
        Date date = new Date();
        String filename = MY_KT_ID + dateFormat.format(date);

        socket.emit("upload_image", MY_KT_ID, image, filename);

        return response;
    }

    @Override
    protected void onPreExecute() {

        Log.d(TAG, "onPreExecute");

        // get kt_id
        sharedPreferences = context.getSharedPreferences(MyPreferences, Context.MODE_PRIVATE);
        MY_KT_ID = sharedPreferences.getString(UserId, "");

        // connecting socket
        try {
            IO.Options options = new IO.Options();
            options.forceNew = true;
            socket = IO.socket(HOST, options);
            socket.on("upload_image", onUploadImage );
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

    }

    @Override
    protected void onPostExecute(String response) {
        Log.d(TAG, "onPostExecute");
        Log.d(TAG, response);

        try {
            socket.disconnect();
        } catch (Exception e) {}
    }

    private Emitter.Listener onUploadImage = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            response = args[0].toString();
        }
    };

}
