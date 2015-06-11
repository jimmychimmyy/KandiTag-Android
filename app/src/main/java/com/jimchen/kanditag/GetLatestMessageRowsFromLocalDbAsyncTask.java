package com.jimchen.kanditag;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;

import java.util.ArrayList;

/**
 * Created by Jim on 3/9/15.
 */
public class GetLatestMessageRowsFromLocalDbAsyncTask extends AsyncTask<ArrayList<String>, Void, ArrayList<MessageRowItem>> {

    private Context context;
    private KtDatabase myDatabase;
    private SharedPreferences sharedPreferences;
    private String MY_KT_ID, MY_FB_ID, MY_USER_NAME;
    public static final String USER_PREFERENCES = "com.jimchen.kanditag.extra.PREFERENCES";
    public static final String USERNAME = "com.jimchen.kanditag.extra.USERNAME";
    public static final String FBID = "com.jimchen.kanditag.extra.FBID";
    public static final String KTID = "com.jimchen.kanditag.extra.KTID";

    public ReturnMessageRowItemArrayListAsyncResponse delegate;

    //array for output MessageRowItems
    private ArrayList<MessageRowItem> messageRowItems;
    private ArrayList<String> kt_idList;

    public GetLatestMessageRowsFromLocalDbAsyncTask(Context context, ReturnMessageRowItemArrayListAsyncResponse response) {
        this.context = context;
        this.delegate = response;
    }

    @Override
    protected ArrayList<MessageRowItem> doInBackground(ArrayList<String>... params) {

        kt_idList = params[0];

        // cannot do two queries in the same async
        for (int i = 0; i < kt_idList.size(); i++) {
            if (!kt_idList.get(i).equals(MY_KT_ID)) {
                try {
                    MessageRowItem rowItem;
                    rowItem= myDatabase.getMessageRowItem(kt_idList.get(i), MY_KT_ID, MY_USER_NAME);
                    if (!rowItem.getMessageContent().equals("")) {
                        messageRowItems.add(rowItem);
                    }
                } catch (NullPointerException nullEx) {}
            }
        }

        return messageRowItems;
    }

    @Override
    protected void onPreExecute() {
        myDatabase = new KtDatabase(context);
        sharedPreferences = context.getSharedPreferences(USER_PREFERENCES, Context.MODE_PRIVATE);
        MY_KT_ID = sharedPreferences.getString(KTID, "");
        MY_USER_NAME = sharedPreferences.getString(USERNAME, "");
        MY_FB_ID = sharedPreferences.getString(FBID, "");
        messageRowItems = new ArrayList<>();
        kt_idList = new ArrayList<>();
    }

    @Override
    protected void onPostExecute(ArrayList<MessageRowItem> list) {
        delegate.processFinish(list);
    }
}
