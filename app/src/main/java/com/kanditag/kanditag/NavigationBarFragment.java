package com.kanditag.kanditag;

import android.graphics.Color;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;


public class NavigationBarFragment extends Fragment {

    private View rootView, navBar;

    private Button toMain, toMessage, toFeed, toGroupMessage, toTickets;

    public static final NavigationBarFragment newInstance() {
        NavigationBarFragment barFragment = new NavigationBarFragment();
        return barFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.navigation_bar, container, false);

        navBar = rootView.findViewById(R.id.NavBar_NavBar);

        toMain = (Button) rootView.findViewById(R.id.NavBar_toMain);
        toMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("toMain clicked");
                ((MainActivity) getActivity()).setPageOnMain();
            }
        });

        toMessage = (Button) rootView.findViewById(R.id.NavBar_toMessage);
        toMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity) getActivity()).setPageOnMessage();
            }
        });

        toFeed = (Button) rootView.findViewById(R.id.NavBar_toFeed);
        toFeed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity) getActivity()).setPageOnFeed();
            }
        });

        toGroupMessage = (Button) rootView.findViewById(R.id.NavBar_toGroupMessage);
        toTickets = (Button) rootView.findViewById(R.id.NavBar_toTickets);

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
    }

    public void highlightMessageNavBar() {
        toMessage.setBackgroundColor(getResources().getColor(R.color.yellow));
        toMessage.setTextColor(Color.BLACK);
    }

    public void highlightFeedNavBar() {
        toFeed.setBackgroundColor(getResources().getColor(R.color.yellow));
        toFeed.setTextColor(Color.BLACK);
    }

    public void expandToShowProfile() {

    }

    public void expandNavBar() {
        navBar.setLayoutParams(new RelativeLayout.LayoutParams(navBar.getWidth(), dpToPixels(60)));
        RelativeLayout.LayoutParams mainLayoutParams = (RelativeLayout.LayoutParams) toMain.getLayoutParams();
        mainLayoutParams.topMargin = dpToPixels(48);
        RelativeLayout.LayoutParams messageLayoutParams = (RelativeLayout.LayoutParams) toMessage.getLayoutParams();
        messageLayoutParams.topMargin = dpToPixels(48);
        RelativeLayout.LayoutParams gMessageLayoutParams = (RelativeLayout.LayoutParams) toGroupMessage.getLayoutParams();
        gMessageLayoutParams.topMargin = dpToPixels(48);
        RelativeLayout.LayoutParams feedLayoutParams = (RelativeLayout.LayoutParams) toFeed.getLayoutParams();
        feedLayoutParams.topMargin = dpToPixels(48);
        RelativeLayout.LayoutParams ticketsLayoutParams = (RelativeLayout.LayoutParams) toTickets.getLayoutParams();
        ticketsLayoutParams.topMargin = dpToPixels(48);

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
        //200
    }

    private int dpToPixels(int dpDimensions) {
        float density = getResources().getDisplayMetrics().density;
        return (int) (dpDimensions * density + 0.5f);
    }
}
