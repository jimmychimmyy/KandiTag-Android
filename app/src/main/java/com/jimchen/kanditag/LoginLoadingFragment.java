package com.jimchen.kanditag;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

/**
 * Created by Jim on 5/14/15.
 */
public class LoginLoadingFragment extends android.support.v4.app.Fragment {

    private View rootView;
    private ImageView spinner;

    public static LoginLoadingFragment newInstance() {
        LoginLoadingFragment frag = new LoginLoadingFragment();
        return frag;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.login_loading_fragment, container, false);

        spinner = (ImageView) rootView.findViewById(R.id.LoginLoadingFragment_spinner);
        Animation rotation = AnimationUtils.loadAnimation(getActivity(), R.anim.clockwise_rotate);
        rotation.setFillAfter(true);
        spinner.startAnimation(rotation);

        return rootView;
    }


}
