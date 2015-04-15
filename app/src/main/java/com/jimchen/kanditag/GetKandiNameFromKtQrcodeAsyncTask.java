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
public class GetKandiNameFromKtQrcodeAsyncTask extends AsyncTask<Void, Void, ArrayList<KandiObject>> {

    private final String TAG = "GetKandiNameFromKtQrcodeAsyncTask";

    private Context context;
    private ReturnKandiObjectArrayAsyncResponse delegate = null;
    private KtDatabase myDatabase;
    SharedPreferences sharedPreferences;
    private String MY_KT_ID, MY_FB_ID, MY_USER_NAME;
    public static final String MyPreferences = "MyPrefs";
    public static final String Name = "nameKey";
    public static final String FbId = "fbidKey";
    public static final String UserId = "userIdKey";


    private ArrayList<KandiObject> kandiObjectArrayList;
    private ArrayList<String> qrCodeArrayList;

    public GetKandiNameFromKtQrcodeAsyncTask(Context context, ReturnKandiObjectArrayAsyncResponse response) {
        this.context = context;
        this.delegate = response;
    }

    //get arraylist of qr codes from kt_users
    @Override
    protected ArrayList<KandiObject> doInBackground(Void... params) {

        qrCodeArrayList = myDatabase.getQrCodesFromKtUserTable();

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        String Url = "http://kandi.nodejitsu.com/get_kandi_name_from_ktqrcode";

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
                        Log.d(TAG, records.getKandiName());
                         **/

                        KandiObject kandiObject = new KandiObject(records.getQrcode(), records.getKandiName());

                        boolean exists = myDatabase.checkIfKandiExists(kandiObject);
                        if (exists) {

                        } else {
                            kandiObjectArrayList.add(kandiObject);
                            myDatabase.saveKandi(kandiObject);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return kandiObjectArrayList;
    }

    @Override
    protected void onPreExecute() {
        kandiObjectArrayList = new ArrayList<>();
        myDatabase = new KtDatabase(context);
        sharedPreferences = context.getSharedPreferences(MyPreferences, Context.MODE_PRIVATE);
        MY_KT_ID = sharedPreferences.getString(UserId, "");
        MY_USER_NAME = sharedPreferences.getString(Name, "");
        MY_FB_ID = sharedPreferences.getString(FbId, "");
    }

    @Override
    protected void onPostExecute(ArrayList<KandiObject> list) {
        delegate.processFinish(list);
    }
}
