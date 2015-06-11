package com.jimchen.kanditag;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Created by Jim on 3/18/15.
 */
public class MessageFragment extends Fragment {

    private String TAG = "MessageFragment";

    // bool to check if messages have already been loaded
    private boolean messagesLoaded = false;

    //request codes
    private int ReturnToMessageFragmentRequestCode = 1;

    private KtDatabase myDatabase;
    // Shared Preferences
    private SharedPreferences sharedPreferences;
    public static final String USER_PREFERENCES = "com.jimchen.kanditag.extra.PREFERENCES";
    public static final String USERNAME = "com.jimchen.kanditag.extra.USERNAME";
    public static final String FBID = "com.jimchen.kanditag.extra.FBID";
    public static final String KTID = "com.jimchen.kanditag.extra.KTID";
    public static final String USER_PROFILE_IMAGE = "com.jimchen.kanditag.extra.USER_PROFILE_IMAGE";
    private String MY_KT_ID, MY_FB_ID, MY_USER_NAME;
    private Context context;

    private View rootView;
    private ListView myListView;

    //List View Adapters
    private MessageListViewAdapter messageListViewAdapter;
    private KtUserObjectListAdapter userListViewAdapter;

    //arrays for the async tasks
    private ArrayList<MessageRowItem> messageRowItems = new ArrayList<>();


    private ArrayList<KtUserObjectParcelable> usersForNewMessageList = new ArrayList<>();
    private ArrayList<KandiGroupObjectParcelable> groupsForNewMessageList = new ArrayList<>();

    // new message button
    private ImageView newMessageButton;

    //exit button to return to main
    private ImageView exitButton;

    // Async Tasks
    private GetLatestGroupMessageRowsFromLocalDbAsyncTask getLatestGroupMessageRowsFromLocalDbAsyncTask;
    private GetLatestMessageRowsFromLocalDbAsyncTask getLatestMessageRowsFromLocalDbAsyncTask;
    private GetAllUsersFromLocalDbAsyncTask getAllUsersFromLocalDbAsyncTask;
    private GetAllKandiFromLocalDbAsyncTask getAllKandiFromLocalDbAsyncTask;
    private GetAllGroupsFromLocalDbAsyncTask getAllGroupsFromLocalDbAsyncTask;


    public static final MessageFragment newInstance() {
        MessageFragment messageFragment = new MessageFragment();
        return messageFragment;
    }

    //Fragment Manager Var
    private FragmentActivity myFragmentContext;

    //this is called when the fragment is first attached to an activity
    @Override
    public void onAttach(Activity activity) {
        myFragmentContext = (FragmentActivity) activity;
        super.onAttach(activity);
    }
    //Fragment Manager Var End

    @Override
    public void onResume() {
        super.onResume();

    }

    @Override
    public void onPause() {
        super.onPause();
        messagesLoaded = false;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.message_fragment, container, false);

        this.context = getActivity();

        myDatabase = new KtDatabase(getActivity());
        sharedPreferences = getActivity().getSharedPreferences(USER_PREFERENCES, Context.MODE_PRIVATE);
        MY_KT_ID = sharedPreferences.getString(KTID, "");
        MY_USER_NAME = sharedPreferences.getString(USERNAME, "");
        MY_FB_ID = sharedPreferences.getString(FBID, "");

        myListView = (ListView) rootView.findViewById(R.id.MessageFragment_ListView);
        myListView.setOverScrollMode(View.OVER_SCROLL_ALWAYS);

