package com.jimchen.kanditag;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.util.ArrayList;


public class GroupFragment extends Fragment {

    private Context context;
    private KtDatabase myDatabase;
    SharedPreferences sharedPreferences;
    public static final String MyPreferences = "MyPrefs";
    public static final String Name = "nameKey";
    public static final String FbId = "fbidKey";
    public static final String UserId = "userIdKey";
    public static final String NEW_MESSAGE = "NEW_MESSAGE";
    private String MY_KT_ID, MY_FB_ID, MY_USER_NAME;

    private View rootView;

    private ListView myListView;
    private UserAndGroupsListAdapter myListAdapter;
    private ArrayList<UserAndGroupListItem> list;

    public static final GroupFragment newInstance() {
        GroupFragment fragment = new GroupFragment();
        return fragment;
    }

    private ArrayList<KtUserObjectParcelable> listOfUsers = new ArrayList<>();
    private ArrayList<KandiGroupObjectParcelable> listOfGroups = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.group_fragment, container, false);

        this.context = getActivity();
        myDatabase = new KtDatabase(getActivity());
        sharedPreferences = getActivity().getSharedPreferences(MyPreferences, Context.MODE_PRIVATE);
        MY_KT_ID = sharedPreferences.getString(UserId, "");
        MY_USER_NAME = sharedPreferences.getString(Name, "");
        MY_FB_ID = sharedPreferences.getString(FbId, "");

        findViews();
        getUsersAndGroupsFromLocalDb();

        return rootView;
    }

    private void findViews() {
        myListView = (ListView) rootView.findViewById(R.id.GroupFragment_ListView);
    }

    private void getUsersAndGroupsFromLocalDb() {
        //get a list of all users from local db
        GetAllUsersFromLocalDbAsyncTask getAllUsersFromLocalDbAsyncTask = new GetAllUsersFromLocalDbAsyncTask(getActivity(), new ReturnKtUserObjectParcelableArrayListAsyncResponse() {
            @Override
            public void processFinish(ArrayList<KtUserObjectParcelable> output) {
                System.out.println("GroupFragment.getAllUsersFromLocalDbAsyncTask.processFinish.output.size() = " + output.size());
                listOfUsers = output;

                for (int i = 0; i < listOfUsers.size(); i++) {
                    UserAndGroupListItem item = new UserAndGroupListItem();
                }
            }
        });

        //get a list of all groups from local db
        GetAllGroupsFromLocalDbAsyncTask getAllGroupsFromLocalDbAsyncTask = new GetAllGroupsFromLocalDbAsyncTask(getActivity(), new ReturnKandiGroupObjectParcelableArrayList() {
            @Override
            public void processFinish(ArrayList<KandiGroupObjectParcelable> output) {
                System.out.println("GroupFragment.getAllGroupsFromLocalDbAsyncTask.processFinish.output.size() = " + output.size());
                listOfGroups = output;
            }
        });

        getAllUsersFromLocalDbAsyncTask.execute();
        getAllGroupsFromLocalDbAsyncTask.execute();
    }
}
