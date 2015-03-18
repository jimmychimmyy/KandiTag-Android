package com.kanditag.kanditag;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.StrictMode;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;


public class Kandi extends FragmentActivity {

    KtDatabase myDatabase;

    ViewPager kandiViewPager;
    MyPageAdapter kandiPageAdapter;

    Button feed;
    Button following;
    Button followers;

    GridView kandiGridView;

    static final String TAG = "Kandi";

    SharedPreferences sharedPreferences;
    public static final String MyPreferences = "MyPrefs";
    public static final String Name = "nameKey";
    public static final String FbId = "fbidKey";
    public static final String UserId = "userIdKey";

    private String MY_KT_ID, MY_FB_ID, MY_USER_NAME;

    private ArrayList<FollowGridItem> followerArray, followingArray;

    private FollowGridViewAdapter followerGridViewAdapter, followingGridViewAdapter;

    private TextView kandiTagTitle;

    private List<Fragment> getFragments() {
        List<Fragment> fList = new ArrayList<Fragment>();
        fList.add(new KandiFragment(KandiTagConstants.FRAGMENT_FOLLOWING));
        fList.add(new KandiFragment(KandiTagConstants.FRAGMENT_FOLLOWERS));
        fList.add(new KandiFragment(KandiTagConstants.FRAGMENT_FEED));
        return fList;
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kandi);

