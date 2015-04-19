package com.jimchen.kanditag;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.support.v4.content.LocalBroadcastManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.ArrayList;


public class GroupMessageDialogue extends Activity {

    private static final String TAG = "GroupMessageDialogue";
    SharedPreferences sharedPreferences;
    public static final String MyPreferences = "MyPrefs";
    public static final String Name = "nameKey";
    public static final String FbId = "fbidKey";
    public static final String UserId = "userIdKey";
    public static final String NEW_MESSAGE = "NEW_MESSAGE";

    private String MY_KT_ID, MY_FB_ID, MY_USER_NAME;
    private String kt_id, fb_id, user_name;

    private String qrcode, kandi_name;

    private String messageToSend;

    KtDatabase myDatabase;

    private final String HOST = "http://kandi.jit.su/";
    private final int portNumber = 3000;


    // XML VARS
    private ScrollView myScrollView;
    private ListView myListView;
    private TextView myTextView;
    private EditText myEditText;
    private Button mySendButton;
    // XML VARS

    //ListView Vars
    private ArrayList<GroupMessageItem> groupMessageItemArrayList = new ArrayList<>();
    private GroupMessageListAdapter groupMessageListAdapter;
    //ListView Vars

    private BroadcastReceiver newMessageBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent.getAction().equals("new_group_message")) {

                String message = intent.getStringExtra("message");
                String from_id = intent.getStringExtra("from_id");
                String from_name = intent.getStringExtra("from_name");
                String qrCode = intent.getStringExtra("kandi_group");
                String kandi_name = intent.getStringExtra("kandi_name");
                String time = intent.getStringExtra("time");

                GroupMessageItem groupMessageItem = new GroupMessageItem();
                groupMessageItem.setMessage(message);
                groupMessageItem.setFromID(from_id);
                groupMessageItem.setFromName(from_name);
                groupMessageItem.setQrCode(qrCode);
                groupMessageItem.setTime(time);

                groupMessageItemArrayList.add(groupMessageItem);
                groupMessageListAdapter.notifyDataSetChanged();

                myListView.invalidate();

                Log.d(TAG, message);
            }
        }
    };

    //AsyncTasks
    class GetAllMessagesFromDb extends AsyncTask<String, Void, ArrayList<GroupMessageItem>> {

        private ArrayList<GroupMessageItem> tempGroupMessageItemList;

        @Override
        protected ArrayList<GroupMessageItem> doInBackground(String... params){
            String qr = params[0];
            tempGroupMessageItemList = myDatabase.getMessagesForGroup(qr);
            return tempGroupMessageItemList;
        }

        @Override
        protected void onPreExecute() {
            tempGroupMessageItemList = new ArrayList<>();
        }

        @Override
        protected void onPostExecute(ArrayList<GroupMessageItem> list) {
            groupMessageListAdapter = new GroupMessageListAdapter(GroupMessageDialogue.this, R.layout.group_message_list_item, tempGroupMessageItemList, MY_FB_ID);
            myListView.setAdapter(groupMessageListAdapter);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_message_dialogue);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        myDatabase = new KtDatabase(this);

        sharedPreferences = getSharedPreferences(MyPreferences, MODE_PRIVATE);
        MY_KT_ID = sharedPreferences.getString(UserId, "");
        MY_USER_NAME = sharedPreferences.getString(Name, "");
        MY_FB_ID = sharedPreferences.getString(FbId, "");

        Bundle bundleParams = getIntent().getExtras();
        qrcode = bundleParams.getString("qrcode");
        kandi_name = bundleParams.getString("kandi_name");

        GetAllMessagesFromDb getAllMessagesFromDb = new GetAllMessagesFromDb();
        getAllMessagesFromDb.execute(qrcode);

        LocalBroadcastManager.getInstance(this).registerReceiver(newMessageBroadcastReceiver, new IntentFilter("new_message"));

        myListView = (ListView) findViewById(R.id.GroupMessageDialogue_ListView);
        myListView.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);

        myScrollView = (ScrollView) findViewById(R.id.GroupMessageDialogue_ScrollView);

        myTextView = (TextView) findViewById(R.id.GroupMessageDialogue_GroupName);
        myTextView.setText(kandi_name);
        myTextView.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
        myTextView.setTextSize(40);
        Typeface typeface = Typeface.createFromAsset(getAssets(), "fonts/stalemate_regular.ttf");
        myTextView.setTypeface(typeface);

        setUpEditText();
        setUpSendButton();
    }

    private void setUpSendButton() {
        mySendButton = (Button) findViewById(R.id.GroupMessageDialogue_SendButton);
        mySendButton.setBackgroundColor(Color.TRANSPARENT);
        mySendButton.setBackgroundResource(R.drawable.send_icon);
        mySendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (myEditText.getText().toString().length() > 1) {
                    messageToSend = myEditText.getText().toString();
                    Animation anim = AnimationUtils.loadAnimation(GroupMessageDialogue.this, R.anim.left_slide_out);
                    mySendButton.startAnimation(anim);
                    //TODO check this
                    /**
                    Message message = new Message();
                    message.newGroupMessage(messageToSend, MY_FB_ID, MY_USER_NAME, qrcode, kandi_name);
                    **/
                    if (messageToSend != null) {
                        myEditText.setText("");
                    }

                    //notify list adapter of change?
                }
            }
        });
    }

    private void setUpEditText() {
        myEditText = (EditText) findViewById(R.id.GroupMessageDialogue_EditText);
        myEditText.setBackgroundColor(Color.TRANSPARENT);
        myEditText.setTextColor(Color.WHITE);
    }

    @Override
    public void onResume() {
        super.onResume();
        //connectSocket();
    }

    @Override
    public void onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(newMessageBroadcastReceiver);
        super.onDestroy();
    }

    @Override
    public void onPause() {
        super.onPause();
    }
}
