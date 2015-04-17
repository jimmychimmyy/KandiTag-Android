package com.jimchen.kanditag;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
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

    private static final String TAG = "MessageDialogue";
    SharedPreferences sharedPreferences;
    public static final String MyPreferences = "MyPrefs";
    public static final String Name = "nameKey";
    public static final String FbId = "fbidKey";
    public static final String UserId = "userIdKey";
    public static final String NEW_MESSAGE = "NEW_MESSAGE";

    private String MY_KT_ID, MY_FB_ID, MY_USER_NAME;
    private String kt_id, fb_id, user_name;

    private String messageToSend;

    KtDatabase myDatabase;

    private final String HOST = "http://kandi.jit.su/";
    private final int portNumber = 3000;


    //textview for username
    private TextView userNameTextView;

    private ImageView exitButton;

    private TextView messagingUIUserName;
    private TableLayout messagingUITableLayout;

    private ListView messagingUIListView;
    private ConversationListAdapter conversationListAdapter;
    private ConversationListItem tempConversationListItem;

    private ArrayList<ConversationListItem> conversationListItemsShown;

    private ArrayList<ConversationListItem> conversationListItems;

    private ScrollView messageUIScrollView;

    private EditText messageUIEditText;

    private Button messageUISendButton;

    private static com.github.nkzawa.socketio.client.Socket socket;

    private ArrayList<ConversationListItem> messagingUIListViewArrayItems = new ArrayList<>();

    // task to which takes a fb_id and returns an array of messages between you and the (fb_id's) user
    public class GetEntireConversationAsyncTask extends AsyncTask<String, Void, ArrayList<ConversationListItem>> {

        private ArrayList<ConversationListItem> conversationListItemArrayList;
        @Override
        protected ArrayList<ConversationListItem> doInBackground(String... params) {

            String fb_id = params[0];

            conversationMessagesArrayList = myDatabase.getEntireConversationPrivateMessage(fb_id);

            return  conversationMessagesArrayList;

        }

        @Override
        protected void onPreExecute() {
            conversationListItemArrayList = new ArrayList<>();
        }

        @Override
        protected void onPostExecute(ArrayList<ConversationListItem> list) {
            messagingUIListViewArrayItems = list;
            System.out.println("sizeOfConversationListItemArrayList = " + list.size());
            conversationListAdapter = new ConversationListAdapter(MessageDialogue.this, R.layout.conversation_list_item, messagingUIListViewArrayItems, MY_FB_ID);
            messagingUIListView.setAdapter(conversationListAdapter);
            //scrollMyListViewToBottom();
        }
    }

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

    GetSingleUserForMessageAsyncTask getSingleUserForMessageAsyncTask = new GetSingleUserForMessageAsyncTask(MessageDialogue.this, new ReturnKtUserObjectAsyncResponse() {
        @Override
        public void processFinish(KtUserObject output) {
            System.out.println("MessageWithUser.getSingleUserForMessageAsyncTask.processFinish.output.kt_id = " + output.getKt_id());
            System.out.println("MessageWithUser.getSingleUserForMessageAsyncTask.processFinish.output.user_name = " + output.getName());
            user_name = output.getName();
            userNameTextView.setText(user_name);
            //setUpXml(user_name);

            //originally this was a framelayout which added a fragment with the username
            // instead im just going to make this a view
            /**

            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.setCustomAnimations(R.anim.abc_slide_in_top, R.anim.abc_slide_out_top);
            BannerFragment bannerFragment = new BannerFragment();
            Bundle extras = new Bundle();
            extras.putString("title", user_name);
            bannerFragment.setArguments(extras);
            fragmentTransaction.add(R.id.MessageDialogue_FrameLayout, bannerFragment);
            fragmentTransaction.commit();

             **/

            GetEntireConversationAsyncTask getEntireConversationAsyncTask = new GetEntireConversationAsyncTask();
            getEntireConversationAsyncTask.execute(output.getFb_id());
        }
    });

    private BroadcastReceiver newMessageBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent.getAction().equals("new_message")) {

                String message = intent.getStringExtra("message");
                String from_id = intent.getStringExtra("from_id");
                String from_name = intent.getStringExtra("from_name");
                String to_id = intent.getStringExtra("to_id");
                String to_name = intent.getStringExtra("to_name");
                String time = intent.getStringExtra("time");

                ConversationListItem conversationListItem = new ConversationListItem();
                conversationListItem.setMessage(message);
                conversationListItem.setSenderID(from_id);
                conversationListItem.setSenderName(from_name);
                conversationListItem.setRecipientID(to_id);
                conversationListItem.setRecipientName(to_name);
                conversationListItem.setTime(time);

                messagingUIListViewArrayItems.add(conversationListItem);
                conversationListAdapter.notifyDataSetChanged();

                messagingUIListView.invalidate();

                Log.d(TAG, message);
            }
        }
    };

