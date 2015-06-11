package com.jimchen.kanditag;


import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.os.Bundle;

import android.os.Handler;
import android.os.StrictMode;

import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.Deflater;

import android.util.DisplayMetrics;
import android.util.Log;

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

import android.content.Intent;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;

public class MainActivity extends FragmentActivity {

    // request codes
    public static final int SIGN_OUT_REQUEST = 0;
    public static final int RESULT_LOGGED_OUT = 9;

    // buttons in xml
    private ImageView notifications, flashlight, flipcamera;

    // notification buttons
    private ImageView notif_feed, notif_message, notif_exchange;

    // bool to keep track of if notification expanded
    private boolean isNotificationsOpen;

    // VerticalZoomBar for zooming in/out camera
    private VerticalZoomBar zoomBar;

    // Handler for runnables (UI updates)
    private Handler myHandler = new Handler();

    //flashlight button
    private ImageView flashlightButton;
    private String flashMode = "off";

    //list of all users (no user should exist more that once in this list)
    private ArrayList<KtUserObjectParcelable> listOfUsers = new ArrayList<>();

    //list of all groups (no group should exist more that once in this list)
    private ArrayList<KandiGroupObjectParcelable> listOfGroups = new ArrayList<>();

    //ScaleGestureDetector Vars
    private ScaleGestureDetector myScaleGestureDetector;
    private float scale = 1.f;
    private Matrix matrix = new Matrix();

    //bottom and top of the preview
    private View previewTopBar, previewBottomBar;

    private ViewPager myViewPager;
    private MyPageAdapter pageAdapter;

    //Gcm variables
    public static final String EXTRA_MESSAGE = "message";
    public static final String PROPERTY_REG_ID = "registration_id";
    private static final String PROPERTY_APP_VERSION = "appVersion";
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    String SENDER_ID = "936676106366";
    String reg_id;
    Context context;
    GoogleCloudMessaging gcm;
    AtomicInteger msgId = new AtomicInteger();
    //End Gcm variables

    private Boolean alreadyOwned;

    private ArrayList<KtUserObject> scannedQrUsersArrayList = new ArrayList<>();
    private ArrayList<String> scannedQrUsersFb_idArrayList = new ArrayList<>();
    private ArrayList<MiniProfileViewItem> miniProfileViewItemArrayList = new ArrayList<>();

    private RelativeLayout main_rootView;

    private FrameLayout miniProfileViewFrameLayout;


    //variables for taking pictures and displaying the container for the captured pictures
    ImageView takenPicture;
    ImageView takePicture;
    ImageView shadedBackground;
    ImageView backToCamera;
    private ImageView takenPictureContainer;
    private Button closePictureDisplay;
    private Button saveAndClosePictureDisplay;


    // Shared Preferences
    private static final String TAG = "MainActivity:";
    SharedPreferences sharedPreferences;
    public static final String MY_PREFERENCES = "MyPrefs";
    public static final String NAME = "nameKey";
    public static final String FBID = "fbidKey";
    public static final String KTID = "userIdKey";
    public static final String MyProfilePicture = "My_Profile_Picture";
    public static final String OPENED_BEFORE = "opened_before";

// RequestCodes ********************************
    private static final int SETTINGS_REQUEST_CODE = 5;
    private static final int TAKEN_PICTURE_CONTAINER_REQUEST_CODE = 6;

    // Local Database
    KtDatabase myDatabase;
    private String MY_KT_ID, MY_FB_ID, MY_USER_NAME;
    protected SQLiteDatabase sqLiteDatabase;
    private static final String DATABASE_NAME = "myData";
    private static final String CREATE_TABLE_IF_NOT = "CREATE TABLE IF NOT EXISTS ";

    // Camera Variables
    Camera myCamera;
    Preview preview;
    int stillCount = 0;
    private Preview myPreview;
    public static final int PHOTO_WIDTH = 480;
    public static final int PHOTO_HEIGHT = 640;

    // QrCode Variables
    Result qrResult;
    String decodedQrString;

    //context for getCameraInstance
    public static Context baseContext;