        myDatabase = new KtDatabase(this);
        sharedPreferences = this.getSharedPreferences(MyPreferences, Context.MODE_PRIVATE);
        MY_KT_ID = sharedPreferences.getString(UserId, "");
        MY_USER_NAME = sharedPreferences.getString(Name, "");
        MY_FB_ID = sharedPreferences.getString(FbId, "");


    }

    private void setFollowingGridViewAdapter() {
        Runnable run = new Runnable() {
            @Override
            public void run() {
                kandiGridView.setAdapter(followingGridViewAdapter);
                kandiGridView.invalidate();
            }
        };
        //TODO read up on threading, .run should be replaced with .start()
        run.run();
    }

    Thread setFollowersGridViewAdapter  = new Thread() {
        @Override
        public void run() {
            kandiGridView.setAdapter(followerGridViewAdapter);
            kandiGridView.invalidate();
        }
    };


    class Returned_Results_Following {
        private Boolean success;
        private String error;
        private Following_Results[] results;

        public void setSuccess(Boolean success) {
            this.success = success;
        }

        public Boolean getSuccess() {
            return success;
        }

        public void setFollowingResults(Following_Results[] results) {
            this.results = results;
        }

        public Following_Results[] getFollowingResults() {
            return results;
        }
    }

    class Returned_Results_Follower {
        private Boolean success;
        private String error;
        private Follower_Results[] results;

        public void setSuccess(Boolean success) {
            this.success = success;
        }

        public Boolean getSuccess() {
            return success;
        }

        public void setFollowerResults(Follower_Results[] results) {
            this.results = results;
        }

        public Follower_Results[] getFollowerResults() {
            return results;
        }
    }

    class Following_Results {
        private String id_user;
        private String id_facebook;
        private String user_name;

        public void setId_user(String string) {
            this.id_user = string;
        }

        public String getId_user() {
            return id_user;
        }

        public void setId_facebook(String string) {
            this.id_facebook = string;
        }

        public String getId_facebook() {
            return id_facebook;
        }

        public void setUser_name(String string) {
            this.user_name = string;
        }

        public String getUser_name() {
            return user_name;
        }
    }

    class Follower_Results {
        private String id_user;
        private String id_facebook;
        private String user_name;

        public void setId_user(String string) {
            this.id_user = string;
        }

        public String getId_user() {
            return id_user;
        }

        public void setId_facebook(String string) {
            this.id_facebook = string;
        }

        public String getId_facebook() {
            return id_facebook;
        }

        public void setUser_name(String string) {
            this.user_name = string;
        }

        public String getUser_name() {
            return user_name;
        }
    }

    class Returned_Results_Follower_Deserializer implements JsonDeserializer<Returned_Results_Follower> {
        public Returned_Results_Follower deserialize (JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException {
            JsonObject jsonObject = json.getAsJsonObject();
            Boolean success = jsonObject.get("success").getAsBoolean();
            Follower_Results[] follower_results = context.deserialize(jsonObject.get("followers_results"), Follower_Results[].class);

            Returned_Results_Follower followerObj = new Returned_Results_Follower();
            followerObj.setSuccess(success);
            followerObj.setFollowerResults(follower_results);

            return followerObj;
        }
    }

    class Follower_Deserializer implements JsonDeserializer<Follower_Results> {
        public Follower_Results deserialize (JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException {
            JsonObject jsonObject = json.getAsJsonObject();
            String user = jsonObject.get("user_id").getAsString();
            String facebook = jsonObject.get("facebookid").getAsString();
            String name = jsonObject.get("username").getAsString();

            Follower_Results returnObj = new Follower_Results();
            returnObj.setId_user(user);
            returnObj.setId_facebook(facebook);
            returnObj.setUser_name(name);

            return returnObj;
        }
    }

    class Returned_Results_Following_Deserializer implements JsonDeserializer<Returned_Results_Following> {
        public Returned_Results_Following deserialize (JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException {
            JsonObject jsonObject = json.getAsJsonObject();
            Boolean success = jsonObject.get("success").getAsBoolean();
            Following_Results[] following_results = context.deserialize(jsonObject.get("following_results"), Following_Results[].class);

            Returned_Results_Following followingObj = new Returned_Results_Following();
            followingObj.setSuccess(success);
            followingObj.setFollowingResults(following_results);

            return followingObj;
        }
    }

    class Following_Deserializer implements JsonDeserializer<Following_Results> {
        public Following_Results deserialize (JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException {
            JsonObject jsonObject = json.getAsJsonObject();
            String user = jsonObject.get("user_id").getAsString();
            String facebook = jsonObject.get("facebookid").getAsString();
            String name = jsonObject.get("username").getAsString();

            Following_Results returnObj = new Following_Results();
            returnObj.setId_user(user);
            returnObj.setId_facebook(facebook);
            returnObj.setUser_name(name);

            return returnObj;
        }
    }

    private class IconAdapter extends BaseAdapter {
        private Context myContext;
        private int myIconSize;
        public IconAdapter(Context myContext, int myIconSize) {
            super();
            this.myContext = myContext;
            this.myIconSize = myIconSize;
            loadIcon();
        }

        public IconAdapter(Kandi iconFragmentSystem, int iconSize) {
            //TODO IconAdapter
        }

        @Override
        public int getCount() {
            return myThumbs.size();
        }

        @Override
        public Object getItem(int position) {
            return myThumbs.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ImageView imageView;
            if (convertView == null) {
                imageView = new ImageView(myContext);
                imageView.setLayoutParams(new GridView.LayoutParams(myIconSize, myIconSize));
            } else {
                imageView = (ImageView) convertView;
            }
            imageView.setImageResource(myThumbs.get(position));
            return imageView;
        }

        private ArrayList<Integer> myThumbs;

        private void loadIcon() {
            myThumbs = new ArrayList<Integer>();

            final Resources resources = getResources();
            final String packageName = getApplication().getPackageName();

            //addIcon(resources, packageName, R.array.systemicons);
        }

        private void addIcon(Resources resources, String packageName, int list) {
            final String[] extras = resources.getStringArray(list);
            for (String extra: extras) {
                int res = resources.getIdentifier(extra, "drawable", packageName);
                if (res != 0) {
                    final int thumbRes = resources.getIdentifier(extra, "drawable", packageName);
                    if (thumbRes != 0) {
                        myThumbs.add(thumbRes);
                    }
                }
            }
        }
    }

    private void getFollowers() {
        Log.i(TAG, "getFollowers");

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        sharedPreferences = getSharedPreferences(MyPreferences, Context.MODE_PRIVATE);

        String user_id = sharedPreferences.getString(UserId, "");

        String Url = "http://kandi.nodejitsu.com/followers";

        HttpClient client = new DefaultHttpClient();

        try {

            HttpPost post = new HttpPost(Url);

            JSONObject kandiObj = new JSONObject();
            kandiObj.put("user_id", user_id);

            StringEntity entity = new StringEntity(kandiObj.toString(), HTTP.UTF_8);

            entity.setContentType("application/json");
            post.setEntity(entity);

            HttpResponse response = client.execute(post);
            BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
            String line = "";
            while ((line = reader.readLine()) != null) {
                //Log.i(TAG, line);
                //parseJSON(line);
                GsonBuilder gsonBuilder = new GsonBuilder();
                gsonBuilder.registerTypeAdapter(Returned_Results_Follower.class, new Returned_Results_Follower_Deserializer());
                gsonBuilder.registerTypeAdapter(Follower_Results.class, new Follower_Deserializer());
                Gson gson = gsonBuilder.create();

                Returned_Results_Follower rj_obj = gson.fromJson(line, Returned_Results_Follower.class);
                //Log.d(TAG, rj_obj.getSuccess().toString());
                for (Follower_Results follower_results:rj_obj.getFollowerResults()) {
                    //Log.d(TAG, follower_results.getId_user());
                    //Log.d(TAG, follower_results.getId_facebook());
                    Log.d(TAG, follower_results.getUser_name());
                    //Log.d(TAG + "doesExistsInFollowersDB?", doesExists.toString());

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


    }


    private void getFollowing() {
        Log.i(TAG, "getFollowing");

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        sharedPreferences = getSharedPreferences(MyPreferences, Context.MODE_PRIVATE);

        String user_id = sharedPreferences.getString(UserId, "");

        String Url = "http://kandi.nodejitsu.com/following";

        HttpClient client = new DefaultHttpClient();

        try {

            HttpPost post = new HttpPost(Url);

            JSONObject kandiObj = new JSONObject();
            kandiObj.put("user_id", user_id);

            StringEntity entity = new StringEntity(kandiObj.toString(), HTTP.UTF_8);

            entity.setContentType("application/json");
            post.setEntity(entity);

            HttpResponse response = client.execute(post);
            BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
            String line = "";
            while ((line = reader.readLine()) != null) {
                //Log.i(TAG, line);
                //parseJSON(line);
                GsonBuilder gsonBuilder = new GsonBuilder();
                gsonBuilder.registerTypeAdapter(Returned_Results_Following.class, new Returned_Results_Following_Deserializer());
                gsonBuilder.registerTypeAdapter(Following_Results.class, new Following_Deserializer());
                Gson gson = gsonBuilder.create();

                Returned_Results_Following rj_obj = gson.fromJson(line, Returned_Results_Following.class);
                //Log.d(TAG, rj_obj.getSuccess().toString());
                for (Following_Results following_results:rj_obj.getFollowingResults()) {
                    //Log.d(TAG, following_results.getId_user());
                    //Log.d(TAG, following_results.getId_facebook());
                    //Log.d(TAG, following_results.getUser_name());
                    //Log.d(TAG + "doesExistsInFollowingDB?", doesExists.toString());

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    /**

    public void parseJSON(String jsonString) {
        JSONParser parser = new JSONParser();
        ContainerFactory containerFactory = new ContainerFactory() {
            @Override
            public Map createObjectContainer() {
                return new LinkedHashMap();
            }

            @Override
            public List creatArrayContainer() {
                return new LinkedList();
            }
        };
        try {
            Map json = (Map)parser.parse(jsonString, containerFactory);
            Iterator iterator = json.entrySet().iterator();
            while(iterator.hasNext()) {
                Map.Entry entry = (Map.Entry)iterator.next();
                //Log.d(Tag, entry.getKey() + "=>" + entry.getValue());
                if (entry.getKey().equals("results")) {
                    Log.i(TAG, "results found:" + entry.getValue());
                }
                if (entry.getKey().equals("follower")) {
                    Log.i(TAG, "followers: " + entry.getValue());
                }
            }

        } catch (ParseException e) {
            System.out.println(e);
        }
    }
     **/

}