        newMessageButton = (ImageView) rootView.findViewById(R.id.MessageFragment_NewMessageButton);
        newMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                myListView.setAdapter(userListViewAdapter);
                myListView.invalidate();
                myListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        Intent startDialogue = new Intent(getActivity(), MessageDialogue.class);
                        Bundle bundle = new Bundle();
                        bundle.putString("kt_id", usersForNewMessageList.get(i).getKt_id());
                        bundle.putString("username", usersForNewMessageList.get(i).getUsername());
                        startDialogue.putExtras(bundle);
                        startActivityForResult(startDialogue, ReturnToMessageFragmentRequestCode);
                    }
                });
            }
        });

        //gets latest group message from local db
        getLatestGroupMessageRowsFromLocalDbAsyncTask = new GetLatestGroupMessageRowsFromLocalDbAsyncTask(getActivity(), new ReturnMessageRowItemArrayListAsyncResponse() {
            @Override
            public void processFinish(ArrayList<MessageRowItem> output) {
                //System.out.println("MessageFragment.displayLatestGroupMessageAsyncTask.processFinish.output.size() = " + output.size());
                // add the latest group message into message row items list
                messageRowItems.addAll(output);
                //TODO check to see if this puts the list of messages in descending order, latest at the top
                Collections.sort(messageRowItems, new Comparator<MessageRowItem>() {
                    @Override
                    public int compare(MessageRowItem messageRowItem, MessageRowItem messageRowItem2) {
                        return messageRowItem.getTimestamp().compareTo(messageRowItem2.getTimestamp());
                    }
                });
                //create message list view adapter for list view
                messageListViewAdapter = new MessageListViewAdapter(getActivity(), R.layout.message_row_item, messageRowItems);
                myListView.setAdapter(messageListViewAdapter);
                messageListViewAdapter.notifyDataSetChanged();
                myListView.invalidate();
                // set list view on item click listener to be able to open message dialogues
                myListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        Intent startDialogue = new Intent(getActivity(), MessageDialogue.class);
                        Bundle dialogueBundle = new Bundle();
                        if (messageRowItems.get(i).getTo_Kandi_Id() == null) {

                            if (messageRowItems.get(i).getFrom_Id().equals(MY_KT_ID)) {
                                // TODO so apparently its not even going inside this control
                                System.out.println("MessageFragment.onItemClick = " + messageRowItems.get(i).getTo_Name());
                                dialogueBundle.putString("username", messageRowItems.get(i).getTo_Name());
                                dialogueBundle.putString("kt_id", messageRowItems.get(i).getTo_Id());
                            } else if (messageRowItems.get(i).getTo_Id().equals(MY_KT_ID)) {
                                System.out.println("MessageFragment.onItemClick = " + messageRowItems.get(i).getFrom_Name());
                                dialogueBundle.putString("username", messageRowItems.get(i).getFrom_Name());
                                dialogueBundle.putString("kt_id", messageRowItems.get(i).getFrom_Id());
                            }
                        } else if (messageRowItems.get(i).getTo_Kandi_Id() != null) {
                            Log.d(TAG, "inside group");
                            try {
                                dialogueBundle.putString("kandi_id", messageRowItems.get(i).getTo_Kandi_Id());
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            try {
                                dialogueBundle.putString("kandi_name", messageRowItems.get(i).getTo_Kandi_Name());
                                System.out.println("MessageFragment.onItemClick = " + messageRowItems.get(i).getTo_Kandi_Name());
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        startDialogue.putExtras(dialogueBundle);
                        startActivityForResult(startDialogue, ReturnToMessageFragmentRequestCode);
                    }
                });
            }
        });

        //gets latest messages from local db
        getLatestMessageRowsFromLocalDbAsyncTask = new GetLatestMessageRowsFromLocalDbAsyncTask(getActivity(), new ReturnMessageRowItemArrayListAsyncResponse() {
            @Override
            public void processFinish(final ArrayList<MessageRowItem> output) {
                //System.out.println("MessageFragment.displayLatestMessagesAsyncTask.processFinish.output.size() = " + output.size());
                // remove all messages before adding the new ones
                messageRowItems.removeAll(messageRowItems);
                //put all the latest messages into message row items list
                messageRowItems.addAll(output);
                //get latest group messages from local db
                getLatestGroupMessageRowsFromLocalDbAsyncTask.execute(myDatabase.getKandi());
            }
        });

        //gets list of all your friends
        getAllUsersFromLocalDbAsyncTask = new GetAllUsersFromLocalDbAsyncTask(getActivity(), new ReturnKtUserObjectParcelableArrayListAsyncResponse() {
            @Override
            public void processFinish(ArrayList<KtUserObjectParcelable> output) {
                System.out.println("MessageFragment.getAllUsersFromLocalDbAsyncTask.processFinish.output.size() = " + output.size());
                usersForNewMessageList.addAll(output);
                userListViewAdapter = new KtUserObjectListAdapter(getActivity(), R.layout.message_list_item, usersForNewMessageList);
            }
        });


        //get list of all groups you are in
        getAllGroupsFromLocalDbAsyncTask = new GetAllGroupsFromLocalDbAsyncTask(getActivity(), new ReturnKandiGroupObjectParcelableArrayList() {
            @Override
            public void processFinish(ArrayList<KandiGroupObjectParcelable> output) {
                //System.out.println("MessageFragment.getAllGroupsFromLocalDbAsyncTask.processFinish.output.size() = " + output.size());
                groupsForNewMessageList.addAll(output);
            }
        });

        //gets all kandi you own (have ownership of) for the kandi name
        getAllKandiFromLocalDbAsyncTask = new GetAllKandiFromLocalDbAsyncTask(getActivity(), new ReturnKandiObjectArrayAsyncResponse() {
            @Override
            public void processFinish(ArrayList<KandiObject> output) {
                //System.out.println("MessageFragment.getAllKandiFromLocalAsyncTask.processFinish.output.size() = " + output.size());
                getAllGroupsFromLocalDbAsyncTask.execute(output);
            }
        });

        if (!messagesLoaded) {
            //get latest messages from local db
            getLatestMessageRowsFromLocalDbAsyncTask.execute(myDatabase.getKTIDsFromLocalDb());
            //get list of all friends from local db
            getAllUsersFromLocalDbAsyncTask.execute();
            //get list of all kandi (groups) you belong to
            getAllKandiFromLocalDbAsyncTask.execute();

            messagesLoaded = true;
        }

        return rootView;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == ReturnToMessageFragmentRequestCode) {
            // just came back from message dialogue


            //TODO this can only be executed once, find another way to do this
            new GetLatestMessageRowsFromLocalDbAsyncTask(getActivity(), new ReturnMessageRowItemArrayListAsyncResponse() {
                @Override
                public void processFinish(ArrayList<MessageRowItem> output) {
                    System.out.println("MessageFragment.displayLatestMessagesAsyncTask.processFinish.output.size() = " + output.size());
                    messageRowItems.removeAll(messageRowItems);
                    messageRowItems.addAll(output);
                    new GetLatestGroupMessageRowsFromLocalDbAsyncTask(getActivity(), new ReturnMessageRowItemArrayListAsyncResponse() {
                        @Override
                        public void processFinish(ArrayList<MessageRowItem> output) {
                            System.out.println("MessageFragment.displayLatestGroupMessageAsyncTask.processFinish.output.size() = " + output.size());
                            // add the latest group message into message row items list
                            messageRowItems.addAll(output);
                            //create message list view adapter for list view
                            messageListViewAdapter = new MessageListViewAdapter(getActivity(), R.layout.message_row_item, messageRowItems);
                            myListView.setAdapter(messageListViewAdapter);
                            messageListViewAdapter.notifyDataSetChanged();
                            myListView.invalidate();
                            // set list view on item click listener to be able to open message dialogues
                            myListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                @Override
                                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                                    Intent startDialogue = new Intent(getActivity(), MessageDialogue.class);
                                    Bundle dialogueBundle = new Bundle();
                                    if (messageRowItems.get(i).getTo_Kandi_Id() == null) {
                                        if (messageRowItems.get(i).getFrom_Id().equals(MY_KT_ID)) {
                                            //System.out.println("MessageFragment.onItemClick = " + messageRowItems.get(i).getTo_Name());
                                            dialogueBundle.putString("username", messageRowItems.get(i).getTo_Name());
                                            dialogueBundle.putString("kt_id", messageRowItems.get(i).getTo_Id());
                                        } else if (messageRowItems.get(i).getTo_Id().equals(MY_KT_ID)) {
                                            //System.out.println("MessageFragment.onItemClick = " + messageRowItems.get(i).getFrom_Name());
                                            dialogueBundle.putString("username", messageRowItems.get(i).getFrom_Name());
                                            dialogueBundle.putString("kt_id", messageRowItems.get(i).getFrom_Id());
                                        }
                                    } else if (messageRowItems.get(i).getTo_Kandi_Id() != null) {
                                        try {
                                            dialogueBundle.putString("kandi_id", messageRowItems.get(i).getTo_Kandi_Id());
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                        try {
                                            dialogueBundle.putString("kandi_name", messageRowItems.get(i).getTo_Kandi_Name());
                                            //System.out.println("MessageFragment.onItemClick = " + messageRowItems.get(i).getTo_Kandi_Name());
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }
                                    startDialogue.putExtras(dialogueBundle);
                                    startActivityForResult(startDialogue, ReturnToMessageFragmentRequestCode);
                                }
                            });
                        }
                    }).execute(myDatabase.getKandi());
                }
            }).execute(myDatabase.getKTIDsFromLocalDb());
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        // Make sure that we are currently visible
        if (this.isVisible()) {
            // If we are becoming invisible, then...
            if (!isVisibleToUser) {
            }
        }
    }

}
