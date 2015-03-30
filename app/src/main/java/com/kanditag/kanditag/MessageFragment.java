package com.kanditag.kanditag;

import android.graphics.Color;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Jim on 3/18/15.
 */
public class MessageFragment extends Fragment {

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
    private ImageView exitButton;

    private MessageListAdapter messageListAdapter;
    private GroupMessageListAdapter groupMessageListAdapter;

    private ArrayList<MessageListItem> messageListItemsArray;
    private ArrayList<GroupMessageItem> groupMessageItemArray;

    // AsyncTask Vars
    private DisplayLatestMessagesAsyncTask displayLatestMessagesAsyncTask;
    private DisplayLatestGroupMessageAsyncTask displayLatestGroupMessageAsyncTask;
    private GetAllUsersFromLocalDbAsyncTask getAllUsersFromLocalDbAsyncTask;
    private GetAllGroupsFromLocalDbAsyncTask getAllGroupsFromLocalDbAsyncTask;
    private GetAllKandiFromLocalAsyncTask getAllKandiFromLocalAsyncTask;

    private ArrayList<KtUserObjectParcelable> usersForNewMessageList = new ArrayList<>();
    private ArrayList<KandiGroupObjectParcelable> groupsForNewMessageList = new ArrayList<>();


    public static final MessageFragment newInstance() {
        MessageFragment messageFragment = new MessageFragment();
        return messageFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.message_fragment, container, false);

        this.context = getActivity();

        myDatabase = new KtDatabase(getActivity());
        sharedPreferences = getActivity().getSharedPreferences(MyPreferences, Context.MODE_PRIVATE);
        MY_KT_ID = sharedPreferences.getString(UserId, "");
        MY_USER_NAME = sharedPreferences.getString(Name, "");
        MY_FB_ID = sharedPreferences.getString(FbId, "");

        myListView = (ListView) rootView.findViewById(R.id.MessageFragment_ListView);

        myTitle = (TextView) rootView.findViewById(R.id.MessageFragment_TitleTextView);
        myTitle.setText("Messages");
        myTitle.setTextSize(45);
        myTitle.setTextColor(Color.BLACK);
        Typeface typeface = Typeface.createFromAsset(getActivity().getAssets(),"fonts/stalemate_regular.ttf");
        myTitle.setTypeface(typeface);

        exitButton = (ImageView) rootView.findViewById(R.id.MessageFragment_ExitButton);
        exitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getFragmentManager().beginTransaction().setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out).remove(MessageFragment.this).commit();
            }
        });

        displayLatestMessagesAsyncTask = new DisplayLatestMessagesAsyncTask(this.context, new ReturnMessageListItemArrayAsyncResponse() {
            @Override
            public void processFinish(final ArrayList<MessageListItem> output) {
                messageListItemsArray = output;
                messageListAdapter = new MessageListAdapter(getActivity(), R.id.list_item, messageListItemsArray);
                myListView.setAdapter(messageListAdapter);

                myListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        System.out.println("messageListView.onItemClick.position = " + position);
                        System.out.println("fbId = " + output.get(position).getSender());
                        Intent openMessageWithUser = new Intent(getActivity(), MessageDialogue.class);
                        Bundle bundle = new Bundle();
                        bundle.putString("fb_id", output.get(position).getSender());
                        openMessageWithUser.putExtras(bundle);
                        startActivity(openMessageWithUser);
                        //setInvisibleMessageTitleMessageButton();
                    }
                });
                System.out.println("Message.displayLatestMessagesAsyncTask.processFinish.output.size() = " + output.size());
            }
        });

        displayLatestGroupMessageAsyncTask = new DisplayLatestGroupMessageAsyncTask(getActivity(), new ReturnGroupMessageArrayListAsyncResponse() {
            @Override
            public void processFinish(ArrayList<GroupMessageItem> output) {
                System.out.println("Message.displayLatestGroupMessageAsyncTask.processFinish.output.size() = " + output.size());
                groupMessageItemArray = output;
                groupMessageListAdapter = new GroupMessageListAdapter(getActivity(), R.id.list_item, groupMessageItemArray, MY_FB_ID);
            }
        });

        //this returns a list of all users you are connected to
        getAllUsersFromLocalDbAsyncTask = new GetAllUsersFromLocalDbAsyncTask(getActivity(), new ReturnKtUserObjectParcelableArrayListAsyncResponse() {
            @Override
            public void processFinish(ArrayList<KtUserObjectParcelable> output) {
                System.out.println("Message.getAllUsersFromLocalDbAsyncTask.processFinish.output.size() = " + output.size());
                usersForNewMessageList.addAll(output);
            }
        });

        getAllKandiFromLocalAsyncTask = new GetAllKandiFromLocalAsyncTask(getActivity(), new ReturnKandiObjectArrayAsyncResponse() {
            @Override
            public void processFinish(ArrayList<KandiObject> output) {
                System.out.println("Message.getAllKandiFromLocalAsyncTask.processFinish.output.size() = " + output.size());
                getAllGroupsFromLocalDbAsyncTask.execute(output);
            }
        });

        //this returns a list of kandi you own
        getAllGroupsFromLocalDbAsyncTask = new GetAllGroupsFromLocalDbAsyncTask(getActivity(), new ReturnKandiGroupObjectParcelableArrayList() {
            @Override
            public void processFinish(ArrayList<KandiGroupObjectParcelable> output) {
                System.out.println("Message.getAllGroupsFromLocalDbAsyncTask.processFinish.output.size() = " + output.size());
                groupsForNewMessageList.addAll(output);
            }
        });

        displayLatestMessagesAsyncTask.execute(myDatabase.getFb_IdFromKtUserToDisplayLatestMessage());
        displayLatestGroupMessageAsyncTask.execute(myDatabase.getKandi());
        //getAllUsersFromLocalDbAsyncTask.execute();
        //getAllKandiFromLocalAsyncTask.execute();

        return rootView;
    }

}
