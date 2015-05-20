package com.jimchen.kanditag;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.app.Activity;
import android.os.Handler;
import android.content.Intent;
import android.util.Log;

import com.facebook.Session;


public class SplashActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        int myTimer = 1200;
        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                Intent i = new Intent(SplashActivity.this, LoginActivity.class);
                startActivityForResult(i, 0);
                //finish(); // close this activity
            }
        }, myTimer);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Session.getActiveSession().onActivityResult(this, requestCode, resultCode, data);


        if (requestCode == 0) {
            if (resultCode == RESULT_OK) {
                System.out.println("Splash: Main Loaded.");
                finish();
            }
        }
    }

}
