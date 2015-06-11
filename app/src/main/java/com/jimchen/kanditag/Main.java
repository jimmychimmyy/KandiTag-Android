package com.jimchen.kanditag;

import android.content.Context;
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
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.os.StrictMode;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
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

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.Deflater;

import eu.livotov.zxscan.ScannerView;
import eu.livotov.zxscan.decoder.zxing.ZXRGBLuminanceSource;


public class Main extends FragmentActivity {

    private static final String TAG = "Main";
    private Context context;

    // request codes
    public static final int SIGN_OUT_REQUEST = 0;
    public static final int SIGN_IN_REQUEST = 1;
    public static final int PREVIEW_IMAGE_REQUEST = 2;
    public static final int RESULT_LOGGED_OUT = 9;
    public static final int DECODE_REQUEST = 3;

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
    private VerticalViewPager mainViewPager;
    private MyPageAdapter mainPageAdapter;
    private ImageView cameraButton;
    private ImageView capturedImageContainer;
    private Button cancelImage, postImage;

    // qr code reader vars
    // QrCode Variables
    private ScannerView scanner;
    private Result qrResult;
    private String decodedKandiID;
    private ArrayList<KtUserObject> ktUsersList; // list to hold kt users from kanditag

    // variables
    private ArrayList<KtUserObject> scannedKtUsersList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = getApplicationContext();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

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

        // TODO this doesnt work bc all the ids in the db are using fb
        //downloadMessagesFromServerAsyncTask.execute();

        /**
        scanner = (ScannerView) findViewById(R.id.Main_Scanner);
        scanner.startScanner();
        scanner.setScannerViewEventListener(new ScannerView.ScannerViewEventListener() {
            @Override
            public void onScannerReady() {

            }

            @Override
            public void onScannerFailure(int i) {

            }

            @Override
            public boolean onCodeScanned(String data) {
                Toast.makeText(Main.this, "Data scanned: " + data, Toast.LENGTH_SHORT).show();
                return true;
            }
        });

         **/

        // init list to hold kanditag users
        ktUsersList = new ArrayList();

        // starting camera
        myCamera = getCameraInstance(); // call to getCameraInstance method
        myPreview = new Preview(Main.this, myCamera);
        decodedKandiID = "";
        ((FrameLayout) findViewById(R.id.Main_PreviewContainer)).addView(myPreview);
        if (myCamera != null) {
            myCamera.setPreviewCallback(new Camera.PreviewCallback() {
                @Override
                public void onPreviewFrame(byte[] data, Camera camera) {

                    if (decodedKandiID.equals("")) {

                        // it might be ok to keep this on the main thread
                        // just move the picture taking, saving and other ui things to another thread
                        // also remove preview callback on

                         FileOutputStream outputStream = null;
                         try {
                         YuvImage yuvImage = new YuvImage(data, ImageFormat.NV21, camera.getParameters().getPreviewSize().width, camera.getParameters().getPreviewSize().height, null);
                         ByteArrayOutputStream baos = new ByteArrayOutputStream();
                         yuvImage.compressToJpeg(new Rect(0, 0, camera.getParameters().getPreviewSize().width, camera.getParameters().getPreviewSize().height), 80, baos);
                         Bitmap bitmap = BitmapFactory.decodeByteArray(baos.toByteArray(), 0, baos.toByteArray().length);
                         if (bitmap != null) {
                         LuminanceSource source = new ZXRGBLuminanceSource(bitmap);
                         BinaryBitmap binaryBitmap = new BinaryBitmap(new HybridBinarizer(source));
                         Reader reader = new QRCodeReader();

                         try {
                         Result result = reader.decode(binaryBitmap);
                         decodedKandiID = result.getText();
                         displayKandiTag(decodedKandiID);
                         Toast.makeText(Main.this, decodedKandiID, Toast.LENGTH_SHORT).show();

                         // TODO add profile view (list view maybe?)
                         // when closing profile view set decodedKandiID to ""

                         } catch (Exception e) {
                         }
                         }
                         } catch (Exception e) {
                         }


                    }

                }
            });
        }

