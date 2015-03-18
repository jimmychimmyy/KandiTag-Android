package com.kanditag.kanditag;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.StrictMode;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.util.ArrayList;


public class Message extends Activity {

    //temporary var
    private ImageView switchButtonForNewMessage;
    private ImageView switchButton;


//Strings ****
    private static final String TAG = "Message";
    private final String HOST = "http://kandi.jit.su/";
    private final int portNumber = 3000;

    private static final int TYPE_INCOMING_MESSAGE = 1;
    private static final int CONVERSATION_INTENT = 2;


    // Preferences ****
    SharedPreferences sharedPreferences;
    public static final String MyPreferences = "MyPrefs";
    public static final String Name = "nameKey";
    public static final String FbId = "fbidKey";
    public static final String UserId = "userIdKey";
    public static final String NEW_MESSAGE = "NEW_MESSAGE";

    private String MY_KT_ID, MY_FB_ID, MY_USER_NAME;

    // Database *****
    KtDatabase myDatabase;

// Layout Vars *****
    private TextView messageTitle;

    private ImageView messageBackButton;

    private ImageView newMessageButton;

    private ArrayList<String> listOfAllTables;

    private ArrayList<String> listOfMessageTables;

    private ArrayList<MessageListItem> messageListItemsArray;

    private ArrayList<GroupMessageItem> groupMessageItemArray;

    private ArrayList<MessageListItem> conversationListItems;

    private ListView messageListView;

    private ListView conversationListView;



// Socket Vars *****
    Thread getAllMessagesTask;
    private static com.github.nkzawa.socketio.client.Socket socket;

// Message Vars *****
    private ListView messageList;
    private MessageListAdapter messageListAdapter;
    private GroupMessageListAdapter groupMessageListAdapter;
    private MessageListItem tempMessageListItem;

    //this opens up a private conversation from a push notification
    public class OpenMessageDialogue extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... params) {
            String fb_id = params[0];

            Intent openMessageWithUser = new Intent(Message.this, MessageDialogue.class);
            Bundle param = new Bundle();
            param.putString("fb_id", fb_id);
            openMessageWithUser.putExtras(param);
            startActivity(openMessageWithUser);

            return null;
        }

        @Override
        protected void onPreExecute() {

        }

        @Override
        protected void onPostExecute(Void result) {
            setInvisibleMessageTitleMessageButton();
        }
    }

    //this gets the latest message between two users/ TODO need to grab the latest message from each group
    DisplayLatestMessagesAsyncTask displayLatestMessagesAsyncTask = new DisplayLatestMessagesAsyncTask(Message.this, new ReturnMessageListItemArrayAsyncResponse() {
        @Override
        public void processFinish(final ArrayList<MessageListItem> output) {
            messageListItemsArray = output;
            messageListAdapter = new MessageListAdapter(Message.this, R.id.list_item, messageListItemsArray);
            messageListView = (ListView) findViewById(R.id.Message_ListView);
            messageListView.setAdapter(messageListAdapter);

            messageListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    System.out.println("messageListView.onItemClick.position = " + position);
                    System.out.println("fbId = " + output.get(position).getSender());
                    Intent openMessageWithUser = new Intent(Message.this, MessageDialogue.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("fb_id", output.get(position).getSender());
                    openMessageWithUser.putExtras(bundle);
                    startActivity(openMessageWithUser);
                    setInvisibleMessageTitleMessageButton();
                }
            });
            System.out.println("Message.displayLatestMessagesAsyncTask.processFinish.output.size() = " + output.size());
        }
    });

    DisplayLatestGroupMessageAsyncTask displayLatestGroupMessageAsyncTask = new DisplayLatestGroupMessageAsyncTask(Message.this, new ReturnGroupMessageArrayListAsyncResponse() {
        @Override
        public void processFinish(ArrayList<GroupMessageItem> output) {
            System.out.println("Message.displayLatestGroupMessageAsyncTask.processFinish.output.size() = " + output.size());
            groupMessageItemArray = output;
            groupMessageListAdapter = new GroupMessageListAdapter(Message.this, R.id.list_item, groupMessageItemArray, MY_FB_ID);
        }
    });

    private ArrayList<KtUserObjectParcelable> usersForNewMessageList = new ArrayList<>();
    //this returns a list of all users you are connected to
    GetAllUsersFromLocalDbAsyncTask getAllUsersFromLocalDbAsyncTask = new GetAllUsersFromLocalDbAsyncTask(Message.this, new GetAllUsersFromLocalDbAsyncResponse() {
        @Override
        public void processFinish(ArrayList<KtUserObjectParcelable> output) {
            System.out.println("Message.getAllUsersFromLocalDbAsyncTask.processFinish.output.size() = " + output.size());
            usersForNewMessageList.addAll(output);
        }
    });

    GetAllKandiFromLocalAsyncTask getAllKandiFromLocalAsyncTask = new GetAllKandiFromLocalAsyncTask(Message.this, new ReturnKandiObjectArrayAsyncResponse() {
        @Override
        public void processFinish(ArrayList<KandiObject> output) {
            System.out.println("Message.getAllKandiFromLocalAsyncTask.processFinish.output.size() = " + output.size());
            getAllGroupsFromLocalDbAsyncTask.execute(output);
        }
    });

    private ArrayList<KandiGroupObjectParcelable> groupsForNewMessageList = new ArrayList<>();
    //this returns a list of kandi you own
    GetAllGroupsFromLocalDbAsyncTask getAllGroupsFromLocalDbAsyncTask = new GetAllGroupsFromLocalDbAsyncTask(Message.this, new ReturnKandiGroupObjectParcelableArrayList() {
        @Override
        public void processFinish(ArrayList<KandiGroupObjectParcelable> output) {
            System.out.println("Message.getAllGroupsFromLocalDbAsyncTask.processFinish.output.size() = " + output.size());
            groupsForNewMessageList.addAll(output);
        }
    });


