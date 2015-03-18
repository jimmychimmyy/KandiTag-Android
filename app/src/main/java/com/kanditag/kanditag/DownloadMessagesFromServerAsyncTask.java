package com.kanditag.kanditag;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.StrictMode;
import android.util.Log;
import android.widget.Toast;

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

                toPostData.setFb_id(MY_FB_ID);

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
                            ktMessageObject.setMessage(records.getMsg());
                            ktMessageObject.setFrom_id(records.getFID());
                            ktMessageObject.setFrom_name(records.getFromName());
                            ktMessageObject.setTo_id(records.getTID());
                            ktMessageObject.setTo_name(records.getToName());
                            ktMessageObject.setTime(records.getTime());

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

    }

}
