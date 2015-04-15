package com.jimchen.kanditag;

import android.os.AsyncTask;

import java.util.ArrayList;

/**
 * Created by Jim on 3/1/15.
 */
//TODO probably dont need this class
public class GetAllKandi extends AsyncTask<ArrayList<String>, Void, String> {

    private ArrayList<String> listOfKandiQrs = new ArrayList<>();

    @Override
    public String doInBackground(ArrayList<String>... params) {
        listOfKandiQrs = params[0];
        return null;
    }

    @Override
    public void onPreExecute() {

    }

    @Override
    protected void onPostExecute(String params) {

    }


}
