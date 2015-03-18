package com.kanditag.kanditag;

import android.content.Context;
import android.os.AsyncTask;

/**
 * Created by Jim on 3/9/15.
 */
public class GetSingleUserForMessageAsyncTask extends AsyncTask<String, Void, KtUserObject> {

    private Context context;
    public ReturnKtUserObjectAsyncResponse delegate = null;

    private KtDatabase myDatabase;

    private KtUserObject ktUserObject;

    public GetSingleUserForMessageAsyncTask(Context context, ReturnKtUserObjectAsyncResponse response) {
        this.context = context;
        this.delegate = response;
    }

    @Override
    protected KtUserObject doInBackground(String... params) {

        String fb_id = params[0];

        ktUserObject = myDatabase.getSingleUserFromKtUser(fb_id);

        return ktUserObject;
    }

    @Override
    protected void onPreExecute() {
        myDatabase = new KtDatabase(context);
    }

    @Override
    protected void onPostExecute(KtUserObject output) {
        delegate.processFinish(output);
    }

}