    // try method to start camera
    public static Camera getCameraInstance() {
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

    private static com.github.nkzawa.socketio.client.Socket socket;
    private final String HOST = "http://kandi.jit.su/";
    private final int portNumber = 3000;

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

    private void uploadProfileImage(final byte[] img) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                socket.connect();
                socket.emit("upload_profile_image", MY_KT_ID, img);
                socket.on("upload_profile_image", onUploadProfileImage);
            }
        }).start();
    }

    private Emitter.Listener onUploadProfileImage = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    String message = (String) args[0];
                    System.out.println("onUploadImage: " + message);
                    //socket.disconnect();
                }
            });
        }
    };


    //TODO write a service to upload images

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

    @Override
    public void onResume() {
        super.onResume();
        checkPlayServices();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
        if (myCamera != null) {
            myCamera.stopPreview();
            myCamera.release();
        }
        socket.disconnect();
        socket.close();
    }

    // when the back button is clicked
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        //getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
        //System.out.println("MainActivity.onBackPressed()");
        // when returning to main from a fragment, set window to fullscreen
        //getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            scale *= detector.getScaleFactor();
            scale = Math.max(0.1f, Math.min(scale, 5.0f));
            matrix.setScale(scale, scale);
            //fragmentLayout.setImageMatrix()
            return true;
        }
    }

    // variables for determining swipe in onTouchEvent()
    private float x1, x2;
    static final int MIN_DISTANCE = 200;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //System.out.println("onTouchEvent: " + event.toString());
        switch(event.getAction())
        {
            case MotionEvent.ACTION_DOWN:
                x1 = event.getX();
                break;
            case MotionEvent.ACTION_UP:
                x2 = event.getX();
                float rightToLeftdeltaX = x1 - x2;
                float leftToRightDeltaX = x2 - x1;
                if (rightToLeftdeltaX > MIN_DISTANCE) {
                    //this is a right to left swipe (this will show to menu)
                    // begin animation to show menu items
                    // by not checking for menu visibility, we are able to keep the fade in animation every time the use swipes
                    //if (!isMenuVisible) {

                    //}

                }
                if (leftToRightDeltaX > MIN_DISTANCE) {
                    //this is a left to right swipe (this will hide menu);
                    // begin animation to hide menu items

                }

                break;
        }
        return false;
    }


    private byte[] getProfileImage(String id) {
        URL img_value = null;
        Bitmap mIcon = null;
        try {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
            img_value = new URL("https://graph.facebook.com/" + id + "/picture?width=1000&height=1000");
            mIcon = BitmapFactory.decodeStream(img_value.openConnection().getInputStream());
        } catch (Exception e) {
            e.printStackTrace();
        }
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        mIcon.compress(Bitmap.CompressFormat.PNG, 0, stream);
        return stream.toByteArray();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        context = getApplicationContext();
        baseContext = getBaseContext();
        //getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        myDatabase = new KtDatabase(this);
        sqLiteDatabase = openOrCreateDatabase(DATABASE_NAME, MODE_PRIVATE, null);
        sharedPreferences = this.getSharedPreferences(MY_PREFERENCES, Context.MODE_PRIVATE);

        MY_KT_ID = sharedPreferences.getString(KTID, "");
        MY_USER_NAME = sharedPreferences.getString(NAME, "");
        MY_FB_ID = sharedPreferences.getString(FBID, "");

        Intent dismissSplash = new Intent();
        setResult(RESULT_OK, dismissSplash);

        // connecting socket
        try {
            IO.Options options = new IO.Options();
            options.forceNew = true;
            socket = IO.socket(HOST, options);
            socket.on("upload_image", onUploadImage );
            socket.on("upload_profile_image", onUploadProfileImage);
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

        //TODO make sure this check works
        boolean openedBefore = sharedPreferences.getBoolean(OPENED_BEFORE, false);
        if (!openedBefore) {
            System.out.println("KandiTag is downloading all user data from server...");
            //TODO create groupMessages first before attempting to download them
            //downloadGroupMessagesFromServerAsyncTask.execute();
            checkKtOwnershipForMeAsyncTask.execute();
            checkKtOwnershipForUsersAsyncTask.execute();
            getKandiNameFromKtQrcodeAsyncTask.execute();
            downloadMessagesFromServerAsyncTask.execute();

            //TODO postImageToServer is still not ready
            /**
            //if signed up through facebook, take facebook profile image and save to db
            try {
                postImageToServer(MY_KT_ID, getProfileImage(MY_FB_ID));
            } catch (Exception e) {
                e.printStackTrace();
            }
             **/

            // saving profile picture to server
            // save profile picture into shared preferences
            byte[] profile_image_bytes = getProfileImage(MY_FB_ID);
            uploadProfileImage(compressByteArray(profile_image_bytes));
            try {
                String profile_image_string = new String(profile_image_bytes, "UTF-8");
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString(MyProfilePicture, profile_image_string);
                editor.commit();
            } catch (UnsupportedEncodingException e) {}

        } else {
            System.out.println("KandiTag is ready to go!");
        }

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


        // this is unneeded, this will only be used if the user has never logged in
        /**
        byte[] profile_image_bytes = getProfileImage(MY_FB_ID);
        //uploadProfileImage(compressByteArray(profile_image_bytes));
        try {
            String profile_image_string = new String(profile_image_bytes, "UTF-8");
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(MyProfilePicture, profile_image_string);
            editor.commit();
        } catch (UnsupportedEncodingException e) {}
         **/


        // instantiate services
        IntentServiceDownloadFeed downloadFeed = new IntentServiceDownloadFeed();
        downloadFeed.startDownloadingFeed(MainActivity.this, MY_KT_ID);

        myScaleGestureDetector = new ScaleGestureDetector(this, new ScaleListener());

        main_rootView = (RelativeLayout) findViewById(R.id.main_relativeLayout);

        // find take picture button, flipcamera button, notification, flashlight
        takePicture = (ImageView) findViewById(R.id.Main_CameraButton);
        flipcamera = (ImageView) findViewById(R.id.Main_FlipCamera);
        notifications = (ImageView) findViewById(R.id.Main_NotificationMaster);
        flashlight = (ImageView) findViewById(R.id.Main_Flashlight);

        // find notification buttons
        notif_feed = (ImageView) findViewById(R.id.Main_NotificationFeed);
        notif_message = (ImageView) findViewById(R.id.Main_NotificationMessage);
        notif_exchange = (ImageView) findViewById(R.id.Main_NotificationExchange);

        // make notification buttons invisible
        notif_feed.setVisibility(View.GONE);
        notif_message.setVisibility(View.GONE);
        notif_exchange.setVisibility(View.GONE);

        // initally set isNotificationOpen to false
        isNotificationsOpen = false;

        // set notification button onclick listener to expand view
        notifications.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (!isNotificationsOpen) {
                    //if notifications are not open, then open them and set bool to true
                    isNotificationsOpen = true;

                    myHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            notif_exchange.setVisibility(View.VISIBLE);

                        }
                    }, 150);
                    myHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            notif_message.setVisibility(View.VISIBLE);

                        }
                    }, 250);
                    myHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            notif_feed.setVisibility(View.VISIBLE);

                        }
                    }, 350);
                } else {
                    //else close them and set bool to false
                    isNotificationsOpen = false;

                    myHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            notif_feed.setVisibility(View.GONE);

                        }
                    }, 150);
                    myHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            notif_message.setVisibility(View.GONE);

                        }
                    }, 250);
                    myHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            notif_exchange.setVisibility(View.GONE);

                        }
                    }, 350);
                }
            }
        });

        pageAdapter = new MyPageAdapter(getSupportFragmentManager(), getFragmentList());
        myViewPager = (ViewPager) findViewById(R.id.Main_ViewPager);
        myViewPager.setAdapter(pageAdapter);
        myViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                switch (position) {
                    case 0:
                        takePicture.setVisibility(View.VISIBLE);
                        flipcamera.setVisibility(View.VISIBLE);
                        notifications.setVisibility(View.VISIBLE);
                        flashlight.setVisibility(View.VISIBLE);
                        myHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
                                //getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                            }
                        }, 500);
                        break;
                    case 1:
                        takePicture.setVisibility(View.GONE);
                        flipcamera.setVisibility(View.GONE);
                        notifications.setVisibility(View.GONE);
                        flashlight.setVisibility(View.GONE);
                        notif_feed.setVisibility(View.GONE);
                        notif_message.setVisibility(View.GONE);
                        notif_exchange.setVisibility(View.GONE);
                        isNotificationsOpen = false;
                        myHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
                                getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                            }
                        }, 500);
                        break;
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

        previewTopBar = findViewById(R.id.Main_PreviewTop);
        previewBottomBar = findViewById(R.id.Main_PreviewBottom);
        previewTopBar.setVisibility(View.GONE);
        previewBottomBar.setVisibility(View.GONE);


        /**
        //find zoomBar in xml file and set on Camera Zoom
        zoomBar = (VerticalZoomBar) findViewById(R.id.Main_ZoomBar);
        zoomBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                //System.out.println("onProgressChanged");
                //this is how i know the zoombar was moved
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
         **/


        //TODO need to set up camera to switch between front and back

        // starting camera
        myCamera = getCameraInstance();
        myPreview = new Preview(this, myCamera);
        ((FrameLayout) findViewById(R.id.main_frameLayoutPreview)).addView(myPreview);

        // set up torch
        flashlightButton = (ImageView) findViewById(R.id.Main_FlashlightButton);
        flashlightButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (flashMode) {
                    case "auto":
                        turnFlashOn();
                        break;
                    case "on":
                        turnFlashOff();
                        break;
                    case "off":
                        setFlashOnAuto();
                        break;
                }
            }
        });


        shadedBackground = (ImageView) findViewById(R.id.main_shadedBackground);
        shadedBackground.setVisibility(View.INVISIBLE);

        // set up button to take pictures TODO take videos
        takePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myPreview.myCamera.takePicture(shutterCallback, rawCallback, pngCallback);
            }
        });

        // container for captured picture
        takenPictureContainer = (ImageView) findViewById(R.id.main_takenPictureContainer);
        takenPictureContainer.setVisibility(View.GONE);


        // this is the display for the captured picture
        closePictureDisplay = (Button) findViewById(R.id.main_closePictureDisplay);
        closePictureDisplay.setVisibility(View.GONE);
        closePictureDisplay.setText("Nope");
        closePictureDisplay.setTextColor(getResources().getColor(R.color.vegas_gold));
        closePictureDisplay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takenPictureContainer.setVisibility(View.GONE);
                closePictureDisplay.setVisibility(View.GONE);
                previewBottomBar.setVisibility(View.GONE);
                previewTopBar.setVisibility(View.GONE);
                saveAndClosePictureDisplay.setVisibility(View.GONE);
                myPreview.myCamera.startPreview();
            }
        });

        // button to save/close the captured picture preview
        saveAndClosePictureDisplay = (Button) findViewById(R.id.main_saveAndClosePictureDisplay);
        saveAndClosePictureDisplay.setText("Yes");
        saveAndClosePictureDisplay.setTextColor(getResources().getColor(R.color.vegas_gold));
        saveAndClosePictureDisplay.setVisibility(View.GONE);

    }

    //method to turn on flash
    private void turnFlashOn() {
        System.out.println("turnFlashOn()");
        Camera.Parameters parameters = myCamera.getParameters();
        parameters.setFlashMode(parameters.FLASH_MODE_TORCH);
        myCamera.setParameters(parameters);
        myCamera.startPreview();
        flashMode = "on";
        flashlightButton.setBackgroundResource(R.drawable.flash_on_icon);
        flashlightButton.invalidate();
    }

    //method to turn off flash
    private void turnFlashOff() {
        System.out.println("turnFlashOff()");
        Camera.Parameters parameters = myCamera.getParameters();
        parameters.setFlashMode(parameters.FLASH_MODE_OFF);
        myCamera.setParameters(parameters);
        myCamera.startPreview();
        flashMode = "off";
        flashlightButton.setBackgroundResource(R.drawable.flash_off_icon);
        flashlightButton.invalidate();
    }

    //method to set flash on auto TODO this does not work yet
    private void setFlashOnAuto() {
        System.out.println("setFlashOnAuto()");
        Camera.Parameters parameters = myCamera.getParameters();
        parameters.setFlashMode(parameters.FLASH_MODE_AUTO);
        myCamera.setParameters(parameters);
        myCamera.startPreview();
        flashMode = "auto";
        flashlightButton.setBackgroundResource(R.drawable.flash_auto_icon);
        flashlightButton.invalidate();
    }

    // method to turn bytes into string
    private String convertBytesIntoString(byte[] bytes) {
        String s = new String(bytes);
        return s;
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
            scannedQrUsersArrayList.removeAll(scannedQrUsersArrayList);
            //System.out.println("scannedQrUsersArrayList.size() = " + scannedQrUsersArrayList.size());
        }
    };

    @SuppressWarnings("null")
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
                    /**

                    DateFormat dateFormat = new SimpleDateFormat("_yyyy-MM-dd_HH-mm-ss");
                    Date date = new Date();
                    String defaultCaption = MY_KT_ID + dateFormat.format(date);

                    // TODO saving image into shared image library
                    String fileUrl = MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, defaultCaption, defaultCaption);

                    if (fileUrl == null) {
                        Log.d(TAG, "Image insert failed");
                        return;
                    } else {
                        Uri picUri = Uri.parse(fileUrl);
                        Log.d(TAG, picUri.toString());
                    }
                     **/

                    int[] intArray = new int[bitmap.getWidth() * bitmap.getHeight()];
                    bitmap.getPixels(intArray, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());
                    LuminanceSource source = new RGBLuminanceSource(bitmap.getWidth(), bitmap.getHeight(), intArray);

                    BinaryBitmap binaryBitmap = new BinaryBitmap(new HybridBinarizer(source));
                    Reader reader = new QRCodeReader();

                    try {
                        qrResult = reader.decode(binaryBitmap);
                        decodedQrString = qrResult.getText();
                    } catch (NotFoundException nfe) {
                        decodedQrString = "";
                        nfe.printStackTrace();
                    } catch (ChecksumException cse) {
                        decodedQrString = "";
                        cse.printStackTrace();
                    } catch (FormatException fe) {
                        decodedQrString = "";
                        fe.printStackTrace();
                    }

                } else {
                    //landscape
                    Bitmap scaled = Bitmap.createScaledBitmap(bitmap, screenWidth, screenHeight, true);
                    bitmap = scaled;
                }

                if (decodedQrString.equals("")) {

                        decodedQrString = "";
                        takenPictureContainer.setVisibility(View.VISIBLE);
                        takenPictureContainer.setImageBitmap(bitmap);
                        closePictureDisplay.setVisibility(View.VISIBLE);

                        previewTopBar.setVisibility(View.VISIBLE);
                        previewBottomBar.setVisibility(View.VISIBLE);

                        saveAndClosePictureDisplay.setVisibility(View.VISIBLE);
                        saveAndClosePictureDisplay.setOnClickListener(new View.OnClickListener() {
                        @SuppressWarnings("null")
                        @Override
                        public void onClick(View v) {

                        DateFormat dateFormat = new SimpleDateFormat("_yyyy-MM-dd_HH-mm-ss");
                        Date date = new Date();
                        String filename = MY_KT_ID + dateFormat.format(date);


                        //TODO allow user to caption their image and add tags before uploading
                        uploadImage(compressByteArray(data), filename);

                        takenPictureContainer.setVisibility(View.GONE);
                        closePictureDisplay.setVisibility(View.GONE);

                        previewTopBar.setVisibility(View.GONE);
                        previewBottomBar.setVisibility(View.GONE);

                        saveAndClosePictureDisplay.setVisibility(View.GONE);
                        myPreview.myCamera.startPreview();
                        }
                    });

                } else if (decodedQrString.contains("dhc")) {
                    myPreview.myCamera.startPreview();
                    //TODO remove this before release
                    Toast.makeText(MainActivity.this, qrResult.getText(), Toast.LENGTH_SHORT).show();
                    checkIfQrExists(decodedQrString);

                }
            }
        }
    };

    private File saveImageToInternalStorage(byte[] image) {
        String path = "";
        File file = new File("");

        DateFormat dateFormat = new SimpleDateFormat("-yyyy-MM-dd_HH-mm-ss");
        Date date = new Date();
        String defaultCaption = MY_KT_ID + dateFormat.format(date) + ".png";

        String FILENAME = defaultCaption;
        try {
            FileOutputStream fos = openFileOutput(FILENAME, Context.MODE_PRIVATE);
            fos.write(image);
            fos.close();

            file = new File(getFilesDir() + "/" + FILENAME);
            Log.d(TAG, file.toString());
            path = file.toString();

        } catch (FileNotFoundException e) {

        } catch (IOException e) {

        }

        return file;
    }

    // TODO will need a method to delete image from internal storage

    private void deleteImageFromInternalStorage() {
        
    }


    //show mini profile card of ktUser when qr code is scanned TODO not sure if im still using this or metaio
    private void showMiniProfileView(ArrayList<MiniProfileViewItem> list) {
        //FragmentManager fragmentManager = getFragmentManager();
        //FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        //fragmentTransaction.add(R.id.main_miniProfileViewFrameLayout, MiniProfileView.newInstance(list));
        //fragmentTransaction.commit();
    }

    byte[] convertPNG(byte[] data) {
        Bitmap original = BitmapFactory.decodeByteArray(data, 0, data.length);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int height = displayMetrics.heightPixels;
        int width = displayMetrics.widthPixels;

        Bitmap resize = Bitmap.createScaledBitmap(original, width, height, true);
        //Bitmap resize = Bitmap.createScaledBitmap(original, PHOTO_WIDTH, PHOTO_HEIGHT, true);

        ByteArrayOutputStream blob = new ByteArrayOutputStream();
        resize.compress(Bitmap.CompressFormat.PNG, 100, blob);

        System.out.println("raw image sized down to: " + blob.size());

        return blob.toByteArray();
    }

    //TODO these methods are not neccessary anymore

    /**

    public void setPageOnTicket() {
        myViewPager.setCurrentItem(0);
    }

    public void setPageOnFeed() {
        myViewPager.setCurrentItem(1);
    }

    public void setPageOnMain() {
        myViewPager.setCurrentItem(2);
    }

    public void setPageOnMessage() {
        myViewPager.setCurrentItem(3);
    }

    public void setPageOnGroup() {
        myViewPager.setCurrentItem(4);
    }

     **/

    private List<android.support.v4.app.Fragment> getFragmentList() {
        List<android.support.v4.app.Fragment> fList = new ArrayList<>();
        fList.add(EmptyFragment.newInstance());
        fList.add(DataFragment.newInstance());
        return fList;
    }



    //method to check/save user's profile image to server
    private void postImageToServer(String id, Bitmap img, ArrayList<String> tags) {
        Log.i(TAG, "postImageToServer");

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        String Url = "http://kandi.nodejitsu.com/save_user_image";

        HttpClient client = new DefaultHttpClient();

        // if user has id and profile image
        // post the image and id to server to check and save image
        if (id != null && img != null) {
            try {
                HttpPost post = new HttpPost(Url);

                JsonQrObject toPostData = new JsonQrObject(id, img);

                DateFormat dateFormat = new SimpleDateFormat("-yyyy/MM/dd-HH:mm:ss");
                Date date = new Date();
                String defaultCaption = MY_KT_ID + dateFormat.format(date);
                System.out.println("default generated img caption: " + defaultCaption);
                toPostData.setImgCaption(defaultCaption);

                if (tags != null) {
                    toPostData.setTags(tags);
                }

                StringEntity entity = new StringEntity(toPostData.toString(), HTTP.UTF_8);
                entity.setContentType("application/json");
                post.setEntity(entity);

                HttpResponse response = client.execute(post);
                BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
                String line = "";

                while ((line = reader.readLine()) != null) {
                    System.out.println(line);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


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
                            tempKtObj.setKt_id(records.getKt_id());
                            tempKtObj.setUsername(records.getUsername());
                            tempKtObj.setPlacement(records.getPlacement());
                            scannedQrUsersArrayList.add(tempKtObj);


                        }
                        System.out.println("length of scannedQrUsersArrayList: " + scannedQrUsersArrayList.size());
                        //TODO use enhanced for loop?
                        //for (int i = 0; i < scannedQrUsersArrayList.size(); i++) {
                        for (KtUserObject scannedQrUser : scannedQrUsersArrayList) {

                            MiniProfileViewItem tempItem = new MiniProfileViewItem();
                            tempItem.setUser_name(scannedQrUser.getUsername());
                            tempItem.setPlacement(scannedQrUser.getPlacement());
                            miniProfileViewItemArrayList.add(tempItem);

                        }

                        showMiniProfileView(miniProfileViewItemArrayList);

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else {
            System.out.println("not a valid kanditag");
            Toast.makeText(MainActivity.this, "Not a valid KandiTag", Toast.LENGTH_SHORT).show();
        }
    }

    //method to save qr code if there is space in the server ownership table
    private void postQr(String qr, String kandi_name) {
        Log.d(TAG, "postQr");

        if (qr.contains("dhc")) {

            //TODO read up on this strictMode policy
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);

            String Url = "http://kandi.nodejitsu.com/kt_qrcode_save";

            HttpClient client = new DefaultHttpClient();

            //TODO make sure this is the proper place to reset the decodedQrString
            decodedQrString = "";

            JsonQrObject toPostData = new JsonQrObject();

            if (MY_KT_ID != null && MY_USER_NAME != null && MY_FB_ID != null) {

                try {

                    Toast.makeText(MainActivity.this, "Saving KandiTag...", Toast.LENGTH_SHORT).show();

                    HttpPost post = new HttpPost(Url);

                    if (kandi_name == null) {
                        toPostData = new JsonQrObject(qr, MY_KT_ID, MY_FB_ID, MY_USER_NAME);
                    } else if (kandi_name != null) {
                        toPostData = new JsonQrObject(qr, MY_KT_ID, MY_FB_ID, MY_USER_NAME, kandi_name);
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
                            Toast.makeText(MainActivity.this, "Saved!", Toast.LENGTH_LONG).show();
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
            Toast.makeText(MainActivity.this, "Not a valid KandiTag", Toast.LENGTH_SHORT).show();
        }
    }


    // call this to save Qr code, this will call PostQr()
    public void startQrSave(Boolean save, String name) {
        if (save) {
            System.out.println("decodedQrString is ... " + decodedQrString);
            postQr(decodedQrString, name);
            decodedQrString = "";
            System.out.println("postedQr, now decodedQrString is ... " + decodedQrString);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Session.getActiveSession().onActivityResult(this, requestCode, resultCode, data);

        if (requestCode == SIGN_OUT_REQUEST) {
            if (resultCode == RESULT_LOGGED_OUT) {
                Session session = Session.getActiveSession();
                session.closeAndClearTokenInformation();
                Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
                startActivityForResult(loginIntent, 1);
                overridePendingTransition(R.anim.abc_slide_in_top, R.anim.abc_fade_out);
                finish();
                overridePendingTransition(R.anim.abc_slide_in_bottom, R.anim.abc_fade_out);
            }

            if (resultCode == RESULT_CANCELED) {

            }
        }


        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                Log.d(TAG, "login complete, back in Main");
            }
        }

        if (requestCode == 2) {
                Bitmap photo = (Bitmap) data.getExtras().get("data");
                takenPicture.setImageBitmap(photo);
        }

        if (requestCode == 3) {
            if (resultCode == RESULT_OK) {
                Log.i(TAG, "back in Main");
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    // check for google play services, this is needed for the device token *************************
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

    // End of Google Play Service Registration *****************************************************

    //ASYNC TASKS TO GET DATA

    //get a list of all users from local db
    GetAllUsersFromLocalDbAsyncTask getAllUsersFromLocalDbAsyncTask = new GetAllUsersFromLocalDbAsyncTask(MainActivity.this, new ReturnKtUserObjectParcelableArrayListAsyncResponse() {
        @Override
        public void processFinish(ArrayList<KtUserObjectParcelable> output) {
            //System.out.println("MainActivity.getAllUsersFromLocalDbAsyncTask.processFinish.output.size() = " + output.size());
            listOfUsers = output;
        }
    });

    /**
    //get a list of all groups from local db
    GetAllGroupsFromLocalDbAsyncTask getAllGroupsFromLocalDbAsyncTask = new GetAllGroupsFromLocalDbAsyncTask(MainActivity.this, new ReturnKandiGroupObjectParcelableArrayList() {
        @Override
        public void processFinish(ArrayList<KandiGroupObjectParcelable> output) {
            System.out.println("MainActivity.getAllGroupsFromLocalDbAsyncTask.processFinish.output.size() = " + output.size());
            listOfGroups = output;
        }
    });
     **/

    //download group messages from server
    DownloadGroupMessagesFromServerAsyncTask downloadGroupMessagesFromServerAsyncTask = new DownloadGroupMessagesFromServerAsyncTask(MainActivity.this, new ReturnKtMessageObjectArrayListAsyncResponse() {
        @Override
        public void processFinish(ArrayList<KtMessageObject> output) {
            //System.out.println("MainActivity.downloadGroupMessagesFromServerAsyncTask.processFinish.output.size() = " + output.size());
        }
    });

    //download messages from servers
    DownloadMessagesFromServerAsyncTask downloadMessagesFromServerAsyncTask = new DownloadMessagesFromServerAsyncTask(MainActivity.this, new ReturnKtMessageObjectArrayListAsyncResponse() {
        @Override
        public void processFinish(ArrayList<KtMessageObject> output) {
            //System.out.println("MainActivity.downloadMessagesFromServerAsyncTask.processFinish.output.size() = " + output.size());
        }
    });

    //downloads all rows in server's kt_ownership where kt_id = MY_KT_ID
    CheckKtOwnershipForMeAsyncTask checkKtOwnershipForMeAsyncTask = new CheckKtOwnershipForMeAsyncTask(MainActivity.this, new CheckKtOwnershipAsyncResponse() {
        @Override
        public void processFinish(ArrayList<KtUserObject> output) {
            //System.out.println("MainActivity.checkKtOwnershipForMeKandiAsyncTask.processFinish.output.size() = " + output.size());
        }
    });

    //uses a list of all qrCodes you own to download all rows in server's kt_ownership where qrcode = qrcodes you sent up
    CheckKtOwnershipForUsersAsyncTask checkKtOwnershipForUsersAsyncTask = new CheckKtOwnershipForUsersAsyncTask(MainActivity.this, new CheckKtOwnershipAsyncResponse() {
        @Override
        public void processFinish(ArrayList<KtUserObject> output) {
            //System.out.println("MainActivity.checkKtOwnershipForUsersAsyncTask.processFinish.output.size() = " + output.size());
        }
    });

    //uses a list of all qrCodes you own to download all rows in server's kt_qrcode for the name of the kandi
    GetKandiNameFromKtQrcodeAsyncTask getKandiNameFromKtQrcodeAsyncTask = new GetKandiNameFromKtQrcodeAsyncTask(MainActivity.this, new ReturnKandiObjectArrayAsyncResponse() {
        @Override
        public void processFinish(ArrayList<KandiObject> output) {
            //System.out.println("MainActivity.getKandiNameFromKtQrCodeAsyncTask.processFinish.output.size() = " + output.size());
            // when this task is done running, set the opened_before to true so that these task will not be executed again
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean(OPENED_BEFORE, true);
            editor.commit();
        }
    });




/**

    // Methods to hide and show menu items/banners *************************************************

    //method to hide toProfileBanner
    private void hideToProfileBanner() {
        Animation fadeOut = AnimationUtils.loadAnimation(this, android.R.anim.fade_out);
        toProfileBanner.startAnimation(fadeOut);
        fadeOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                toProfileBanner.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    //method to show toProfileBanner
    private void showToProfileBanner() {
        Animation fadeIn = AnimationUtils.loadAnimation(this, android.R.anim.fade_in);
        toProfileBanner.startAnimation(fadeIn);
        fadeIn.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                toProfileBanner.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    //method to hide toFeedBanner
    private void hideToFeedBanner() {
        Animation fadeOut = AnimationUtils.loadAnimation(this, android.R.anim.fade_out);
        toFeedBanner.startAnimation(fadeOut);
        fadeOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                toFeedBanner.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    //method to show toFeedBanner
    private void showToFeedBanner() {
        Animation fadeIn = AnimationUtils.loadAnimation(this, android.R.anim.fade_in);
        toFeedBanner.startAnimation(fadeIn);
        fadeIn.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                toFeedBanner.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    //method to hide toMessageBanner
    private void hideToMessageBanner() {
        Animation fadeOut = AnimationUtils.loadAnimation(this, android.R.anim.fade_out);
        toMessageBanner.startAnimation(fadeOut);
        fadeOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                toMessageBanner.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    //method to show toMessageBanner
    private void showToMessageBanner() {
        Animation fadeIn = AnimationUtils.loadAnimation(this, android.R.anim.fade_in);
        toMessageBanner.startAnimation(fadeIn);
        fadeIn.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                toMessageBanner.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    //method to hide toExchangeBanner
    private void hideToExchangeBanner() {
        Animation fadeOut = AnimationUtils.loadAnimation(this, android.R.anim.fade_out);
        toExchangeBanner.startAnimation(fadeOut);
        fadeOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                toExchangeBanner.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    //method to show toExchangeBanner
    private void showToExchangeBanner() {
        Animation fadeIn = AnimationUtils.loadAnimation(this, android.R.anim.fade_in);
        toExchangeBanner.startAnimation(fadeIn);
        fadeIn.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                toExchangeBanner.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    //method to hide toMeetBanner
    private void hideToMeetBanner() {
        Animation fadeOut = AnimationUtils.loadAnimation(this, android.R.anim.fade_out);
        toMeetBanner.startAnimation(fadeOut);
        fadeOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                toMeetBanner.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    //method to show toMeetBanner
    private void showToMeetBanner() {
        Animation fadeIn = AnimationUtils.loadAnimation(this, android.R.anim.fade_in);
        toMeetBanner.startAnimation(fadeIn);
        fadeIn.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                toMeetBanner.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    //method to hide toProfile
    private void hideToProfile() {
        Animation slideOutRight = AnimationUtils.loadAnimation(this, android.R.anim.slide_out_right);
        toProfile.startAnimation(slideOutRight);
        slideOutRight.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                toProfile.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    //method to show toProfile
    private void showToProfile() {
        Animation slideInRight = AnimationUtils.loadAnimation(this, R.anim.right_slide_in);
        toProfile.startAnimation(slideInRight);
        slideInRight.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                toProfile.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    //method to hide toFeed
    private void hideToFeed() {
        Animation slideOutRight = AnimationUtils.loadAnimation(this, android.R.anim.slide_out_right);
        toFeed.startAnimation(slideOutRight);
        slideOutRight.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                toFeed.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    //method to show toFeed
    private void showToFeed() {
        Animation slideInRight = AnimationUtils.loadAnimation(this, R.anim.right_slide_in);
        toFeed.startAnimation(slideInRight);
        slideInRight.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                toFeed.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }


    //method to hide toExchange
    private void hideToTicket() {
        Animation slideOutRight = AnimationUtils.loadAnimation(this, android.R.anim.slide_out_right);
        toExchange.startAnimation(slideOutRight);
        slideOutRight.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                toExchange.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    //method to show toExchange
    private void showToTicket() {
        Animation slideInRight = AnimationUtils.loadAnimation(this, R.anim.right_slide_in);
        toExchange.startAnimation(slideInRight);
        slideInRight.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                toExchange.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }


    //method to hide toMessage
    private void hideToMessage() {
        Animation slideOutRight = AnimationUtils.loadAnimation(this, android.R.anim.slide_out_right);
        toMessage.startAnimation(slideOutRight);
        slideOutRight.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                toMessage.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    //method to show toMessage
    private void showToMessage() {
        Animation slideInRight = AnimationUtils.loadAnimation(this, R.anim.right_slide_in);
        toMessage.startAnimation(slideInRight);
        slideInRight.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                toMessage.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    //method to hide toMeet
    private void hideToMeet() {
        Animation slideOutRight = AnimationUtils.loadAnimation(this, R.anim.right_slide_out);
        toMeet.startAnimation(slideOutRight);
        slideOutRight.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                toMeet.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    //method to show toMeet
    private void showToMeet() {
        Animation slideInRight = AnimationUtils.loadAnimation(this, R.anim.right_slide_in);
        toMeet.startAnimation(slideInRight);
        slideInRight.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                toMeet.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    // End Methods to hide and show menu items/banners *********************************************

    // Runnables to update UI with hide/show toMenuItems *******************************************

    // runnable to hide toProfile
    private Runnable hideToProfileRunnable = new Runnable() {
        @Override
        public void run() {
            hideToProfile();
        }
    };

    // runnable to hide toProfileBanner
    private Runnable hideToProfileBannerRunnable = new Runnable() {
        @Override
        public void run() {
            hideToProfileBanner();
        }
    };

    // runnable to show toProfileBanner
    private Runnable showToProfileBannerRunnable = new Runnable() {
        @Override
        public void run() {
            showToProfileBanner();
        }
    };

    // runnable to hide toMessageBanner
    private Runnable hideToMessageBannerRunnable = new Runnable() {
        @Override
        public void run() {
            hideToMessageBanner();
        }
    };

    // runnable to show toMessageBanner
    // also set menu visible to true
    private Runnable showToMessageBannerRunnable = new Runnable() {
        @Override
        public void run() {
            showToMessageBanner();
        }
    };

    // runnable to hide toFeedBanner
    private Runnable hideToFeedBannerRunnable = new Runnable() {
        @Override
        public void run() {
            hideToFeedBanner();
        }
    };

    // runnable to show toFeedBanner
    private Runnable showToFeedBannerRunnable = new Runnable() {
        @Override
        public void run() {
            showToFeedBanner();
        }
    };

    // runnable to hide toExchangeBanner
    // also set menu visible to false
    private Runnable hideToExchangeBannerRunnable = new Runnable() {
        @Override
        public void run() {
            hideToExchangeBanner();
        }
    };

    // runnable to show toExchangeBanner
    private Runnable showToExchangeBannerRunnable = new Runnable() {
        @Override
        public void run() {
            showToExchangeBanner();
        }
    };

    // runnable to hide toMeetBanner
    private Runnable hideToMeetBannerRunnable = new Runnable() {
        @Override
        public void run() {
            hideToMeetBanner();
            isMenuVisible = false;
        }
    };

    //runnable to show toMeetBanner
    private Runnable showToMeetBannerRunnable = new Runnable() {
        @Override
        public void run() {
            showToMeetBanner();
            isMenuVisible = true;
        }
    };

    // runnable to hide toFeed
    private Runnable hideToFeedRunnable = new Runnable() {
        @Override
        public void run() {
            hideToFeed();
        }
    };

    // runnable to hide toExchange
    private Runnable hideToExchangeRunnable = new Runnable() {
        @Override
        public void run() {
            hideToTicket();
        }
    };

    // runnable to hide toMessage
    private Runnable hideToMessageRunnable = new Runnable() {
        @Override
        public void run() {
            hideToMessage();
        }
    };

    // runnable to show toProfile
    private Runnable showToProfileRunnable = new Runnable() {
        @Override
        public void run() {
            showToProfile();
        }
    };

    // runnable to show toFeed
    private Runnable showToFeedRunnable = new Runnable() {
        @Override
        public void run() {
            showToFeed();
        }
    };

    // runnable to show toExchange
    private Runnable showToExchangeRunnable = new Runnable() {
        @Override
        public void run() {
            showToTicket();
        }
    };

    // runnable to show toMessage
    private Runnable showToMessageRunnable = new Runnable() {
        @Override
        public void run() {
            showToMessage();
        }
    };

    // runnable to hide toMeet
    private Runnable hideToMeetRunnable = new Runnable() {
        @Override
        public void run() {
            hideToMeet();
        }
    };

    //runnable to show toMeet
    private Runnable showToMeetRunnable = new Runnable() {
        @Override
        public void run() {
            showToMeet();
        }
    };

    // End Runnables to update UI with hide/show toMenuItems ***************************************

 **/

}
