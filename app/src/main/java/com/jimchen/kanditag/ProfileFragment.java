package com.jimchen.kanditag;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.StrictMode;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.Session;
import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.zip.Inflater;


public class ProfileFragment extends Fragment {

    private String TAG = "ProfileFragment";

    public static final int SIGN_OUT_REQUEST = 0;
    public static final int RESULT_LOGGED_OUT = 9;

    private Context context;
    private View rootView;

    // grid view
    private GridView galleryGridView;

    // profile image container;
    private ImageView profileImageContainer;

    private Bundle extras;

    private KtDatabase myDatabase;
    // Shared Preferences
    private SharedPreferences sharedPreferences;
    public static final String USER_PREFERENCES = "com.jimchen.kanditag.extra.PREFERENCES";
    public static final String USERNAME = "com.jimchen.kanditag.extra.USERNAME";
    public static final String FBID = "com.jimchen.kanditag.extra.FBID";
    public static final String KTID = "com.jimchen.kanditag.extra.KTID";
    public static final String USER_PROFILE_IMAGE = "com.jimchen.kanditag.extra.USER_PROFILE_IMAGE";
    public static final String NEW_MESSAGE = "com.jimchen.kanditag.extra.NEW_MESSAGE";

    private String kt_id, user_name, fb_id;
    private String MY_KT_ID, MY_FB_ID, MY_USER_NAME, MY_PROFILE_PICTURE;

    // to settings variable
    private ImageView toSettings;

    // socket variables
    private static com.github.nkzawa.socketio.client.Socket socket;
    private final String HOST = "http://kandi.jit.su/";
    private final int portNumber = 3000;


    public static final ProfileFragment newInstance() {
        ProfileFragment profileFragment = new ProfileFragment();
        return profileFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.profile_fragment, container, false);

        this.context = getActivity();
        myDatabase = new KtDatabase(getActivity());
        sharedPreferences = getActivity().getSharedPreferences(USER_PREFERENCES, Context.MODE_PRIVATE);
        MY_KT_ID = sharedPreferences.getString(KTID, "");
        MY_USER_NAME = sharedPreferences.getString(USERNAME, "");
        MY_FB_ID = sharedPreferences.getString(FBID, "");
        MY_PROFILE_PICTURE = sharedPreferences.getString(USER_PROFILE_IMAGE, "");

        galleryGridView = (GridView) rootView.findViewById(R.id.ProfileFragment_GalleryGridView);

        profileImageContainer = (ImageView) rootView.findViewById(R.id.ProfileFragment_ProfileImageContainer);

        downloadProfileImage();

        /**
        toSettings = (ImageView) rootView.findViewById(R.id.ProfileFragment_toSettings);
        toSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent toSettings = new Intent(getActivity(), SettingsActivity.class);
                startActivityForResult(toSettings, SIGN_OUT_REQUEST);
                getActivity().overridePendingTransition(R.anim.abc_slide_in_bottom, R.anim.abc_slide_out_bottom);
            }
        });

         **/

        return rootView;
    }

    private void downloadProfileImage() {
        try {
            IO.Options options = new IO.Options();
            options.forceNew = true;
            socket = IO.socket(HOST, options);
            socket.on("download_profile_image", onDownloadProfileImage);
            //socket.on("download_images", onDownloadImages);
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

    private Emitter.Listener onDownloadProfileImage = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {

            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    byte[] image = (byte[]) args[0];
                    if (image != null) {
                        byte[] decompressed_image = decompressByteArray(image);
                        Bitmap bitmap = BitmapFactory.decodeByteArray(decompressed_image, 0, decompressed_image.length);
                        //profileImageByteArray = bitmap;
                        setProfileImage(bitmap);
                        //profileImage.setImageBitmap(profileImageByteArray);
                    } else {
                        profileImageContainer.setImageResource(R.drawable.golden_kt_logo);
                    }
                    //socket.close();
                    //socket.disconnect();
                    //socket = null;
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

    private void setProfileImage(Bitmap mIcon) {

        try {
            //make bitmap circular
            Bitmap circleBitmap = Bitmap.createBitmap(mIcon.getWidth(), mIcon.getHeight(), Bitmap.Config.ARGB_8888);
            BitmapShader shader = new BitmapShader(mIcon, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
            Paint paint = new Paint();
            paint.setShader(shader);
            Canvas canvas = new Canvas(circleBitmap);
            canvas.drawCircle(mIcon.getWidth()/2, mIcon.getHeight()/2, mIcon.getWidth()/2, paint);

            profileImageContainer.setImageBitmap(circleBitmap);

        } catch (Exception e) {
            e.printStackTrace();
        }

        profileImageContainer.invalidate();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SIGN_OUT_REQUEST) {
            if (resultCode == RESULT_LOGGED_OUT) {

                System.out.println("logged out");
                Session session = Session.getActiveSession();
                session.closeAndClearTokenInformation();
                Intent loginIntent = new Intent(getActivity(), LoginActivity.class);
                startActivityForResult(loginIntent, 1);
                getActivity().overridePendingTransition(R.anim.abc_slide_in_top, R.anim.abc_fade_out);
                getActivity().finish();
                getActivity().overridePendingTransition(R.anim.abc_slide_in_bottom, R.anim.abc_fade_out);
            }

            if (resultCode == getActivity().RESULT_CANCELED) {

            }

        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        // Make sure that we are currently visible
        if (this.isVisible()) {
            // If we are becoming invisible, then...
            if (!isVisibleToUser) {
            }
        }
    }

}
