package com.jimchen.kanditag;

import android.os.AsyncTask;

/**
 * Created by Jim on 3/1/15.
 */
public class SaveImageAsyncTask extends AsyncTask<Void, Void, Void> {

    //TODO make sure the database can be accessed from this thread
    KtDatabase myDatabase;

    byte[] imageData;

    public SaveImageAsyncTask (byte[] image_data) {
        imageData = image_data;
    }

    @Override
    protected void onPreExecute() {
        System.out.println("SaveImage.onPreExecute");
    }

    @Override
    protected Void doInBackground(Void... params) {
        return null;
    }

    @Override
    protected void onPostExecute(Void res) {
        System.out.println("Inside onPostExecute");
    }
}
