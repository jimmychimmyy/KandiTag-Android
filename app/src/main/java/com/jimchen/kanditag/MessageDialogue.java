package com.jimchen.kanditag;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TextView;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.net.URISyntaxException;
import java.util.ArrayList;

public class MessageDialogue extends FragmentActivity {

    // request codes
    private int ReturnToMessageActivityRequestCode = 1;

    private static final String TAG = "MessageDialogue";
    SharedPreferences sharedPreferences;
    public static final String MY_PREFERENCES = "MyPrefs";
    public static final String NAME = "nameKey";
    public static final String FBID = "fbidKey";
    public static final String KTID = "userIdKey";
    public static final String NEW_MESSAGE = "NEW_MESSAGE";

    private String MY_KT_ID, MY_FB_ID, MY_USER_NAME;
    private String kt_id, fb_id, user_name;

    //this is for group messages
    private String kandi_id;

    private String messageToSend;

    KtDatabase myDatabase;

    private final String HOST = "http://kandi.jit.su/";
    private final int portNumber = 3000;


    //textview for username
    private TextView userNameTextView;

    private ImageView exitButton;

    private TextView messagingUIUserName;
    private TableLayout messagingUITableLayout;

    private ListView listView;
    private ConversationListAdapter conversationListAdapter;
    private ConversationListItem tempConversationListItem;

    private ArrayList<ConversationListItem> conversationListItemsShown;

    private ArrayList<ConversationListItem> conversationListItems;

    private ScrollView messageUIScrollView;

    private EditText messageUIEditText;

    private Button messageUISendButton;

    private static com.github.nkzawa.socketio.client.Socket socket;

    //private ArrayList<ConversationListItem> messagingUIListViewArrayItems = new ArrayList<>();


    private void scrollMyListViewToBottom() {
        messageUIScrollView.post(new Runnable() {
            @Override
            public void run() {
                messageUIScrollView.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });
    }


    private AbsListView.OnScrollListener messageWithUserScrollListener = new AbsListView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
            System.out.println("MessageWithUser.onScrollStateChanged");
            //messagingUIUserName.setVisibility(View.INVISIBLE);
        }

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

        }
    };

    //TODO async task to get all conversation messages into this array
    ArrayList<ConversationListItem> conversationMessagesArrayList;


    // background task to get all message for list view
    private GetAllMessagesFromLocalDbAsyncTask getMessagesTask = new GetAllMessagesFromLocalDbAsyncTask(this, new ReturnMessageRowItemArrayListAsyncResponse() {
        @Override
        public void processFinish(final ArrayList<MessageRowItem> output) {
            System.out.println("MessageDialogue.getMessageTask.output.size() = " + output.size());
            messageRowItems.addAll(output);
            messageDialogueListViewAdapter = new MessageDialogueListViewAdapter(MessageDialogue.this, R.layout.message_row_item, messageRowItems);
            listView.setAdapter(messageDialogueListViewAdapter);
        }
    });

    private ArrayList<MessageRowItem> messageRowItems = new ArrayList<>();

    //adapter for list view
    private MessageDialogueListViewAdapter messageDialogueListViewAdapter;


// OnCreate ****************************************************************************************
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_dialogue);
        //getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        System.out.println("MessageDialogue.onCreate");

        myDatabase = new KtDatabase(this);

        sharedPreferences = getSharedPreferences(MY_PREFERENCES, MODE_PRIVATE);
        MY_KT_ID = sharedPreferences.getString(KTID, "");
        MY_USER_NAME = sharedPreferences.getString(NAME, "");
        MY_FB_ID = sharedPreferences.getString(FBID, "");

        //LocalBroadcastManager.getInstance(this).registerReceiver(newMessageBroadcastReceiver, new IntentFilter("new_message"));

        //TODO need to change all fb_ids usage to kt_id

        Bundle bundleParams = getIntent().getExtras();
        kt_id = bundleParams.getString("kt_id");
        // get messages
        getMessagesTask.execute(kt_id);
        user_name = bundleParams.getString("username");
        System.out.println(user_name);
        //try to get kandiID, if none that means its a regular message
        try {
            kandi_id = bundleParams.getString("kandi_id");
        } catch (Exception e) {
            e.printStackTrace();
        }


        // find exit button in xml and set on click to return to MessageActivity
        exitButton = (ImageView) findViewById(R.id.MessageDialogue_ExitButton);
        exitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                MessageDialogue.this.overridePendingTransition(R.anim.right_slide_in, R.anim.right_slide_out);
            }
        });

        // find list view and change the display mode from latest message at the bottom
        listView = (ListView) findViewById(R.id.MessageDialogue_ListView);
        listView.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
        //messagingUIListView.setOnScrollListener(messageWithUserScrollListener);


        // find xml container for username
        // set text as user's name
        userNameTextView = (TextView) findViewById(R.id.MessageDialogue_UserNameTextView);
        Typeface typeface = Typeface.createFromAsset(getAssets(), "fonts/kanditagfont-sth.ttf");
        userNameTextView.setTypeface(typeface);
        userNameTextView.setText(user_name);

        // edit text to write message
        //TODO need to make this expand when message gets long
        messageUIEditText = (EditText) findViewById(R.id.MessageDialogue_EditText);
        //messageUIEditText.setBackgroundColor(Color.TRANSPARENT);
        messageUIEditText.setTextColor(Color.WHITE);

        messageUISendButton = (Button) findViewById(R.id.MessageDialogue_SendButton);
        //messageUISendButton.setBackgroundColor(Color.TRANSPARENT);
        //messageUISendButton.setBackgroundResource(R.drawable.send_icon);
        messageUISendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sharedPreferences = getSharedPreferences(MY_PREFERENCES, Context.MODE_PRIVATE);
                if (messageUIEditText.getText().toString().length() > 1) {
                    messageToSend = messageUIEditText.getText().toString();
                    //TODO brush up on the animations
                    Animation anim = AnimationUtils.loadAnimation(MessageDialogue.this, R.anim.left_slide_out);
                    messageUISendButton.startAnimation(anim);
                    //Sending the message
                    newMessage(messageToSend, kt_id, MY_KT_ID, user_name, MY_USER_NAME);

                    if (messageToSend != null) {
                        messageUIEditText.setText("");
                    }

                    //TODO send message and have emitter listener notify the changes to the db
                    //conversationListAdapter.notifyDataSetChanged();


                }
            }
        });

    }

