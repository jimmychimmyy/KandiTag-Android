package com.jimchen.kanditag;

import android.hardware.Camera;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;


public class CameraPreview extends Fragment  {

    private static final String TAG = "CameraPreview";

    private View rootView;

    // Camera Variables
    private Camera myCamera;
    private Preview myPreview;

    public static final CameraPreview newInstance() {
        CameraPreview preview = new CameraPreview();
        return preview;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.activity_camera_preview, container, false);

        // starting camera
        myCamera = getCameraInstance(); // call to getCameraInstance method
        myPreview = new Preview(getActivity(), myCamera);
        ((FrameLayout) rootView.findViewById(R.id.CameraPreview_PreviewContainer)).addView(myPreview);

        return rootView;
    }

    private static Camera getCameraInstance() {
        Camera camera = null;
        try {
            camera = Camera.open();
        } catch (Exception e) {
            e.printStackTrace();
            //TODO check to make sure to catch the camera cannot be connected error
            // show alert dialogue that user needs to restart camera
        }

        return camera;
    }

}
