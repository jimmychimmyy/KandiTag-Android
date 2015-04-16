package com.jimchen.kanditag;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by Jim on 4/16/15.
 */
public class MeetFragment extends Fragment {

    private KtDatabase myDatabase;
    SharedPreferences sharedPreferences;
    public static final String MyPreferences = "MyPrefs";
    public static final String Name = "nameKey";
    public static final String FbId = "fbidKey";
    public static final String UserId = "userIdKey";
    public static final String NEW_MESSAGE = "NEW_MESSAGE";
    private String MY_KT_ID, MY_FB_ID, MY_USER_NAME;
    private Context context;

    private View rootView;

    // for the main activity to instantiate this fragment
    public static final MeetFragment newInstance() {
        MeetFragment frag = new MeetFragment();
        return frag;
    }

    //Fragment Manager Var
    private FragmentActivity myFragmentContext;

    //this is called when the fragment is first attached to an activity
    @Override
    public void onAttach(Activity activity) {
        myFragmentContext = (FragmentActivity) activity;
        super.onAttach(activity);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.meet_fragment, container, false);

        return rootView;
    }
}
