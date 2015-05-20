package com.jimchen.kanditag;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

/**
 * Created by Jim on 5/13/15.
 */
public class ProfileAboutMeFragment extends android.support.v4.app.Fragment {

    private View rootView;

    public static final ProfileAboutMeFragment newInstance() {
        ProfileAboutMeFragment fragment = new ProfileAboutMeFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.profile_aboutme_fragment, container, false);


        return rootView;
    }


}
