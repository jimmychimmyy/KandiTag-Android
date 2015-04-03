package com.kanditag.kanditag;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;


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
