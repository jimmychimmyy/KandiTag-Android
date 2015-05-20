package com.jimchen.kanditag;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jim on 5/16/15.
 */
public class DataFragment extends Fragment {

    private View rootView;

    private ViewPager viewPager;
    private MyPageAdapter pageAdapter;

    // buttons TODO use images on the buttons
    private Button feed, message, exchange, profile;

    public static DataFragment newInstance() {
        DataFragment fragment = new DataFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.data_fragment, container, false);

        getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);


        // find view pager and set page adapter
        pageAdapter = new MyPageAdapter(getActivity().getSupportFragmentManager(), getFragmentList());
        viewPager = (ViewPager) rootView.findViewById(R.id.DataFragment_ViewPager);
        viewPager.setAdapter(pageAdapter);

        // find buttons
        feed = (Button) rootView.findViewById(R.id.DataFragment_FeedButton);
        message = (Button) rootView.findViewById(R.id.DataFragment_MessageButton);
        exchange = (Button) rootView.findViewById(R.id.DataFragment_ExchangeButton);
        profile = (Button) rootView.findViewById(R.id.DataFragment_ProfileButton);

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
                        break;
                    case 1:
                        feed.setTextColor(getResources().getColor(R.color.white));
                        message.setTextColor(getResources().getColor(R.color.gold));
                        exchange.setTextColor(getResources().getColor(R.color.white));
                        profile.setTextColor(getResources().getColor(R.color.white));
                        break;
                    case 2:
                        feed.setTextColor(getResources().getColor(R.color.white));
                        message.setTextColor(getResources().getColor(R.color.white));
                        exchange.setTextColor(getResources().getColor(R.color.gold));
                        profile.setTextColor(getResources().getColor(R.color.white));
                        break;
                    case 3:
                        feed.setTextColor(getResources().getColor(R.color.white));
                        message.setTextColor(getResources().getColor(R.color.white));
                        exchange.setTextColor(getResources().getColor(R.color.white));
                        profile.setTextColor(getResources().getColor(R.color.gold));
                        break;
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

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
}
