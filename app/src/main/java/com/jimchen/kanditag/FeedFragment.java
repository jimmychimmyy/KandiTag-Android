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
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;


public class FeedFragment extends Fragment {

    KtDatabase myDatabase;
    SharedPreferences sharedPreferences;
    public static final String MyPreferences = "MyPrefs";
    public static final String Name = "nameKey";
    public static final String FbId = "fbidKey";
    public static final String UserId = "userIdKey";
    private String MY_KT_ID, MY_FB_ID, MY_USER_NAME;

    private Context context;

    private int CONTAINER_TYPE;

    View rootView;
    GridView kandiGridView;
    TextView kandiTitleTextView;

    // exit button to return to main
    private ImageView exitButton;

    private ArrayList<FriendsGridItem> followerArray, followingArray;
    private FriendsGridViewAdapter followerGridViewAdapter, followingGridViewAdapter;

    public static final FeedFragment newInstance() {
        FeedFragment feedFragment = new FeedFragment();
        return feedFragment;
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.feed_fragment, container, false);

        this.context = getActivity();

        myDatabase = new KtDatabase(context);
        sharedPreferences = getActivity().getSharedPreferences(MyPreferences, Context.MODE_PRIVATE);
        MY_KT_ID = sharedPreferences.getString(UserId, "");
        MY_USER_NAME = sharedPreferences.getString(Name, "");
        MY_FB_ID = sharedPreferences.getString(FbId, "");

        rootView.setId(CONTAINER_TYPE);

        kandiGridView = (GridView) rootView.findViewById(R.id.kandiGridView);

        exitButton = (ImageView) rootView.findViewById(R.id.FeedFragment_ExitButton);
        exitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().getSupportFragmentManager().beginTransaction().setCustomAnimations(R.anim.right_slide_in, R.anim.right_slide_out).remove(FeedFragment.this).commit();
                FragmentManager fm = getActivity().getSupportFragmentManager();
                fm.popBackStack();
                getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            }
        });

        /**

        kandiTitleTextView = (TextView) rootView.findViewById(R.id.kandiTagTextView);
        kandiTitleTextView.setTextSize(40);
        Typeface typeface = Typeface.createFromAsset(getActivity().getAssets(),"fonts/stalemate_regular.ttf");
        kandiTitleTextView.setTypeface(typeface);

         **/

        /**
        followerGridViewAdapter = new FollowGridViewAdapter(getActivity(), R.id.list_item, followerArray);
        followingGridViewAdapter = new FollowGridViewAdapter(getActivity(), R.id.list_item, followingArray);

        if (CONTAINER_TYPE == KandiTagConstants.FRAGMENT_FOLLOWERS) {
            kandiGridView.setAdapter(followerGridViewAdapter);
            kandiTitleTextView.setText("Followers");
        }

        if (CONTAINER_TYPE == KandiTagConstants.FRAGMENT_FOLLOWING) {
            kandiGridView.setAdapter(followingGridViewAdapter);
            kandiTitleTextView.setText("Following");
        }

        if (CONTAINER_TYPE == KandiTagConstants.FRAGMENT_FEED) {
            kandiGridView.setAdapter(null);
            kandiTitleTextView.setText("Feed");
        }

         **/

        return rootView;
    }
}
