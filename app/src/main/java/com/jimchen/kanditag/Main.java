package com.jimchen.kanditag;

import android.app.ActionBar;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Shader;
import android.graphics.drawable.ColorDrawable;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.os.StrictMode;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.*;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.facebook.Session;
import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.FormatException;
import com.google.zxing.LuminanceSource;
import com.google.zxing.NotFoundException;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Reader;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;
import com.ikimuhendis.ldrawer.DrawerArrowDrawable;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.Deflater;

import eu.livotov.zxscan.ScannerView;


public class Main extends ActionBarActivity {

    private static final String TAG = "Main";
    private Context context;

    // request codes
    public static final int SIGN_OUT_REQUEST = 0;
    public static final int SIGN_IN_REQUEST = 1;
    public static final int PREVIEW_IMAGE_REQUEST = 2;
    public static final int RESULT_LOGGED_OUT = 9;
    public static final int DECODE_REQUEST = 3;

    // actions
    public static final String ACTION_POPULATE_KANDITAG_DISPLAY = "com.jimchen.kanditag.action.POPULATE_KT_DISPLAY";
    public static final String ACTION_ADD_NEW_MESSAGE = "com.jimchen.kanditag.action.ADD_NEW_MESSAGE";
    public static final String ACTION_ADD_NEW_EXCHANGE = "com.jimchen.kanditag.action.ADD_NEW_EXCHANGE";
    public static final String ACTION_ADD_NEW_POST = "com.jimchen.kanditag.action.ADD_NEW_POST";


    // extras
    public static final String KT_USER_DATA = "com.jimchen.kanditag.extra.KT_USER";
    public static final String DONE = "com.jimchen.kanditag.extra.DONE";

    //Gcm variables
    public static final String EXTRA_MESSAGE = "com.jimchen.kanditag.extra.EXTRA_MESSAGE";
    public static final String PROPERTY_REG_ID = "com.jimchen.kanditag.extra.REGISTRATION_ID";
    private static final String PROPERTY_APP_VERSION = "com.jimchen.kanditag.extra.APP_VERSION";
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private String SENDER_ID = "936676106366";
    private String reg_id;
    private GoogleCloudMessaging gcm;
    private AtomicInteger msgId = new AtomicInteger();

    // Shared Preferences
    private SharedPreferences sharedPreferences;
    public static final String USER_PREFERENCES = "com.jimchen.kanditag.extra.PREFERENCES";
    public static final String USERNAME = "com.jimchen.kanditag.extra.USERNAME";
    public static final String FBID = "com.jimchen.kanditag.extra.FBID";
    public static final String KTID = "com.jimchen.kanditag.extra.KTID";
    public static final String USER_PROFILE_IMAGE = "com.jimchen.kanditag.extra.USER_PROFILE_IMAGE";
    public static final String OPENED_BEFORE = "com.jimchen.kanditag.extra.OPENED_BEFORE";

    // Local Database
    private KtDatabase myDatabase;
    private String MY_KT_ID, MY_FB_ID, MY_USER_NAME;
    protected SQLiteDatabase sqLiteDatabase;
    private static final String DATABASE_NAME = "com.jimchen.kanditag.extra.KTDATABASE";
    private static final String CREATE_TABLE_IF_NOT = "CREATE TABLE IF NOT EXISTS ";

    // socket variables
    private static com.github.nkzawa.socketio.client.Socket socket;
    private final String HOST = "http://kandi.jit.su/";
    private final int portNumber = 3000;

    // Camera Variables
    private Camera myCamera;
    private Preview myPreview;
    public static final String CAPTURED_IMAGE = "com.jimchen.kanditag.extras.CAPTURED_IMAGE";

    // xml
    private ViewPager mainViewPager;
    private MyPageAdapter mainPageAdapter;
    private ImageView cameraButton;
    private ImageView capturedImageContainer;
    private Button cancelImage, postImage;

    // qr code reader vars
    // QrCode Variables
    private ScannerView scanner;
    private boolean takenImage = false;
    private Result qrResult;
    private String decodedKandiID;
    private ArrayList<KtUserObject> ktUsersList; // list to hold kt users from kanditag

    // variables
    private ArrayList<KtUserObject> scannedKtUsersList = new ArrayList<>();

    // drawer
    private ListView leftDrawer;

    // drawer layout
    private ImageView profileImageContainer;
    private ListView listViewDrawer;
    private DrawerLayout drawerLayout;
    //private DrawerArrowDrawable mDrawerArrow;
    //private com.ikimuhendis.ldrawer.ActionBarDrawerToggle mDrawerToggle;

    private ActionBarDrawerToggle mDrawerToggle;

    // int to tell what page view is on
    private int currentPage = 0;

    private FeedFragment feedFragment;
    private MessageFragment messageFragment;
    private ExchangeFragment exchangeFragment;

    // open camera
    private ImageView openCamera;

    // add new
    private ImageView addNew;

    // download profile image async task
    private DownloadProfileImageAsyncTask downloadProfileImage;

    private int getScreenOrientation() {
        return getResources().getConfiguration().orientation;
    }

