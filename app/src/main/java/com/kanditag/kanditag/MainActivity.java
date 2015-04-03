package com.kanditag.kanditag;


import android.content.ClipData;
import android.content.ClipDescription;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
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
import java.util.Timer;
import java.util.TimerTask;
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
import android.view.DragEvent;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
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

    //bottom and top of the preview
    private View previewTopBar, previewBottomBar;

    //Main_NavBarFrameLayout
    private FrameLayout main_navBarFrameLayout;

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
    //Gcm variables

    private Boolean alreadyOwned;

    private ArrayList<KtUserObject> scannedQrUsersArrayList = new ArrayList<>();
    private ArrayList<String> scannedQrUsersFb_idArrayList = new ArrayList<>();
    private ArrayList<MiniProfileViewItem> miniProfileViewItemArrayList = new ArrayList<>();

    private RelativeLayout main_rootView;

    private FrameLayout miniProfileViewFrameLayout;

    ImageView takenPicture;
    ImageView takePicture;
    ImageView shadedBackground;
    ImageView backToCamera;

    private ImageView takenPictureContainer;
    private Button closePictureDisplay;
    private Button saveAndClosePictureDisplay;

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

    KtDatabase myDatabase;

    protected SQLiteDatabase sqLiteDatabase;
    private static final String DATABASE_NAME = "myData";
    private static final String CREATE_TABLE_IF_NOT = "CREATE TABLE IF NOT EXISTS ";

    //VerticalViewPager mainViewPager;
    MyPageAdapter myPageAdapter;

    Camera myCamera;
    Preview preview;
    int stillCount = 0;
    private Preview myPreview;

    Result qrResult;
    String decodedQrString;

    public static final int PHOTO_WIDTH = 480;
    public static final int PHOTO_HEIGHT = 640;

    private String MY_KT_ID, MY_FB_ID, MY_USER_NAME;

    public static Camera getCameraInstance() {
        Camera camera = null;
        try {
            camera = Camera.open();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return camera;
    }

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

    float initialX, initialY;

    private Handler myHandler = new Handler();

    private Runnable shrinkNavBarRunnable = new Runnable() {
        @Override
        public void run() {
            NavigationBarFragment navFragment = (NavigationBarFragment) getSupportFragmentManager().findFragmentById(R.id.Main_NavigationBarFrameLayout);
            navFragment.shrinkNavBar();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = getApplicationContext();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        myDatabase = new KtDatabase(this);
        sqLiteDatabase = openOrCreateDatabase(DATABASE_NAME, MODE_PRIVATE, null);
        sharedPreferences = this.getSharedPreferences(MyPreferences, Context.MODE_PRIVATE);


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

        main_rootView = (RelativeLayout) findViewById(R.id.main_relativeLayout);

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.setCustomAnimations(R.anim.abc_slide_in_top, R.anim.abc_slide_out_top);
        fragmentTransaction.add(R.id.Main_NavigationBarFrameLayout, NavigationBarFragment.newInstance());
        fragmentTransaction.commit();

        previewTopBar = findViewById(R.id.Main_PreviewTop);
        previewBottomBar = findViewById(R.id.Main_PreviewBottom);
        previewTopBar.setVisibility(View.GONE);
        previewBottomBar.setVisibility(View.GONE);

        //mainViewPager = (VerticalViewPager) findViewById(R.id.main_viewPager);
        List<Fragment> fragments = getFragments();
        myPageAdapter = new MyPageAdapter(getSupportFragmentManager(), fragments);
        //mainViewPager.setAdapter(myPageAdapter);

        myViewPager = (ViewPager) findViewById(R.id.Main_ViewPager);
        myViewPager.setAdapter(myPageAdapter);
        myViewPager.setCurrentItem(1);

        myViewPager.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                //System.out.println(event.toString());

                int action = event.getActionMasked();

                switch (action) {
                    case MotionEvent.ACTION_DOWN:
                        initialX = event.getX();
                        initialY = event.getY();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        break;
                    case MotionEvent.ACTION_UP:
                        float finalX = event.getX();
                        float finalY = event.getY();

                        if (initialY < finalY) {
                            System.out.println("swiped down");
                            float deltaY = finalY - initialY;
                            System.out.println(deltaY);
                            if (deltaY > 500.0) {
                                final NavigationBarFragment navFragment = (NavigationBarFragment) getSupportFragmentManager().findFragmentById(R.id.Main_NavigationBarFrameLayout);
                                navFragment.expandNavBar();
                                myHandler.postDelayed(shrinkNavBarRunnable, 5000);
                            }
                        }

                        if (initialY > finalY) {
                            System.out.println("swiped up");
                            float deltaY = initialY - finalY;
                            System.out.println(deltaY);
                            if (deltaY > 500.0) {
                                NavigationBarFragment navFragment = (NavigationBarFragment) getSupportFragmentManager().findFragmentById(R.id.Main_NavigationBarFrameLayout);
                                navFragment.shrinkNavBar();
                            }
                        }

                        break;
                    case MotionEvent.ACTION_CANCEL:
                        break;
                    case MotionEvent.ACTION_OUTSIDE:
                        break;
                }

                return false;
            }
        });

        myViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {

                NavigationBarFragment navFragment = (NavigationBarFragment) getSupportFragmentManager().findFragmentById(R.id.Main_NavigationBarFrameLayout);

                switch (position) {
                    case 0:
                        System.out.println("0");
                        navFragment.removeHighlight();
                        navFragment.highlightFeedNavBar();
                        break;
                    case 1:
                        System.out.println("1");
                        navFragment.removeHighlight();
                        navFragment.highlightMainNavBar();
                        break;
                    case 2:
                        System.out.println("2");
                        navFragment.removeHighlight();
                        navFragment.highlightMessageNavBar();
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        myCamera = getCameraInstance();
        myPreview = new Preview(this, myCamera);
        ((FrameLayout) findViewById(R.id.main_frameLayoutPreview)).addView(myPreview);

        shadedBackground = (ImageView) findViewById(R.id.main_shadedBackground);
        shadedBackground.setVisibility(View.INVISIBLE);

        takePicture = (ImageView) findViewById(R.id.main_CameraButton);
        takePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myPreview.myCamera.takePicture(shutterCallback, rawCallback, pngCallback);
            }
        });

        takenPictureContainer = (ImageView) findViewById(R.id.main_takenPictureContainer);
        takenPictureContainer.setVisibility(View.GONE);

        Typeface typeface = Typeface.createFromAsset(getAssets(), "fonts/stalemate_regular.ttf");

        closePictureDisplay = (Button) findViewById(R.id.main_closePictureDisplay);
        closePictureDisplay.setVisibility(View.GONE);
        closePictureDisplay.setText("Nope");
        closePictureDisplay.setTextColor(getResources().getColor(R.color.vegas_gold));
        closePictureDisplay.setTypeface(typeface);
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

        saveAndClosePictureDisplay = (Button) findViewById(R.id.main_saveAndClosePictureDisplay);
        saveAndClosePictureDisplay.setText("Yes");
        saveAndClosePictureDisplay.setTextColor(getResources().getColor(R.color.vegas_gold));
        saveAndClosePictureDisplay.setTypeface(typeface);
        saveAndClosePictureDisplay.setVisibility(View.GONE);

