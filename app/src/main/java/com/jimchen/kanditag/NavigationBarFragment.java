package com.jimchen.kanditag;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.os.StrictMode;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.net.URL;


public class NavigationBarFragment extends Fragment {

    private KtDatabase myDatabase;
    SharedPreferences sharedPreferences;
    public static final String MyPreferences = "MyPrefs";
    public static final String Name = "nameKey";
    public static final String FbId = "fbidKey";
    public static final String UserId = "userIdKey";
    public static final String NEW_MESSAGE = "NEW_MESSAGE";

    private String MY_KT_ID, MY_FB_ID, MY_USER_NAME;

    private View rootView, navBar;

    private Button toMain, toMessage, toFeed, toGroupMessage, toTickets;

    private TextView navBarTextView;

    private ImageView navBarUserImage, toSettings;

    public static final NavigationBarFragment newInstance() {
        NavigationBarFragment barFragment = new NavigationBarFragment();
        return barFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.navigation_bar, container, false);

        sharedPreferences = getActivity().getSharedPreferences(MyPreferences, Context.MODE_PRIVATE);
        MY_KT_ID = sharedPreferences.getString(UserId, "");
        MY_USER_NAME = sharedPreferences.getString(Name, "");
        MY_FB_ID = sharedPreferences.getString(FbId, "");

        navBar = rootView.findViewById(R.id.NavBar_NavBar);

        navBarTextView = (TextView) rootView.findViewById(R.id.NavBar_TextView);
        navBarTextView.setTextColor(getResources().getColor(R.color.yellow));
        navBarTextView.setTextSize(30);
        navBarTextView.setPadding(0, 5, 0, 0);
        Typeface typeface = Typeface.createFromAsset(getActivity().getAssets(), "fonts/stalemate_regular.ttf");
        navBarTextView.setTypeface(typeface);

