package com.kanditag.kanditag;

import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Shader;
import android.os.StrictMode;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.net.URL;


public class ProfileFragment extends Fragment {

    private Context context;
    private View rootView;

    //Layout Vars
    private ImageView profileImageBackground, exitButton, circularProfileImage;
    private TextView usernameTextView;
    //Layout Vars End

    private Bundle extras;

    private KtDatabase myDatabase;
    SharedPreferences sharedPreferences;
    public static final String MyPreferences = "MyPrefs";
    public static final String Name = "nameKey";
    public static final String FbId = "fbidKey";
    public static final String UserId = "userIdKey";
    public static final String NEW_MESSAGE = "NEW_MESSAGE";

    private String MY_KT_ID, MY_FB_ID, MY_USER_NAME;

    public static final ProfileFragment newInstance() {
        ProfileFragment profileFragment = new ProfileFragment();
        return profileFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.profile_fragment, container, false);

        this.context = getActivity();
        myDatabase = new KtDatabase(getActivity());
        sharedPreferences = getActivity().getSharedPreferences(MyPreferences, Context.MODE_PRIVATE);
        MY_KT_ID = sharedPreferences.getString(UserId, "");
        MY_USER_NAME = sharedPreferences.getString(Name, "");
        MY_FB_ID = sharedPreferences.getString(FbId, "");

        try {
            extras = getArguments();
        } catch (NullPointerException nullEx) {}

        profileImageBackground = (ImageView) rootView.findViewById(R.id.ProfileFragment_ProfilePicture);
        circularProfileImage = (ImageView) rootView.findViewById(R.id.ProfileFragment_CircularProfilePicture);
        usernameTextView = (TextView) rootView.findViewById(R.id.ProfileFragment_Username);


        if (extras == null) {
            usernameTextView.setText(MY_USER_NAME);
            URL img_value = null;
            try {
                StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                StrictMode.setThreadPolicy(policy);
                img_value = new URL("https://graph.facebook.com/" + MY_FB_ID + "/picture?width=500&height=500");
                Bitmap mIcon = BitmapFactory.decodeStream(img_value.openConnection().getInputStream());
                profileImageBackground.setImageBitmap(mIcon);

                //make bitmap circular
                Bitmap circleBitmap = Bitmap.createBitmap(mIcon.getWidth(), mIcon.getHeight(), Bitmap.Config.ARGB_8888);
                BitmapShader shader = new BitmapShader(mIcon, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
                Paint paint = new Paint();
                paint.setShader(shader);
                Canvas canvas = new Canvas(circleBitmap);
                canvas.drawCircle(mIcon.getWidth()/2, mIcon.getHeight()/2, mIcon.getWidth()/2, paint);
                circularProfileImage.setImageBitmap(circleBitmap);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        exitButton = (ImageView) rootView.findViewById(R.id.ProfileFragment_ExitButton);

        exitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getFragmentManager().beginTransaction().remove(ProfileFragment.this).commit();
            }
        });

        return rootView;
    }
}
