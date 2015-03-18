package com.kanditag.kanditag;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;

import java.util.ArrayList;

/**
 * Created by Jim on 3/9/15.
 */
public class DisplayLatestMessagesAsyncTask extends AsyncTask<ArrayList<String>, Void, ArrayList<MessageListItem>> {

    private Context context;
    private KtDatabase myDatabase;
    SharedPreferences sharedPreferences;
    private String MY_KT_ID, MY_FB_ID, MY_USER_NAME;
    public static final String MyPreferences = "MyPrefs";
    public static final String Name = "nameKey";
    public static final String FbId = "fbidKey";
    public static final String UserId = "userIdKey";

    public ReturnMessageListItemArrayAsyncResponse delegate = null;

    private ArrayList<MessageListItem> messageListItemArrayList;
    private ArrayList<String> fb_idsArrayList;

    public DisplayLatestMessagesAsyncTask(Context context, ReturnMessageListItemArrayAsyncResponse response) {
        this.context = context;
        this.delegate = response;
    }

    @Override
    protected ArrayList<MessageListItem> doInBackground(ArrayList<String>... params) {

        fb_idsArrayList = params[0];

        MessageListItem messageListItem = new MessageListItem();

        // cannot do two queries in the same async
        for (int i = 0; i < fb_idsArrayList.size(); i++) {
            if (!fb_idsArrayList.get(i).equals(MY_FB_ID)) {
                try {
                    messageListItem = myDatabase.getLatestMessageBetween(fb_idsArrayList.get(i), MY_FB_ID, MY_USER_NAME);
                    if (!messageListItem.getDescription().equals("")) {
                        messageListItemArrayList.add(messageListItem);
                    }
                } catch (NullPointerException nullEx) {}
            }
        }

        return messageListItemArrayList;
    }

    @Override
    protected void onPreExecute() {
        myDatabase = new KtDatabase(context);
        sharedPreferences = context.getSharedPreferences(MyPreferences, Context.MODE_PRIVATE);
        MY_KT_ID = sharedPreferences.getString(UserId, "");
        MY_USER_NAME = sharedPreferences.getString(Name, "");
        MY_FB_ID = sharedPreferences.getString(FbId, "");
        messageListItemArrayList = new ArrayList<>();
        fb_idsArrayList = new ArrayList<>();
    }

    @Override
    protected void onPostExecute(ArrayList<MessageListItem> list) {
        delegate.processFinish(list);
    }
}
