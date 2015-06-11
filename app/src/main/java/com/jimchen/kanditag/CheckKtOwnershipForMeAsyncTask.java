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
 * Created by Jim on 3/8/15.
 */

//TODO this will check kt_ownership for any occurrences of your kt_id, then it will take those records and save all the qrcodes to the database for kt_kandi
    // then do another task to use the kandi in the local db to query for all users who share the kandi with you

// this class finds all occurrences of user in the kt_ownership table
public class CheckKtOwnershipForMeAsyncTask extends AsyncTask<Void, Void, ArrayList<KtUserObject>> {

    private final String TAG = "CheckKtOwnershipForMeAsyncTask";

    private CheckKtOwnershipAsyncResponse delegate = null;

    private Context context;
    private KtDatabase myDatabase;
    SharedPreferences sharedPreferences;
    private String MY_KT_ID, MY_FB_ID, MY_USER_NAME;
    public static final String MyPreferences = "MyPrefs";
    public static final String Name = "nameKey";
    public static final String FbId = "fbidKey";
    public static final String UserId = "userIdKey";

    private ArrayList<KtUserObject> ktUserObjectArrayList;

    public CheckKtOwnershipForMeAsyncTask(Context context, CheckKtOwnershipAsyncResponse response) {
        this.context = context;
        this.delegate = response;
    }

    @Override
    protected ArrayList<KtUserObject> doInBackground(Void... params) {

            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);

            String Url = "http://kandi.nodejitsu.com/check_ktownership_for_me";

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
                        KtUserObject ktUserObject = new KtUserObject();
                        ktUserObject.setKt_id(records.getKt_id());
                        ktUserObject.setUsername(records.getUsername());
                        ktUserObject.setPlacement(records.getPlacement());
                        ktUserObject.setKandiId(records.getQrcode());
                        /**
                        boolean exists = myDatabase.checkIfKtUserExists(ktUserObject);
                        if (exists) {

                        } else {
                            ktUserObjectArrayList.add(ktUserObject);
                            myDatabase.saveKtUser(ktUserObject);
                        }
                         **/
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }


        return ktUserObjectArrayList;
    }

    @Override
    protected void onPreExecute() {
        myDatabase = new KtDatabase(context);
        sharedPreferences = context.getSharedPreferences(MyPreferences, Context.MODE_PRIVATE);
        MY_KT_ID = sharedPreferences.getString(UserId, "");
        MY_USER_NAME = sharedPreferences.getString(Name, "");
        MY_FB_ID = sharedPreferences.getString(FbId, "");
        ktUserObjectArrayList = new ArrayList<>();
    }

    @Override
    protected void onPostExecute(ArrayList<KtUserObject> object) {
        delegate.processFinish(object);
    }

}
