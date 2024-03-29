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
public class DownloadGroupMessagesFromServerAsyncTask extends AsyncTask<Void, Void, ArrayList<KtMessageObject>> {

    private Context context;
    private ReturnKtMessageObjectArrayListAsyncResponse delegate = null;
    private KtDatabase myDatabase;
    SharedPreferences sharedPreferences;
    private String MY_KT_ID, MY_FB_ID, MY_USER_NAME;
    public static final String MY_PREFERENCES = "MyPrefs";
    public static final String NAME = "nameKey";
    public static final String FBID = "fbidKey";
    public static final String KTID = "userIdKey";

    private ArrayList<KtMessageObject> groupMessageItemArrayList;

    public DownloadGroupMessagesFromServerAsyncTask(Context context, ReturnKtMessageObjectArrayListAsyncResponse response) {
        this.context = context;
        this.delegate = response;
    }

    @Override
    protected ArrayList<KtMessageObject> doInBackground(Void... params) {

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        String Url = "http://kandi.nodejitsu.com/download_group_messages";

        HttpClient client = new DefaultHttpClient();

        JsonQrObject toPostObject = new JsonQrObject();

        try {

            HttpPost post = new HttpPost(Url);

            toPostObject.setKt_id(MY_KT_ID);

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

                KtMessageObject messageObject = new KtMessageObject();
                    messageObject.setMessage(records.getMessage());
                    messageObject.setFrom_id(records.getFrom_id());
                    messageObject.setFrom_name(records.getFrom_name());
                    messageObject.setTo_Kandi_Id(records.getQrcode());
                    try {
                        messageObject.setTo_Kandi_Name(records.getKandi_name());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    messageObject.setTimestamp(records.getTimestamp());

                    boolean exists = myDatabase.checkIfGroupMessageExists(messageObject);
                    if (exists) {

                    } else {
                        groupMessageItemArrayList.add(messageObject);
                        myDatabase.saveGroupMessage(messageObject);
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
        sharedPreferences = context.getSharedPreferences(MY_PREFERENCES, Context.MODE_PRIVATE);
        MY_KT_ID = sharedPreferences.getString(KTID, "");
        MY_USER_NAME = sharedPreferences.getString(NAME, "");
        MY_FB_ID = sharedPreferences.getString(FBID, "");

        groupMessageItemArrayList = new ArrayList<>();
    }

    @Override
    protected void onPostExecute(ArrayList<KtMessageObject> list) {
        delegate.processFinish(list);
    }
}
