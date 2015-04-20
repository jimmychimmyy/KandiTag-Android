package com.jimchen.kanditag;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.widget.ArrayAdapter;

import java.util.ArrayList;

/**
 * Created by Jim on 4/20/15.
 */
public class GetAllGroupMessagesFromLocalDbAsyncTask extends AsyncTask<String, Void, ArrayList<MessageRowItem>> {

    private Context context;

    private KtDatabase myDatabase;
    private SharedPreferences sharedPreferences;
    private String MY_KT_ID, MY_FB_ID, MY_USER_NAME;
    public static final String MY_PREFERENCES = "MyPrefs";
    public static final String NAME = "nameKey";
    public static final String FBID = "fbidKey";
    public static final String KTID = "userIdKey";

    private ReturnMessageRowItemArrayListAsyncResponse delegate;

    private ArrayList<MessageRowItem> messageRowItems;

    public GetAllGroupMessagesFromLocalDbAsyncTask (Context context, ReturnMessageRowItemArrayListAsyncResponse response) {
        this.context = context;
        this.delegate = response;
    }

    @Override
    protected ArrayList<MessageRowItem> doInBackground(String... params) {
        String kandi_id = params[0];

        //retrieve all rows from local db kt_group_message where the kandi_id = param
        messageRowItems = myDatabase.getGroupMessagesFor(kandi_id);

        return messageRowItems;
    }

    @Override
    protected void onPreExecute() {
        myDatabase = new KtDatabase(context);
        sharedPreferences = context.getSharedPreferences(MY_PREFERENCES, Context.MODE_PRIVATE);
        MY_KT_ID = sharedPreferences.getString(KTID, "");
        MY_USER_NAME = sharedPreferences.getString(NAME, "");
        MY_FB_ID = sharedPreferences.getString(FBID, "");
        messageRowItems = new ArrayList<>();
    }

    @Override
    protected void onPostExecute(ArrayList<MessageRowItem> list) {
        delegate.processFinish(list);
    }
}

