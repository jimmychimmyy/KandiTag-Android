package com.jimchen.kanditag;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;

import java.util.ArrayList;

/**
 * Created by Jim on 3/16/15.
 */
public class GetAllKandiFromLocalDbAsyncTask extends AsyncTask<Void, Void, ArrayList<KandiObject>> {

    private Context context;
    private KtDatabase myDatabase;
    SharedPreferences sharedPreferences;
    private String MY_KT_ID, MY_FB_ID, MY_USER_NAME;
    public static final String MyPreferences = "MyPrefs";
    public static final String Name = "nameKey";
    public static final String FbId = "fbidKey";
    public static final String UserId = "userIdKey";

    private ArrayList<KandiObject> kandiObjectArrayList;

    private ReturnKandiObjectArrayAsyncResponse delegate = null;

    public GetAllKandiFromLocalDbAsyncTask(Context context, ReturnKandiObjectArrayAsyncResponse response) {
        this.context = context;
        this.delegate = response;
    }

    @Override
    protected ArrayList<KandiObject> doInBackground(Void... params) {

        kandiObjectArrayList = myDatabase.getKandi();

        return kandiObjectArrayList;
    }

    @Override
    protected void onPreExecute() {
        myDatabase = new KtDatabase(context);
        sharedPreferences = context.getSharedPreferences(MyPreferences, Context.MODE_PRIVATE);
        MY_KT_ID = sharedPreferences.getString(UserId, "");
        MY_USER_NAME = sharedPreferences.getString(Name, "");
        MY_FB_ID = sharedPreferences.getString(FbId, "");
        kandiObjectArrayList = new ArrayList<>();
    }

    @Override
    protected void onPostExecute(ArrayList<KandiObject> list) {
        delegate.processFinish(list);
    }
}
