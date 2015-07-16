package com.jimchen.kanditag;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
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

import com.github.ksoichiro.android.observablescrollview.ObservableListView;
import com.github.ksoichiro.android.observablescrollview.ObservableScrollView;
import com.github.ksoichiro.android.observablescrollview.ObservableScrollViewCallbacks;
import com.github.ksoichiro.android.observablescrollview.ScrollState;
import com.github.ksoichiro.android.observablescrollview.ScrollUtils;
import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.ByteArrayOutputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;

public class MessageDialogue extends ActionBarActivity implements ObservableScrollViewCallbacks {

    // boolean for group message
    private boolean isGroupDialogue = false;

    // request codes
    private int ReturnToMessageActivityRequestCode = 1;

    private static final String TAG = "MessageDialogue";
    private SharedPreferences sharedPreferences;
    public static final String USER_PREFERENCES = "com.jimchen.kanditag.extra.PREFERENCES";
    public static final String USERNAME = "com.jimchen.kanditag.extra.USERNAME";
    public static final String FBID = "com.jimchen.kanditag.extra.FBID";
    public static final String KTID = "com.jimchen.kanditag.extra.KTID";

    public static final String ACTION_NEW_MESSAGE = "com.jimchen.kanditag.action.NEW_MESSAGE";
    public static final String NEW_MESSAGE_EXTRA = "com.jimchen.kanditag.extra.NEW_MESSAGE";

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
            messageDialogueListViewAdapter = new MessageDialogueListViewAdapter(MessageDialogue.this, R.layout.message_dialogue_row_item, messageRowItems);
            //listView.setAdapter(messageDialogueListViewAdapter);
            obListView.setAdapter(messageDialogueListViewAdapter);
        }
    });

    // background task to get all group messages for list view
    private GetAllGroupMessagesFromLocalDbAsyncTask getGroupMessagesTask = new GetAllGroupMessagesFromLocalDbAsyncTask(this, new ReturnMessageRowItemArrayListAsyncResponse() {
        @Override
        public void processFinish(ArrayList<MessageRowItem> output) {
            messageRowItems.addAll(output);
            messageDialogueListViewAdapter = new MessageDialogueListViewAdapter(MessageDialogue.this, R.layout.message_dialogue_row_item, messageRowItems);
            //listView.setAdapter(messageDialogueListViewAdapter);
            obListView.setAdapter(messageDialogueListViewAdapter);
        }
    });

    private ArrayList<MessageRowItem> messageRowItems = new ArrayList<>();

    //adapter for list view
    private MessageDialogueListViewAdapter messageDialogueListViewAdapter;

    // observable list view callbacks
    @Override
    public void onScrollChanged(int scrollY, boolean firstScroll, boolean dragging) {
        int baseColor = getResources().getColor(R.color.gold);
        float alpha = Math.min(1, (float) scrollY / mParallaxImageHeight);
        mToolbarView.setBackgroundColor(ScrollUtils.getColorWithAlpha(alpha, baseColor));
        ViewHelper.setTranslationY(mImageView, -scrollY / 2);

        // Translate list background
        ViewHelper.setTranslationY(mListBackgroundView, Math.max(0, -scrollY + mParallaxImageHeight));
    }

    @Override
    public void onDownMotionEvent() {
        Log.d(TAG, "ondownmotionevent");
    }

    @Override
    public void onUpOrCancelMotionEvent(ScrollState scrollstate) {
        Log.d(TAG, "onuporcancel");
    }

    private ObservableListView obListView;

    private ImageView mImageView;
    private Toolbar mActionToolbar;
    private View mToolbarView;
    private View mListBackgroundView;
    private int mParallaxImageHeight;

    private EditText mEditText;

