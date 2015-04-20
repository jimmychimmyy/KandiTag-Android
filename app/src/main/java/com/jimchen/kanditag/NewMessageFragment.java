package com.jimchen.kanditag;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Created by Jim on 3/18/15.
 */
public class NewMessageFragment extends Fragment {

    // request codes
    private int ReturnToMessageActivityRequestCode = 1;

    private KtDatabase myDatabase;
    SharedPreferences sharedPreferences;
    public static final String MyPreferences = "MyPrefs";
    public static final String Name = "nameKey";
    public static final String FbId = "fbidKey";
    public static final String UserId = "userIdKey";
    public static final String NEW_MESSAGE = "NEW_MESSAGE";

    private String MY_KT_ID, MY_FB_ID, MY_USER_NAME;

    private Context context;

    private View rootView;
    private ListView myListView;
    private TextView myTitle;

    // this button is used to switch between users and groups
    private Button switchButton;
    private boolean isGroupListVisible = false;

    private KtUserObjectListAdapter ktUserObjectListAdapter;
    private KtGroupListViewAdapter ktGroupListViewAdapter;

    private GetAllUsersFromLocalDbAsyncTask getAllUsersFromLocalDbAsyncTask;
    private GetAllGroupsFromLocalDbAsyncTask getAllGroupsFromLocalDbAsyncTask;
    private GetAllKandiFromLocalDbAsyncTask getAllKandiFromLocalDbAsyncTask;

    private ArrayList<KtUserObjectParcelable> usersForNewMessageList = new ArrayList<>();
    private ArrayList<KandiGroupObjectParcelable> groupsForNewMessageList = new ArrayList<>();

