package com.jimchen.kanditag;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;

import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;

import android.os.StrictMode;
import android.support.v4.view.ViewPager;

import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.model.GraphUser;
import com.facebook.widget.LoginButton;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;


import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;

import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;
import org.json.JSONObject;

import android.view.View;
import android.widget.Toast;

public class LoginActivity extends Activity {

    private Button ktLoginButton;
    private ImageView loginButtonBackground;
    private LoginButton fbLoginButton;

    private Button loginWithFacebookButton, loginButton, signUpButton;

    private String resEndKt_id, resEndFb_id, resEndUserName;

    private String SUCCESS = "success";
    private String USERID = "user_id";

    // Shared Preferences
    private SharedPreferences sharedPreferences;
    public static final String USER_PREFERENCES = "com.jimchen.kanditag.extra.PREFERENCES";
    public static final String USERNAME = "com.jimchen.kanditag.extra.USERNAME";
    public static final String FBID = "com.jimchen.kanditag.extra.FBID";
    public static final String KTID = "com.jimchen.kanditag.extra.KTID";
    public static final String USER_PROFILE_IMAGE = "com.jimchen.kanditag.extra.USER_PROFILE_IMAGE";
    public static final String OPENED_BEFORE = "com.jimchen.kanditag.extra.OPENED_BEFORE";
    public static final String NEW_MESSAGE = "com.jimchen.kanditag.extra.NEW_MESSAGE";

    String Pref_Name;
    String Pref_FbId;
    String Pref_UserId;

    static InputStream httpResponseStream = null;
    static String jsonString = "";

    private String TAG = "Login";
    private TextView lblEmail;

    public MyPageAdapter pageAdapter;
    public ViewPager viewPager;
    public LoginImageAdapter loginImageAdapter;

    // login progress spinner
    private ProgressDialog progressDialog;

    private UiLifecycleHelper uiHelper;
    private Session.StatusCallback callback = new Session.StatusCallback() {
        @Override
        public void call(Session session, SessionState state, Exception exception) {
            onSessionStateChange(session, state, exception);
        }
    };

