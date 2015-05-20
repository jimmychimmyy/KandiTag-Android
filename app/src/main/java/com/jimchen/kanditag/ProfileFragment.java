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
import android.os.StrictMode;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.Session;

import java.net.URL;
import java.util.ArrayList;


public class ProfileFragment extends Fragment {

    private Context context;
    private View rootView;

    //Layout Vars
    private ImageView profileImageBackground, exitButton, circularProfileImage;
    private TextView usernameTextView;
    private GridView galleryGridView, kandiGroupsGridView;
    //Layout Vars End

    //Adapters for Grids
    private KandiGroupObjectListAdapter kandiGroupAdapter;
    private GalleryGridAdapter galleryGridAdapter;
    //Adapters for Grids End

    private Bundle extras;

    private KtDatabase myDatabase;
    SharedPreferences sharedPreferences;
    public static final String MyPreferences = "MyPrefs";
    public static final String Name = "nameKey";
    public static final String FbId = "fbidKey";
    public static final String UserId = "userIdKey";
    public static final String NEW_MESSAGE = "NEW_MESSAGE";

    private String kt_id, user_name, fb_id;
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


        galleryGridView = (GridView) rootView.findViewById(R.id.ProfileFragment_GalleryGridView);


        return rootView;
    }

}
