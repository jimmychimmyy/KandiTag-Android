package com.jimchen.kanditag;

import android.content.Context;
import android.os.AsyncTask;

import java.util.ArrayList;

/**
 * Created by Jim on 3/6/15.
 */
public class GetAllGroupsFromLocalDbAsyncTask extends AsyncTask<ArrayList<KandiObject>, Void, ArrayList<KandiGroupObjectParcelable>> {

    private Context context;

    private KtDatabase myDatabase;

    public ReturnKandiGroupObjectParcelableArrayList delegate = null;

    private ArrayList<KandiGroupObjectParcelable> kandiGroupObjectList;

    private ArrayList<KtUserObjectParcelable> ktUserObjectList;

    private ArrayList<KandiObject> kandiObjectList;

    public GetAllGroupsFromLocalDbAsyncTask(Context context, ReturnKandiGroupObjectParcelableArrayList response) {
        this.context = context;
        this.delegate = response;
    }

    @Override
    protected ArrayList<KandiGroupObjectParcelable> doInBackground(ArrayList<KandiObject>... params) {

        try {

            kandiObjectList = params[0];

            for (int i = 0; i < kandiObjectList.size(); i++) {
                ktUserObjectList = myDatabase.getAllKtUsersFromGroup(kandiObjectList.get(i).getKandi_id());
                KandiGroupObjectParcelable kandiGroupObject = new KandiGroupObjectParcelable();
                kandiGroupObject.setKandi_id(kandiObjectList.get(i).getKandi_id());
                kandiGroupObject.setKandi_name(kandiObjectList.get(i).getKandi_name());
                kandiGroupObject.setListOfUsers(ktUserObjectList);
                kandiGroupObjectList.add(kandiGroupObject);
            }
        } catch (ArrayIndexOutOfBoundsException ex) {
            ex.printStackTrace();
        }

        return kandiGroupObjectList;
    }

    @Override
    protected void onPreExecute() {
        myDatabase = new KtDatabase(context);
        kandiGroupObjectList = new ArrayList<>();
        ktUserObjectList = new ArrayList<>();
        kandiObjectList = new ArrayList<>();
    }

    @Override
    protected void onPostExecute(ArrayList<KandiGroupObjectParcelable> list) {
        delegate.processFinish(list);
    }

    @Override
    protected void onProgressUpdate(Void... params) {

    }
}