// OnCreate ****************************************************************************************
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_message_dialogue);
        setContentView(R.layout.message_dialogue_ob);
        //getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        System.out.println("MessageDialogueOb.onCreate");

        myDatabase = new KtDatabase(this);

        sharedPreferences = getSharedPreferences(USER_PREFERENCES, MODE_PRIVATE);
        MY_KT_ID = sharedPreferences.getString(KTID, "");
        MY_USER_NAME = sharedPreferences.getString(USERNAME, "");
        MY_FB_ID = sharedPreferences.getString(FBID, "");


        //LocalBroadcastManager.getInstance(this).registerReceiver(newMessageBroadcastReceiver, new IntentFilter("new_message"));

        //TODO need to change all fb_ids usage to kt_id

        Bundle bundleParams = getIntent().getExtras();

        //try to get kt_id and username, if successful, this is a regular message dialogue

        try {
            kt_id = bundleParams.getString("kt_id");
            getMessagesTask.execute(kt_id);
            Log.d(TAG, kt_id);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            user_name = bundleParams.getString("username");
            Log.d(TAG, user_name);
            mActionToolbar = (Toolbar) findViewById(R.id.MessageDialogueOb_toolbar);
            mActionToolbar.setTitle(user_name);
            setSupportActionBar((Toolbar) findViewById(R.id.MessageDialogueOb_toolbar));
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
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
                mActionToolbar = (Toolbar) findViewById(R.id.MessageDialogueOb_toolbar);
                mActionToolbar.setTitle(kandi_name);
                setSupportActionBar((Toolbar) findViewById(R.id.MessageDialogueOb_toolbar));
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                isGroupDialogue = true;
                getGroupMessagesTask.execute(kandi_id);
                System.out.println(kandi_name);
                System.out.println("group message");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        //setSupportActionBar((Toolbar) findViewById(R.id.MessageDialogueOb_toolbar));

        mImageView = (ImageView) findViewById(R.id.MessageDialogueOb_image);
        mToolbarView = findViewById(R.id.MessageDialogueOb_toolbar);
        mToolbarView.setBackgroundColor(ScrollUtils.getColorWithAlpha(0, getResources().getColor(R.color.gold)));

        mParallaxImageHeight = getResources().getDimensionPixelSize(R.dimen.parallax_image_height);

        obListView = (ObservableListView) findViewById(R.id.MessageDialogueOb_list);
        obListView.setScrollViewCallbacks(this);

        View paddingView = new View(this);
        AbsListView.LayoutParams lp = new AbsListView.LayoutParams(AbsListView.LayoutParams.MATCH_PARENT,
                mParallaxImageHeight);
        paddingView.setLayoutParams(lp);

        // This is required to disable header's list selector effect
        paddingView.setClickable(true);

        obListView.addHeaderView(paddingView);

        // mListBackgroundView makes ListView's background except header view.
        mListBackgroundView = findViewById(R.id.MessageDialogueOb_list_background);

        mEditText = (EditText) findViewById(R.id.MessageDialogueOb_TextEdit);
        mEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO need to scroll to bottom of listview
                obListView.smoothScrollToPosition(messageDialogueListViewAdapter.getCount());
            }
        });

        byte[] image = getProfileImage(MY_FB_ID);
        Bitmap pic = BitmapFactory.decodeByteArray(image, 0, image.length);
        mImageView.setImageBitmap(pic);

        /**
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

        messageUISendButton = (Button) findViewById(R.id.MessageDialogue_SendButton);
        messageUISendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (messageUIEditText.getText().toString().length() > 1) {
                    messageToSend = messageUIEditText.getText().toString();
                    //TODO brush up on the animations
                    Animation anim = AnimationUtils.loadAnimation(MessageDialogue.this, R.anim.left_slide_out);
                    messageUISendButton.startAnimation(anim);
                    //Send the message
                    if (!isGroupDialogue) {
                        newMessage(messageToSend, MY_KT_ID, MY_USER_NAME, kt_id, user_name);
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
        }); **/

    }

