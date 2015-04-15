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
import android.util.Log;
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

        kandiGroupsGridView = (GridView) rootView.findViewById(R.id.ProfileFragment_KandiGroupsGridView);
        galleryGridView = (GridView) rootView.findViewById(R.id.ProfileFragment_GalleryGridView);

        //galleryGridAdapter = new GalleryGridAdapter(getActivity(), R.layout.gallery_grid_item, "list");

        try {
            extras = getArguments();
            user_name = extras.getString("user_name");
            fb_id = extras.getString("user_fbid");
            kt_id = extras.getString("user_ktid");
        } catch (NullPointerException nullEx) {}


        final GetAllGroupsFromLocalDbAsyncTask getAllGroups = new GetAllGroupsFromLocalDbAsyncTask(getActivity(), new ReturnKandiGroupObjectParcelableArrayList() {
            @Override
            public void processFinish(final ArrayList<KandiGroupObjectParcelable> output) {
                System.out.println("ProfileFragment.getAllGroups.output.size() = " + output.size());
                if (output.size() < 5) {
                    kandiGroupsGridView.setNumColumns(output.size());
                }
                kandiGroupAdapter = new KandiGroupObjectListAdapter(getActivity(), R.layout.kandi_group_list_item, output);
                kandiGroupsGridView.setAdapter(kandiGroupAdapter);
                kandiGroupsGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        System.out.println("Kandi Group Selected: " + output.get(position).getGroupName());
                    }
                });
                //getActivity().getSupportFragmentManager().beginTransaction().setCustomAnimations(R.anim.left_slide_in, R.anim.left_slide_out).remove(ProfileFragment.this).commit();
                //getActivity().getSupportFragmentManager().beginTransaction().setCustomAnimations(R.anim.left_slide_in, R.anim.left_slide_out).add(R.id.main_miniProfileViewFrameLayout, profileFragment, output.get(position).getKt_id()).commit();
            }
        });

        GetAllKandiFromLocalAsyncTask getAllKandi = new GetAllKandiFromLocalAsyncTask(getActivity(), new ReturnKandiObjectArrayAsyncResponse() {
            @Override
            public void processFinish(ArrayList<KandiObject> output) {
                getAllGroups.execute(output);
            }
        });


        profileImageBackground = (ImageView) rootView.findViewById(R.id.ProfileFragment_ProfilePicture);
        circularProfileImage = (ImageView) rootView.findViewById(R.id.ProfileFragment_CircularProfilePicture);
        circularProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //remove this fragment
                getActivity().getSupportFragmentManager().beginTransaction().setCustomAnimations(R.anim.left_slide_in, R.anim.abc_slide_out_top).remove(ProfileFragment.this).commit();

                //log out of facebook (if logged in through facebook)
                Session session = Session.getActiveSession();
                session.closeAndClearTokenInformation();

                //close MainActivity
                getActivity().finish();

                //Show Login Page
                Intent showLoginPage = new Intent(getActivity(), Login.class);
                startActivity(showLoginPage);

            }
        });


        usernameTextView = (TextView) rootView.findViewById(R.id.ProfileFragment_Username);
        //usernameTextView.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
        Typeface typeface = Typeface.createFromAsset(getActivity().getAssets(), "fonts/stalemate_regular.ttf");
        usernameTextView.setTypeface(typeface);
        usernameTextView.setTextSize(30);
        usernameTextView.setTextColor(getResources().getColor(R.color.vegas_gold));


        if (extras == null) {
            getAllKandi.execute();
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
        } else {
            usernameTextView.setText(user_name);
            URL img_value = null;
            try {
                StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                StrictMode.setThreadPolicy(policy);
                img_value = new URL("https://graph.facebook.com/" + fb_id + "/picture?width=500&height=500");
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
                getActivity().getSupportFragmentManager().beginTransaction().setCustomAnimations(R.anim.left_slide_in, R.anim.right_slide_out).remove(ProfileFragment.this).commit();
            }
        });

        return rootView;
    }

}
