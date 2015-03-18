package com.kanditag.kanditag;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
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
 * Created by Jim on 3/1/15.
 */

public class GetKandiAndUpdateUsersAsyncTask extends AsyncTask<ArrayList<String>, Void, ArrayList<KtUserObject>> {

    public GetKandiAndUpdateUsersAsyncResponse delegate = null;

    KtDatabase myDatabase;

    private SharedPreferences sharedPreferences;
    public static final String MyPreferences = "MyPrefs";
    public static final String Name = "nameKey";
    public static final String FbId = "fbidKey";
    public static final String UserId = "userIdKey";
    private String MY_KT_ID, MY_FB_ID, MY_USER_NAME;

    private Context context;

    private ArrayList<KtUserObject> ktUserObjectArrayList;

    private ArrayList<String> listOfKandiQrs;

    public GetKandiAndUpdateUsersAsyncTask(Context context, GetKandiAndUpdateUsersAsyncResponse response) {
        this.context = context;
        delegate = response;
    }

    @Override
    protected ArrayList<KtUserObject> doInBackground(ArrayList<String>... params) {
        listOfKandiQrs = params[0];


        for (int i = 0; i < listOfKandiQrs.size(); i++) {

            if (listOfKandiQrs.get(i).contains("dhc")) {

                StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                StrictMode.setThreadPolicy(policy);

                String Url = "http://kandi.nodejitsu.com/kt_ownership_findall";

                HttpClient client = new DefaultHttpClient();

                try {

                    HttpPost post = new HttpPost(Url);

                    JsonQrObject toPostData = new JsonQrObject(listOfKandiQrs.get(i));

                    StringEntity entity = new StringEntity(toPostData.toString(), HTTP.UTF_8);
                    entity.setContentType("application/json");
                    post.setEntity(entity);

                    HttpResponse response = client.execute(post);
                    BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
                    String line = "";

                    while ((line = reader.readLine()) != null) {
                        Log.i("GetKandiAsyncTask:", line);

                        GsonBuilder gsonBuilder = new GsonBuilder();
                        gsonBuilder.registerTypeAdapter(Res_End_Results.class, new Res_End_Results_Deserializer());
                        gsonBuilder.registerTypeAdapter(Records.class, new Records_Deserializer());
                        Gson gson = gsonBuilder.create();

                        Res_End_Results resObj = gson.fromJson(line, Res_End_Results.class);
                        Log.i("GetKandiAsyncTask:", resObj.getSuccess().toString());

                        for (Records records: resObj.getRecords()) {
                            Log.i("GetKandiAndUpdateUsersAsyncTask:", records.getQrcode());
                            Log.i("GetKandiAndUpdateUsersAsyncTask:", records.getKt_id());
                            Log.i("GetKandiAndUpdateUsersAsyncTask:", records.getFb_id());
                            Log.i("GetKandiAndUpdateUsersAsyncTask:", records.getUsername());
                            Log.i("GetKandiAndUpdateUsersAsyncTask:", records.get_id());
                            System.out.println("placement:" + records.getPlacement());

                            KtUserObject tempObject = new KtUserObject(records.getUsername(), records.getKt_id(), records.getFb_id(), records.getQrcode(), records.getPlacement());

                            boolean exists = myDatabase.checkIfKtUserExists(tempObject);

                            if (exists) {
                                System.out.println("exists");
                            } else {
                                System.out.println("does not exist");
                                myDatabase.saveKtUser(tempObject);
                            }

                            ktUserObjectArrayList.add(tempObject);

                            //TODO this would be a good place to cross check if the ownership row exists in the local db already
                        }

                    }

                } catch (Exception allEx) {
                    allEx.printStackTrace();
                }
            }
        }

        return ktUserObjectArrayList;
    }

    @Override
    protected void onPreExecute() {
        ktUserObjectArrayList = new ArrayList<>();
        listOfKandiQrs = new ArrayList<>();
        myDatabase = new KtDatabase(context);
    }

    @Override
    protected void onPostExecute(ArrayList<KtUserObject> result) {
        //Log.d("getKandiAsyncTask:", "Result = " + result.size());
        delegate.processFinish(result);
    }

    @Override
    protected void onProgressUpdate(Void... params) {

    }
}