    /**
    private void setupOpenCamera() {
        openCamera = (ImageView) findViewById(R.id.Main_openCamera);
        openCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Main.this, CameraPreview.class);
                startActivity(intent);
                overridePendingTransition(R.anim.abc_slide_in_bottom, R.anim.abc_slide_out_bottom);
            }
        });
    }

    private void setupAddNew() {
        addNew = (ImageView) findViewById(R.id.Main_addNew);
        addNew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                switch (currentPage) {
                    case 0:
                        Intent postIntent = new Intent(ACTION_ADD_NEW_POST);
                        LocalBroadcastManager.getInstance(Main.this).sendBroadcast(postIntent);
                        break;
                    case 1:
                        Intent messageIntent = new Intent(ACTION_ADD_NEW_MESSAGE);
                        LocalBroadcastManager.getInstance(Main.this).sendBroadcast(messageIntent);
                        break;
                    case 2:
                        Intent exchangeIntent = new Intent(ACTION_ADD_NEW_EXCHANGE);
                        LocalBroadcastManager.getInstance(Main.this).sendBroadcast(exchangeIntent);
                        break;
                }
            }
        });
    } **/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_portrait);
        context = getApplicationContext();
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

        // open database and get user info from shared preferences
        myDatabase = new KtDatabase(this);
        sqLiteDatabase = openOrCreateDatabase(DATABASE_NAME, MODE_PRIVATE, null);
        sharedPreferences = this.getSharedPreferences(USER_PREFERENCES, Context.MODE_PRIVATE);
        MY_KT_ID = sharedPreferences.getString(KTID, "");
        MY_USER_NAME = sharedPreferences.getString(USERNAME, "");
        MY_FB_ID = sharedPreferences.getString(FBID, "");

        // check if opened before
        checkIfOpenedBefore();

        // connect to gcm notification service
        checkForGCMServices();

        // connect socket
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
        } catch (URISyntaxException use) {
            use.printStackTrace();
        }

        /**
        ActionBar ab = getActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setHomeButtonEnabled(true);
        **/

        // TODO grab profile image from server

        // instantiate fragments and add feed to view

        //setupOpenCamera();

        //setupAddNew();

        feedFragment = FeedFragment.newInstance();
        messageFragment = MessageFragment.newInstance();
        exchangeFragment = ExchangeFragment.newInstance();

        getSupportFragmentManager().beginTransaction().add(R.id.Main_ContentContainer, messageFragment, "Message").commit();
        getSupportFragmentManager().beginTransaction().add(R.id.Main_ContentContainer, exchangeFragment, "Exchange").commit();
        getSupportFragmentManager().beginTransaction().add(R.id.Main_ContentContainer, feedFragment, "Feed").commit();


        profileImageContainer = (ImageView) findViewById(R.id.Main_ProfileImageContainer);

        // list view drawer
        drawerLayout = (DrawerLayout) findViewById(R.id.Main_DrawerLayout);
        listViewDrawer = (ListView) findViewById(R.id.Main_ListViewDrawer);

        ArrayList<NavDrawerItem> navDrawerItems = new ArrayList<>();
        navDrawerItems.add(new NavDrawerItem(getResources().getDrawable(R.drawable.splash_screen_kt_logo_universal), "Home"));
        navDrawerItems.add(new NavDrawerItem(getResources().getDrawable(R.drawable.splash_screen_kt_logo_universal), "Messages"));
        navDrawerItems.add(new NavDrawerItem(getResources().getDrawable(R.drawable.splash_screen_kt_logo_universal), "Exchange"));
        navDrawerItems.add(new NavDrawerItem(getResources().getDrawable(R.drawable.splash_screen_kt_logo_universal), "Friends"));
        navDrawerItems.add(new NavDrawerItem(getResources().getDrawable(R.drawable.splash_screen_kt_logo_universal), "Meet"));
        //navDrawerItems.add(new NavDrawerItem(getResources().getDrawable(R.drawable.splash_screen_kt_logo_universal), "Settings"));

        final NavDrawerAdapter drawerAdapter = new NavDrawerAdapter(Main.this, R.layout.nav_drawer_layout, navDrawerItems);

        listViewDrawer.setAdapter(drawerAdapter);

        listViewDrawer.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Log.d(TAG, position + " has been selected");
                switch (position) {
                    case 0:
                        currentPage = 0;
                        getSupportFragmentManager().beginTransaction().hide(exchangeFragment).hide(messageFragment).show(feedFragment).commit();

                        // TODO may want to use a handler to delay the drawer closing, maybe
                        drawerLayout.closeDrawers();
                        break;
                    case 1:
                        currentPage = 1;
                        getSupportFragmentManager().beginTransaction().hide(feedFragment).hide(exchangeFragment).show(messageFragment).commit();
                        drawerLayout.closeDrawers();
                        break;
                    case 2:
                        currentPage = 2;
                        getSupportFragmentManager().beginTransaction().hide(feedFragment).hide(messageFragment).show(exchangeFragment).commit();
                        drawerLayout.closeDrawers();
                        break;
                    /**
                    case 3:
                        drawerLayout.closeDrawers();
                        Intent intent = new Intent(Main.this, SettingsActivity.class);
                        startActivityForResult(intent, SIGN_OUT_REQUEST);
                        break; **/
                }
            }
        });


        // instantiate services
        IntentServiceDownloadFeed downloadFeed = new IntentServiceDownloadFeed();
        downloadFeed.startDownloadingFeed(Main.this);

        mDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {

            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                //getSupportActionBar().setTitle("");
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
                //invalidateOptionsMenu();
            }

            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                //getSupportActionBar().setTitle("username");
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
                //invalidateOptionsMenu();
            }

        };


        android.support.v7.app.ActionBar actionBar = getSupportActionBar();

        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM | ActionBar.DISPLAY_SHOW_HOME);
        actionBar.setDisplayUseLogoEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        //actionBar.setDisplayShowTitleEnabled(false);

        //actionBar.setLogo(R.drawable.kanditag_icon);
        actionBar.setCustomView(R.layout.kanditag_actionbar);
        actionBar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.black)));

        // TODO will need to create a custom action bar

        mDrawerToggle.setDrawerIndicatorEnabled(true);
        drawerLayout.setDrawerListener(mDrawerToggle);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        //getSupportActionBar().setLogo(R.drawable.splash_screen_kt_logo_universal);
        //getSupportActionBar().invalidateOptionsMenu();
        //downloadProfileImage(MY_KT_ID);

        // TODO will need to make sure the profile image downloads instead of getting it from facebook
        getProfileImage(MY_FB_ID);
        //Bitmap pic = BitmapFactory.decodeByteArray(image, 0, image.length);
        //profileImageContainer.setImageBitmap(pic);

    }


    // TODO make sure that this works
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        /**
        if (item.getItemId() == android.R.id.home) {
            if (drawerLayout.isDrawerOpen(drawerLayout)) {
                drawerLayout.closeDrawer(listViewDrawer);
            } else {
                drawerLayout.openDrawer(listViewDrawer);
            }
        } **/

        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        if (id == R.id.action_camera) {
            Log.i(TAG, "action camera");
            Intent intent = new Intent(Main.this, CameraPreview.class);
            startActivity(intent);
            overridePendingTransition(R.anim.abc_slide_in_bottom, R.anim.abc_slide_out_bottom);
        }

        if (id == R.id.action_notification) {
            Log.i(TAG, "action notification");
        }

        if (id == R.id.action_open_settings) {
            Log.i(TAG, "action settings");
            drawerLayout.closeDrawers();
            Intent intent = new Intent(Main.this, SettingsActivity.class);
            startActivityForResult(intent, SIGN_OUT_REQUEST);
        }

        if (id == R.id.action_contact) {
            Log.i(TAG, "action contact us");
        }

        // Activate the navigation drawer toggle
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(android.view.Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.kanditag_actionbar, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        mDrawerToggle.onConfigurationChanged(newConfig);
        // Checks the orientation of the screen
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            Toast.makeText(this, "landscape", Toast.LENGTH_SHORT).show();
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
            Toast.makeText(this, "portrait", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public void onResume() {
        super.onResume();
        checkPlayServices();
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

    }

    @Override
    public void onPause() {
        super.onPause();
    }

    private static Camera getCameraInstance() {
        Camera camera = null;
        try {
            camera = Camera.open();
        } catch (Exception e) {
            e.printStackTrace();
            //TODO check to make sure to catch the camera cannot be connected error
            // show alert dialogue that user needs to restart camera
        }

        return camera;
    }

    // method to compress image before posting
    private byte[] compressByteArray(byte[] bytes) {

        ByteArrayOutputStream baos = null;
        Deflater deflater = new Deflater();
        deflater.setLevel(Deflater.BEST_COMPRESSION);
        deflater.setInput(bytes);
        deflater.finish();
        baos = new ByteArrayOutputStream();
        byte[] temp = new byte[64*1024];
        try {
            while (!deflater.finished()) {
                int size = deflater.deflate(temp);
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

    private void getProfileImage(final String id) {

        AsyncTask<String, Void, Bitmap> getProfilePic = new AsyncTask<String, Void, Bitmap>() {
            @Override
            protected Bitmap doInBackground(String... params) {

                String _id = params[0];

                Log.d(TAG, "downloading profile image");
                URL img_value = null;
                Bitmap mIcon = null;
                try {
                    StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                    StrictMode.setThreadPolicy(policy);
                    img_value = new URL("https://graph.facebook.com/" + _id + "/picture?width=1000&height=1000");
                    mIcon = BitmapFactory.decodeStream(img_value.openConnection().getInputStream());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                try {
                    mIcon.compress(Bitmap.CompressFormat.PNG, 0, stream);
                } catch (NullPointerException e) {

                }
                return mIcon;
            }

            @Override
            protected void onPostExecute(Bitmap bitmap) {
                profileImageContainer.setImageBitmap(bitmap);
            }
        };

        getProfilePic.execute(id);

        //return stream.toByteArray();
    }

    // socket io ***********************************************************************************

    private void uploadImage(final byte[] img, final String img_caption) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                socket.connect();
                socket.on("upload_image", onUploadImage);
                socket.emit("upload_image", MY_KT_ID, img, img_caption);
            }
        }).start();
    }

    private Emitter.Listener onUploadImage = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    String message = (String) args[0];
                    Log.d(TAG, message);
                    //socket.disconnect();
                }
            });
        }
    };

    private void displayKandiTag(final String kandi_id) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                socket.connect();
                socket.on("display_kanditag", onDisplayKandiTag);
                socket.emit("display_kanditag", MY_KT_ID, kandi_id);
            }
        }).start();
    }

    // TODO honestly this shit is a mess
    private Emitter.Listener onDisplayKandiTag = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            String message = (String) args[0];
            try {
                GsonBuilder gsonBuilder = new GsonBuilder();
                gsonBuilder.registerTypeAdapter(ResponseResults.class, new ResponseResultsDeserializer());
                Gson gson = gsonBuilder.create();
                ResponseResults results = gson.fromJson(message, ResponseResults.class);

                // build ktUsers based off of the json messages that come back
                KtUserObject user = new KtUserObject();
                user.setKt_id(results.getKt_id());
                user.setUsername(results.getUsername());
                user.setKandiId(results.getKandi_id());
                user.setPlacement(results.getPlacement());
                // TODO this contain statement does not work

                    ktUsersList.add(user);
                    Log.d(TAG, "kt_id: " + user.getKt_id());
                    Log.d(TAG, "placement: " + user.getPlacement());
                    Log.d(TAG, "kandi_id: " + user.getKandiId());
                    Log.d(TAG, "username: " + user.getUsername());
                    Intent localIntent = new Intent(ACTION_POPULATE_KANDITAG_DISPLAY).putExtra(KT_USER_DATA, message);
                    LocalBroadcastManager.getInstance(Main.this).sendBroadcast(localIntent);

            } catch (Exception e) {}

            try {
                if (message.equals("done")) {
                    Log.d(TAG, "done checking kanditag");
                    decodedKandiID = "";
                }
            } catch (Exception e) {}
        }
    };


    private void registerKandiTag(final String kandi_id) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                socket.connect();
                socket.on("register_kanditag", onRegisterKandiTag);
                socket.emit("register_kanditag", MY_KT_ID, MY_USER_NAME, kandi_id);
            }
        }).start();
    }

    private void addKandiName(final String kandi_name, final String kandi_id) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                socket.connect();
                socket.on("add_kandi_name", onAddKandiName);
                socket.emit("add_kandi_name", MY_KT_ID, kandi_id, kandi_name);

            }
        }).start();
    }

    private Emitter.Listener onRegisterKandiTag = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            try {
                String message = (String) args[0];
                Log.d(TAG, message);

                if (message.equals("successfully registered new KandiTag")) {

                } else {
                    GsonBuilder gsonBuilder = new GsonBuilder();
                    gsonBuilder.registerTypeAdapter(ResponseResults.class, new ResponseResultsDeserializer());
                    Gson gson = gsonBuilder.create();
                    ResponseResults results = gson.fromJson(message, ResponseResults.class);
                    KtUserObject user = new KtUserObject();
                    user.setKt_id(results.getKt_id());
                    user.setUsername(results.getUsername());
                    user.setKandiId(results.getKandi_id());
                    user.setPlacement(results.getPlacement());
                    boolean exists = myDatabase.checkForExistingKtUser(user);
                    if (!exists) {
                        myDatabase.saveKtUser(user);
                    }
                }
            } catch (Exception e) {}
        }
    };

    private Emitter.Listener onAddKandiName = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            String message = (String) args[0];
        }
    };

    private void uploadProfileImage(final byte[] image) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                socket.connect();
                Log.d(TAG, "trying to upload profile image");
                socket.on("upload_profile_image", onUploadProfileImage);
                socket.emit("upload_profile_image", MY_KT_ID, image);

            }
        }).start();
    }

    private Emitter.Listener onUploadProfileImage = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            String response = (String) args[0];
            Log.d(TAG, response);

        }
    };

    private void downloadProfileImage(final String kt_id) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                socket.connect();
                socket.on("download_profile_image", onDownloadProfileImage);
                socket.emit("download_profile_image", kt_id);

            }
        }).start();
    }

    private Emitter.Listener onDownloadProfileImage = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            byte[] data = (byte[]) args[0];
            try {
                final Bitmap image = BitmapFactory.decodeByteArray(data, 0, data.length);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        profileImageContainer.setImageBitmap(image);
                        Log.d(TAG, "got the profile image");
                        profileImageContainer.invalidate();
                    }
                });
            } catch (NullPointerException e) {
                Log.d(TAG, "error downloading profile image from server");
                 //byte[] image = getProfileImage(MY_FB_ID);
                 //Bitmap pic = BitmapFactory.decodeByteArray(image, 0, image.length);
                 //profileImageContainer.setImageBitmap(pic);
            }
            //socket.disconnect();
        }
    };

    // socket io end *******************************************************************************


    private void checkIfOpenedBefore() {

        boolean openedBefore = sharedPreferences.getBoolean(OPENED_BEFORE, false);

        if (!openedBefore) {
            Log.d(TAG, "not opened before");

            // save profile image into the server
            //uploadProfileImage(compressByteArray(getProfileImage(MY_FB_ID)));

            //checkKtOwnershipForMeAsyncTask.execute();
            //checkKtOwnershipForUsersAsyncTask.execute();
            //1getKandiNameFromKtQrcodeAsyncTask.execute();
            downloadMessagesFromServerAsyncTask.execute();

            // TODO start services to download whatever

            sharedPreferences.edit().putBoolean(OPENED_BEFORE, true).commit();
        }

        Log.d(TAG, "KandiTag is ready to go!");
    }

    private void checkForGCMServices() {

        if (checkPlayServices()) {
            //proceed
            gcm = GoogleCloudMessaging.getInstance(this);
            reg_id = getRegistrationId(context);

            if (reg_id.isEmpty()) {
                registerInBackground();
            }
        } else {
            //alert user that they need to get valid play services apk
            Log.i(TAG, "No valid Google Play Services APK found");
        }

    }

    private List<Fragment> getFragmentList() {
        List<android.support.v4.app.Fragment> fList = new ArrayList<>();
        //fList.add(EmptyFragment.newInstance());
        //fList.add(DataFragment.newInstance());
        fList.add(FeedFragment.newInstance());
       // fList.add(KandiTagDisplay.newInstance());
        return fList;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Session.getActiveSession().onActivityResult(this, requestCode, resultCode, data);

        if (requestCode == SIGN_OUT_REQUEST) {
            if (resultCode == RESULT_LOGGED_OUT) {
                Session session = Session.getActiveSession();
                session.closeAndClearTokenInformation();
                Intent loginIntent = new Intent(Main.this, LoginActivity.class);
                startActivityForResult(loginIntent, SIGN_IN_REQUEST);
                overridePendingTransition(R.anim.abc_slide_in_bottom, R.anim.abc_fade_out);
                finish();
                overridePendingTransition(R.anim.abc_slide_in_bottom, R.anim.abc_fade_out);
            }

            if (resultCode == RESULT_CANCELED) {

            }
        }


        if (requestCode == SIGN_IN_REQUEST) {
            if (resultCode == RESULT_OK) {
                Log.d(TAG, "login complete, back in Main");
            }
        }

        if (requestCode == PREVIEW_IMAGE_REQUEST) {
            if (resultCode == RESULT_OK) {

            }
        }

        if (requestCode == DECODE_REQUEST) {
            Toast.makeText(Main.this, "Data scanned", Toast.LENGTH_SHORT).show();
        }

        /**
        if (requestCode == 2) {
            Bitmap photo = (Bitmap) data.getExtras().get("data");
            takenPicture.setImageBitmap(photo);
        } **/

        if (requestCode == 3) {
            if (resultCode == RESULT_OK) {
                Log.i(TAG, "back in Main");
            }
        }
    }

    private void makeBitmapCircular(Bitmap image) {

        try {
            //make bitmap circular
            Bitmap circleBitmap = Bitmap.createBitmap(image.getWidth(), image.getHeight(), Bitmap.Config.ARGB_8888);
            BitmapShader shader = new BitmapShader(image, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
            Paint paint = new Paint();
            paint.setShader(shader);
            Canvas canvas = new Canvas(circleBitmap);
            canvas.drawCircle(image.getWidth()/2, image.getHeight()/2, image.getWidth()/2, paint);

            cameraButton.setImageBitmap(circleBitmap);

        } catch (Exception e) {
            e.printStackTrace();
        }

        cameraButton.invalidate();
    }

    // qr scanning methods *************************************************************************

    //method to check server for preexisting qr code
    private void checkIfQrExists(String qr) {
        Log.d(TAG, "checkIfQrExists");

        if (qr.contains("dhc")) {

            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);

            String Url = "http://kandi.nodejitsu.com/kt_ownership_finduser";

            HttpClient client = new DefaultHttpClient();

            if (MY_KT_ID != null && MY_USER_NAME != null && MY_FB_ID != null) {

                try {

                    //Toast.makeText(MainActivity.this, "checking KandiTag...", Toast.LENGTH_SHORT).show();

                    HttpPost post = new HttpPost(Url);

                    JsonQrObject toPostData = new JsonQrObject(qr, MY_KT_ID, MY_FB_ID, MY_USER_NAME);

                    StringEntity entity = new StringEntity(toPostData.toString(), HTTP.UTF_8);
                    entity.setContentType("application/json");
                    post.setEntity(entity);


                    HttpResponse response = client.execute(post);
                    BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
                    String line = "";

                    while ((line = reader.readLine()) != null) {
                        //Log.i(TAG, "parsed: " + line);
                        //parseJSON(line);

                        GsonBuilder gsonBuilder = new GsonBuilder();
                        gsonBuilder.registerTypeAdapter(Res_End_Results.class, new Res_End_Results_Deserializer());
                        gsonBuilder.registerTypeAdapter(Records.class, new Records_Deserializer());
                        Gson gson = gsonBuilder.create();

                        Res_End_Results rj_obj = gson.fromJson(line, Res_End_Results.class);
                        Log.d(TAG, rj_obj.getSuccess().toString());

                        /**
                        try {
                            // there is no previous user, opening miniProfileView without params
                            Log.d(TAG, rj_obj.getPrevious_user().toString());
                            if (rj_obj.getPrevious_user().equals(false)) {
                                showMiniProfileView(null);
                            }
                            //if no previous user, then open mini profile view with kandi naming prompt
                            System.out.println("rj_obj.getPrevious_userCount() = " + rj_obj.getPrevious_userCount());

                        } catch (NullPointerException nulle) {}

                        for (Records records:rj_obj.getRecords()) {
                            //add these records into an object array, check to see if user is in the array, if no then allow add confirmation
                            //if object array has 8 users, kandi is full, do not allow add
                            Log.d(TAG, records.getKt_id());
                            Log.d(TAG, records.getFb_id());
                            Log.d(TAG, records.getUsername());
                            Log.d(TAG, records.getQrcode());

                            KtUserObject tempKtObj = new KtUserObject();
                            tempKtObj.setFb_id(records.getFb_id());
                            tempKtObj.setKt_id(records.getKt_id());
                            tempKtObj.setUsername(records.getUsername());
                            tempKtObj.setPlacement(records.getPlacement());
                            scannedQrUsersArrayList.add(tempKtObj);


                        }
                        System.out.println("length of scannedQrUsersArrayList: " + scannedQrUsersArrayList.size());
                        //TODO use enhanced for loop?
                        //for (int i = 0; i < scannedQrUsersArrayList.size(); i++) {
                        for (KtUserObject scannedQrUser : scannedQrUsersArrayList) {

                            scannedQrUsersFb_idArrayList.add(scannedQrUser.getFb_id());
                            MiniProfileViewItem tempItem = new MiniProfileViewItem();
                            tempItem.setFb_id(scannedQrUser.getFb_id());
                            tempItem.setUser_name(scannedQrUser.getUsername());
                            tempItem.setPlacement(scannedQrUser.getPlacement());
                            miniProfileViewItemArrayList.add(tempItem);

                        }

                        showMiniProfileView(miniProfileViewItemArrayList);
                         **/

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else {
            System.out.println("not a valid kanditag");
            Toast.makeText(Main.this, "Not a valid KandiTag", Toast.LENGTH_SHORT).show();
        }
    }

    //method to save qr code if there is space in the server ownership table
    private void postQr(String kandi_id, String kandi_name) {
        Log.d(TAG, "postQr");

        if (kandi_id.contains("dhc")) {

            //TODO read up on this strictMode policy
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);

            String Url = "http://kandi.nodejitsu.com/kt_qrcode_save";

            HttpClient client = new DefaultHttpClient();

            //TODO make sure this is the proper place to reset the decodedKandiID
            decodedKandiID = "";

            JsonQrObject toPostData = new JsonQrObject();

            if (MY_KT_ID != null && MY_USER_NAME != null && MY_FB_ID != null) {

                try {

                    Toast.makeText(Main.this, "Saving KandiTag...", Toast.LENGTH_SHORT).show();

                    HttpPost post = new HttpPost(Url);

                    if (kandi_name == null) {
                        toPostData = new JsonQrObject(kandi_id, MY_KT_ID, MY_FB_ID, MY_USER_NAME);
                    } else if (kandi_name != null) {
                        toPostData = new JsonQrObject(kandi_id, MY_KT_ID, MY_FB_ID, MY_USER_NAME, kandi_name);
                    }

                    StringEntity entity = new StringEntity(toPostData.toString(), HTTP.UTF_8);

                    entity.setContentType("application/json");
                    post.setEntity(entity);

                    HttpResponse response = client.execute(post);
                    BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
                    String line = "";

                    while ((line = reader.readLine()) != null) {
                        Log.i(TAG, "parsed: " + line);
                        //parseJSON(line);

                        //TODO this deserializer does not work because it is all one array, unlike the check
                        GsonBuilder gsonBuilder = new GsonBuilder();
                        gsonBuilder.registerTypeAdapter(Res_End_Results.class, new Res_End_Results_Deserializer());
                        gsonBuilder.registerTypeAdapter(Records.class, new Records_Deserializer());
                        Gson gson = gsonBuilder.create();

                        Res_End_Results resEndObj = gson.fromJson(line, Res_End_Results.class);
                        System.out.println("placement: " + resEndObj.getPlacement());
                        Log.i(TAG, resEndObj.getFb_id());
                        Log.i(TAG, resEndObj.getQrCode());
                        Log.i(TAG, resEndObj.getKt_id());
                        Log.i(TAG, resEndObj.getKandiName());

                        KandiObject kandiObject = new KandiObject();
                        kandiObject.setKandi_id(resEndObj.getQrCode());
                        kandiObject.setKandi_name(resEndObj.getKandiName());
                        if (resEndObj.getSuccess().equals(true)) {
                            Toast.makeText(Main.this, "Saved!", Toast.LENGTH_LONG).show();
                            myDatabase.saveKandi(kandiObject);
                        }

                        try {
                            for (Records records : resEndObj.getRecords()) {
                                Log.d(TAG, records.getKt_id());
                                Log.d(TAG, records.getFb_id());
                                Log.d(TAG, records.getUsername());
                                Log.d(TAG, records.getQrcode());
                                System.out.println("PostQr.Records.placement = " + records.getPlacement());
                            }
                        } catch (NullPointerException nullEx) {
                            nullEx.printStackTrace();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else {
            System.out.println("not a valid kanditag");
            Toast.makeText(Main.this, "Not a valid KandiTag", Toast.LENGTH_SHORT).show();
        }
    }

    // end qr scanning methods *********************************************************************

    // capture image methods ***********************************************************************

    // takePicture Callbacks **********
    // TODO I changed these fields to private, make sure that it still works
    private Camera.ShutterCallback shutterCallback = new Camera.ShutterCallback() {
        @Override
        public void onShutter() {
            Log.d(TAG, "onShutterCallback");
        }
    };

    private Camera.PictureCallback rawCallback = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            Log.d(TAG, "onPictureTaken - rawCallback");
            //TODO make sure this clears the scannedQrUsersArrayList
            scannedKtUsersList.removeAll(scannedKtUsersList);
        }
    };

    private Camera.PictureCallback pictureCallback = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] bytes, Camera camera) {

            String image_path;

            if (bytes != null) {

                int screenWidth = getApplicationContext().getResources().getDisplayMetrics().widthPixels;
                int screenHeight = getApplicationContext().getResources().getDisplayMetrics().heightPixels;
                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, (bytes != null) ? bytes.length : 0);

                Bitmap scaledB = Bitmap.createScaledBitmap(bitmap, bitmap.getWidth() / 2, bitmap.getHeight() / 2, true);
                int width = scaledB.getWidth();
                int height = scaledB.getHeight();

                //rotate 90 degrees with matrix
                Matrix matrix = new Matrix();
                matrix.postRotate(90);
                bitmap = Bitmap.createBitmap(scaledB, 0, 0, width, height, matrix, true);

                // check to see if a kanditag was scanned
                int[] intArray = new int[bitmap.getWidth() * bitmap.getHeight()];
                bitmap.getPixels(intArray, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());
                LuminanceSource source = new RGBLuminanceSource(bitmap.getWidth(), bitmap.getHeight(), intArray);

                BinaryBitmap binaryBitmap = new BinaryBitmap(new HybridBinarizer(source));
                Reader reader = new QRCodeReader();

                try {
                    qrResult = reader.decode(binaryBitmap);
                    decodedKandiID = qrResult.getText();
                } catch (NotFoundException nfe) {
                    decodedKandiID = "";
                    nfe.printStackTrace();
                } catch (ChecksumException cse) {
                    decodedKandiID = "";
                    cse.printStackTrace();
                } catch (FormatException fe) {
                    decodedKandiID = "";
                    fe.printStackTrace();
                }

                // if no kanditag was scanned
                if (decodedKandiID.equals("")) {
                    // upload image and save it internally
                    // start activity for image preview
                    DateFormat dateFormat = new SimpleDateFormat("_yyyy-MM-dd_HH-mm-ss");
                    Date date = new Date();
                    String filename = MY_KT_ID + dateFormat.format(date);

                    uploadImage(compressByteArray(bytes), filename);
                    image_path = saveImageInternally(bitmap, filename);

                    Intent imagePreview = new Intent(Main.this, ImagePreview.class);
                    Bundle extras = new Bundle();
                    extras.putString("filepath", image_path);
                    extras.putString("filename", filename);
                    imagePreview.putExtras(extras);
                    startActivityForResult(imagePreview, PREVIEW_IMAGE_REQUEST);

                } else if (decodedKandiID.contains("dhc")) {

                    // register kanditag

                    myPreview.myCamera.startPreview();

                    //TODO remove this before production
                    Toast.makeText(Main.this, qrResult.getText(), Toast.LENGTH_SHORT).show();

                    registerKandiTag(decodedKandiID);

                }


            }
        }
    };

    private Camera.PictureCallback pngCallback = new Camera.PictureCallback() {


        @Override
        public void onPictureTaken(final byte[] data, Camera camera) {

            Log.i(TAG, "data file size: " + data.length);

            Log.d(TAG, "onPictureTaken");


            // if there is data for the image
            if (data != null) {

                int screenWidth = getApplicationContext().getResources().getDisplayMetrics().widthPixels;
                int screenHeight = getApplicationContext().getResources().getDisplayMetrics().heightPixels;
                Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, (data != null) ? data.length : 0);

                if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                    Bitmap scaledB = Bitmap.createScaledBitmap(bitmap, bitmap.getWidth() / 2, bitmap.getHeight() / 2, true);
                    int width = scaledB.getWidth();
                    int height = scaledB.getHeight();

                    //rotate 90 degrees with matrix
                    Matrix matrix = new Matrix();
                    matrix.postRotate(90);
                    bitmap = Bitmap.createBitmap(scaledB, 0, 0, width, height, matrix, true);

                    // TODO upload the image here
                    // will need to get a response just in case the image needs to be deleted

                    // put image inside container
                    capturedImageContainer.setImageBitmap(bitmap);

                    int[] intArray = new int[bitmap.getWidth() * bitmap.getHeight()];
                    bitmap.getPixels(intArray, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());
                    LuminanceSource source = new RGBLuminanceSource(bitmap.getWidth(), bitmap.getHeight(), intArray);

                    BinaryBitmap binaryBitmap = new BinaryBitmap(new HybridBinarizer(source));
                    Reader reader = new QRCodeReader();

                    try {
                        qrResult = reader.decode(binaryBitmap);
                        decodedKandiID = qrResult.getText();
                    } catch (NotFoundException nfe) {
                        decodedKandiID = "";
                        nfe.printStackTrace();
                    } catch (ChecksumException cse) {
                        decodedKandiID = "";
                        cse.printStackTrace();
                    } catch (FormatException fe) {
                        decodedKandiID = "";
                        fe.printStackTrace();
                    }

                } else {
                    //landscape
                    Bitmap scaled = Bitmap.createScaledBitmap(bitmap, screenWidth, screenHeight, true);
                    bitmap = scaled;
                }

                if (decodedKandiID.equals("")) {

                    decodedKandiID = "";

                    // TODO will need to display the image in an external fragment

                    capturedImageContainer.setVisibility(View.VISIBLE);

                    cancelImage.setVisibility(View.VISIBLE);
                    cancelImage.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            capturedImageContainer.setVisibility(View.GONE);
                            cancelImage.setVisibility(View.GONE);
                            postImage.setVisibility(View.GONE);
                            myPreview.myCamera.startPreview();
                        }
                    });

                    postImage.setVisibility(View.VISIBLE);
                    postImage.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            // TODO this is temporary, the image upload will happen before this
                            DateFormat dateFormat = new SimpleDateFormat("_yyyy-MM-dd_HH-mm-ss");
                            Date date = new Date();
                            String filename = MY_KT_ID + dateFormat.format(date);
                            uploadImage(compressByteArray(data), filename);

                            capturedImageContainer.setVisibility(View.GONE);
                            cancelImage.setVisibility(View.GONE);
                            postImage.setVisibility(View.GONE);
                            myPreview.myCamera.startPreview();
                            // TODO ask for tags, any photo editing options
                        }
                    });

                    // TODO make sure to call myPreview.myCamera.startPreview(); when done with preview activity


                } else if (decodedKandiID.contains("dhc")) {
                    // if the image contains a kanditag qr code

                    // restart camera
                    myPreview.myCamera.startPreview();

                    //TODO remove this before production
                    Toast.makeText(Main.this, qrResult.getText(), Toast.LENGTH_SHORT).show();

                    registerKandiTag(decodedKandiID);

                }
            }
        }
    };

    // method to save taken images into internal storage
    // TODO save images into internal storage then open them up in another activity to add filters/etc
    private String saveImageInternally(Bitmap bitmap, String filename) {
        ContextWrapper wrapper = new ContextWrapper(getApplicationContext());

        // path to /data/data/kanditag/app_data/tmp_images
        File directory = wrapper.getDir("tmp_images", Context.MODE_PRIVATE);

        //create tmp_images
        File path = new File(directory, filename);

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(path);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return directory.getAbsolutePath();
    }

    // end capture image methods *******************************************************************

    // google play service methods *****************************************************************
    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this, PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.i(TAG, "This device is not supported");
            }
            return false;
        }
        return true;
    }

    private String getRegistrationId(Context context) {
        final SharedPreferences myPreferences = getGCMPreferences(context);
        String registrationId = myPreferences.getString(PROPERTY_REG_ID, "");
        if (registrationId.isEmpty()) {
            Log.i(TAG, "Registration not found");
            return "";
        }

        int registeredVersion = myPreferences.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
        int currentVersion = getAppVersion(context);
        if (registeredVersion != currentVersion) {
            Log.i(TAG, "App Version changed");
            return "";
        }
        return registrationId;
    }

    private SharedPreferences getGCMPreferences(Context context) {
        return getSharedPreferences(MainActivity.class.getSimpleName(), Context.MODE_PRIVATE);
    }

    private static int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException nameNotFoundEx) {
            throw new RuntimeException("Could not get package name: " + nameNotFoundEx);
        }
    }

    private void registerInBackground() {
        new AsyncTask<Void, Void, String>() {

            @Override
            protected String doInBackground(Void... params) {
                String mssg = "";
                try {
                    if (gcm == null) {
                        gcm = GoogleCloudMessaging.getInstance(context);
                    }
                    reg_id = gcm.register(SENDER_ID);
                    mssg = "Device registered, registration ID =" + reg_id;

                    sendRegistrationIdToBackend(reg_id);

                    storeRegistrationId(context, reg_id);
                } catch (IOException ioEx) {
                    mssg = "Error: " + ioEx.getMessage();
                }
                return mssg;
            }
        }.execute(null, null, null);
    }

    private void sendRegistrationIdToBackend(final String reg_id) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                StrictMode.setThreadPolicy(policy);

                String Url = "http://kandi.nodejitsu.com/save_device_token_android";

                HttpClient client = new DefaultHttpClient();

                JsonQrObject toPostData = new JsonQrObject();

                try {

                    HttpPost post = new HttpPost(Url);

                    toPostData.setToken(reg_id);
                    toPostData.setKt_id(MY_KT_ID);
                    toPostData.setFb_id(MY_FB_ID);
                    toPostData.setUser_name(MY_USER_NAME);

                    StringEntity entity = new StringEntity(toPostData.toString(), HTTP.UTF_8);

                    entity.setContentType("application/json");
                    post.setEntity(entity);

                    HttpResponse response = client.execute(post);
                    BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
                    String line = "";

                    while ((line = reader.readLine()) != null) {
                        Log.i(TAG, "parsed: " + line);
                    }

                } catch (Exception ex) {

                }
                return null;
            }
        }.execute(null, null, null);
    }

    private void storeRegistrationId(Context context, String reg_id) {
        final SharedPreferences myPreferences = getGCMPreferences(context);
        int appVersion = getAppVersion(context);
        Log.i(TAG, "Saving reg_id: " + reg_id +" on app version " + appVersion);
        SharedPreferences.Editor editor = myPreferences.edit();
        editor.putString(PROPERTY_REG_ID, reg_id);
        editor.putInt(PROPERTY_APP_VERSION, appVersion);
        editor.commit();
    }

    // End of Google Play Service methods **********************************************************

    // download for first time **********************************************************************

    //download group messages from server
    DownloadGroupMessagesFromServerAsyncTask downloadGroupMessagesFromServerAsyncTask = new DownloadGroupMessagesFromServerAsyncTask(Main.this, new ReturnKtMessageObjectArrayListAsyncResponse() {
        @Override
        public void processFinish(ArrayList<KtMessageObject> output) {
            System.out.println("MainActivity.downloadGroupMessagesFromServerAsyncTask.processFinish.output.size() = " + output.size());
        }
    });

    //download messages from servers
    DownloadMessagesFromServerAsyncTask downloadMessagesFromServerAsyncTask = new DownloadMessagesFromServerAsyncTask(Main.this, new ReturnKtMessageObjectArrayListAsyncResponse() {
        @Override
        public void processFinish(ArrayList<KtMessageObject> output) {
            System.out.println("MainActivity.downloadMessagesFromServerAsyncTask.processFinish.output.size() = " + output.size());
        }
    });

}