        // connect camera button
        cameraButton = (ImageView) findViewById(R.id.Main_Camera);
        cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                myPreview.myCamera.takePicture(shutterCallback, rawCallback, pngCallback);
            }
        });


        // connect image preview buttons
        cancelImage = (Button) findViewById(R.id.Main_CancelImage);
        postImage = (Button) findViewById(R.id.Main_PostImage);
        cancelImage.setVisibility(View.GONE);
        postImage.setVisibility(View.GONE);

        capturedImageContainer = (ImageView) findViewById(R.id.Main_CapturedImagePreviewContainer);
        capturedImageContainer.setVisibility(View.GONE);

        // connect xml
        mainViewPager = (VerticalViewPager) findViewById(R.id.Main_VerticalViewPager);
        mainPageAdapter = new MyPageAdapter(getSupportFragmentManager(), getFragmentList());
        mainViewPager.setAdapter(mainPageAdapter);
        mainViewPager.setCurrentItem(1);
        mainViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                switch (position) {
                    case 0:
                        cameraButton.setImageResource(R.drawable.kanditag_cameraback);
                        cameraButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                mainViewPager.setCurrentItem(1, true);
                            }
                        });
                        myCamera.setPreviewCallback(null);
                        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
                        break;
                    case 1:
                        cameraButton.setImageResource(R.drawable.kanditag_camera);
                        cameraButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                myPreview.myCamera.takePicture(shutterCallback, rawCallback, pngCallback);
                            }
                        });
                        if (myCamera != null) {
                            myCamera.setPreviewCallback(new Camera.PreviewCallback() {
                                @Override
                                public void onPreviewFrame(byte[] data, Camera camera) {

                                    if (decodedKandiID.equals("")) {

                                        // it might be ok to keep this on the main thread
                                        // just move the picture taking, saving and other ui things to another thread
                                        // also remove preview callback on

                                        FileOutputStream outputStream = null;
                                        try {
                                            YuvImage yuvImage = new YuvImage(data, ImageFormat.NV21, camera.getParameters().getPreviewSize().width, camera.getParameters().getPreviewSize().height, null);
                                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                            yuvImage.compressToJpeg(new Rect(0, 0, camera.getParameters().getPreviewSize().width, camera.getParameters().getPreviewSize().height), 80, baos);
                                            Bitmap bitmap = BitmapFactory.decodeByteArray(baos.toByteArray(), 0, baos.toByteArray().length);
                                            if (bitmap != null) {
                                                LuminanceSource source = new ZXRGBLuminanceSource(bitmap);
                                                BinaryBitmap binaryBitmap = new BinaryBitmap(new HybridBinarizer(source));
                                                Reader reader = new QRCodeReader();

                                                try {
                                                    Result result = reader.decode(binaryBitmap);
                                                    decodedKandiID = result.getText();
                                                    displayKandiTag(decodedKandiID);
                                                    Toast.makeText(Main.this, decodedKandiID, Toast.LENGTH_SHORT).show();

                                                    // TODO add profile view (list view maybe?)
                                                    // when closing profile view set decodedKandiID to ""

                                                } catch (Exception e) {
                                                }
                                            }
                                        } catch (Exception e) {
                                        }


                                    }

                                }
                            });
                        }
                        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
                        break;
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        // instantiate services
        IntentServiceDownloadFeed downloadFeed = new IntentServiceDownloadFeed();
        downloadFeed.startDownloadingFeed(Main.this, MY_KT_ID);

    }

    @Override
    public void onResume() {
        super.onResume();
        checkPlayServices();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
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

    private Emitter.Listener onDisplayKandiTag = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            String message = (String) args[0];
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
            ktUsersList.add(user);
            Log.d(TAG, "kt_id: " + user.getKt_id());
            Log.d(TAG, "placement: " + user.getPlacement());
            Log.d(TAG, "kandi_id: " + user.getKandiId());
            Log.d(TAG, "username: " + user.getUsername());
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

    // socket io end *******************************************************************************


    private void checkIfOpenedBefore() {

        boolean openedBefore = sharedPreferences.getBoolean(OPENED_BEFORE, false);

        if (!openedBefore) {
            Log.d(TAG, "not opened before");

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
        fList.add(DataFragment.newInstance());
        fList.add(EmptyFragment.newInstance());
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
                overridePendingTransition(R.anim.abc_slide_in_top, R.anim.abc_fade_out);
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