// end of OnCreate *********************************************************************************

    private void connectSocket() {
        try {
            IO.Options options = new IO.Options();
            options.forceNew = true;
            socket = IO.socket(HOST, options);
            socket.on("privatemessage", onNewMessage);
            socket.on(com.github.nkzawa.socketio.client.Socket.EVENT_CONNECT, new Emitter.Listener() {
                @Override
                public void call(Object... args) {

                }
            }).on(com.github.nkzawa.socketio.client.Socket.EVENT_DISCONNECT, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    System.out.println("socket disconnected");
                }
            }).on("sign_in", new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    String message = (String) args[0];
                    System.out.println(message);
                }
            });
        } catch (URISyntaxException use) {
            use.printStackTrace();
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.i(TAG, "connecting socket");
                socket.connect();
                socket.emit("sign_in", MY_FB_ID, MY_KT_ID);
            }
        }).start();
    }

    private void newMessage(String message, String toKTID, String myKTID, String toName, String fromName) {
        System.out.println("MessageDialogue.class.newMessage");
        socket.emit("privatemessage", message, toKTID, myKTID, toName, fromName);
    }

    private Emitter.Listener onNewMessage = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    String message = (String) args[0];
                    System.out.println("message " + message);
                    GsonBuilder gsonBuilder = new GsonBuilder();
                    gsonBuilder.registerTypeAdapter(Res_End_Results.class, new Res_End_Results_Deserializer());
                    gsonBuilder.registerTypeAdapter(Records.class, new Records_Deserializer());
                    Gson gson = gsonBuilder.create();

                    Res_End_Results re_obj = gson.fromJson(message, Res_End_Results.class);
                    for (Records records:re_obj.getRecords()) {
                        System.out.println("messages sent =" + records.getMsg());
                        System.out.println("messages sent from id =" + records.getFID());
                        System.out.println("messages sent to id =" + records.getTID());
                        System.out.println("messages sent date =" + records.getDate());
                        System.out.println("messages sent to name =" + records.getToName());
                        System.out.println("messages sent from name =" + records.getFromName());
                        System.out.println("messages sent time =" + records.getTime());
                        KtMessageObject ktMessageObject = new KtMessageObject();
                        ktMessageObject.setMessage(records.getMsg());
                        ktMessageObject.setFrom_id(records.getFID());
                        ktMessageObject.setFrom_name(records.getFromName());
                        ktMessageObject.setTo_id(records.getTID());
                        ktMessageObject.setTo_name(records.getToName());
                        ktMessageObject.setTime(records.getTime());
                        boolean exists = myDatabase.checkIfAlreadyExistsInKtMessage(ktMessageObject);
                        if (exists) {
                            System.out.println("exists");
                        } else {
                            System.out.println("does not exist");
                            myDatabase.saveMessage(ktMessageObject);

                            // create messageRow object to add to the listView
                            MessageRowItem rowItem = new MessageRowItem();
                            rowItem.setMessageText(ktMessageObject.getMessage());
                            rowItem.setMessageSender(ktMessageObject.getFrom_name());
                            rowItem.setMessageSenderID(ktMessageObject.getFrom_id());
                            rowItem.setMessageRecipient(ktMessageObject.getTo_name());
                            rowItem.setMessageRecipientID(ktMessageObject.getTo_id());
                            rowItem.setMessageTimestamp(ktMessageObject.getTime());

                            messageRowItems.add(rowItem);
                            messageDialogueListViewAdapter.notifyDataSetChanged();

                            //TODO need to change all this to messageRowItems

                            //messagingUIListViewArrayItems.add(conversationListItem);
                            //conversationListAdapter.notifyDataSetChanged();
                            listView.invalidate();

                        }
                    }

                    args[0] = null;
                }
            });
        }
    };

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private void setUpXml(String user_name) {
        messagingUIUserName = (TextView) findViewById(R.id.MessageDialogue_UserName);
        messagingUIUserName.setText(user_name);
        messagingUIUserName.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
        messagingUIUserName.setTextSize(40);
        Typeface typeface = Typeface.createFromAsset(getAssets(), "fonts/stalemate_regular.ttf");
        messagingUIUserName.setTypeface(typeface);
    }

    public void invalidateMessageUIListView() {
        conversationListAdapter.notifyDataSetChanged();
        listView.invalidate();
    }

    @Override
    public void onResume() {
        super.onResume();
        connectSocket();
    }

    @Override
    public void onDestroy() {
        socket.disconnect();
        socket.close();
        //LocalBroadcastManager.getInstance(this).unregisterReceiver(newMessageBroadcastReceiver);
        super.onDestroy();
        Intent result = new Intent();
        setResult(RESULT_OK, result);
        finish();
    }

    @Override
    public void onPause() {
        super.onPause();
        socket.disconnect();
    }

}
