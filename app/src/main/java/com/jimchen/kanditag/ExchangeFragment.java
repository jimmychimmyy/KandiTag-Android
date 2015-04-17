package com.jimchen.kanditag;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ListView;


public class ExchangeFragment extends Fragment{

    private Context context;

    private KtDatabase myDatabase;
    SharedPreferences sharedPreferences;
    public static final String MyPreferences = "MyPrefs";
    public static final String Name = "nameKey";
    public static final String FbId = "fbidKey";
    public static final String UserId = "userIdKey";
    public static final String NEW_MESSAGE = "NEW_MESSAGE";

    private String MY_KT_ID, MY_FB_ID, MY_USER_NAME;

    private View rootView;

    private ListView myListView;

    //exit button to return to main
    private ImageView exitButton;

    public static final ExchangeFragment newInstance() {
        ExchangeFragment fragment = new ExchangeFragment();
        return fragment;
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.exchange_fragment, container, false);

        myListView = (ListView) rootView.findViewById(R.id.ExchangeFragment_ListView);

        exitButton = (ImageView) rootView.findViewById(R.id.ExchangeFragment_ExitButton);
        exitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //remove fragment from view and from stack
                getActivity().getSupportFragmentManager().beginTransaction().setCustomAnimations(R.anim.right_slide_in, R.anim.right_slide_out).remove(ExchangeFragment.this).commit();
                FragmentManager fm = getActivity().getSupportFragmentManager();
                fm.popBackStack();
                //set window back to fullscreen upon entering main
                getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            }
        });

        return rootView;
    }

}
