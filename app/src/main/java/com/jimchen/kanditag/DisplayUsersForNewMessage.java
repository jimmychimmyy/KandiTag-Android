package com.jimchen.kanditag;

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

public class DisplayUsersForNewMessage extends Fragment {

    SharedPreferences sharedPreferences;

    public static final String MyPreferences = "MyPrefs";
    public static final String Name = "nameKey";
    public static final String FbId = "fbidKey";
    public static final String UserId = "userIdKey";
    private String MY_KT_ID, MY_FB_ID, MY_USER_NAME;

    private View rootView;

    //TODO this is the bundle that comes in
    private ArrayList<KtUserObjectParcelable> ktUserObjectParcelableArrayList;

    //TODO pass this into the adapter
    private ArrayList<KtUserObject> ktUserObjectArrayList;

    private ListView listView;

    public static DisplayUsersForNewMessage newInstance(ArrayList<KtUserObjectParcelable> list) {

        DisplayUsersForNewMessage m = new DisplayUsersForNewMessage();

        try {
            Bundle bundle = new Bundle();
            bundle.putParcelableArrayList("data", list);
            m.setArguments(bundle);
        } catch (NullPointerException nulle) {}

        return m;
    }

    public DisplayUsersForNewMessage() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.activity_display_for_new_message, container, false);

        sharedPreferences = getActivity().getSharedPreferences(MyPreferences, Context.MODE_PRIVATE);

        MY_KT_ID = sharedPreferences.getString(UserId, "");
        MY_USER_NAME = sharedPreferences.getString(Name, "");
        MY_FB_ID = sharedPreferences.getString(FbId, "");

        Bundle bundle = getArguments();
        ktUserObjectParcelableArrayList = bundle.getParcelableArrayList("data");

        KtUserObjectListAdapter ktUserObjectListAdapter = new KtUserObjectListAdapter(getActivity(), R.id.DisplayForNewMessage_ListView, ktUserObjectParcelableArrayList);

        listView = (ListView) rootView.findViewById(R.id.DisplayForNewMessage_ListView);
        //TODO set adapter
        listView.setAdapter(ktUserObjectListAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                System.out.println("newMessageList.onItemClick " + position);
                ktUserObjectParcelableArrayList.get(position);
                //TODO check if this fragment is still relevant
                /**
                Message message = (Message) getActivity();
                message.setInvisibleMessageTitleMessageButton();
                Intent openMessagingUI = new Intent(getActivity(), MessageDialogue.class);
                Bundle bundleParams = new Bundle();
                bundleParams.putString("kt_id", ktUserObjectParcelableArrayList.get(position).getKt_id());
                bundleParams.putString("fb_id", ktUserObjectParcelableArrayList.get(position).getFb_id());
                bundleParams.putString("user_name", ktUserObjectParcelableArrayList.get(position).getUsername());
                openMessagingUI.putExtras(bundleParams);
                //messageListView.setVisibility(View.GONE);
                //messageTitle.setVisibility(View.GONE);
                startActivity(openMessagingUI);

                 **/
                getActivity().getFragmentManager().beginTransaction().remove(DisplayUsersForNewMessage.this).commit();
                //TODO double check to make sure this method doesnt override the original name of the kanditag
            }
        });

        return rootView;
    }

}
