package com.jimchen.kanditag;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;

import java.util.ArrayList;

/**
 * Created by Jim on 3/6/15.
 */

//this task returns a list of all users in the local database (max one occurrence of each user in the returned list)
public class GetAllUsersFromLocalDbAsyncTask extends AsyncTask<Void, Void, ArrayList<KtUserObjectParcelable>> {

    private Context context;
    public ReturnKtUserObjectParcelableArrayListAsyncResponse delegate = null;

    private KtDatabase myDatabase;

    private ArrayList<KtUserObjectParcelable> ktUsersList;

    SharedPreferences sharedPreferences;
    private String MY_KT_ID, MY_FB_ID, MY_USER_NAME;

    public static final String MyPreferences = "MyPrefs";
    public static final String Name = "nameKey";
    public static final String FbId = "fbidKey";
    public static final String UserId = "userIdKey";


    public GetAllUsersFromLocalDbAsyncTask(Context context, ReturnKtUserObjectParcelableArrayListAsyncResponse response) {
        this.context = context;
        this.delegate = response;
    }

    @Override
    protected ArrayList<KtUserObjectParcelable> doInBackground(Void... params) {

        ktUsersList = myDatabase.getAllKtUsers();
        for (int i = 0; i < ktUsersList.size();) {
            if (ktUsersList.get(i).getKt_id().equals(MY_KT_ID)) {
                ktUsersList.remove(i);
            } else {
                i++;
            }
        }
        return ktUsersList;
    }

    @Override
    protected void onPreExecute() {
        myDatabase = new KtDatabase(context);
        sharedPreferences = context.getSharedPreferences(MyPreferences, Context.MODE_PRIVATE);
        MY_KT_ID = sharedPreferences.getString(UserId, "");
        MY_USER_NAME = sharedPreferences.getString(Name, "");
        MY_FB_ID = sharedPreferences.getString(FbId, "");

        ktUsersList = new ArrayList<>();
    }

    @Override
    protected void onPostExecute(ArrayList<KtUserObjectParcelable> list) {
        delegate.processFinish(list);
    }

    @Override
    protected void onProgressUpdate(Void... params) {

    }
}
