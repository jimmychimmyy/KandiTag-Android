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
 * Created by Jim on 3/10/15.
 */
public class DownloadMessagesFromServerAsyncTask extends AsyncTask<Void, Void, ArrayList<KtMessageObject>> {

    private final String TAG = "DownloadMessagesFromServerFromAsyncTask";

    private Context context;
    private ReturnKtMessageObjectArrayListAsyncResponse delegate = null;

    private KtDatabase myDatabase;
    SharedPreferences sharedPreferences;
    private String MY_KT_ID, MY_FB_ID, MY_USER_NAME;
    public static final String MyPreferences = "MyPrefs";
    public static final String Name = "nameKey";
    public static final String FbId = "fbidKey";
    public static final String UserId = "userIdKey";

    private ArrayList<KtMessageObject> ktMessageObjectArrayList;

    public DownloadMessagesFromServerAsyncTask(Context context, ReturnKtMessageObjectArrayListAsyncResponse response) {
        this.context = context;
        this.delegate = response;
    }

    @Override
    protected ArrayList<KtMessageObject> doInBackground(Void... params) {

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        String Url = "http://kandi.nodejitsu.com/download_messages";

        HttpClient client = new DefaultHttpClient();

        JsonQrObject toPostData = new JsonQrObject();

            try {

                HttpPost post = new HttpPost(Url);

                toPostData.setKt_id(MY_KT_ID);

                StringEntity entity = new StringEntity(toPostData.toString(), HTTP.UTF_8);

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

                    System.out.println(line);

                    Res_End_Results resEndObj = gson.fromJson(line, Res_End_Results.class);

                    try {
                        for (Records records : resEndObj.getRecords()) {
                            /**
                            Log.d(TAG, records.getKt_id());
                            Log.d(TAG, records.getFb_id());
                            Log.d(TAG, records.getUsername());
                            Log.d(TAG, records.getQrcode());
                            System.out.println("PostQr.Records.placement = " + records.getPlacement());
                             **/

                            KtMessageObject ktMessageObject = new KtMessageObject();
                            ktMessageObject.setMessage(records.getMessage());
                            ktMessageObject.setFrom_id(records.getFrom_id());
                            ktMessageObject.setFrom_name(records.getFrom_name());
                            ktMessageObject.setTo_id(records.getTo_id());
                            String toName = records.getTo_name().replace("\"","");
                            ktMessageObject.setTo_name(toName);
                            ktMessageObject.setTimestamp(records.getTimestamp());

                            boolean exists = myDatabase.checkIfAlreadyExistsInKtMessage(ktMessageObject);
                            if (exists) {
                            } else {
                                ktMessageObjectArrayList.add(ktMessageObject);
                                myDatabase.saveMessage(ktMessageObject);
                            }
                        }
                    } catch (NullPointerException nullEx) {
                        nullEx.printStackTrace();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        return ktMessageObjectArrayList;
    }

    @Override
    protected void onPreExecute() {
        myDatabase = new KtDatabase(context);
        ktMessageObjectArrayList = new ArrayList<>();
        sharedPreferences = context.getSharedPreferences(MyPreferences, Context.MODE_PRIVATE);
        MY_KT_ID = sharedPreferences.getString(UserId, "");
        MY_USER_NAME = sharedPreferences.getString(Name, "");
        MY_FB_ID = sharedPreferences.getString(FbId, "");
    }

    @Override
    protected void onPostExecute(ArrayList<KtMessageObject> list) {
        delegate.processFinish(list);
    }

}
