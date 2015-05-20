package com.jimchen.kanditag;

import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by Jim on 5/13/15.
 */
public class ProfileFriendsFragment extends android.support.v4.app.Fragment {

    private View rootView;

    public static final ProfileFriendsFragment newInstance() {
        ProfileFriendsFragment fragment = new ProfileFriendsFragment();
        return fragment;
    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            String message = intent.getStringExtra("Message");
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.profile_friends_fragment, container, false);


        return rootView;
    }

}
