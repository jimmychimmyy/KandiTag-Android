package com.kanditag.kanditag;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;

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
 * Created by Jim on 3/8/15.
 */
public class CheckKtOwnershipForUsersAsyncTask extends AsyncTask<Void, Void, ArrayList<KtUserObject>> {

    private final String TAG = "CheckKtOwnershipForUsersAsyncTask";

    private Context context;
    private CheckKtOwnershipAsyncResponse delegate = null;

    private KtDatabase myDatabase;
    SharedPreferences sharedPreferences;
    private String MY_KT_ID, MY_FB_ID, MY_USER_NAME;
    public static final String MyPreferences = "MyPrefs";
    public static final String Name = "nameKey";
    public static final String FbId = "fbidKey";
    public static final String UserId = "userIdKey";

    private ArrayList<KtUserObject> ktUserObjectArrayList;
    private ArrayList<String> qrCodeArrayList;

    public CheckKtOwnershipForUsersAsyncTask(Context context, CheckKtOwnershipAsyncResponse response) {
        this.context = context;
        this.delegate = response;
    }

    @Override
    protected ArrayList<KtUserObject> doInBackground(Void... params) {

        qrCodeArrayList = myDatabase.getQrCodesFromKtUserTable();

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        String Url = "http://kandi.nodejitsu.com/check_ktownership_for_user";

        HttpClient client = new DefaultHttpClient();

        JsonQrObject toPostObject = new JsonQrObject();

        for (int i = 0; i < qrCodeArrayList.size(); i++) {

            try {

                HttpPost post = new HttpPost(Url);

                toPostObject.setQrCode(qrCodeArrayList.get(i));

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
                    for (Records records : resEndResults.getRecords()) {
                        /**
                        Log.d(TAG, records.getKt_id());
                        Log.d(TAG, records.getFb_id());
                        Log.d(TAG, records.getUsername());
                        Log.d(TAG, records.getQrcode());
                        System.out.println("CheckKtOwnershipForUsersAsyncTask.Records.placement = " + records.getPlacement());
                         **/
                        KtUserObject ktUserObject = new KtUserObject();
                        ktUserObject.setKt_id(records.getKt_id());
                        ktUserObject.setFb_id(records.getFb_id());
                        ktUserObject.setName(records.getUsername());
                        ktUserObject.setPlacement(records.getPlacement());
                        ktUserObject.setQrCode(records.getQrcode());
                        boolean exists = myDatabase.checkIfKtUserExists(ktUserObject);
                        if (exists) {

                        } else {
                            ktUserObjectArrayList.add(ktUserObject);
                            myDatabase.saveKtUser(ktUserObject);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return ktUserObjectArrayList;
    }

    @Override
    protected void onPreExecute() {
        myDatabase = new KtDatabase(context);
        qrCodeArrayList = new ArrayList<>();
        ktUserObjectArrayList = new ArrayList<>();
        sharedPreferences = context.getSharedPreferences(MyPreferences, Context.MODE_PRIVATE);
        MY_KT_ID = sharedPreferences.getString(UserId, "");
        MY_USER_NAME = sharedPreferences.getString(Name, "");
        MY_FB_ID = sharedPreferences.getString(FbId, "");
    }

    @Override
    protected void onPostExecute(ArrayList<KtUserObject> list) {
        delegate.processFinish(list);
    }
}
