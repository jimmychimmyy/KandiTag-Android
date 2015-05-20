package com.jimchen.kanditag;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

/**
 * Created by Jim on 5/13/15.
 */
public class ProfileGalleryFragment extends android.support.v4.app.Fragment {

    private View rootView;
    private GridView gridView;
    private ImageGridAdapter imageGridAdapter;

    public static final ProfileGalleryFragment newInstance() {
        ProfileGalleryFragment fragment = new ProfileGalleryFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.profile_gallery_fragment, container, false);

        //TODO need to get the data passed into this fragment
        /**
        imageGridAdapter = new ImageGridAdapter(ProfileGalleryFragment.this, R.id.ProfileGalleryFragment_GridView, images);

        gridView = (GridView) rootView.findViewById(R.id.ProfileGalleryFragment_GridView);
        gridView.setAdapter(imageGridAdapter);
         **/

        return rootView;
    }

}
