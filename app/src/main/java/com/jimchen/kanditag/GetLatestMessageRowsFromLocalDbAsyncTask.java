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
    SharedPreferences sharedPreferences;
    private String MY_KT_ID, MY_FB_ID, MY_USER_NAME;
    public static final String MyPreferences = "MyPrefs";
    public static final String Name = "nameKey";
    public static final String FbId = "fbidKey";
    public static final String UserId = "userIdKey";

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
                    if (!rowItem.getMessageText().equals("")) {
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
        sharedPreferences = context.getSharedPreferences(MyPreferences, Context.MODE_PRIVATE);
        MY_KT_ID = sharedPreferences.getString(UserId, "");
        MY_USER_NAME = sharedPreferences.getString(Name, "");
        MY_FB_ID = sharedPreferences.getString(FbId, "");
        messageRowItems = new ArrayList<>();
        kt_idList = new ArrayList<>();
    }

    @Override
    protected void onPostExecute(ArrayList<MessageRowItem> list) {
        delegate.processFinish(list);
    }
}
