package com.jimchen.kanditag;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class MessageActivity extends FragmentActivity {

    //request codes
    private int ReturnToMessageActivityRequestCode = 1;

    //Async Tasks to fill list view ****************************************************************

    //arrays for the async tasks
    private ArrayList<MessageRowItem> messageRowItems = new ArrayList<>();


    private ArrayList<KtUserObjectParcelable> usersForNewMessageList = new ArrayList<>();
    private ArrayList<KandiGroupObjectParcelable> groupsForNewMessageList = new ArrayList<>();

    //List View Adapters
    private MessageListViewAdapter messageListViewAdapter;

    //gets latest messages from local db
    private GetLatestMessageRowsFromLocalDbAsyncTask getLatestMessageRowsFromLocalDbAsyncTask = new GetLatestMessageRowsFromLocalDbAsyncTask(this, new ReturnMessageRowItemArrayListAsyncResponse() {
        @Override
        public void processFinish(final ArrayList<MessageRowItem> output) {
            System.out.println("MessageActivity.displayLatestMessagesAsyncTask.processFinish.output.size() = " + output.size());
            //put all the latest messages into message row items list
            messageRowItems.addAll(output);
            //get latest group messages from local db
            getLatestGroupMessageRowsFromLocalDbAsyncTask.execute(myDatabase.getKandi());
        }
    });

    //gets latest group message from local db
    private GetLatestGroupMessageRowsFromLocalDbAsyncTask getLatestGroupMessageRowsFromLocalDbAsyncTask = new GetLatestGroupMessageRowsFromLocalDbAsyncTask(this, new ReturnMessageRowItemArrayListAsyncResponse() {
        @Override
        public void processFinish(ArrayList<MessageRowItem> output) {
            System.out.println("MessageActivity.displayLatestGroupMessageAsyncTask.processFinish.output.size() = " + output.size());
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
            messageListViewAdapter = new MessageListViewAdapter(MessageActivity.this, R.layout.message_row_item, messageRowItems);
            listView.setAdapter(messageListViewAdapter);
            messageListViewAdapter.notifyDataSetChanged();
            listView.invalidate();
            // set list view on item click listener to be able to open message dialogues
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    Intent startDialogue = new Intent(MessageActivity.this, MessageDialogue.class);
                    Bundle dialogueBundle = new Bundle();
                    if (messageRowItems.get(i).getTo_Kandi_Id() == null) {
                        if (messageRowItems.get(i).getFrom_Id().equals(MY_KT_ID)) {
                            System.out.println("MessageActivity.onItemClick = " + messageRowItems.get(i).getTo_Name());
                            dialogueBundle.putString("username", messageRowItems.get(i).getTo_Name());
                            dialogueBundle.putString("kt_id", messageRowItems.get(i).getTo_Id());
                        } else if (messageRowItems.get(i).getTo_Id().equals(MY_KT_ID)) {
                            System.out.println("MessageActivity.onItemClick = " + messageRowItems.get(i).getFrom_Name());
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
                            System.out.println("MessageActivity.onItemClick = " + messageRowItems.get(i).getTo_Kandi_Name());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    startDialogue.putExtras(dialogueBundle);
                    startActivityForResult(startDialogue, ReturnToMessageActivityRequestCode);
                }
            });
        }
    });


    //gets all kandi you own (have ownership of) for the kandi name
    private GetAllKandiFromLocalDbAsyncTask getAllKandiFromLocalDbAsyncTask = new GetAllKandiFromLocalDbAsyncTask(this, new ReturnKandiObjectArrayAsyncResponse() {
        @Override
        public void processFinish(ArrayList<KandiObject> output) {
            System.out.println("MessageActivity.getAllKandiFromLocalAsyncTask.processFinish.output.size() = " + output.size());
            getAllGroupsFromLocalDbAsyncTask.execute(output);
        }
    });

    //gets list of all your friends
    private GetAllUsersFromLocalDbAsyncTask getAllUsersFromLocalDbAsyncTask = new GetAllUsersFromLocalDbAsyncTask(this, new ReturnKtUserObjectParcelableArrayListAsyncResponse() {
        @Override
        public void processFinish(ArrayList<KtUserObjectParcelable> output) {
            System.out.println("MessageActivity.getAllUsersFromLocalDbAsyncTask.processFinish.output.size() = " + output.size());
            usersForNewMessageList.addAll(output);
        }
    });

    //get list of all groups you are in
    private GetAllGroupsFromLocalDbAsyncTask getAllGroupsFromLocalDbAsyncTask = new GetAllGroupsFromLocalDbAsyncTask(this, new ReturnKandiGroupObjectParcelableArrayList() {
        @Override
        public void processFinish(ArrayList<KandiGroupObjectParcelable> output) {
            System.out.println("MessageActivity.getAllGroupsFromLocalDbAsyncTask.processFinish.output.size() = " + output.size());
            groupsForNewMessageList.addAll(output);
        }
    });

    // Async Tasks End *****************************************************************************

    //Shared Preferences
    private static final String TAG = "MessageActivity:";
    private SharedPreferences sharedPreferences;
    public static final String MY_PREFERENCES = "MyPrefs";
    public static final String NAME = "nameKey";
    public static final String FBID = "fbidKey";
    public static final String KTID = "userIdKey";
    public static final String OPENED_BEFORE = "opened_before";

    //Local Database
    //TODO should i make the database static?
    private KtDatabase myDatabase;
    private String MY_KT_ID, MY_FB_ID, MY_USER_NAME;

    //MessageActivity Context;
    private Context context;
    //list view
    private ListView listView;
    //backButton (return to main)
    private ImageView backButton;
    //newMessageButton (display users/groups for new message)
    private ImageView newMessageButton;

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        overridePendingTransition(R.anim.right_slide_in, R.anim.right_slide_out);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.message_activity);
        context = getApplicationContext();

        //get data from sharedPreferences and local database
        myDatabase = new KtDatabase(this);
        sharedPreferences = this.getSharedPreferences(MY_PREFERENCES, Context.MODE_PRIVATE);
        MY_KT_ID = sharedPreferences.getString(KTID, "");
        MY_USER_NAME = sharedPreferences.getString(NAME, "");
        MY_FB_ID = sharedPreferences.getString(FBID, "");

        //find list view in xml
        //TODO set adapter for list view
        listView = (ListView) findViewById(R.id.MessageActivity_ListView);

        //find back button in xml
        //set back button to return to main
        backButton = (ImageView) findViewById(R.id.MessageActivity_BackButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
                overridePendingTransition(R.anim.right_slide_in, R.anim.right_slide_out);
            }
        });

        //find new message button in xml
        //set new message button to show fragment
        newMessageButton = (ImageView) findViewById(R.id.MessageActivity_NewMessageButton);
        newMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getSupportFragmentManager()
                        .beginTransaction()
                        .setCustomAnimations(R.anim.abc_slide_in_bottom, R.anim.abc_slide_out_bottom)
                        .add(R.id.MessageActivity_NewMessageFragmentContainer, NewMessageFragment.newInstance())
                        .addToBackStack("newMessageFragment")
                        .commit();
            }
        });

        //get latest messages from local db
        getLatestMessageRowsFromLocalDbAsyncTask.execute(myDatabase.getKTIDsFromLocalDb());
        //get list of all friends from local db
        getAllUsersFromLocalDbAsyncTask.execute();
        //get list of all kandi (groups) you belong to
        getAllKandiFromLocalDbAsyncTask.execute();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == ReturnToMessageActivityRequestCode) {
            // just came back from message dialogue

            //TODO this can only be executed once, find another way to do this
            new GetLatestMessageRowsFromLocalDbAsyncTask(MessageActivity.this, new ReturnMessageRowItemArrayListAsyncResponse() {
                @Override
                public void processFinish(ArrayList<MessageRowItem> output) {
                    System.out.println("MessageActivity.displayLatestMessagesAsyncTask.processFinish.output.size() = " + output.size());
                    messageRowItems.removeAll(messageRowItems);
                    messageRowItems.addAll(output);
                    new GetLatestGroupMessageRowsFromLocalDbAsyncTask(MessageActivity.this, new ReturnMessageRowItemArrayListAsyncResponse() {
                        @Override
                        public void processFinish(ArrayList<MessageRowItem> output) {
                            System.out.println("MessageActivity.displayLatestGroupMessageAsyncTask.processFinish.output.size() = " + output.size());
                            // add the latest group message into message row items list
                            messageRowItems.addAll(output);
                            //create message list view adapter for list view
                            messageListViewAdapter = new MessageListViewAdapter(MessageActivity.this, R.layout.message_row_item, messageRowItems);
                            listView.setAdapter(messageListViewAdapter);
                            messageListViewAdapter.notifyDataSetChanged();
                            listView.invalidate();
                            // set list view on item click listener to be able to open message dialogues
                            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                @Override
                                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                                    Intent startDialogue = new Intent(MessageActivity.this, MessageDialogue.class);
                                    Bundle dialogueBundle = new Bundle();
                                    if (messageRowItems.get(i).getTo_Kandi_Id() == null) {
                                        if (messageRowItems.get(i).getFrom_Id().equals(MY_KT_ID)) {
                                            System.out.println("MessageActivity.onItemClick = " + messageRowItems.get(i).getTo_Name());
                                            dialogueBundle.putString("username", messageRowItems.get(i).getTo_Name());
                                            dialogueBundle.putString("kt_id", messageRowItems.get(i).getTo_Id());
                                        } else if (messageRowItems.get(i).getTo_Id().equals(MY_KT_ID)) {
                                            System.out.println("MessageActivity.onItemClick = " + messageRowItems.get(i).getFrom_Name());
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
                                            System.out.println("MessageActivity.onItemClick = " + messageRowItems.get(i).getTo_Kandi_Name());
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }
                                    startDialogue.putExtras(dialogueBundle);
                                    startActivityForResult(startDialogue, ReturnToMessageActivityRequestCode);
                                }
                            });
                        }
                    }).execute(myDatabase.getKandi());
                }
            }).execute(myDatabase.getKTIDsFromLocalDb());
        }
    }
}
