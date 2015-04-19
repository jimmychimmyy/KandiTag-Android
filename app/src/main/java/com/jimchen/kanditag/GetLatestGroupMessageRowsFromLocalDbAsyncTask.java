package com.jimchen.kanditag;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;

import java.util.ArrayList;

/**
 * Created by Jim on 3/17/15.
 */
public class GetLatestGroupMessageRowsFromLocalDbAsyncTask extends AsyncTask<ArrayList<KandiObject>, Void, ArrayList<MessageRowItem>> {

    private Context context;
    private KtDatabase myDatabase;
    SharedPreferences sharedPreferences;
    private String MY_KT_ID, MY_FB_ID, MY_USER_NAME;
    public static final String MyPreferences = "MyPrefs";
    public static final String Name = "nameKey";
    public static final String FbId = "fbidKey";
    public static final String UserId = "userIdKey";

    private ReturnMessageRowItemArrayListAsyncResponse delegate;

    //array of message row items to return
    private ArrayList<MessageRowItem> messageRowItems;

    //kandi object list param to get qr codes
    private ArrayList<KandiObject> kandiObjectList;


    public GetLatestGroupMessageRowsFromLocalDbAsyncTask(Context context, ReturnMessageRowItemArrayListAsyncResponse response) {
        this.delegate = response;
        this.context = context;
    }

    @Override
    protected ArrayList<MessageRowItem> doInBackground(ArrayList<KandiObject>... params) {

        kandiObjectList = params[0];

        for (int i = 0; i < kandiObjectList.size(); i++) {
            MessageRowItem rowItem = myDatabase.getMessageRowItemForGroup(kandiObjectList.get(i).getKandi_id());
            if (rowItem.getMessageText() != null) {
                messageRowItems.add(rowItem);
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

        kandiObjectList = new ArrayList<>();
        messageRowItems = new ArrayList<>();
    }

    @Override
    protected void onPostExecute(ArrayList<MessageRowItem> list) {
        delegate.processFinish(list);
    }
}
