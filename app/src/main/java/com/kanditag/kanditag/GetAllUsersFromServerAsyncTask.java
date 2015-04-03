package com.kanditag.kanditag;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.StrictMode;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;

import java.util.ArrayList;

/**
 * Created by Jim on 3/30/15.
 */
public class GetAllUsersFromServerAsyncTask extends AsyncTask<Void, Void, ArrayList<KtUserObjectParcelable>> {

    private Context context;
    public ReturnKtUserObjectParcelableArrayListAsyncResponse delegate = null;

    private KtDatabase myDatabase;
    SharedPreferences sharedPreferences;
    private String MY_KT_ID, MY_FB_ID, MY_USER_NAME;
    public static final String MyPreferences = "MyPrefs";
    public static final String Name = "nameKey";
    public static final String FbId = "fbidKey";
    public static final String UserId = "userIdKey";

    private ArrayList<KtUserObjectParcelable> ktUserObjectArrayList;

    public GetAllUsersFromServerAsyncTask(Context context, ReturnKtUserObjectParcelableArrayListAsyncResponse response) {
        this.context = context;
        this.delegate = response;
    }

    @Override
    protected ArrayList<KtUserObjectParcelable> doInBackground(Void... params) {

        //do separate calls to the server here
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        //given a ktid/fbid i am trying to find every occurrence of that id in kt_ownership, return results
        //given results.getQrCode i am trying to find
        String Url = "http://kandi.nodejitsu.com/kt_users_finduser";

        HttpClient client = new DefaultHttpClient();

        HttpPost post = new HttpPost(Url);





        return ktUserObjectArrayList;
    }

    @Override
    protected void onPreExecute() {
        myDatabase = new KtDatabase(context);
        sharedPreferences = context.getSharedPreferences(MyPreferences, Context.MODE_PRIVATE);
        MY_KT_ID = sharedPreferences.getString(UserId, "");
        MY_USER_NAME = sharedPreferences.getString(Name, "");
        MY_FB_ID = sharedPreferences.getString(FbId, "");
        ktUserObjectArrayList = new ArrayList<>();
    }

    @Override
    protected void onPostExecute(ArrayList<KtUserObjectParcelable> list) {
        delegate.processFinish(list);
    }

    @Override
    protected void onProgressUpdate(Void... params) {

    }
}