    public static final NewMessageFragment newInstance() {
        NewMessageFragment newMessageFragment = new NewMessageFragment();
        return newMessageFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle saveInstanceState) {
        rootView = inflater.inflate(R.layout.new_message_fragment, container, false);

        this.context = getActivity();

        myDatabase = new KtDatabase(context);
        sharedPreferences = getActivity().getSharedPreferences(MyPreferences, Context.MODE_PRIVATE);
        MY_KT_ID = sharedPreferences.getString(UserId, "");
        MY_USER_NAME = sharedPreferences.getString(Name, "");
        MY_FB_ID = sharedPreferences.getString(FbId, "");

        myListView = (ListView) rootView.findViewById(R.id.NewMessageFragment_ListView);

        switchButton = (Button) rootView.findViewById(R.id.NewMessageFragment_SwitchBetweenUserAndGroup);
        switchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isGroupListVisible) {
                    isGroupListVisible = true;
                    myListView.setAdapter(ktGroupListViewAdapter);
                    myListView.invalidate();
                    myListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                            System.out.println("NewMessageFragment.myListView.onItemClick = " + groupsForNewMessageList.get(i).getKandi_name());
                            Intent startDialogue = new Intent(context, MessageDialogue.class);
                            Bundle dialogueBundle = new Bundle();
                            dialogueBundle.putString("kandi_id", groupsForNewMessageList.get(i).getKandi_id());
                            dialogueBundle.putString("kandi_name", groupsForNewMessageList.get(i).getKandi_name());
                            startDialogue.putExtras(dialogueBundle);
                            startActivityForResult(startDialogue, ReturnToMessageActivityRequestCode);
                            getActivity().overridePendingTransition(R.anim.abc_slide_in_bottom, R.anim.right_slide_out);
                            getActivity().getSupportFragmentManager()
                                    .beginTransaction()
                                    .remove(NewMessageFragment.this)
                                    .commit();
                        }
                    });
                } else if (isGroupListVisible) {
                    isGroupListVisible = false;
                    myListView.setAdapter(ktUserObjectListAdapter);
                    myListView.invalidate();
                    myListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                            System.out.println("NewMessageFragment.myListView.onItemClick = " + usersForNewMessageList.get(i).getUsername());
                            Intent startDialogue = new Intent(context, MessageDialogue.class);
                            Bundle dialogueBundle = new Bundle();
                            dialogueBundle.putString("kt_id", usersForNewMessageList.get(i).getKt_id());
                            dialogueBundle.putString("username", usersForNewMessageList.get(i).getUsername());
                            startDialogue.putExtras(dialogueBundle);
                            startActivityForResult(startDialogue, ReturnToMessageActivityRequestCode);
                            getActivity().overridePendingTransition(R.anim.abc_slide_in_bottom, R.anim.right_slide_out);
                            getActivity().getSupportFragmentManager()
                                    .beginTransaction()
                                    .remove(NewMessageFragment.this)
                                    .commit();
                        }
                    });
                }
            }
        });

        /**
        myTitle = (TextView) rootView.findViewById(R.id.NewMessageFragment_TitleTextView);
        myTitle.setText("Friends");
        myTitle.setTextSize(45);
        myTitle.setTextColor(Color.WHITE);
        Typeface typeface = Typeface.createFromAsset(getActivity().getAssets(),"fonts/stalemate_regular.ttf");
        myTitle.setTypeface(typeface);
         **/

        getAllUsersFromLocalDbAsyncTask = new GetAllUsersFromLocalDbAsyncTask(getActivity(), new ReturnKtUserObjectParcelableArrayListAsyncResponse() {
            @Override
            public void processFinish(ArrayList<KtUserObjectParcelable> output) {
                System.out.println("NewMessageFragment.getAllUsersFromLocalDbAsyncTask.processFinish.output.size() = " + output.size());
                usersForNewMessageList.removeAll(usersForNewMessageList);
                usersForNewMessageList.addAll(output);
                Collections.sort(usersForNewMessageList, new Comparator<KtUserObjectParcelable>() {
                    @Override
                    public int compare(KtUserObjectParcelable lhs, KtUserObjectParcelable rhs) {
                        return lhs.getUsername().compareTo(rhs.getUsername());
                    }
                });
                ktUserObjectListAdapter.notifyDataSetChanged();
                myListView.invalidate();
            }
        });

        getAllKandiFromLocalDbAsyncTask = new GetAllKandiFromLocalDbAsyncTask(getActivity(), new ReturnKandiObjectArrayAsyncResponse() {
            @Override
            public void processFinish(ArrayList<KandiObject> output) {
                System.out.println("NewMessageFragment.getAllKandiFromLocalAsyncTask.processFinish.output.size() = " + output.size());
                getAllGroupsFromLocalDbAsyncTask.execute(output);
            }
        });

        //this returns a list of kandi you own
        getAllGroupsFromLocalDbAsyncTask = new GetAllGroupsFromLocalDbAsyncTask(getActivity(), new ReturnKandiGroupObjectParcelableArrayList() {
            @Override
            public void processFinish(ArrayList<KandiGroupObjectParcelable> output) {
                System.out.println("NewMessageFragment.getAllGroupsFromLocalDbAsyncTask.processFinish.output.size() = " + output.size());
                groupsForNewMessageList.addAll(output);
                ktGroupListViewAdapter.notifyDataSetChanged();
                myListView.invalidate();
            }
        });

        getAllUsersFromLocalDbAsyncTask.execute();
        getAllKandiFromLocalDbAsyncTask.execute();

        ktGroupListViewAdapter = new KtGroupListViewAdapter(getActivity(), R.id.DisplayForNewMessage_ListView, groupsForNewMessageList);

        ktUserObjectListAdapter = new KtUserObjectListAdapter(getActivity(), R.id.DisplayForNewMessage_ListView, usersForNewMessageList);
        myListView.setAdapter(ktUserObjectListAdapter);
        myListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                System.out.println("NewMessageFragment.myListView.onItemClick = " + usersForNewMessageList.get(i).getUsername());
                Intent startDialogue = new Intent(context, MessageDialogue.class);
                Bundle dialogueBundle = new Bundle();
                dialogueBundle.putString("kt_id", usersForNewMessageList.get(i).getKt_id());
                dialogueBundle.putString("username", usersForNewMessageList.get(i).getUsername());
                startDialogue.putExtras(dialogueBundle);
                startActivityForResult(startDialogue, ReturnToMessageActivityRequestCode);
                getActivity().overridePendingTransition(R.anim.abc_slide_in_bottom, R.anim.right_slide_out);
                getActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .remove(NewMessageFragment.this)
                        .commit();
            }
        });

        return rootView;
    }

}