/**
        myViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                System.out.println("Main.myViewPager.onPageScrolled.offSet= " + positionOffset);
                System.out.println("Main.myViewPager.onPageScrolled.position=" + position);
                System.out.println("Main.myViewPager.onPageScrolled.positionOffsetPixels= " + positionOffsetPixels);

            }

            @Override
            public void onPageSelected(int position) {
                switch (position) {
                    case 0: //System.out.println("0");
                        takePicture.setVisibility(View.GONE);
                        break;
                    case 1: //System.out.println("1");
                        takePicture.setVisibility(View.VISIBLE);
                        break;
                    case 2:
                        takePicture.setVisibility(View.GONE);
                        break;
                    case 3:
                        break;

                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        **/
        /**

        mainViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                int currentPage = position;
                switch (currentPage) {
                    case 0:
                        shadedBackground.setVisibility(View.INVISIBLE);
                        takePicture.setVisibility(View.VISIBLE);
                        takePicture.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                myPreview.myCamera.takePicture(shutterCallback, rawCallback, pngCallback);
                            }
                        });
                        break;
                    case 1:
                        shadedBackground.setVisibility(View.VISIBLE);
                        takePicture.setVisibility(View.INVISIBLE);
                        takePicture.setOnClickListener(null);
                        break;
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

         **/

        //TODO make sure this check works
        boolean openedBefore = sharedPreferences.getBoolean(OPENED_BEFORE, false);
        if (!openedBefore) {
            System.out.println("KandiTag is downloading all user data from server...");
            downloadMessagesFromServerAsyncTask.execute();
            //TODO create groupMessages first before attempting to download them
            //downloadGroupMessagesFromServerAsyncTask.execute();
            checkKtOwnershipForMeAsyncTask.execute();
            checkKtOwnershipForUsersAsyncTask.execute();
            getKandiNameFromKtQrcodeAsyncTask.execute();
        } else {
            System.out.println("KandiTag is ready to go!");
        }

    }


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

    public void setPageOnFeed() {
        myViewPager.setCurrentItem(0);
    }

    public void setPageOnMain() {
        myViewPager.setCurrentItem(1);
    }

    public void setPageOnMessage() {
        myViewPager.setCurrentItem(2);
    }


    private List<Fragment> getFragments() {
        List<Fragment> fList = new ArrayList<Fragment>();
        fList.add(KandiFragment.newInstance());
        //fList.add(CameraPreview.instantiate(this, CameraPreview.class.getName()));
        fList.add(CameraPreview.newInstance());
        fList.add(MessageFragment.newInstance());
        //fList.add(NewMessageFragment.newInstance());
        return fList;
    }

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

}
