package com.jimchen.kanditag;

import android.content.Context;
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
 * Created by Jim on 5/21/15.
 */
public class DownloadProfileImageAsyncTask extends AsyncTask<String, Void, String> {

    private final String TAG = "DownloadProfileImageAsyncTask";

    private Context context;
    private ReturnProfileImageAsyncResponse delegate = null;

    private String line = "";

    public DownloadProfileImageAsyncTask(Context context, ReturnProfileImageAsyncResponse response) {
        this.context = context;
        this.delegate = response;
    }


    @Override
    protected String doInBackground(String... params) {

        String query_id = params[0];

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        String Url = "http://kandi.nodejitsu.com/download_profile_image";

        HttpClient client = new DefaultHttpClient();

        JsonQrObject toPostData = new JsonQrObject();

        try {

            HttpPost post = new HttpPost(Url);

            toPostData.setKt_id(query_id);

            StringEntity entity = new StringEntity(toPostData.toString(), HTTP.UTF_8);

            entity.setContentType("application/json");
            post.setEntity(entity);

            HttpResponse response = client.execute(post);
            BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

            while ((line = reader.readLine()) != null) {

                GsonBuilder gsonBuilder = new GsonBuilder();
                gsonBuilder.registerTypeAdapter(Res_End_Results.class, new Res_End_Results_Deserializer());
                gsonBuilder.registerTypeAdapter(Records.class, new Records_Deserializer());
                Gson gson = gsonBuilder.create();

                System.out.println(line);

                    }
                } catch (NullPointerException nullEx) {
                    nullEx.printStackTrace();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return line;
    }

    @Override
    protected void onPreExecute() {
    }

    @Override
    protected void onPostExecute(String image) {
        delegate.processFinish(image);
    }

}