// OnCreate ****************************************************************************************
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_dialogue);
        //getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        System.out.println("MessageWithUser.onCreate");

        myDatabase = new KtDatabase(this);

        sharedPreferences = getSharedPreferences(MyPreferences, MODE_PRIVATE);
        MY_KT_ID = sharedPreferences.getString(UserId, "");
        MY_USER_NAME = sharedPreferences.getString(Name, "");
        MY_FB_ID = sharedPreferences.getString(FbId, "");

        //LocalBroadcastManager.getInstance(this).registerReceiver(newMessageBroadcastReceiver, new IntentFilter("new_message"));

        Bundle bundleParams = getIntent().getExtras();
        //kt_id = bundleParams.getString("kt_id");
        fb_id = bundleParams.getString("fb_id");
        //user_name = bundleParams.getString("user_name");

        getSingleUserForMessageAsyncTask.execute(fb_id);

        exitButton = (ImageView) findViewById(R.id.MessageDialogue_ExitButton);
        exitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                MessageDialogue.this.overridePendingTransition(R.anim.right_slide_in, R.anim.right_slide_out);
            }
        });

        messagingUIListView = (ListView) findViewById(R.id.MessageDialogue_ListView);
        messagingUIListView.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
        //messagingUIListView.setOnScrollListener(messageWithUserScrollListener);


        //GetEntireConversationAsyncTask getEntireConversationAsyncTask = new GetEntireConversationAsyncTask();
        //getEntireConversationAsyncTask.execute(kt_id);

        /**

        messagingUITableLayout = (TableLayout) findViewById(R.id.messagingUITableLayout);


        for (int i = 0; i < messageList.size(); i++) {
            //System.out.println(messageList.get(i));
            TableRow tableRow = new TableRow(this);
            tableRow.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
            Button button = new Button(this);
            button.setBackgroundColor(Color.TRANSPARENT);
            button.setTextColor(Color.WHITE);
            button.setText(messageList.get(i));
            button.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT, Gravity.CENTER));
            tableRow.addView(button);
            messagingUITableLayout.addView(tableRow);
        }

         **/

        /**

        messageUIScrollView = (ScrollView) findViewById(R.id.messageUIScrollView);

        messageUIScrollView.post(new Runnable() {
            @Override
            public void run() {
                messageUIScrollView.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });

         **/

        userNameTextView = (TextView) findViewById(R.id.MessageDialogue_UserNameTextView);
        Typeface typeface = Typeface.createFromAsset(getAssets(), "fonts/kanditagfont-sth.ttf");
        userNameTextView.setTypeface(typeface);


        messageUIEditText = (EditText) findViewById(R.id.MessageDialogue_EditText);
        //messageUIEditText.setBackgroundColor(Color.TRANSPARENT);
        messageUIEditText.setTextColor(Color.WHITE);

        messageUISendButton = (Button) findViewById(R.id.MessageDialogue_SendButton);
        //messageUISendButton.setBackgroundColor(Color.TRANSPARENT);
        //messageUISendButton.setBackgroundResource(R.drawable.send_icon);
        messageUISendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sharedPreferences = getSharedPreferences(MyPreferences, Context.MODE_PRIVATE);
                if (messageUIEditText.getText().toString().length() > 1) {
                    messageToSend = messageUIEditText.getText().toString();
                    //TODO brush up on the animations
                    Animation anim = AnimationUtils.loadAnimation(MessageDialogue.this, R.anim.left_slide_out);
                    messageUISendButton.startAnimation(anim);
                    //Sending the message
                    newMessage(messageToSend, fb_id, MY_FB_ID, user_name, MY_USER_NAME);

                    if (messageToSend != null) {
                        messageUIEditText.setText("");
                    }

                    //TODO send message and have emitter listener notify the changes to the db
                    conversationListAdapter.notifyDataSetChanged();


                    //socket.on("privatemessage", messageClass.onNewMessage);
                    //messageList.add(messageUIEditText.getText().toString());
                    /**
                    TableRow tableRow = new TableRow(MessagingUI.this);
                    tableRow.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT, Gravity.CENTER_HORIZONTAL));
                    Button button = new Button(MessagingUI.this);
                    button.setBackgroundColor(Color.TRANSPARENT);
                    button.setText(messageUIEditText.getText().toString());
                    button.setTextColor(Color.WHITE);
                    button.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT, Gravity.CENTER_HORIZONTAL));
                    tableRow.addView(button);
                    messagingUITableLayout.addView(tableRow);
                    messagingUITableLayout.invalidate();

                    messageUIScrollView.post(new Runnable() {
                        @Override
                        public void run() {
                            messageUIScrollView.fullScroll(ScrollView.FOCUS_DOWN);
                        }
                    });

                    messageUIEditText.setText("");
                     **/
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

    private void newMessage(String message, String toFb_id, String myFb_id, String toName, String fromName) {
        System.out.println("MessageDialogue.class.newMessage");
        socket.emit("privatemessage", message, toFb_id, myFb_id, toName, fromName);
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
                        System.out.println("messages+sent =" + records.getMsg());
                        System.out.println("messages+sentfrom =" + records.getFID());
                        System.out.println("messages+sentto =" + records.getTID());
                        System.out.println("messages+sentwhen =" + records.getDate());
                        System.out.println("messages+senttoname =" + records.getToName());
                        System.out.println("messages+sentfromname =" + records.getFromName());
                        System.out.println("messages+senttime =" + records.getTime());
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

                            ConversationListItem conversationListItem = new ConversationListItem();

                            conversationListItem.setMessage(ktMessageObject.getMessage());
                            conversationListItem.setSenderID(ktMessageObject.getFrom_id());
                            conversationListItem.setSenderName(ktMessageObject.getFrom_name());
                            conversationListItem.setRecipientID(ktMessageObject.getTo_id());
                            conversationListItem.setRecipientName(ktMessageObject.getTo_name());
                            conversationListItem.setTime(ktMessageObject.getTime());

                            messagingUIListViewArrayItems.add(conversationListItem);
                            conversationListAdapter.notifyDataSetChanged();
                            messagingUIListView.invalidate();

                            /**
                            Intent new_message_intent = new Intent("new_message");
                            new_message_intent.putExtra("message", ktMessageObject.getMessage());
                            new_message_intent.putExtra("from_id", ktMessageObject.getFrom_id());
                            new_message_intent.putExtra("from_name", ktMessageObject.getFrom_name());
                            new_message_intent.putExtra("to_id", ktMessageObject.getTo_id());
                            new_message_intent.putExtra("to_name", ktMessageObject.getTo_name());
                            new_message_intent.putExtra("time", ktMessageObject.getTime());

                            LocalBroadcastManager.getInstance(MessageDialogue.this).sendBroadcast(new_message_intent);
                            //TODO cannot do this because the objects inside invalidate..method are null without context
                            //MessageWithUser messageWithUser = new MessageWithUser();
                            //messageWithUser.invalidateMessageUIListView();
                             **/

                        }
                    }

                    args[0] = null;
                }
            });
        }
    };

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
        messagingUIListView.invalidate();
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
    }

    @Override
    public void onPause() {
        super.onPause();
        socket.disconnect();
    }
}