// end of OnCreate *********************************************************************************

    private byte[] getProfileImage(String id) {
        URL img_value = null;
        Bitmap mIcon = null;
        try {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
            img_value = new URL("https://graph.facebook.com/" + id + "/picture?width=1000&height=1000");
            mIcon = BitmapFactory.decodeStream(img_value.openConnection().getInputStream());
        } catch (Exception e) {
            e.printStackTrace();
        }
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        mIcon.compress(Bitmap.CompressFormat.PNG, 0, stream);
        return stream.toByteArray();
    }

    private void connectSocket() {
        try {
            IO.Options options = new IO.Options();
            options.forceNew = true;
            socket = IO.socket(HOST, options);
            if (!isGroupDialogue) {
                socket.on("message", onNewMessage);
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
                socket.emit("sign_in", MY_KT_ID);
            }
        }).start();
    }

    private void newGroupMessage(String message, String from_id, String from_name, String to_kandi_id, String to_kandi_name) {

        KtMessageObject mssg = new KtMessageObject();
        mssg.setMessage(message);
        mssg.setFrom_id(from_id);
        mssg.setFrom_name(from_name);
        mssg.setTo_Kandi_Id(to_kandi_id);
        mssg.setTo_Kandi_Name(to_kandi_name);

        Log.d(TAG, "newGroupMessage");

        JsonQrObject json = new JsonQrObject();
        json.setMessage(message);
        json.setFrom_id(from_id);
        json.setFrom_name(from_name);
        json.setTo_kandi_id(to_kandi_id);
        json.setTo_kandi_name(to_kandi_name);

        socket.emit("group_message", json);
    }

    private void newMessage(String message, String from_id, String from_name, String to_id, String to_name) {

        // TODO send local broadcast to self and update listview
        // add message to list
        // when response is heard from message listener
        // remove message from list
        // if no response then alert that message was not sent

        KtMessageObject mssg = new KtMessageObject();
        mssg.setMessage(message);
        mssg.setFrom_id(from_id);
        mssg.setFrom_name(from_name);
        mssg.setTo_id(to_id);
        mssg.setTo_name(to_name);

        // TODO
        // add this mssg into the listview
        // wait for response from the socket
        // check to make sure the response matches up to one the the rows
        // if there is no response after 30 seconds, show send failed


        Log.d(TAG, "newMessage");

        JsonQrObject json = new JsonQrObject();
        json.setMessage(message);
        json.setFrom_id(from_id);
        json.setFrom_name(from_name);
        json.setTo_id(to_id);
        json.setTo_name(to_name);

        socket.emit("message", json);
    }

    private Emitter.Listener onGroupMessage =  new Emitter.Listener() {
        @Override
        public void call(final Object... args) {

            // TODO this should not be running on the main thread
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    String message = (String) args[0];
                    System.out.println("group message " + message);
                    GsonBuilder gsonBuilder = new GsonBuilder();
                    gsonBuilder.registerTypeAdapter(ResponseResults.class, new ResponseResultsDeserializer());
                    Gson gson = gsonBuilder.create();

                    ResponseResults results = gson.fromJson(message, ResponseResults.class);
                    /**
                     System.out.println(results.getMessage());
                     System.out.println(results.getFrom_id());
                     System.out.println(results.getFrom_name());
                     System.out.println(results.getTo_kandi_id());
                     System.out.println(results.getTo_kandi_name());
                     System.out.println(results.getTimestamp());
                     **/

                    KtMessageObject messageObject = new KtMessageObject();
                    messageObject.setMessage(results.getMessage());
                    messageObject.setFrom_id(results.getFrom_id());
                    messageObject.setFrom_name(results.getFrom_name());
                    messageObject.setTo_Kandi_Id(results.getTo_kandi_id());
                    messageObject.setTo_Kandi_Name(results.getTo_kandi_name());
                    messageObject.setTimestamp(results.getTimestamp());

                    boolean exists = myDatabase.checkIfAlreadyExistsInKtMessage(messageObject);
                    if (!exists) {
                        myDatabase.saveGroupMessage(messageObject);

                        // create messageRow object to add to the listView
                        MessageRowItem rowItem = new MessageRowItem();
                        rowItem.setMessageContent(messageObject.getMessage());
                        rowItem.setFrom_Name(messageObject.getFrom_name());
                        rowItem.setFrom_Id(messageObject.getFrom_id());
                        rowItem.setTo_Kandi_Name(messageObject.getTo_Kandi_Name());
                        rowItem.setTo_Kandi_Id(messageObject.getTo_Kandi_Id());
                        rowItem.setTimestamp(messageObject.getTimestamp());

                        messageRowItems.add(rowItem);
                        messageDialogueListViewAdapter.notifyDataSetChanged();

                        //TODO need to change all this to messageRowItems

                        //messagingUIListViewArrayItems.add(conversationListItem);
                        //conversationListAdapter.notifyDataSetChanged();
                        listView.invalidate();

                    } else {
                        Log.d(TAG, "already exists in db");
                    }
                }
            });
        }
    };

    private Emitter.Listener onNewMessage = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {

            // TODO why is this running on the main thread?
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    String message = (String) args[0];
                    System.out.println("message " + message);
                    GsonBuilder gsonBuilder = new GsonBuilder();
                    gsonBuilder.registerTypeAdapter(ResponseResults.class, new ResponseResultsDeserializer());
                    Gson gson = gsonBuilder.create();

                    ResponseResults results = gson.fromJson(message, ResponseResults.class);
                    /**
                    System.out.println(results.getMessage());
                    System.out.println(results.getFrom_id());
                    System.out.println(results.getFrom_name());
                    System.out.println(results.getTo_id());
                    System.out.println(results.getTo_name());
                    System.out.println(results.getTimestamp());
                     **/

                    KtMessageObject messageObject = new KtMessageObject();
                    messageObject.setMessage(results.getMessage());
                    messageObject.setFrom_id(results.getFrom_id());
                    messageObject.setFrom_name(results.getFrom_name());
                    messageObject.setTo_id(results.getTo_id());
                    messageObject.setTo_name(results.getTo_name());
                    messageObject.setTimestamp(results.getTimestamp());

                    boolean exists = myDatabase.checkIfAlreadyExistsInKtMessage(messageObject);
                    if (!exists) {
                        myDatabase.saveMessage(messageObject);

                        // create messageRow object to add to the listView
                        MessageRowItem rowItem = new MessageRowItem();
                        rowItem.setMessageContent(messageObject.getMessage());
                        rowItem.setFrom_Name(messageObject.getFrom_name());
                        rowItem.setFrom_Id(messageObject.getFrom_id());
                        rowItem.setTo_Name(messageObject.getTo_name());
                        rowItem.setTo_Id(messageObject.getTo_id());
                        rowItem.setTimestamp(messageObject.getTimestamp());

                        messageRowItems.add(rowItem);
                        messageDialogueListViewAdapter.notifyDataSetChanged();

                        //TODO need to change all this to messageRowItems

                        //messagingUIListViewArrayItems.add(conversationListItem);
                        //conversationListAdapter.notifyDataSetChanged();
                        listView.invalidate();

                    } else {
                        Log.d(TAG, "already exists in db");
                    }


                    /**
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
                            rowItem.setMessageContent(ktMessageObject.getMessage());
                            rowItem.setFrom_Name(ktMessageObject.getFrom_name());
                            rowItem.setFrom_Id(ktMessageObject.getFrom_id());
                            rowItem.setTo_Name(ktMessageObject.getTo_name());
                            rowItem.setTo_Id(ktMessageObject.getTo_id());
                            rowItem.setTimestamp(ktMessageObject.getTimestamp());

                            messageRowItems.add(rowItem);
                            messageDialogueListViewAdapter.notifyDataSetChanged();

                            //TODO need to change all this to messageRowItems

                            //messagingUIListViewArrayItems.add(conversationListItem);
                            //conversationListAdapter.notifyDataSetChanged();
                            listView.invalidate();

                        }
                    }
                     **/

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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Log.d(TAG, "clicked");
                finish();
                overridePendingTransition(R.anim.right_slide_in, R.anim.right_slide_out);
        }

        return (super.onOptionsItemSelected(item));
    }

}