    public boolean isLoggedIn() {
        Session session = Session.getActiveSession();
        return (session != null && session.isOpened());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        sharedPreferences = getSharedPreferences(USER_PREFERENCES, Context.MODE_PRIVATE);

        if (!isConnectedToNetwork()) {
            Toast.makeText(LoginActivity.this, "No Internet Connection", Toast.LENGTH_LONG).show();
        }

        Log.d(TAG, "user logged in? : " + isLoggedIn() );

        if (isLoggedIn()) {
            Intent intent_to_start = new Intent(this, Main.class);
            startActivity(intent_to_start);
            //overridePendingTransition(R.anim.abc_slide_in_bottom, R.anim.abc_slide_out_bottom);
            finish();
            Intent dismissSplash = new Intent();
            setResult(RESULT_OK, dismissSplash);
        }

        fbLoginButton = (LoginButton) findViewById(R.id.login_fbLoginButton);
        fbLoginButton.setVisibility(View.INVISIBLE);
        loginButtonBackground = (ImageView) findViewById(R.id.Login_loginButtonImageViewBackground);

        loginWithFacebookButton = (Button) findViewById(R.id.Login_LoginWithFacebookButton);
        loginWithFacebookButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openFacebookSession();
                progressDialog = ProgressDialog.show(LoginActivity.this, "", "Logging in..");

            }
        });

        uiHelper = new UiLifecycleHelper(this, callback);
        uiHelper.onCreate(savedInstanceState);

    }

    private void openFacebookSession() {
        Session.openActiveSession(this, true, new Session.StatusCallback() {
            @Override
            public void call(Session session, SessionState state, Exception exception) {
                if (exception != null) {
                    Log.d("Facebook", exception.getMessage());
                    if (!isConnectedToNetwork()) {
                        Toast.makeText(LoginActivity.this, "No Internet Connection", Toast.LENGTH_LONG).show();
                    }
                }
                Log.d("Facebook", "SessionState:" + session.getState());
                if (session.isOpened()) {
                    Log.i(TAG, "isOpen");
                    Log.i(TAG, "Access Token " + session.getAccessToken());
                    Request.executeMeRequestAsync(session, new Request.GraphUserCallback() {
                        @Override
                        public void onCompleted(GraphUser user, Response response) {
                            if (user != null) {
                                Log.i(TAG, "Facebook ID " + user.getId());
                                Log.i(TAG, "Email " + user.asMap().get("email"));
                                Log.i(TAG, "UserName " + user.getName());

                                requestKtLogin(user.getId(), user.getName());
                            }
                        }
                    });
                }
            }
        });
    }

    // check if device is connected to internet
    private boolean isConnectedToNetwork() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo == null) {
            return false;
        } else {
            return true;
        }
    }


    class Login_Res_End {
        private Boolean success;
        private String kt_id, username, fb_id;

        public void setSuccess(Boolean bool) {
            this.success = bool;
        }

        public void setKt_id(String kt) {
            this.kt_id = kt;
        }

        public void setUsername(String name) {
            this.username = name;
        }

        public void setFb_id(String fb) {
            this.fb_id = fb;
        }

        public Boolean getSuccess() {
            return success;
        }

        public String getKt_id() {
            return kt_id;
        }

        public String getUsername() {
            return username;
        }

        public String getFb_id() {
            return fb_id;
        }
    }

    class Login_Res_End_Deserializer implements JsonDeserializer<Login_Res_End> {
        public Login_Res_End deserialize (JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException {
            JsonObject jsonObj = json.getAsJsonObject();
            Boolean success = jsonObj.get("success").getAsBoolean();
            try {
                resEndKt_id = jsonObj.get("user_id").getAsString();
                resEndUserName = jsonObj.get("username").getAsString();
                resEndFb_id = jsonObj.get("facebookid").getAsString();
            } catch (NullPointerException nullex) {}

            Login_Res_End resEndObj = new Login_Res_End();
            resEndObj.setSuccess(success);
            resEndObj.setKt_id(resEndKt_id);
            resEndObj.setUsername(resEndUserName);
            resEndObj.setFb_id(resEndFb_id);

            return resEndObj;
        }
    }

    private void requestKtLogin(String id, String name) {
        Log.i(TAG, "requestKtLogin: (" + id + ") + " + name);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        String Url = "http://kandi.nodejitsu.com/login";

        HttpClient client = new DefaultHttpClient();

        try {

            HttpPost post = new HttpPost(Url);

            JSONObject loginObj = new JSONObject();
            loginObj.put("facebookid", id);
            loginObj.put("username", name);

            StringEntity entity = new StringEntity(loginObj.toString(), HTTP.UTF_8);

            entity.setContentType("application/json");
            post.setEntity(entity);

            HttpResponse response = client.execute(post);
            BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
            String line = "";
            while ((line = reader.readLine()) != null) {
                //Log.i(TAG, line);

                GsonBuilder gsonBuilder = new GsonBuilder();
                gsonBuilder.registerTypeAdapter(Login_Res_End.class, new Login_Res_End_Deserializer());
                Gson gson = gsonBuilder.create();

                Login_Res_End resEndObj = gson.fromJson(line, Login_Res_End.class);

                String resEndKt_id = resEndObj.getKt_id();
                String resEndUserName = resEndObj.getUsername();
                String resEndFb_id = resEndObj.getFb_id();

                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString(KTID, resEndKt_id);
                editor.putString(FBID, resEndFb_id);
                editor.putString(USERNAME, resEndUserName);
                editor.commit();

                String myKt_id = sharedPreferences.getString(KTID, "");
                String myFb_id = sharedPreferences.getString(FBID, "");
                String myUserName = sharedPreferences.getString(USERNAME, "");

                System.out.println("Login: " + myUserName + " (" + myKt_id + ") " + "(" + myFb_id + ") " + "is logged in");

                Intent intent_to_start = new Intent(this, Main.class);
                startActivity(intent_to_start);
                //overridePendingTransition(R.anim.abc_slide_in_bottom, R.anim.abc_slide_out_bottom);
                progressDialog.dismiss();
                finish();
                Intent dismissSplash = new Intent();
                setResult(RESULT_OK, dismissSplash);

            }
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    private void onSessionStateChange(Session session, SessionState state, Exception exception) {
        if (state.isOpened()) {
            Log.i(TAG, "Logged in");
            /*
            Intent returnIntent = new Intent();
            setResult(RESULT_OK, returnIntent);
            finish(); */
            //Intent intent_to_start = new Intent(this, Main.class);
            //startActivity(intent_to_start);
        } else if (state.isClosed()) {
            Log.i(TAG, "Logged out");
        }
    }


    private void hideLoginButton() {
        ktLoginButton.setVisibility(View.GONE);
        loginButtonBackground.setVisibility(View.GONE);
    }

    @Override
    public void onResume() {
        super.onResume();
        uiHelper.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        uiHelper.onPause();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //uiHelper.onActivityResult(requestCode, resultCode, data);
        Session.getActiveSession().onActivityResult(this, requestCode, resultCode, data);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        uiHelper.onDestroy();
        Intent dismissSplash = new Intent();
        setResult(RESULT_OK, dismissSplash);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        uiHelper.onSaveInstanceState(outState);
    }
}
