package com.jimchen.kanditag;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;

/**
 * Created by Jim on 6/11/15.
 */
public class KandiTagDisplay extends Fragment {

    private static final String TAG = "KandiTagDisplay";

    private View rootview;
    private ListView listview;
    private KandiTagDisplayListAdapter listviewAdapter;
    private ArrayList<KtUserObject> ktUserObjects;

    // actions
    public static final String ACTION_POPULATE_KANDITAG_DISPLAY = "com.jimchen.kanditag.action.POPULATE_KT_DISPLAY";

    // extras
    public static final String KT_USER_DATA = "com.jimchen.kanditag.extra.KT_USER";
    public static final String DONE = "com.jimchen.kanditag.extra.DONE";


    public static KandiTagDisplay newInstance() {
        KandiTagDisplay f = new KandiTagDisplay();
        return f;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootview = inflater.inflate(R.layout.kanditag_display, container, false);

        ktUserObjects = new ArrayList<>();

        listview = (ListView) rootview.findViewById(R.id.KandiTagDisplay_ListView);
        listviewAdapter = new KandiTagDisplayListAdapter(getActivity(), R.layout.kanditag_display_row, ktUserObjects);
        listview.setAdapter(listviewAdapter);

        IntentFilter filter = new IntentFilter(ACTION_POPULATE_KANDITAG_DISPLAY);
        ResponseReceiver receiver = new ResponseReceiver();
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(receiver, filter);

        return rootview;
    }

    private class ResponseReceiver extends BroadcastReceiver {

        private ResponseReceiver() {

        }

        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                String data = intent.getStringExtra(KT_USER_DATA);

                GsonBuilder gsonBuilder = new GsonBuilder();
                gsonBuilder.registerTypeAdapter(ResponseResults.class, new ResponseResultsDeserializer());
                Gson gson = gsonBuilder.create();
                ResponseResults results = gson.fromJson(data, ResponseResults.class);

                // build ktUsers based off of the json messages that come back
                KtUserObject user = new KtUserObject();
                user.setKt_id(results.getKt_id());
                user.setUsername(results.getUsername());
                user.setKandiId(results.getKandi_id());
                user.setPlacement(results.getPlacement());

                boolean exists = false;
                for (int i = 0; i < ktUserObjects.size(); i++) {
                    if (user.equals(ktUserObjects.get(i))) {
                        exists = true;
                    }
                }

                if (!exists) {
                    ktUserObjects.add(user);
                    listviewAdapter.notifyDataSetChanged();
                    listview.invalidate();
                }

                Log.d(TAG, "received something");

            } catch (NullPointerException e) {

            }
        }
    }
}
