package com.jimchen.kanditag;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by Jim on 7/13/15.
 */
public class TestFragment extends Fragment {

    private View rootView;

    public static final TestFragment newInstance() {
        TestFragment fragment = new TestFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fillgap_message, container, false);

        return rootView;
    }
}
