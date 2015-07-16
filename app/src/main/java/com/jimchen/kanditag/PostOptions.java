package com.jimchen.kanditag;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;

import java.util.ArrayList;


public class PostOptions extends Activity {

    private static final String TAG = "PostOptions";

    private static final int REQUEST_CODE_POST_OPTIONS = 5;

    private KtDatabase myDatabase;
    private String MY_KT_ID, MY_FB_ID, MY_USER_NAME;

    private SharedPreferences sharedPreferences;
    public static final String USER_PREFERENCES = "com.jimchen.kanditag.extra.PREFERENCES";
    public static final String USERNAME = "com.jimchen.kanditag.extra.USERNAME";
    public static final String FBID = "com.jimchen.kanditag.extra.FBID";
    public static final String KTID = "com.jimchen.kanditag.extra.KTID";

    private ListView listview;
    private PostOptionsListAdapter listAdapter;

    private GetAllUsersFromLocalDbAsyncTask getUsersTask;
    private ArrayList<KtUserObjectParcelable> ktUsers = new ArrayList<>();

    // list to hold selected users for snap
    private ArrayList<KtUserObjectParcelable> selectedUsers = new ArrayList<>();

    // button to post
    private ImageView postButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_options);

        // grab basic info
        myDatabase = new KtDatabase(this);
        sharedPreferences = this.getSharedPreferences(USER_PREFERENCES, Context.MODE_PRIVATE);
        MY_KT_ID = sharedPreferences.getString(KTID, "");
        MY_USER_NAME = sharedPreferences.getString(USERNAME, "");
        MY_FB_ID = sharedPreferences.getString(FBID, "");

        getUsers();
        connectListAdapter();

        // TODO grab list of users
        // have option to post to own, wall and snap to users

        // TODO set up done button for returning to ImagePreview
    }

    private void connectPostButton() {
        postButton = (ImageView) findViewById(R.id.PostOptions_PostButton);
        postButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO set up button to start service and upload tags to server
            }
        });
    }

    private void connectListAdapter() {

        listview = (ListView) findViewById(R.id.PostOptions_ListView);
        listAdapter = new PostOptionsListAdapter(PostOptions.this, R.layout.post_options_row, ktUsers);
        listview.setAdapter(listAdapter);

        // on item click add user into selected users list and
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Log.d(TAG, ktUsers.get(i).getUsername() + " selected");
                selectedUsers.add(ktUsers.get(i));
            }
        });
    }

    // async task to grab all users
    private void getUsers() {
        getUsersTask = new GetAllUsersFromLocalDbAsyncTask(PostOptions.this, new ReturnKtUserObjectParcelableArrayListAsyncResponse() {
            @Override
            public void processFinish(ArrayList<KtUserObjectParcelable> output) {
                ktUsers = output;
                listAdapter.notifyDataSetChanged();
                listview.invalidate();
            }
        });

        getUsersTask.execute();
    }

    private void finishedSelecting() {
        Intent intent = new Intent();
        setResult(RESULT_OK, intent);
        // ...  TODO send selected users list back into Image Preview
        finish();
    }

}
