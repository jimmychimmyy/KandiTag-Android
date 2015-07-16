package com.jimchen.kanditag;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import com.facebook.Session;
import com.github.nkzawa.emitter.Emitter;
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

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.zip.Deflater;


public class CameraPreview extends Activity {

    private static final String TAG = "CameraPreview";

    public static final int PREVIEW_IMAGE_REQUEST = 2;

    // Shared Preferences
    private SharedPreferences sharedPreferences;
    public static final String USER_PREFERENCES = "com.jimchen.kanditag.extra.PREFERENCES";
    public static final String USERNAME = "com.jimchen.kanditag.extra.USERNAME";
    public static final String FBID = "com.jimchen.kanditag.extra.FBID";
    public static final String KTID = "com.jimchen.kanditag.extra.KTID";

    // Local Database
    private KtDatabase myDatabase;
    private String MY_KT_ID, MY_FB_ID, MY_USER_NAME;

    // Camera Variables
    private Camera myCamera;
    private Preview myPreview;
    private ImageView cameraButton;
    private ImageView closeCamera;

    private ImageView switchCamera;

    // socket variables
    private static com.github.nkzawa.socketio.client.Socket socket;
    private final String HOST = "http://kandi.jit.su/";
    private final int portNumber = 3000;

    // scanned kanditag vars
    private Result qrResult;
    private String decodedKandiID;
    private ArrayList<KtUserObject> ktUsersList;

    @Override
    public void onResume() {
        super.onResume();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        if (myCamera != null) {
            myCamera.startPreview();
        }

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
        overridePendingTransition(R.anim.abc_slide_in_bottom, R.anim.abc_slide_out_bottom);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_preview);

        // starting camera
        myCamera = getCameraInstance(); // call to getCameraInstance method
        myPreview = new Preview(CameraPreview.this, myCamera);
        ((FrameLayout) findViewById(R.id.CameraPreview_PreviewContainer)).addView(myPreview);

        // grab user's info
        myDatabase = new KtDatabase(this);
        sharedPreferences = this.getSharedPreferences(USER_PREFERENCES, Context.MODE_PRIVATE);
        MY_KT_ID = sharedPreferences.getString(KTID, "");
        MY_USER_NAME = sharedPreferences.getString(USERNAME, "");

        cameraButton = (ImageView) findViewById(R.id.CameraPreview_Camera);
        cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                myPreview.myCamera.takePicture(shutterCallback, rawCallback, pictureCallback);
            }
        });

        closeCamera = (ImageView) findViewById(R.id.CameraPreview_closeCameraPreview);
        closeCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
                overridePendingTransition(R.anim.abc_slide_in_bottom, R.anim.abc_slide_out_bottom);
            }
        });

        switchCamera = (ImageView) findViewById(R.id.CameraPreview_FlipCamera);
        switchCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

    }


    private void switchCamera() {
        myPreview.myCamera.stopPreview();
        myCamera.release();
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
            //scannedKtUsersList.removeAll(scannedKtUsersList);
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

                    Log.d(TAG, filename);

                    //uploadImage(compressByteArray(bytes), filename);
                    image_path = saveImageInternally(bitmap, filename);
                    AsyncTask<String, Void, Void> upload = new AsyncTask<String, Void, Void>() {
                        @Override
                        protected Void doInBackground(String... param) {
                            String path = param[0];
                            uploadImageFile(path);
                            return null;
                        }
                    };

                    upload.execute(image_path);
                    //uploadImageFile(image_path);

                    Intent imagePreview = new Intent(CameraPreview.this, ImagePreview.class);
                    Bundle extras = new Bundle();
                    extras.putString("filepath", image_path);
                    //extras.putString("filename", filename); // probably will not need to include the filename
                    imagePreview.putExtras(extras);
                    startActivityForResult(imagePreview, PREVIEW_IMAGE_REQUEST);

                } else if (decodedKandiID.contains("dhc")) {

                    // register kanditag

                    myPreview.myCamera.startPreview();

                    //TODO remove this before production
                    Toast.makeText(CameraPreview.this, qrResult.getText(), Toast.LENGTH_SHORT).show();

                    registerKandiTag(decodedKandiID);

                }


            }
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

        return path.toString(); //directory.getAbsolutePath(); // path.toString();
    }

    private void uploadImageFile(String filePath) {

        String url = "http://kandi.nodejitsu.com/upload_image";

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        int bytesRead, bytesAvailable, bufferSize;
        byte[] buffer;
        int maxBufferSize = 1 * 1024 * 1024;
        File sourceFile = new File(filePath);

        HttpPost post = null;
        //HttpClient client = null;
        CloseableHttpClient httpClient = null;
        CloseableHttpResponse httpResponse = null;
        //HttpResponse response = null;
        HttpEntity entityRes = null;
        String result;

        Log.d(TAG, filePath);

        if (!sourceFile.isFile()) {
            Log.e(TAG, "source file does not exist, cannot upload");
        }

        Log.d(TAG, "uploadImageFile");

        try {

            httpClient = HttpClients.createDefault();
            post = new HttpPost(url);

            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);

            builder.addTextBody("filename", sourceFile.getName(), ContentType.TEXT_PLAIN);
            builder.addBinaryBody("file", sourceFile, ContentType.create("image/png"), sourceFile.getName());


            HttpEntity entity = builder.build();
            post.setEntity(entity);

            httpResponse = httpClient.execute(post);
            entityRes = httpResponse.getEntity();

            result = EntityUtils.toString(entityRes);

            System.out.print(result);


        } catch (Exception e) {

        }
    }

    private class uploadImageTask extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... params) {

            String filepath = params[0];
            String Url = "http://kandi.nodejitsu.com/upload_image";

            try {

                File file = new File(filepath);

                HttpURLConnection connection = null;
                DataOutputStream dos = null;
                

            } catch (Exception e) {

            }

            return null;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Session.getActiveSession().onActivityResult(this, requestCode, resultCode, data);


        // TODO when user returns from image preview if the image was posted then finish this
        // if it was canceled, allow user to take another picture
    }

}
