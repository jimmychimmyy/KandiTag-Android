package com.jimchen.kanditag;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;

import java.util.ArrayList;

/**
 * Created by Jim on 7/1/15.
 */
public class AddNewMessage extends Activity {

    private GetAllUsersFromLocalDbAsyncTask getAllUsersFromLocalDbAsyncTask;
    private GetAllKandiFromLocalDbAsyncTask getAllKandiFromLocalDbAsyncTask;

    private ArrayList<KtUserObjectParcelable> users = new ArrayList<>();
    private KtUserObjectListAdapter listAdapter;

    // xml
    private ListView listview;
    private ImageView cancel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_message);

        listview = (ListView) findViewById(R.id.AddNewMessage_ListView);

        cancel = (ImageView) findViewById(R.id.AddNewMessage_Cancel);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
                overridePendingTransition(R.anim.right_slide_in, R.anim.right_slide_out);
            }
        });

        //gets list of all your friends
        getAllUsersFromLocalDbAsyncTask = new GetAllUsersFromLocalDbAsyncTask(AddNewMessage.this, new ReturnKtUserObjectParcelableArrayListAsyncResponse() {
            @Override
            public void processFinish(ArrayList<KtUserObjectParcelable> output) {
                users.addAll(output);
                listAdapter = new KtUserObjectListAdapter(AddNewMessage.this, R.layout.message_fragment_row_item, users);
                listview.setAdapter(listAdapter);
                listview.invalidate();
                listAdapter.notifyDataSetChanged();
            }
        });

        getAllUsersFromLocalDbAsyncTask.execute();
    }
}
