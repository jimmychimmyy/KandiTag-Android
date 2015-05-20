package com.jimchen.kanditag;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Shader;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.Session;
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
import java.io.ByteArrayOutputStream;
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
import java.util.List;
import java.util.zip.Inflater;

/**
 * Created by Jim on 4/20/15.
 */
public class ProfileActivity extends FragmentActivity {

    private static final int RESULT_LOGGED_OUT = 9;

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
    // labels for aboutme, friends, gallery
    private Button aboutme_button, friends_button, gallery_button;

    private GridView gridView;

    private ArrayList<byte[]> images = new ArrayList<>();

    private ImageGridAdapter imageGridAdapter;

    private Bitmap profileImageByteArray;

    private ViewPager viewPager;
    private MyPageAdapter pgAdapter;

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
        Intent result = new Intent();
        setResult(RESULT_OK, result);
        finish();
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

        backButton = (ImageView) findViewById(R.id.ProfileActivity_back);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
                overridePendingTransition(R.anim.right_slide_in, R.anim.right_slide_out);
            }
        });

        toSettings = (ImageView) findViewById(R.id.ProfileActivity_opensettings);
        toSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent openSettings = new Intent(ProfileActivity.this, SettingsActivity.class);
                startActivityForResult(openSettings, 1);
                overridePendingTransition(R.anim.right_slide_in, R.anim.right_slide_out);
            }
        });

        usernameTextView = (TextView) findViewById(R.id.ProfileActivity_UserName);
        if (kt_id == null) {
            usernameTextView.setText(MY_USER_NAME);
        }

        //backButton = (ImageView) findViewById(R.id.ProfileActivity_BackButton);

        //toSettings = (ImageView) findViewById(R.id.ProfileActivity_SettingsButton);

        profileImage = (ImageView) findViewById(R.id.ProfileActivity_ProfileImageContainer);

        aboutme_button = (Button) findViewById(R.id.ProfileActivity_AboutMeLabel);
        gallery_button = (Button) findViewById(R.id.ProfileActivity_GalleryLabel);
        friends_button = (Button) findViewById(R.id.ProfileActivity_FriendsLabel);

        pgAdapter = new MyPageAdapter(getSupportFragmentManager(), getFragmentList());
        viewPager = (ViewPager) findViewById(R.id.ProfileActivity_ViewPager);
        viewPager.setAdapter(pgAdapter);
        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                switch (position) {
                    case 0:
                        aboutme_button.setTextColor(getResources().getColor(R.color.gold));
                        gallery_button.setTextColor(getResources().getColor(R.color.white));
                        friends_button.setTextColor(getResources().getColor(R.color.white));
                        break;
                    case 1:
                        aboutme_button.setTextColor(getResources().getColor(R.color.white));
                        gallery_button.setTextColor(getResources().getColor(R.color.gold));
                        friends_button.setTextColor(getResources().getColor(R.color.white));
                        break;
                    case 2:
                        aboutme_button.setTextColor(getResources().getColor(R.color.white));
                        gallery_button.setTextColor(getResources().getColor(R.color.white));
                        friends_button.setTextColor(getResources().getColor(R.color.gold));
                        break;
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        aboutme_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewPager.setCurrentItem(0);
            }
        });

        gallery_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewPager.setCurrentItem(1);
            }
        });

        friends_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewPager.setCurrentItem(2);
            }
        });

        //TODO move this into the view pager
        /**
        imageGridAdapter = new ImageGridAdapter(ProfileActivity.this, R.id.ProfileActivity_GridView, images);

        gridView = (GridView) findViewById(R.id.ProfileActivity_GridView);
        gridView.setAdapter(imageGridAdapter);
         **/

        //downloadImages(MY_KT_ID);
    }

    private void setProfileImage(Bitmap mIcon) {

        try {
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

        profileImage.invalidate();
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //Session.getActiveSession().onActivityResult(this, requestCode, resultCode, data);
        System.out.println(resultCode + requestCode);
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                System.out.println("Its okay");
            } else if (resultCode == RESULT_LOGGED_OUT) {
                System.out.print("Logged out");
                Intent result = new Intent();
                setResult(RESULT_LOGGED_OUT, result);
                finish();
                overridePendingTransition(R.anim.right_slide_in, R.anim.abc_fade_out);
            }
        }
    }

    private List<android.support.v4.app.Fragment> getFragmentList() {
        List<android.support.v4.app.Fragment> fList = new ArrayList<>();
        fList.add(ProfileAboutMeFragment.newInstance());
        fList.add(ProfileGalleryFragment.newInstance());
        fList.add(ProfileFriendsFragment.newInstance());
        return fList;
    }

    private void connectSocket() {
        try {
            IO.Options options = new IO.Options();
            options.forceNew = true;
            socket = IO.socket(HOST, options);
            socket.on("download_profile_image", onDownloadProfileImage);
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
                socket.emit("download_profile_image", MY_KT_ID);
                //TODO download the images, but send them into the fragment
                //socket.emit("download_images", MY_KT_ID);
                System.out.println(MY_KT_ID);
            }
        }).start();
    }

    //TODO use this to send the image into the fragment
    private void sendImageBroadcast(Intent intent, Context context, String message) {
        intent.putExtra("Message", message);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
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

    private Emitter.Listener onDownloadProfileImage = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    byte[] image = (byte[]) args[0];
                    byte[] decompressed_image = decompressByteArray(image);
                    Bitmap bitmap = BitmapFactory.decodeByteArray(decompressed_image, 0, decompressed_image.length);
                    //profileImageByteArray = bitmap;
                    setProfileImage(bitmap);
                    //profileImage.setImageBitmap(profileImageByteArray);
                }
            });
        }
    };

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
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.right_slide_in, R.anim.right_slide_out);
    }
}
