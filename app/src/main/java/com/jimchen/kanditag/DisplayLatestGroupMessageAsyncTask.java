package com.jimchen.kanditag;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;

import java.util.ArrayList;

/**
 * Created by Jim on 3/17/15.
 */
public class DisplayLatestGroupMessageAsyncTask extends AsyncTask<ArrayList<KandiObject>, Void, ArrayList<GroupMessageItem>> {

    private Context context;
    private KtDatabase myDatabase;
    SharedPreferences sharedPreferences;
    private String MY_KT_ID, MY_FB_ID, MY_USER_NAME;
    public static final String MyPreferences = "MyPrefs";
    public static final String Name = "nameKey";
    public static final String FbId = "fbidKey";
    public static final String UserId = "userIdKey";
    private ReturnGroupMessageArrayListAsyncResponse delegate = null;

    private ArrayList<GroupMessageItem> groupMessageItemArrayList;
    private ArrayList<String> qrCodeArrayList;
    private ArrayList<KandiObject> kandiObjectArrayList;

    public DisplayLatestGroupMessageAsyncTask(Context context, ReturnGroupMessageArrayListAsyncResponse response) {
        this.delegate = response;
        this.context = context;
    }

    @Override
    protected ArrayList<GroupMessageItem> doInBackground(ArrayList<KandiObject>... params) {

        kandiObjectArrayList = params[0];

        for (int i = 0; i < kandiObjectArrayList.size(); i++) {
            qrCodeArrayList.add(kandiObjectArrayList.get(i).getQrCode());
        }

        for (int i = 0; i < qrCodeArrayList.size(); i++) {
            GroupMessageItem groupMessageItem = myDatabase.getLatestGroupMessage(qrCodeArrayList.get(i));
            if (groupMessageItem.getMessage() != null) {
                groupMessageItemArrayList.add(groupMessageItem);
            }
        }

        return groupMessageItemArrayList;
    }

    @Override
    protected void onPreExecute() {
        myDatabase = new KtDatabase(context);
        sharedPreferences = context.getSharedPreferences(MyPreferences, Context.MODE_PRIVATE);
        MY_KT_ID = sharedPreferences.getString(UserId, "");
        MY_USER_NAME = sharedPreferences.getString(Name, "");
        MY_FB_ID = sharedPreferences.getString(FbId, "");
        groupMessageItemArrayList = new ArrayList<>();
        qrCodeArrayList = new ArrayList<>();
        kandiObjectArrayList = new ArrayList<>();
    }

    @Override
    protected void onPostExecute(ArrayList<GroupMessageItem> list) {
        delegate.processFinish(list);
    }
}
