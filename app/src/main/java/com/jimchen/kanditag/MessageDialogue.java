package com.jimchen.kanditag;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
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

    // boolean for group message
    private boolean isGroupDialogue = false;

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
    private String kandi_id, kandi_name;

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

    // background task to get all group messages for list view
    private GetAllGroupMessagesFromLocalDbAsyncTask getGroupMessagesTask = new GetAllGroupMessagesFromLocalDbAsyncTask(this, new ReturnMessageRowItemArrayListAsyncResponse() {
        @Override
        public void processFinish(ArrayList<MessageRowItem> output) {
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

        //try to get kt_id and username, if successful, this is a regular message dialogue

        try {
            kt_id = bundleParams.getString("kt_id");
            getMessagesTask.execute(kt_id);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            user_name = bundleParams.getString("username");
            if (!user_name.equals("")) {
                System.out.println(user_name);
                isGroupDialogue = false;
                System.out.println("regular message");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // try to get kandi_id and kandi_name, if successful, this is a group message

        try {
            kandi_id = bundleParams.getString("kandi_id");
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            kandi_name = bundleParams.getString("kandi_name");
            if (!kandi_name.equals("")) {
                isGroupDialogue = true;
                getGroupMessagesTask.execute(kandi_id);
                System.out.println(kandi_name);
                System.out.println("group message");
            }
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
        if (!isGroupDialogue) {
            userNameTextView.setText(user_name);
        } else if (isGroupDialogue) {
            userNameTextView.setText(kandi_name);
        }

        // edit text to write message
        //TODO need to make this expand when message gets long
        messageUIEditText = (EditText) findViewById(R.id.MessageDialogue_EditText);
        messageUIEditText.setTextColor(Color.WHITE);

        messageUISendButton = (Button) findViewById(R.id.MessageDialogue_SendButton);
        messageUISendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sharedPreferences = getSharedPreferences(MY_PREFERENCES, Context.MODE_PRIVATE);
                if (messageUIEditText.getText().toString().length() > 1) {
                    messageToSend = messageUIEditText.getText().toString();
                    //TODO brush up on the animations
                    Animation anim = AnimationUtils.loadAnimation(MessageDialogue.this, R.anim.left_slide_out);
                    messageUISendButton.startAnimation(anim);
                    //Send the message
                    if (!isGroupDialogue) {
                        newMessage(messageToSend, kt_id, MY_KT_ID, user_name, MY_USER_NAME);
                    } else if (isGroupDialogue) {
                        newGroupMessage(messageToSend, MY_KT_ID, MY_USER_NAME, kandi_id, kandi_name);
                    }

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
            if (!isGroupDialogue) {
                socket.on("privatemessage", onNewMessage);
            } else if (isGroupDialogue) {
                socket.on("group_message", onGroupMessage);
            }
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

    private void newGroupMessage(String message, String myKTID, String fromName, String kandiID, String kandiName) {
        System.out.println("MessageDialogue.newGroupMessage");
        socket.emit("group_message", message, myKTID, fromName, kandiID, kandiName);
    }

    private void newMessage(String message, String toKTID, String myKTID, String toName, String fromName) {
        System.out.println("MessageDialogue.newMessage");
        socket.emit("privatemessage", message, toKTID, myKTID, toName, fromName);
    }

    private Emitter.Listener onGroupMessage =  new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    String message = (String) args[0];
                    System.out.println("group message " + message);
                    GsonBuilder gsonBuilder = new GsonBuilder();
                    gsonBuilder.registerTypeAdapter(Res_End_Results.class, new Res_End_Results_Deserializer());
                    gsonBuilder.registerTypeAdapter(Records.class, new Records_Deserializer());
                    Gson gson = gsonBuilder.create();

                    Res_End_Results re_obj = gson.fromJson(message, Res_End_Results.class);
                    for (Records records:re_obj.getRecords()) {
                        System.out.println("messages sent =" + records.getMessage());
                        System.out.println("messages sent from id =" + records.getFrom_id());
                        System.out.println("messages sent to kandi_id =" + records.getKandiID());
                        System.out.println("messages sent to kandi_name =" + records.getKandi_name());
                        System.out.println("messages sent from name =" + records.getFrom_name());
                        System.out.println("messages sent timestamp =" + records.getTimestamp());
                        KtMessageObject ktMessageObject = new KtMessageObject();
                        ktMessageObject.setMessage(records.getMessage());
                        ktMessageObject.setFrom_id(records.getFrom_id());
                        ktMessageObject.setFrom_name(records.getFrom_name());
                        ktMessageObject.setKandiID(records.getKandiID());
                        ktMessageObject.setKandiName(records.getKandi_name());
                        ktMessageObject.setTimestamp(records.getTimestamp());
                        boolean exists = myDatabase.checkIfGroupMessageExists(ktMessageObject);
                        if (exists) {
                            System.out.println("exists");
                        } else {
                            System.out.println("does not exist");
                            myDatabase.saveGroupMessage(ktMessageObject);

                            // create messageRow object to add to the listView
                            MessageRowItem rowItem = new MessageRowItem();
                            rowItem.setMessageText(ktMessageObject.getMessage());
                            rowItem.setMessageSender(ktMessageObject.getFrom_name());
                            rowItem.setMessageSenderID(ktMessageObject.getFrom_id());
                            rowItem.setMessageKandiName(ktMessageObject.getKandiName());
                            rowItem.setMessageKandiID(ktMessageObject.getKandiID());
                            rowItem.setMessageTimestamp(ktMessageObject.getTimestamp());

                            messageRowItems.add(rowItem);
                            messageDialogueListViewAdapter.notifyDataSetChanged();

                            //TODO need to change all this to messageRowItems

                            //messagingUIListViewArrayItems.add(conversationListItem);
                            //conversationListAdapter.notifyDataSetChanged();
                            listView.invalidate();

                        }
                    }
                }
            });
        }
    };

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
                        System.out.println("messages sent =" + records.getMessage());
                        System.out.println("messages sent from id =" + records.getFrom_id());
                        System.out.println("messages sent to id =" + records.getTo_id());
                        System.out.println("messages sent to name =" + records.getTo_name());
                        System.out.println("messages sent from name =" + records.getFrom_name());
                        System.out.println("messages sent timestamp =" + records.getTimestamp());
                        KtMessageObject ktMessageObject = new KtMessageObject();
                        ktMessageObject.setMessage(records.getMessage());
                        ktMessageObject.setFrom_id(records.getFrom_id());
                        ktMessageObject.setFrom_name(records.getFrom_name());
                        ktMessageObject.setTo_id(records.getTo_id());
                        ktMessageObject.setTo_name(records.getTo_name());
                        ktMessageObject.setTimestamp(records.getTimestamp());
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
                            rowItem.setMessageTimestamp(ktMessageObject.getTimestamp());

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
        Intent result = new Intent(this, MessageFragment.class);
        setResult(RESULT_OK, result);
        finish();
    }

    @Override
    public void onPause() {
        super.onPause();
        socket.disconnect();
    }

}
