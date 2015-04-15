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
import android.graphics.Typeface;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.os.Bundle;

import android.os.Handler;
import android.os.StrictMode;

import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v4.app.Fragment;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import android.util.DisplayMetrics;
import android.util.Log;

import com.facebook.Session;
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
import android.view.Display;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
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

    // Handler for runnables (UI updates)
    private Handler myHandler = new Handler();

    //popout menu buttons
    private ImageView toProfile, toMessage, toTicket, toFeed;

    //flashlight button
    private ImageView flashlightButton;
    private boolean flashOn = false;

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

    //viewPager TODO this is outdated
    private ViewPager myViewPager;

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
    public static final String MyPreferences = "MyPrefs";
    public static final String Name = "nameKey";
    public static final String FbId = "fbidKey";
    public static final String UserId = "userIdKey";
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

    //VerticalViewPager mainViewPager;
    MyPageAdapter myPageAdapter;

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

    // try method to start camera
    public static Camera getCameraInstance() {
        Camera camera = null;
        try {
            camera = Camera.open();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return camera;
    }

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
        if (myCamera != null) {
            myCamera.stopPreview();
            myCamera.release();
        }

    }

    // runnable to hide toProfile
    private Runnable hideToProfileRunnable = new Runnable() {
        @Override
        public void run() {
            hideToProfile();
        }
    };

    // runnable to hide toFeed
    private Runnable hideToFeedRunnable = new Runnable() {
        @Override
        public void run() {
            hideToFeed();
        }
    };

    // runnable to hide toTicket
    private Runnable hideToTicketRunnable = new Runnable() {
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

    // runnable to show toTicket
    private Runnable showToTicketRunnable = new Runnable() {
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
                    myHandler.postDelayed(showToMessageRunnable, 10);
                    myHandler.postDelayed(showToProfileRunnable, 110);
                    myHandler.postDelayed(showToFeedRunnable, 210);
                    myHandler.postDelayed(showToTicketRunnable, 310);

                }
                if (leftToRightDeltaX > MIN_DISTANCE) {
                    //this is a left to right swipe (this will hide menu);
                    myHandler.postDelayed(hideToMessageRunnable, 10);
                    myHandler.postDelayed(hideToProfileRunnable, 110);
                    myHandler.postDelayed(hideToFeedRunnable, 210);
                    myHandler.postDelayed(hideToTicketRunnable, 310);
                }

                break;
        }
        return false;
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


    //method to hide toTicket
    private void hideToTicket() {
        Animation slideOutRight = AnimationUtils.loadAnimation(this, android.R.anim.slide_out_right);
        toTicket.startAnimation(slideOutRight);
        slideOutRight.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                toTicket.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    //method to show toTicket
    private void showToTicket() {
        Animation slideInRight = AnimationUtils.loadAnimation(this, R.anim.right_slide_in);
        toTicket.startAnimation(slideInRight);
        slideInRight.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                toTicket.setVisibility(View.VISIBLE);
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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = getApplicationContext();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        myDatabase = new KtDatabase(this);
        sqLiteDatabase = openOrCreateDatabase(DATABASE_NAME, MODE_PRIVATE, null);
        sharedPreferences = this.getSharedPreferences(MyPreferences, Context.MODE_PRIVATE);

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

        } else {
            System.out.println("KandiTag is ready to go!");
        }


        MY_KT_ID = sharedPreferences.getString(UserId, "");
        MY_USER_NAME = sharedPreferences.getString(Name, "");
        MY_FB_ID = sharedPreferences.getString(FbId, "");

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

        myScaleGestureDetector = new ScaleGestureDetector(this, new ScaleListener());

        main_rootView = (RelativeLayout) findViewById(R.id.main_relativeLayout);

        previewTopBar = findViewById(R.id.Main_PreviewTop);
        previewBottomBar = findViewById(R.id.Main_PreviewBottom);
        previewTopBar.setVisibility(View.GONE);
        previewBottomBar.setVisibility(View.GONE);

        // find menu buttons in the xml file
        toProfile = (ImageView) findViewById(R.id.Main_toProfile);
        toFeed = (ImageView) findViewById(R.id.Main_toFeed);
        toTicket = (ImageView) findViewById(R.id.Main_toTicket);
        toMessage = (ImageView) findViewById(R.id.Main_toMessage);

        // begin animations to remove buttons
        myHandler.postDelayed(hideToMessageRunnable, 3000);
        myHandler.postDelayed(hideToProfileRunnable, 3100);
        myHandler.postDelayed(hideToFeedRunnable, 3200);
        myHandler.postDelayed(hideToTicketRunnable, 3300);

        // starting camera
        myCamera = getCameraInstance();
        myPreview = new Preview(this, myCamera);
        ((FrameLayout) findViewById(R.id.main_frameLayoutPreview)).addView(myPreview);

        // set up torch
        flashlightButton = (ImageView) findViewById(R.id.Main_FlashlightButton);
        flashlightButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!flashOn) {
                    turnFlashOn();
                } else {
                    turnFlashOff();
                }
            }
        });


        shadedBackground = (ImageView) findViewById(R.id.main_shadedBackground);
        shadedBackground.setVisibility(View.INVISIBLE);

        // set up button to take pictures TODO take videos
        takePicture = (ImageView) findViewById(R.id.main_CameraButton);
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


    //TODO task to save image into local database
    private class SaveImage extends AsyncTask<Void, Void, Void> {

        byte[] imageData;

        public SaveImage (byte[] image_data) {
            imageData = image_data;
        }

        @Override
        protected void onPreExecute() {
            System.out.println("SaveImage.onPreExecute");
        }

        @Override
        protected Void doInBackground(Void... params) {
            //myDatabase.insertImage(imageData);
            return null;
        }

        @Override
        protected void onPostExecute(Void res) {
            System.out.println("Inside onPostExecute");
        }
    }

    //method to turn on flash
    private void turnFlashOn() {
        flashlightButton.setImageResource(R.drawable.flash_on_icon);
        flashlightButton.invalidate();
        System.out.println("turnFlashOn()");
        Camera.Parameters parameters = myCamera.getParameters();
        parameters.setFlashMode(parameters.FLASH_MODE_TORCH);
        myCamera.setParameters(parameters);
        myCamera.startPreview();
        flashOn = true;
    }

    //method to turn off flash
    private void turnFlashOff() {
        flashlightButton.setImageResource(R.drawable.flash_off_icon);
        flashlightButton.invalidate();
        System.out.println("turnFlashOff()");
        Camera.Parameters parameters = myCamera.getParameters();
        parameters.setFlashMode(parameters.FLASH_MODE_OFF);
        myCamera.setParameters(parameters);
        myCamera.startPreview();
        flashOn = false;
    }

    //method to set flash on auto
    private void setFlashOnAuto() {
        flashlightButton.setImageResource(R.drawable.flash_auto_icon);
        flashlightButton.invalidate();
        System.out.println("setFlashOnAuto()");
        Camera.Parameters parameters = myCamera.getParameters();
        parameters.setFlashMode(parameters.FLASH_MODE_AUTO);
        myCamera.setParameters(parameters);
        myCamera.startPreview();
        flashOn = true;
    }


