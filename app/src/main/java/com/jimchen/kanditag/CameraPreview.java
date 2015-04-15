package com.jimchen.kanditag;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;


public class CameraPreview extends Fragment  {

    RelativeLayout relativeLayout;
    View rootView;
    ImageView takeGifIV;

    public static final CameraPreview newInstance() {
        CameraPreview preview = new CameraPreview();
        return preview;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.activity_camera_preview, container, false);

        return rootView;
    }

}
