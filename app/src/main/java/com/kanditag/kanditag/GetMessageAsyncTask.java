package com.kanditag.kanditag;

import android.os.AsyncTask;

import java.util.ArrayList;

/**
 * Created by Jim on 3/2/15.
 */
public class GetMessageAsyncTask extends AsyncTask<String, Void, ArrayList<ConversationListItem>> {

    private ArrayList<ConversationListItem> conversationListItems;

    @Override
    protected ArrayList<ConversationListItem> doInBackground(String... params) {
        String userTableId = params[0];

        return conversationListItems;
    }

    @Override
    protected void onPreExecute() {
        conversationListItems = new ArrayList<>();
    }

    @Override
    protected void onPostExecute(ArrayList<ConversationListItem> list) {

    }

    @Override
    protected void onProgressUpdate(Void... params) {

    }
}
