package com.kanditag.kanditag;

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

    private ArrayList<KandiGroupObjectParcelable> kandiGroupObjectArrayList;

    private ArrayList<KtUserObject> ktUserObjectArrayList;

    public GetAllGroupsFromLocalDbAsyncTask(Context context, ReturnKandiGroupObjectParcelableArrayList response) {
        this.context = context;
        this.delegate = response;
    }

    @Override
    protected ArrayList<KandiGroupObjectParcelable> doInBackground(ArrayList<KandiObject>... params) {

        ArrayList<KandiObject> tempKandiObjectList = params[0];

        for (int i = 0; i < tempKandiObjectList.size(); i++) {
            ktUserObjectArrayList = myDatabase.getKtUserObjectArrayForKandiGroupObjects(tempKandiObjectList.get(i).getQrCode());
            KandiGroupObjectParcelable kandiGroupObject = new KandiGroupObjectParcelable();
            kandiGroupObject.setQrCode(tempKandiObjectList.get(i).getQrCode());
            kandiGroupObject.setGroupName(tempKandiObjectList.get(i).getKandi_name());
            kandiGroupObject.setListOfUsers(ktUserObjectArrayList);
            kandiGroupObjectArrayList.add(kandiGroupObject);
        }

        return kandiGroupObjectArrayList;
    }

    @Override
    protected void onPreExecute() {
        myDatabase = new KtDatabase(context);
        kandiGroupObjectArrayList = new ArrayList<>();
        ktUserObjectArrayList = new ArrayList<>();
    }

    @Override
    protected void onPostExecute(ArrayList<KandiGroupObjectParcelable> list) {
        delegate.processFinish(list);
    }

    @Override
    protected void onProgressUpdate(Void... params) {

    }
}
