package com.jimchen.kanditag;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.facebook.Session;

/**
 * Created by Jim on 5/23/15.
 */
public class DataFragmentNavigationBar extends Fragment{

    public static final int SIGN_OUT_REQUEST = 0;
    public static final int RESULT_LOGGED_OUT = 9;

    private Context context;
    private View rootView;

    private ImageView toSettings, createNewPost;

    public static DataFragmentNavigationBar newInstance() {
        DataFragmentNavigationBar fragmentNavigationBar = new DataFragmentNavigationBar();
        return fragmentNavigationBar;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.data_fragment_navigation_bar, container, false);

        toSettings = (ImageView) rootView.findViewById(R.id.DataFragmentNavigationBar_toSetting);
        toSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                System.out.println("datafragmentnav - onclick");
                Intent toSettings = new Intent(getActivity(), SettingsActivity.class);
                startActivityForResult(toSettings, SIGN_OUT_REQUEST);
                getActivity().overridePendingTransition(R.anim.abc_slide_in_bottom, R.anim.abc_slide_out_bottom);
            }
        });

        createNewPost = (ImageView) rootView.findViewById(R.id.DataFragmentNavigationBar_newPost);
        createNewPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                System.out.println("createNewPost clicked");
                android.support.v4.app.FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                transaction.add(R.id.DataFragment_CreateNewPostContainer, NewPostOptionsFragment.newInstance()).setCustomAnimations(R.anim.abc_slide_in_bottom, R.anim.abc_slide_out_bottom).commit();
            }
        });

        return rootView;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SIGN_OUT_REQUEST) {
            if (resultCode == RESULT_LOGGED_OUT) {

                System.out.println("logged out");
                Session session = Session.getActiveSession();
                session.closeAndClearTokenInformation();
                Intent loginIntent = new Intent(getActivity(), LoginActivity.class);
                startActivityForResult(loginIntent, 1);
                getActivity().overridePendingTransition(R.anim.abc_slide_in_top, R.anim.abc_fade_out);
                getActivity().finish();
                getActivity().overridePendingTransition(R.anim.abc_slide_in_bottom, R.anim.abc_fade_out);
            }

            if (resultCode == getActivity().RESULT_CANCELED) {

            }

        }
    }
}
