package com.jimchen.kanditag;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Shader;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by Jim on 4/20/15.
 */
public class ProfileActivity extends Activity {

    private static final String TAG = "ProfileActivity";
    private KtDatabase myDatabase;
    SharedPreferences sharedPreferences;
    public static final String MY_PREFERENCES = "MyPrefs";
    public static final String NAME = "nameKey";
    public static final String FBID = "fbidKey";
    public static final String KTID = "userIdKey";
    public static final String NEW_MESSAGE = "NEW_MESSAGE";

    private String MY_KT_ID, MY_FB_ID, MY_USER_NAME;
    private String kt_id, fb_id, user_name;

    private static com.github.nkzawa.socketio.client.Socket socket;
    private final String HOST = "http://kandi.jit.su/";
    private final int portNumber = 3000;

    private TextView usernameTextView;
    private ImageView backButton, toSettings, profileImage;

    private GridView gridView;

    private ArrayList<byte[]> images = new ArrayList<>();

    private ImageGridAdapter imageGridAdapter;

    @Override
    public void onResume() {
        super.onResume();
        sharedPreferences = this.getSharedPreferences(MY_PREFERENCES, Context.MODE_PRIVATE);
        MY_KT_ID = sharedPreferences.getString(KTID, "");
        MY_USER_NAME = sharedPreferences.getString(NAME, "");
        MY_FB_ID = sharedPreferences.getString(FBID, "");
        connectSocket();
    }

    @Override
    public void onDestroy() {
        socket.disconnect();
        socket.close();
        super.onDestroy();
        //Intent result = new Intent();
        //setResult(RESULT_OK, result);
        //finish();
    }

    @Override
    public void onPause() {
        super.onPause();
        socket.disconnect();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_activity);

        //get data from sharedPreferences and local database
        myDatabase = new KtDatabase(this);
        sharedPreferences = this.getSharedPreferences(MY_PREFERENCES, Context.MODE_PRIVATE);
        MY_KT_ID = sharedPreferences.getString(KTID, "");
        MY_USER_NAME = sharedPreferences.getString(NAME, "");
        MY_FB_ID = sharedPreferences.getString(FBID, "");

        Bundle params = getIntent().getExtras();
        try {
            kt_id = params.getString("kt_id");
        } catch (NullPointerException e) {

        }

        usernameTextView = (TextView) findViewById(R.id.ProfileActivity_UserName);
        if (kt_id == null) {
            usernameTextView.setText(MY_USER_NAME);
        }

        backButton = (ImageView) findViewById(R.id.ProfileActivity_BackButton);

        toSettings = (ImageView) findViewById(R.id.ProfileActivity_SettingsButton);

        profileImage = (ImageView) findViewById(R.id.ProfileActivity_ProfileImageContainer);
        /**
        if (kt_id == null) {
            setProfileImage(MY_KT_ID);
        } else {
            setProfileImage(kt_id);
        }
         **/

        imageGridAdapter = new ImageGridAdapter(ProfileActivity.this, R.id.ProfileActivity_GridView, images);

        gridView = (GridView) findViewById(R.id.ProfileActivity_GridView);
        gridView.setAdapter(imageGridAdapter);

        //downloadImages(MY_KT_ID);
    }

    private void setProfileImage(String id) {

        URL img_value = null;

        try {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
            img_value = new URL("https://graph.facebook.com/" + id + "/picture?width=120&height=120");
            Bitmap mIcon = BitmapFactory.decodeStream(img_value.openConnection().getInputStream());

            //make bitmap circular
            Bitmap circleBitmap = Bitmap.createBitmap(mIcon.getWidth(), mIcon.getHeight(), Bitmap.Config.ARGB_8888);
            BitmapShader shader = new BitmapShader(mIcon, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
            Paint paint = new Paint();
            paint.setShader(shader);
            Canvas canvas = new Canvas(circleBitmap);
            canvas.drawCircle(mIcon.getWidth()/2, mIcon.getHeight()/2, mIcon.getWidth()/2, paint);

            profileImage.setImageBitmap(circleBitmap);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    // connect and download streaming
    private void downloadImages(String id) {

        System.out.println("downloadImages");

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        try {

            URL url = new URL("http://kandi.nodejitsu.com/download_my_images");

            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setDoOutput(true);
            urlConnection.setDoInput(true);
            urlConnection.setRequestMethod("POST");
            urlConnection.setUseCaches(false);
            urlConnection.setRequestProperty("Content-Type", "application/json");

            DataOutputStream outputStream = new DataOutputStream(urlConnection.getOutputStream());

            JsonQrObject postObject = new JsonQrObject();
            postObject.setKt_id(id);
            String entity = new String(postObject.toString());

            outputStream.writeBytes(entity);
            outputStream.close();
            outputStream.flush();

            // get response
            InputStream responseStream = new BufferedInputStream(urlConnection.getInputStream());
            BufferedReader responseStreamReader = new BufferedReader(new InputStreamReader(responseStream));

            String line = "";
            StringBuilder stringBuilder = new StringBuilder();
            while ((line = responseStreamReader.readLine()) != null) {
                stringBuilder.append(line);
            }
            responseStreamReader.close();

            String response = stringBuilder.toString();
            System.out.println(response);


        } catch (MalformedURLException e) {

        } catch (IOException e) {

        }
    }

    private void connectSocket() {
        try {
            IO.Options options = new IO.Options();
            options.forceNew = true;
            socket = IO.socket(HOST, options);
            socket.on("download_images", onDownloadImages);
            socket.on(com.github.nkzawa.socketio.client.Socket.EVENT_CONNECT, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    System.out.println("socket connected");
                }
            }).on(com.github.nkzawa.socketio.client.Socket.EVENT_DISCONNECT, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    System.out.println("socket disconnected");
                }
            });
        } catch (URISyntaxException use) {
            use.printStackTrace();
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.i(TAG, "connecting socket");
                socket.connect();
                socket.emit("download_images", MY_KT_ID);
                System.out.println(MY_KT_ID);
            }
        }).start();
    }

    private Emitter.Listener onDownloadImages = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    byte[] image = (byte[]) args[0];
                    images.add(image);
                    System.out.println(images.size());
                    imageGridAdapter.notifyDataSetChanged();
                    gridView.invalidate();
                    //String message = (String) args[0];
                    //System.out.println("onDownloadImages: " + message);
                }
            });
        }
    };
}