// takePicture Callbacks **********
    Camera.ShutterCallback shutterCallback = new Camera.ShutterCallback() {
        @Override
        public void onShutter() {
            Log.d(TAG, "onShutterCallback");
        }
    };

    Camera.PictureCallback rawCallback = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            Log.d(TAG, "onPictureTaken - rawCallback");
            //TODO make sure this clears the scannedQrUsersArrayList
            scannedQrUsersArrayList.removeAll(scannedQrUsersArrayList);
            System.out.println("scannedQrUsersArrayList.size() = " + scannedQrUsersArrayList.size());
        }
    };

    Camera.PictureCallback pngCallback = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(final byte[] data, Camera camera) {

            Log.i(TAG, "data file size: " + data.length);

            Log.d(TAG, "onPictureTaken");


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
                        @Override
                        public void onClick(View v) {
                            SaveImage save = new SaveImage(convertPNG(data));
                            save.execute();
                            System.out.println("image has been saved into database");

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

    private List<Fragment> getFragmentList() {
        List<Fragment> fList = new ArrayList<Fragment>();
        fList.add(TicketFragment.newInstance());
        fList.add(KandiFragment.newInstance());
        fList.add(CameraPreview.newInstance());
        fList.add(MessageFragment.newInstance());
        fList.add(GroupFragment.newInstance());
        return fList;
    }

     **/


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
                            tempKtObj.setFb_id(records.getFb_id());
                            tempKtObj.setKt_id(records.getKt_id());
                            tempKtObj.setName(records.getUsername());
                            tempKtObj.setPlacement(records.getPlacement());
                            scannedQrUsersArrayList.add(tempKtObj);


                        }
                        System.out.println("length of scannedQrUsersArrayList: " + scannedQrUsersArrayList.size());

                        for (int i = 0; i < scannedQrUsersArrayList.size(); i++) {

                            scannedQrUsersFb_idArrayList.add(scannedQrUsersArrayList.get(i).getFb_id());
                            MiniProfileViewItem tempItem = new MiniProfileViewItem();
                            tempItem.setFb_id(scannedQrUsersArrayList.get(i).getFb_id());
                            tempItem.setUser_name(scannedQrUsersArrayList.get(i).getName());
                            tempItem.setPlacement(scannedQrUsersArrayList.get(i).getPlacement());
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
                        kandiObject.setQrCode(resEndObj.getQrCode());
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

        if (requestCode == SETTINGS_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Log.i(TAG, "result ok from setting");
                Intent loginIntent = new Intent(MainActivity.this, Login.class);
                startActivityForResult(loginIntent, 1);
                finish();
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
            System.out.println("MainActivity.getAllUsersFromLocalDbAsyncTask.processFinish.output.size() = " + output.size());
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
    DownloadGroupMessagesFromServerAsyncTask downloadGroupMessagesFromServerAsyncTask = new DownloadGroupMessagesFromServerAsyncTask(MainActivity.this, new ReturnGroupMessageArrayListAsyncResponse() {
        @Override
        public void processFinish(ArrayList<GroupMessageItem> output) {
            System.out.println("MainActivity.downloadGroupMessagesFromServerAsyncTask.processFinish.output.size() = " + output.size());
        }
    });

    //download messages from servers
    DownloadMessagesFromServerAsyncTask downloadMessagesFromServerAsyncTask = new DownloadMessagesFromServerAsyncTask(MainActivity.this, new ReturnKtMessageObjectArrayListAsyncResponse() {
        @Override
        public void processFinish(ArrayList<KtMessageObject> output) {
            System.out.println("MainActivity.downloadMessagesFromServerAsyncTask.processFinish.output.size() = " + output.size());
        }
    });

    //downloads all rows in server's kt_ownership where kt_id = MY_KT_ID
    CheckKtOwnershipForMeAsyncTask checkKtOwnershipForMeAsyncTask = new CheckKtOwnershipForMeAsyncTask(MainActivity.this, new CheckKtOwnershipAsyncResponse() {
        @Override
        public void processFinish(ArrayList<KtUserObject> output) {
            System.out.println("MainActivity.checkKtOwnershipForMeKandiAsyncTask.processFinish.output.size() = " + output.size());
        }
    });

    //uses a list of all qrCodes you own to download all rows in server's kt_ownership where qrcode = qrcodes you sent up
    CheckKtOwnershipForUsersAsyncTask checkKtOwnershipForUsersAsyncTask = new CheckKtOwnershipForUsersAsyncTask(MainActivity.this, new CheckKtOwnershipAsyncResponse() {
        @Override
        public void processFinish(ArrayList<KtUserObject> output) {
            System.out.println("MainActivity.checkKtOwnershipForUsersAsyncTask.processFinish.output.size() = " + output.size());
        }
    });

    //uses a list of all qrCodes you own to download all rows in server's kt_qrcode for the name of the kandi
    GetKandiNameFromKtQrcodeAsyncTask getKandiNameFromKtQrcodeAsyncTask = new GetKandiNameFromKtQrcodeAsyncTask(MainActivity.this, new ReturnKandiObjectArrayAsyncResponse() {
        @Override
        public void processFinish(ArrayList<KandiObject> output) {
            System.out.println("MainActivity.getKandiNameFromKtQrCodeAsyncTask.processFinish.output.size() = " + output.size());
            // when this task is done running, set the opened_before to true so that these task will not be executed again
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean(OPENED_BEFORE, true);
            editor.commit();
        }
    });
}
