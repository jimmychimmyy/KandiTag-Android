package com.jimchen.kanditag;

import android.app.FragmentTransaction;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;

import java.io.ByteArrayOutputStream;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.Inflater;

/**
 * Created by Jim on 5/16/15.
 */
public class DataFragment extends Fragment {

    private String TAG = "DataFragment";

    private Context context;

    private KtDatabase myDatabase;

    // Shared Preferences
    private SharedPreferences sharedPreferences;
    public static final String USER_PREFERENCES = "com.jimchen.kanditag.extra.PREFERENCES";
    public static final String USERNAME = "com.jimchen.kanditag.extra.USERNAME";
    public static final String FBID = "com.jimchen.kanditag.extra.FBID";
    public static final String KTID = "com.jimchen.kanditag.extra.KTID";
    public static final String USER_PROFILE_IMAGE = "com.jimchen.kanditag.extra.USER_PROFILE_IMAGE";

    private String kt_id, user_name, fb_id;
    private String MY_KT_ID, MY_FB_ID, MY_USER_NAME;

    private View rootView;

    private ViewPager viewPager;
    private MyPageAdapter pageAdapter;

    // buttons TODO use images on the buttons
    private Button feed, message, exchange, profile;

    // bottom of buttons
    private Button feedB, messageB, exchangeB, profileB;


    public static DataFragment newInstance() {
        DataFragment fragment = new DataFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.data_fragment, container, false);

        this.context = getActivity();
        myDatabase = new KtDatabase(getActivity());
        sharedPreferences = getActivity().getSharedPreferences(USER_PREFERENCES, Context.MODE_PRIVATE);
        MY_KT_ID = sharedPreferences.getString(KTID, "");
        MY_USER_NAME = sharedPreferences.getString(USERNAME, "");
        MY_FB_ID = sharedPreferences.getString(FBID, "");

        /**
        android.support.v4.app.FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.add(R.id.DataFragment_NavigationBarContainer, DataFragmentNavigationBar.newInstance()).commit();
         **/

        // find view pager and set page adapter
        pageAdapter = new MyPageAdapter(getActivity().getSupportFragmentManager(), getFragmentList());
        viewPager = (ViewPager) rootView.findViewById(R.id.DataFragment_ViewPager);
        viewPager.setAdapter(pageAdapter);

        /**
        // find buttons
        feed = (Button) rootView.findViewById(R.id.DataFragment_FeedButton);
        message = (Button) rootView.findViewById(R.id.DataFragment_MessageButton);
        exchange = (Button) rootView.findViewById(R.id.DataFragment_ExchangeButton);
        profile = (Button) rootView.findViewById(R.id.DataFragment_ProfileButton);

        // find button bottoms
        feedB = (Button) rootView.findViewById(R.id.DataFragment_FeedButtonBottom);
        messageB = (Button) rootView.findViewById(R.id.DataFragment_MessageButtonBottom);
        exchangeB = (Button) rootView.findViewById(R.id.DataFragment_ExchangeButtonBottom);
        profileB = (Button) rootView.findViewById(R.id.DataFragment_ProfileButtonBottom);

        // set up on page listener to change button colors
        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                switch (position) {
                    case 0:
                        feed.setTextColor(getResources().getColor(R.color.gold));
                        message.setTextColor(getResources().getColor(R.color.white));
                        exchange.setTextColor(getResources().getColor(R.color.white));
                        profile.setTextColor(getResources().getColor(R.color.white));

                        feedB.setBackgroundColor(getResources().getColor(R.color.gold));
                        messageB.setBackgroundColor(getResources().getColor(R.color.white));
                        exchangeB.setBackgroundColor(getResources().getColor(R.color.white));
                        profileB.setBackgroundColor(getResources().getColor(R.color.white));
                        break;
                    case 1:
                        feed.setTextColor(getResources().getColor(R.color.white));
                        message.setTextColor(getResources().getColor(R.color.gold));
                        exchange.setTextColor(getResources().getColor(R.color.white));
                        profile.setTextColor(getResources().getColor(R.color.white));

                        feedB.setBackgroundColor(getResources().getColor(R.color.white));
                        messageB.setBackgroundColor(getResources().getColor(R.color.gold));
                        exchangeB.setBackgroundColor(getResources().getColor(R.color.white));
                        profileB.setBackgroundColor(getResources().getColor(R.color.white));
                        break;
                    case 2:
                        feed.setTextColor(getResources().getColor(R.color.white));
                        message.setTextColor(getResources().getColor(R.color.white));
                        exchange.setTextColor(getResources().getColor(R.color.gold));
                        profile.setTextColor(getResources().getColor(R.color.white));

                        feedB.setBackgroundColor(getResources().getColor(R.color.white));
                        messageB.setBackgroundColor(getResources().getColor(R.color.white));
                        exchangeB.setBackgroundColor(getResources().getColor(R.color.gold));
                        profileB.setBackgroundColor(getResources().getColor(R.color.white));
                        break;
                    case 3:
                        feed.setTextColor(getResources().getColor(R.color.white));
                        message.setTextColor(getResources().getColor(R.color.white));
                        exchange.setTextColor(getResources().getColor(R.color.white));
                        profile.setTextColor(getResources().getColor(R.color.gold));

                        feedB.setBackgroundColor(getResources().getColor(R.color.white));
                        messageB.setBackgroundColor(getResources().getColor(R.color.white));
                        exchangeB.setBackgroundColor(getResources().getColor(R.color.white));
                        profileB.setBackgroundColor(getResources().getColor(R.color.gold));
                        break;
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

         **/

        return rootView;
    }

    private List<Fragment> getFragmentList() {
        List<android.support.v4.app.Fragment> fList = new ArrayList<>();
        fList.add(FeedFragment.newInstance());
        fList.add(MessageFragment.newInstance());
        fList.add(ExchangeFragment.newInstance());
        fList.add(ProfileFragment.newInstance());
        return fList;
    }

    public int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

}
