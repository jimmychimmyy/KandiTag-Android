package com.jimchen.kanditag;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;


public class FeedFragment extends Fragment {

    public static final String ACTION_DOWNLOAD_FEED = "com.jimchen.kanditag.action.DOWNLOAD_FEED";
    public static final String IMAGE_DATA = "com.jimchen.kanditag.data.IMAGE";
    public static final String FILE_ID = "com.jimchen.kanditag.data.FILEID";
    public static final String ACTION_ADD_NEW_POST = "com.jimchen.kanditag.action.ADD_NEW_POST";

    private ArrayList<String> file_ids = new ArrayList<>();


    private String TAG = "FeedFragment";

    KtDatabase myDatabase;
    // Shared Preferences
    private SharedPreferences sharedPreferences;
    public static final String USER_PREFERENCES = "com.jimchen.kanditag.extra.PREFERENCES";
    public static final String USERNAME = "com.jimchen.kanditag.extra.USERNAME";
    public static final String FBID = "com.jimchen.kanditag.extra.FBID";
    public static final String KTID = "com.jimchen.kanditag.extra.KTID";
    public static final String USER_PROFILE_IMAGE = "com.jimchen.kanditag.extra.USER_PROFILE_IMAGE";
    private String MY_KT_ID, MY_FB_ID, MY_USER_NAME;

    private Context context;

    // socket io
    private static com.github.nkzawa.socketio.client.Socket socket;
    private final String HOST = "http://kandi.jit.su/";
    private final int portNumber = 3000;

    //private int CONTAINER_TYPE;

    // root view
    View rootView;

    // list view
    private ListView feedListView;
    private FeedListViewAdapter feedListViewAdapter;

    // exit button to return to main
    private ImageView exitButton;

    private ArrayList<FriendsGridItem> followerArray, followingArray;
    private FriendsGridViewAdapter followerGridViewAdapter, followingGridViewAdapter;


    // Async Tasks
    private GetAllUsersFromLocalDbAsyncTask getAllUsersFromLocalDbAsyncTask;

    // downloaded images TODO will need to sort them by timestamp
    private ArrayList<byte[]> images = new ArrayList<>();
    private ArrayList<KtMedia> media = new ArrayList<>();
    //private ArrayList<String> filenames = new ArrayList<>();

    private ImageView openCamera;


    public static final FeedFragment newInstance() {
        FeedFragment feedFragment = new FeedFragment();
        return feedFragment;
    }

    @Override
    public void onResume() {
        super.onResume();
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
        sharedPreferences = getActivity().getSharedPreferences(USER_PREFERENCES, Context.MODE_PRIVATE);
        MY_KT_ID = sharedPreferences.getString(KTID, "");
        MY_USER_NAME = sharedPreferences.getString(USERNAME, "");
        MY_FB_ID = sharedPreferences.getString(FBID, "");

        //rootView.setId(CONTAINER_TYPE);

        // TODO will need to change images to media

        feedListView = (ListView) rootView.findViewById(R.id.FeedFragment_ListView);
        feedListViewAdapter = new FeedListViewAdapter(getActivity(), R.layout.feed_list_item, file_ids);
        feedListView.setAdapter(feedListViewAdapter);
        feedListView.setRecyclerListener(feedListViewAdapter.mRecycleListener);
        feedListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int scrollSt) {

                // pause disk cache access to ensure smoother scrolling
                if (scrollSt == SCROLL_STATE_FLING) {
                    // TODO need to stop images from loading at this point

                }
            }

            @Override
            public void onScroll(AbsListView absListView, int i, int i2, int i3) {

            }
        });

        /**

        openCamera = (ImageView) rootView.findViewById(R.id.FeedFragment_OpenCamera);
        openCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), CameraPreview.class);
                startActivity(intent);
                getActivity().overridePendingTransition(R.anim.abc_slide_in_bottom, R.anim.abc_slide_out_bottom);
            }
        }); **/


        IntentFilter filter = new IntentFilter(ACTION_DOWNLOAD_FEED);
        IntentFilter addNewFilter = new IntentFilter(ACTION_ADD_NEW_POST);
        ResponseReceiver receiver = new ResponseReceiver();
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(receiver, filter);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(receiver, addNewFilter);

        return rootView;
    }

    private class ResponseReceiver extends BroadcastReceiver {

        private ResponseReceiver() {

        }

        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent.getAction().equals(ACTION_DOWNLOAD_FEED)) {
                String file_id = intent.getStringExtra(FILE_ID);

                file_ids.add(file_id);
                feedListViewAdapter.notifyDataSetChanged();
                feedListView.invalidate();
            }

            /**
            // this one gets the filename
            if (intent.getAction().equals(ACTION_DOWNLOAD_FEED)) {
                KtMedia file = intent.getParcelableExtra(IMAGE_DATA);
                //String filename = intent.getStringExtra(IMAGE_DATA);
                //media.add(file);

                // TODO sort the Ktmedia files by date before adding into arraylist

                media.add(file);
                Collections.sort(media);
                //filenames.add(filename);
                feedListViewAdapter.notifyDataSetChanged();
                feedListView.invalidate();
                Log.d(TAG, "received a feed");
            } else if (intent.getAction().equals(ACTION_ADD_NEW_POST)) {
                Log.d(TAG, "addNew");
            } **/
        }
    }

    // TODO will connect the socket inside this fragment

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        // Make sure that we are currently visible
        if (this.isVisible()) {
            // If we are becoming invisible, then...
            if (!isVisibleToUser) {
            }
        }
    }
}
