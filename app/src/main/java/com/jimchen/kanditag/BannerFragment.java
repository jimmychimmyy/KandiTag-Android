package com.jimchen.kanditag;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


public class BannerFragment extends Fragment {

    private Context context;
    private View rootView;

    private TextView titleBar;

    public static final BannerFragment newInstance() {
        BannerFragment bannerFragment = new BannerFragment();
        return bannerFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.banner_fragment, container, false);

        titleBar = (TextView) rootView.findViewById(R.id.BannerFragment_TitleBar);
        Typeface typeface = Typeface.createFromAsset(getActivity().getAssets(), "fonts/stalemate_regular.ttf");
        titleBar.setTypeface(typeface);
        titleBar.setTextSize(38);
        titleBar.setTextColor(getResources().getColor(R.color.vegas_gold));
        //titleBar.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);

        Bundle extras = getArguments();
        String titleBarText = extras.getString("title");
        titleBar.setText(titleBarText);

        return rootView;
    }

}
