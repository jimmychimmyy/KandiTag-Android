package com.jimchen.kanditag;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;

import java.util.ArrayList;

public class MessageActivity extends Activity {

    //Async Tasks to fill list view ****************************************************************

    //arrays for the async tasks
    private ArrayList<MessageListItem> messageListItemsArray = new ArrayList<>();
    private ArrayList<GroupMessageItem> groupMessageItemArray = new ArrayList<>();
    private ArrayList<KtUserObjectParcelable> usersForNewMessageList = new ArrayList<>();
    private ArrayList<KandiGroupObjectParcelable> groupsForNewMessageList = new ArrayList<>();

    //List View Adapters
    private MessageListAdapter messageListAdapter;
    private GroupMessageListAdapter groupMessageListAdapter;

    //gets latest messages from local db
    private DisplayLatestMessagesAsyncTask displayLatestMessagesAsyncTask = new DisplayLatestMessagesAsyncTask(this, new ReturnMessageListItemArrayAsyncResponse() {
        @Override
        public void processFinish(final ArrayList<MessageListItem> output) {
            messageListItemsArray = output;
            messageListAdapter = new MessageListAdapter(MessageActivity.this, R.id.list_item, messageListItemsArray);
            listView.setAdapter(messageListAdapter);

            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    System.out.println("messageListView.onItemClick.position = " + position);
                    System.out.println("fbId = " + output.get(position).getSender());
                    Intent openMessageWithUser = new Intent(MessageActivity.this, MessageDialogue.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("fb_id", output.get(position).getSender());
                    openMessageWithUser.putExtras(bundle);
                    startActivity(openMessageWithUser);
                    MessageActivity.this.overridePendingTransition(R.anim.right_slide_in, R.anim.left_slide_out);
                }
            });
            System.out.println("MessageFragment.displayLatestMessagesAsyncTask.processFinish.output.size() = " + output.size());
        }
    });

    //gets latest group message from local db
    private DisplayLatestGroupMessageAsyncTask displayLatestGroupMessageAsyncTask = new DisplayLatestGroupMessageAsyncTask(this, new ReturnGroupMessageArrayListAsyncResponse() {
        @Override
        public void processFinish(ArrayList<GroupMessageItem> output) {
            System.out.println("MessageFragment.displayLatestGroupMessageAsyncTask.processFinish.output.size() = " + output.size());
            groupMessageItemArray = output;
            groupMessageListAdapter = new GroupMessageListAdapter(MessageActivity.this, R.id.list_item, groupMessageItemArray, MY_FB_ID);
        }
    });

    //gets list of all your friends
    private GetAllUsersFromLocalDbAsyncTask getAllUsersFromLocalDbAsyncTask = new GetAllUsersFromLocalDbAsyncTask(this, new ReturnKtUserObjectParcelableArrayListAsyncResponse() {
        @Override
        public void processFinish(ArrayList<KtUserObjectParcelable> output) {
            System.out.println("MessageFragment.getAllUsersFromLocalDbAsyncTask.processFinish.output.size() = " + output.size());
            usersForNewMessageList.addAll(output);
        }
    });

    //gets all kandi you own (have ownership of) for the kandi name
    private GetAllKandiFromLocalAsyncTask getAllKandiFromLocalAsyncTask = new GetAllKandiFromLocalAsyncTask(this, new ReturnKandiObjectArrayAsyncResponse() {
        @Override
        public void processFinish(ArrayList<KandiObject> output) {
            System.out.println("MessageFragment.getAllKandiFromLocalAsyncTask.processFinish.output.size() = " + output.size());
            getAllGroupsFromLocalDbAsyncTask.execute(output);
        }
    });

    //get list of all groups you are in
    private GetAllGroupsFromLocalDbAsyncTask getAllGroupsFromLocalDbAsyncTask = new GetAllGroupsFromLocalDbAsyncTask(this, new ReturnKandiGroupObjectParcelableArrayList() {
        @Override
        public void processFinish(ArrayList<KandiGroupObjectParcelable> output) {
            System.out.println("MessageFragment.getAllGroupsFromLocalDbAsyncTask.processFinish.output.size() = " + output.size());
            groupsForNewMessageList.addAll(output);
        }
    });

    // Async Tasks End *****************************************************************************

    //Shared Preferences
    private static final String TAG = "MainActivity:";
    private SharedPreferences sharedPreferences;
    public static final String MyPreferences = "MyPrefs";
    public static final String Name = "nameKey";
    public static final String FbId = "fbidKey";
    public static final String UserId = "userIdKey";
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
        sharedPreferences = this.getSharedPreferences(MyPreferences, Context.MODE_PRIVATE);
        MY_KT_ID = sharedPreferences.getString(UserId, "");
        MY_USER_NAME = sharedPreferences.getString(Name, "");
        MY_FB_ID = sharedPreferences.getString(FbId, "");

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

        //get latest messages from local db
        displayLatestMessagesAsyncTask.execute(myDatabase.getFb_IdFromKtUserToDisplayLatestMessage());
        //get latest group messages from local db
        displayLatestGroupMessageAsyncTask.execute(myDatabase.getKandi());
        //get list of all friends from local db
        getAllUsersFromLocalDbAsyncTask.execute();
        //get list of all kandi (groups) you belong to
        getAllKandiFromLocalAsyncTask.execute();
    }

}
