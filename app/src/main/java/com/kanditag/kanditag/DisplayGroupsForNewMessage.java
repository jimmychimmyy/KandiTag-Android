package com.kanditag.kanditag;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;

/**
 * Created by Jim on 3/16/15.
 */
public class DisplayGroupsForNewMessage extends Fragment {

    SharedPreferences sharedPreferences;

    public static final String MyPreferences = "MyPrefs";
    public static final String Name = "nameKey";
    public static final String FbId = "fbidKey";
    public static final String UserId = "userIdKey";
    private String MY_KT_ID, MY_FB_ID, MY_USER_NAME;

    private View rootView;

    private ListView listView;

    private ArrayList<KandiGroupObjectParcelable> kandiGroupObjectParcelableArrayList;

    public static DisplayGroupsForNewMessage newInstance(ArrayList<KandiGroupObjectParcelable> list) {

        DisplayGroupsForNewMessage m = new DisplayGroupsForNewMessage();

        try {
            Bundle bundle = new Bundle();
            bundle.putParcelableArrayList("data", list);
            m.setArguments(bundle);
        } catch (NullPointerException nulle) {}

        return m;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.activity_display_for_new_message, container, false);

        sharedPreferences = getActivity().getSharedPreferences(MyPreferences, Context.MODE_PRIVATE);

        MY_KT_ID = sharedPreferences.getString(UserId, "");
        MY_USER_NAME = sharedPreferences.getString(Name, "");
        MY_FB_ID = sharedPreferences.getString(FbId, "");

        Bundle bundle = getArguments();
        kandiGroupObjectParcelableArrayList = bundle.getParcelableArrayList("data");

        KandiGroupObjectListAdapter kandiGroupObjectListAdapter = new KandiGroupObjectListAdapter(getActivity(), R.id.DisplayForNewMessage_ListView, kandiGroupObjectParcelableArrayList);

        listView = (ListView) rootView.findViewById(R.id.DisplayForNewMessage_ListView);
        //TODO set adapter
        listView.setAdapter(kandiGroupObjectListAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                System.out.println("newMessageList.onItemClick " + position);
                kandiGroupObjectParcelableArrayList.get(position);
                Message message = (Message) getActivity();
                message.setInvisibleMessageTitleMessageButton();
                Intent openMessageDialogue = new Intent(getActivity(), GroupMessageDialogue.class);
                Bundle bundleParams = new Bundle();
                bundleParams.putString("qrcode", kandiGroupObjectParcelableArrayList.get(position).getQrCode());
                bundleParams.putString("kandi_name", kandiGroupObjectParcelableArrayList.get(position).getGroupName());
                openMessageDialogue.putExtras(bundleParams);
                //messageListView.setVisibility(View.GONE);
                //messageTitle.setVisibility(View.GONE);
                startActivity(openMessageDialogue);
                getActivity().getFragmentManager().beginTransaction().remove(DisplayGroupsForNewMessage.this).commit();
                //TODO double check to make sure this method doesnt override the original name of the kanditag
            }
        });

        return rootView;
    }
}
