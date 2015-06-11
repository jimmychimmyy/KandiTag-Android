package com.jimchen.kanditag;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

/**
 * Created by Jim on 5/26/15.
 */
public class NewPostOptionsFragment extends Fragment {

    private String TAG = "NewPostOptionsFragment";

    private Context context;
    private View rootView;

    private Button close;

    private ImageView newImagePost, newVideoPost, newTicketPost;

    public static NewPostOptionsFragment newInstance() {
        NewPostOptionsFragment fragment = new NewPostOptionsFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.new_post_options_fragment, container, false);

        close = (Button) rootView.findViewById(R.id.NewPostOptionsFragment_close);
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "need to remove self");
                android.support.v4.app.FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                transaction.remove(NewPostOptionsFragment.this).setCustomAnimations(R.anim.abc_slide_in_bottom, R.anim.abc_slide_out_bottom).commit();
            }
        });

        newImagePost = (ImageView) rootView.findViewById(R.id.NewPostOptionsFragment_newImagePost);
        newVideoPost = (ImageView) rootView.findViewById(R.id.NewPostOptionsFragment_newVideoPost);
        newTicketPost = (ImageView) rootView.findViewById(R.id.NewPostOptionsFragment_newTicketPost);

        newImagePost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "newImagePost on click");
            }
        });

        newVideoPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "newVideoPost on click");
            }
        });

        newTicketPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "newTicketPost on click");
                Intent postNewTicket = new Intent(getActivity(), PostNewExchangeActivity.class);
                startActivity(postNewTicket);
                android.support.v4.app.FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                transaction.remove(NewPostOptionsFragment.this).setCustomAnimations(R.anim.abc_slide_in_bottom, R.anim.abc_slide_out_bottom).commit();
            }
        });

        return rootView;
    }
}
