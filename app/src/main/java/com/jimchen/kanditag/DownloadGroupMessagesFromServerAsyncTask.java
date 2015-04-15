package com.jimchen.kanditag;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.StrictMode;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * Created by Jim on 3/12/15.
 */
public class DownloadGroupMessagesFromServerAsyncTask extends AsyncTask<Void, Void, ArrayList<GroupMessageItem>> {

    private Context context;
    private ReturnGroupMessageArrayListAsyncResponse delegate = null;
    private KtDatabase myDatabase;
    SharedPreferences sharedPreferences;
    private String MY_KT_ID, MY_FB_ID, MY_USER_NAME;
    public static final String MyPreferences = "MyPrefs";
    public static final String Name = "nameKey";
    public static final String FbId = "fbidKey";
    public static final String UserId = "userIdKey";

    private ArrayList<GroupMessageItem> groupMessageItemArrayList;

    public DownloadGroupMessagesFromServerAsyncTask(Context context, ReturnGroupMessageArrayListAsyncResponse response) {
        this.context = context;
        this.delegate = response;
    }

    @Override
    protected ArrayList<GroupMessageItem> doInBackground(Void... params) {

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        String Url = "http://kandi.nodejitsu.com/download_group_messages";

        HttpClient client = new DefaultHttpClient();

        JsonQrObject toPostObject = new JsonQrObject();

        try {

            HttpPost post = new HttpPost(Url);

            toPostObject.setFb_id(MY_FB_ID);

            StringEntity entity = new StringEntity(toPostObject.toString(), HTTP.UTF_8);

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
                    /**
                     Log.d(TAG, records.getKt_id());
                     Log.d(TAG, records.getFb_id());
                     Log.d(TAG, records.getUsername());
                     Log.d(TAG, records.getQrcode());
                     System.out.println("PostQr.Records.placement = " + records.getPlacement());
                     **/

                GroupMessageItem groupMessageItem = new GroupMessageItem();
                    groupMessageItem.setMessage(records.getMsg());
                    groupMessageItem.setFromID(records.getFID());
                    groupMessageItem.setFromName(records.getFromName());
                    groupMessageItem.setQrCode(records.getQrcode());
                    groupMessageItem.setTime(records.getTime());

                    boolean exists = myDatabase.checkIfGroupMessageExists(groupMessageItem);
                    if (exists) {

                    } else {
                        groupMessageItemArrayList.add(groupMessageItem);
                        myDatabase.saveGroupMessage(groupMessageItem);
                    }
                }
            }

        } catch (Exception ex) {

        }

        return groupMessageItemArrayList;
    }

    @Override
    protected void onPreExecute() {
        myDatabase = new KtDatabase(context);
        sharedPreferences = context.getSharedPreferences(MyPreferences, Context.MODE_PRIVATE);
        MY_KT_ID = sharedPreferences.getString(UserId, "");
        MY_USER_NAME = sharedPreferences.getString(Name, "");
        MY_FB_ID = sharedPreferences.getString(FbId, "");

        groupMessageItemArrayList = new ArrayList<>();
    }

    @Override
    protected void onPostExecute(ArrayList<GroupMessageItem> list) {
        delegate.processFinish(list);
    }
}