// OnCreate ***************************************************************************************
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        myDatabase = new KtDatabase(this);
        sharedPreferences = getSharedPreferences(MyPreferences, Context.MODE_PRIVATE);

        MY_KT_ID = sharedPreferences.getString(UserId, "");
        MY_USER_NAME = sharedPreferences.getString(Name, "");
        MY_FB_ID = sharedPreferences.getString(FbId, "");

        displayLatestMessagesAsyncTask.execute(myDatabase.getFb_IdFromKtUserToDisplayLatestMessage());
        displayLatestGroupMessageAsyncTask.execute(myDatabase.getKandi());
        getAllUsersFromLocalDbAsyncTask.execute();
        getAllKandiFromLocalAsyncTask.execute();

//  socket ****************************

        //TODO connect to socket, get all messages to populate existing conversations (this includes private message and group message)

        //TODO cross check the server and local database to make sure user has downloaded all possible messages

        try {
            IO.Options options = new IO.Options();
            options.forceNew = true;
            socket = IO.socket(HOST, options);
            socket.on("privatemessage", onNewMessage);
            socket.on("group_message", onNewGroupMessage);
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

        //TODO class this is subclassed, should i move it into its own file?

        /**
        GetMessageAsyncTask getMessageAsyncTask = new GetMessageAsyncTask();
        getMessageAsyncTask.execute();
         **/

// setup layout ************

        messageTitle = (TextView) findViewById(R.id.Message_titleTextView);
        messageTitle.setTextSize(45);
        Typeface typeface = Typeface.createFromAsset(getAssets(),"fonts/stalemate_regular.ttf");
        messageTitle.setTypeface(typeface);


        /**
        //List view for all messages
        messageListAdapter = new MessageListAdapter(this, R.id.list_item, messageListItemsArray);
        messageListView = (ListView) findViewById(R.id.allMessagesListView);
        messageListView.setAdapter(messageListAdapter);

        messageListView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_MOVE) {
                    return true;
                }
                return false;
            }
        });

        messageListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent openMessagingUI = new Intent(Message.this, MessagingUI.class);
                Bundle bundleParams = new Bundle();
                final MessageListItem userObject = messageListItemsArray.get(position);
                final String user = userObject.getSender();
                final String name = userObject.getName();
                bundleParams.putString("user", user);
                bundleParams.putString("name", name);
                openMessagingUI.putExtras(bundleParams);
                messageListView.setVisibility(View.GONE);
                messageTitle.setVisibility(View.GONE);
                startActivityForResult(openMessagingUI, CONVERSATION_INTENT);
            }
        });

         **/

        newMessageButton = (ImageView) findViewById(R.id.Message_newMessageButton);
        newMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //this opens up a list of all users/groups you can send a message to
                switchButton.setVisibility(View.GONE);
                showListOfUsersForMessages(usersForNewMessageList);
                newMessageButton.setVisibility(View.GONE);
                messageBackButton.setVisibility(View.VISIBLE);
                switchButtonForNewMessage.setVisibility(View.VISIBLE);
                switchButtonForNewMessage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showListOfGroupsForMessages(groupsForNewMessageList);
                    }
                });
                setInvisibleMessageTitleMessageButtonForNewMessagePrompt();


            }
        });

        messageBackButton = (ImageView) findViewById(R.id.Message_closeNewMessageButton);
        messageBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchButton.setVisibility(View.VISIBLE);
                messageBackButton.setVisibility(View.GONE);
                newMessageButton.setVisibility(View.VISIBLE);
                FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction().remove(getFragmentManager().findFragmentById(R.id.Message_FrameLayout));
                fragmentTransaction.commit();
                try {
                    messageListView.setVisibility(View.VISIBLE);
                    switchButtonForNewMessage.setVisibility(View.GONE);
                } catch (NullPointerException nullEx) {}
            }
        });
        messageBackButton.setVisibility(View.GONE);


        switchButtonForNewMessage = (ImageView) findViewById(R.id.Message_SwitchBetweenGroupAndUserNewMessage);
        switchButtonForNewMessage.setVisibility(View.GONE);

        //TODO get the push notification to open the right activity stack
        try {
            Bundle bundle = getIntent().getExtras();
            String fb_id = bundle.getString("fb_id");
            if (fb_id != null) {
                //System.out.println("there is a fb id");
                OpenMessageDialogue openMessageDialogue = new OpenMessageDialogue();
                openMessageDialogue.execute(fb_id);
            }
        } catch (NullPointerException nullEx) {}

        switchButton = (ImageView) findViewById(R.id.Message_SwitchBetweenGroupAndUser);
        switchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                messageListView.setAdapter(groupMessageListAdapter);
                messageListView.invalidate();
                messageListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        System.out.println("new on click listener");
                    }
                });
                switchAdapterToSingleUser();
            }
        });
    }

    private void switchAdapterToSingleUser() {
        messageListView.setAdapter(messageListAdapter);
        messageListView.invalidate();
        messageListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                System.out.println("messageListView.onItemClick.position = " + position);
                System.out.println("fbId = " + messageListItemsArray.get(position).getSender());
                Intent openMessageWithUser = new Intent(Message.this, MessageDialogue.class);
                Bundle bundle = new Bundle();
                bundle.putString("fb_id", messageListItemsArray.get(position).getSender());
                openMessageWithUser.putExtras(bundle);
                startActivity(openMessageWithUser);
                setInvisibleMessageTitleMessageButton();
            }
        });
        switchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchAdapterToGroup();
            }
        });
    }

    private void switchAdapterToGroup() {
        messageListView.setAdapter(groupMessageListAdapter);
        messageListView.invalidate();
        messageListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                System.out.println("new on click listener");
            }
        });
        switchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchAdapterToSingleUser();
            }
        });
    }


    public void removeNewUsersForMessage(Fragment fragment) {

    }

    public void RemoveXmlFromMessageLayout() {
        messageTitle.setVisibility(View.GONE);
        newMessageButton.setVisibility(View.GONE);
        messageBackButton.setVisibility(View.VISIBLE);

    }

    private void showListOfUsersForMessages(ArrayList<KtUserObjectParcelable> list) {
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.Message_FrameLayout, DisplayUsersForNewMessage.newInstance(list));
        fragmentTransaction.commit();
    }

    private void showListOfGroupsForMessages(ArrayList<KandiGroupObjectParcelable> list) {
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.Message_FrameLayout, DisplayGroupsForNewMessage.newInstance(list));
        fragmentTransaction.commit();
        switchButtonForNewMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reshowListOfUsersForMessage(usersForNewMessageList);
            }
        });
    }

    private void reshowListOfUsersForMessage(ArrayList<KtUserObjectParcelable> list) {
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.Message_FrameLayout, DisplayUsersForNewMessage.newInstance(list));
        fragmentTransaction.commit();
        switchButtonForNewMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showListOfGroupsForMessages(groupsForNewMessageList);
            }
        });
    }


    //TODO this was for the individual messages but unsure if im going to use this now
    public ArrayList<MessageListItem> getConversationMessages(String user) {
        ArrayList<String> tempSender;
        ArrayList<String> tempMessage;
        ArrayList<MessageListItem> temp = new ArrayList<>();
        String parameter = "kt" + user;

        return temp;
    }


    // Sending and receiving messages

    //this is for messageUI to send a message
    public void newMessage(String message, String toFb_id, String myFb_id, String toName, String fromName) {
        System.out.println("Message.class.newMessage");
        socket.emit("privatemessage", message, toFb_id, myFb_id, toName, fromName);
    }

    public void newGroupMessage(String message, String from_id, String from_name, String qrCode, String kandi_name) {
        System.out.println("Message.class.newGroupMessage");
        socket.emit("group_message", message, from_id, from_name, qrCode, kandi_name);
    }

    public Emitter.Listener onNewGroupMessage = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    String message = (String) args[0];
                    System.out.println("group_message " + message);
                    GsonBuilder gsonBuilder = new GsonBuilder();
                    gsonBuilder.registerTypeAdapter(Res_End_Results.class, new Res_End_Results_Deserializer());
                    gsonBuilder.registerTypeAdapter(Records.class, new Records_Deserializer());
                    Gson gson = gsonBuilder.create();

                    Res_End_Results re_obj = gson.fromJson(message, Res_End_Results.class);
                    /**
                    System.out.println("message = " + re_obj.getGmMessage());
                    System.out.println("from_id = " + re_obj.getGmFrom_id());
                    System.out.println("from_name = " + re_obj.getGmFrom_name());
                    System.out.println("kandi_group = " + re_obj.getGmKandi_group());
                    System.out.println("kandi_name = " + re_obj.getGmKandi_name());
                    System.out.println("time = " + re_obj.getGmTime());
                     **/

                    GroupMessageItem groupMessageItem = new GroupMessageItem();
                    groupMessageItem.setMessage(re_obj.getGmMessage());
                    groupMessageItem.setFromID(re_obj.getGmFrom_id());
                    groupMessageItem.setFromName(re_obj.getGmFrom_name());
                    groupMessageItem.setQrCode(re_obj.getGmKandi_group());
                    groupMessageItem.setTime(re_obj.getGmTime());

                    boolean exists = myDatabase.checkIfGroupMessageExists(groupMessageItem);
                    if (exists) {
                        System.out.println("exists");
                    } else {
                        System.out.println("does not exist");

                        myDatabase.saveGroupMessage(groupMessageItem);

                        Intent new_group_message_intent = new Intent("new_group_message");
                        new_group_message_intent.putExtra("message", groupMessageItem.getMessage());
                        new_group_message_intent.putExtra("from_id", groupMessageItem.getFromID());
                        new_group_message_intent.putExtra("from_name", groupMessageItem.getFromName());
                        new_group_message_intent.putExtra("kandi_group", groupMessageItem.getQrCode());
                        new_group_message_intent.putExtra("kandi_name", re_obj.getGmKandi_name());
                        new_group_message_intent.putExtra("time", groupMessageItem.getTime());

                        LocalBroadcastManager.getInstance(Message.this).sendBroadcast(new_group_message_intent);
                    }
                }
            });
        }

    };

    public Emitter.Listener onNewMessage = new Emitter.Listener() {
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

                        /**
                        System.out.println("messages+sent =" + records.getMsg());
                        System.out.println("messages+sentfrom =" + records.getFID());
                        System.out.println("messages+sentto =" + records.getTID());
                        System.out.println("messages+sentwhen =" + records.getDate());
                        System.out.println("messages+senttoname =" + records.getToName());
                        System.out.println("messages+sentfromname =" + records.getFromName());
                        System.out.println("messages+senttime =" + records.getTime());
                         **/

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

                            Intent new_message_intent = new Intent("new_message");
                            new_message_intent.putExtra("message", ktMessageObject.getMessage());
                            new_message_intent.putExtra("from_id", ktMessageObject.getFrom_id());
                            new_message_intent.putExtra("from_name", ktMessageObject.getFrom_name());
                            new_message_intent.putExtra("to_id", ktMessageObject.getTo_id());
                            new_message_intent.putExtra("to_name", ktMessageObject.getTo_name());
                            new_message_intent.putExtra("time", ktMessageObject.getTime());

                            LocalBroadcastManager.getInstance(Message.this).sendBroadcast(new_message_intent);
                            //TODO cannot do this because the objects inside invalidate..method are null without context
                            //MessageWithUser messageWithUser = new MessageWithUser();
                            //messageWithUser.invalidateMessageUIListView();

                        }
                    }

                    args[0] = null;
                }
            });
        }
    };

    private void getAllMessages() {
        Log.i(TAG, "getAllMessages");

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        sharedPreferences = getSharedPreferences(MyPreferences, Context.MODE_PRIVATE);

        String facebook_id = sharedPreferences.getString(FbId, "");

        String Url = "http://kandi.nodejitsu.com/messagehistory";

        HttpClient client = new DefaultHttpClient();

        try {

            HttpPost post = new HttpPost(Url);

            JSONObject postObj = new JSONObject();
            postObj.put("recipient",facebook_id);
            postObj.put("sender", facebook_id);

            StringEntity entity = new StringEntity(postObj.toString(), HTTP.UTF_8);

            entity.setContentType("application/json");
            post.setEntity(entity);

            HttpResponse response = client.execute(post);

            BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

            String line = "";

            while ((line = reader.readLine()) != null) {

                GsonBuilder gsonBuilder = new GsonBuilder();
                gsonBuilder.registerTypeAdapter(Res_End_Results.class, new Res_End_Results_Deserializer());
                gsonBuilder.registerTypeAdapter(Records.class, new Records_Deserializer());
                Gson gson = gsonBuilder.create();

                Res_End_Results resEndResults = gson.fromJson(line, Res_End_Results.class);
                for (Records records:resEndResults.getRecords()) {

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    public void setInvisibleMessageTitleMessageButton() {
        messageTitle.setVisibility(View.GONE);
        newMessageButton.setVisibility(View.GONE);
        messageBackButton.setVisibility(View.GONE);
        messageListView.setVisibility(View.GONE);
    }

    public void setInvisibleMessageTitleMessageButtonForNewMessagePrompt() {
        newMessageButton.setVisibility(View.GONE);
        messageListView.setVisibility(View.GONE);
    }

    @Override
    public void onResume() {
        super.onResume();
        messageTitle.setVisibility(View.VISIBLE);
        newMessageButton.setVisibility(View.VISIBLE);
        try {
            messageListView.setVisibility(View.VISIBLE);
        } catch (NullPointerException nullEx) {}
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy () {
        super.onDestroy();
        socket.disconnect();
        socket.close();
        Intent returnIntent = new Intent();
        setResult(RESULT_OK, returnIntent);
        finish();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CONVERSATION_INTENT) {
                messageListView.setVisibility(View.VISIBLE);
                messageTitle.setVisibility(View.VISIBLE);
        }

    }
}