        navBarUserImage = (ImageView) rootView.findViewById(R.id.NavBar_UserImage);
        navBarUserImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //need to open activity/show fragment for profile
                getActivity().getSupportFragmentManager().beginTransaction().setCustomAnimations(R.anim.abc_slide_in_top, R.anim.abc_slide_out_top).add(R.id.main_miniProfileViewFrameLayout, ProfileFragment.newInstance(), MY_KT_ID).commit();
            }
        });

        URL img_value = null;
        try {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
            img_value = new URL("https://graph.facebook.com/" + MY_FB_ID + "/picture?width=250&height=250");
            Bitmap mIcon = BitmapFactory.decodeStream(img_value.openConnection().getInputStream());
            //holder.profilePicture.setImageBitmap(mIcon);
            //holder.profilePicture.setPadding(6, 6, 6, 6);

            //make bitmap circular
            Bitmap circleBitmap = Bitmap.createBitmap(mIcon.getWidth(), mIcon.getHeight(), Bitmap.Config.ARGB_8888);
            BitmapShader shader = new BitmapShader(mIcon, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
            Paint paint = new Paint();
            paint.setShader(shader);
            Canvas canvas = new Canvas(circleBitmap);
            canvas.drawCircle(mIcon.getWidth()/2, mIcon.getHeight()/2, mIcon.getWidth()/2, paint);
            navBarUserImage.setImageBitmap(circleBitmap);

        } catch (Exception e) {
            e.printStackTrace();
        }

        toSettings = (ImageView) rootView.findViewById(R.id.NavBar_toSettings);

        toMain = (Button) rootView.findViewById(R.id.NavBar_toMain);
        toMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("toMain clicked");
                ((MainActivity) getActivity()).setPageOnMain();
                expandNavBar();
            }
        });

        toMessage = (Button) rootView.findViewById(R.id.NavBar_toMessage);
        toMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity) getActivity()).setPageOnMessage();
                expandNavBar();
            }
        });

        toFeed = (Button) rootView.findViewById(R.id.NavBar_toFeed);
        toFeed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity) getActivity()).setPageOnFeed();
                expandNavBar();
            }
        });

        toGroupMessage = (Button) rootView.findViewById(R.id.NavBar_toGroupMessage);
        toGroupMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity) getActivity()).setPageOnGroup();
                expandNavBar();
            }
        });

        toTickets = (Button) rootView.findViewById(R.id.NavBar_toTickets);
        toTickets.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity) getActivity()).setPageOnTicket();
                expandNavBar();
            }
        });

        return rootView;
    }

    public void removeHighlight() {
        toMain.setBackgroundColor(getResources().getColor(R.color.transparent_charcoal));
        toMain.setTextColor(Color.YELLOW);
        toFeed.setBackgroundColor(getResources().getColor(R.color.transparent_charcoal));
        toFeed.setTextColor(Color.YELLOW);
        toMessage.setBackgroundColor(getResources().getColor(R.color.transparent_charcoal));
        toMessage.setTextColor(Color.YELLOW);
        toGroupMessage.setBackgroundColor(getResources().getColor(R.color.transparent_charcoal));
        toGroupMessage.setTextColor(Color.YELLOW);
        toTickets.setBackgroundColor(getResources().getColor(R.color.transparent_charcoal));
        toTickets.setTextColor(Color.YELLOW);
    }

    public void highlightMainNavBar() {
        toMain.setBackgroundColor(getResources().getColor(R.color.yellow));
        toMain.setTextColor(Color.BLACK);
        navBarTextView.setText("C a m e r a");
        navBarTextView.invalidate();
    }

    public void highlightMessageNavBar() {
        toMessage.setBackgroundColor(getResources().getColor(R.color.yellow));
        toMessage.setTextColor(Color.BLACK);
        navBarTextView.setText("M e s s a g e");
        navBarTextView.invalidate();

    }

    public void highlightFeedNavBar() {
        toFeed.setBackgroundColor(getResources().getColor(R.color.yellow));
        toFeed.setTextColor(Color.BLACK);
        navBarTextView.setText("F e e d");
        navBarTextView.invalidate();

    }

    public void highlightGroupNavBar() {
        toGroupMessage.setBackgroundColor(getResources().getColor(R.color.yellow));
        toGroupMessage.setTextColor(Color.BLACK);
        navBarTextView.setText("G r o u p s");
        navBarTextView.invalidate();

    }

    public void highlightTicketNavBar() {
        toTickets.setBackgroundColor(getResources().getColor(R.color.yellow));
        toTickets.setTextColor(Color.BLACK);
        navBarTextView.setText("T i c k e t s");
        navBarTextView.invalidate();

    }

    public void expandToShowProfile() {

    }

    public void expandNavBar() {
        navBar.setLayoutParams(new RelativeLayout.LayoutParams(navBar.getWidth(), dpToPixels(85)));
        RelativeLayout.LayoutParams mainLayoutParams = (RelativeLayout.LayoutParams) toMain.getLayoutParams();
        mainLayoutParams.topMargin = dpToPixels(73);
        RelativeLayout.LayoutParams messageLayoutParams = (RelativeLayout.LayoutParams) toMessage.getLayoutParams();
        messageLayoutParams.topMargin = dpToPixels(73);
        RelativeLayout.LayoutParams gMessageLayoutParams = (RelativeLayout.LayoutParams) toGroupMessage.getLayoutParams();
        gMessageLayoutParams.topMargin = dpToPixels(73);
        RelativeLayout.LayoutParams feedLayoutParams = (RelativeLayout.LayoutParams) toFeed.getLayoutParams();
        feedLayoutParams.topMargin = dpToPixels(73);
        RelativeLayout.LayoutParams ticketsLayoutParams = (RelativeLayout.LayoutParams) toTickets.getLayoutParams();
        ticketsLayoutParams.topMargin = dpToPixels(73);

        navBarTextView.setLayoutParams(new RelativeLayout.LayoutParams(navBarTextView.getWidth(), dpToPixels(72)));

        navBarUserImage.setLayoutParams(new RelativeLayout.LayoutParams(dpToPixels(50), dpToPixels(50)));
        RelativeLayout.LayoutParams userImageLayoutParams = (RelativeLayout.LayoutParams) navBarUserImage.getLayoutParams();
        userImageLayoutParams.topMargin = dpToPixels(8);
        userImageLayoutParams.leftMargin = dpToPixels(10);

        toSettings.setLayoutParams(new RelativeLayout.LayoutParams(dpToPixels(54), dpToPixels(54)));
        RelativeLayout.LayoutParams toSettingsLayoutParams = (RelativeLayout.LayoutParams) toSettings.getLayoutParams();
        toSettingsLayoutParams.topMargin = dpToPixels(8);
        toSettingsLayoutParams.rightMargin = dpToPixels(10);
        toSettingsLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);

        //when done, no need to setLayout, instead call view.requestLayout();
        //45
    }

    public void shrinkNavBar() {
        navBar.setLayoutParams(new RelativeLayout.LayoutParams(navBar.getWidth(), dpToPixels(12)));
        RelativeLayout.LayoutParams mainLayoutParams = (RelativeLayout.LayoutParams) toMain.getLayoutParams();
        mainLayoutParams.topMargin = dpToPixels(0);
        RelativeLayout.LayoutParams messageLayoutParams = (RelativeLayout.LayoutParams) toMessage.getLayoutParams();
        messageLayoutParams.topMargin = dpToPixels(0);
        RelativeLayout.LayoutParams gMessageLayoutParams = (RelativeLayout.LayoutParams) toGroupMessage.getLayoutParams();
        gMessageLayoutParams.topMargin = dpToPixels(0);
        RelativeLayout.LayoutParams feedLayoutParams = (RelativeLayout.LayoutParams) toFeed.getLayoutParams();
        feedLayoutParams.topMargin = dpToPixels(0);
        RelativeLayout.LayoutParams ticketsLayoutParams = (RelativeLayout.LayoutParams) toTickets.getLayoutParams();
        ticketsLayoutParams.topMargin = dpToPixels(0);

        navBarTextView.setLayoutParams(new RelativeLayout.LayoutParams(navBarTextView.getWidth(), dpToPixels(0)));

        navBarUserImage.setLayoutParams(new RelativeLayout.LayoutParams(0, 0));

        toSettings.setLayoutParams(new RelativeLayout.LayoutParams(0, 0));

        //200
    }

    private int dpToPixels(int dpDimensions) {
        float density = getResources().getDisplayMetrics().density;
        return (int) (dpDimensions * density + 0.5f);
    }

    public void scaleNavBar() {

    }
}