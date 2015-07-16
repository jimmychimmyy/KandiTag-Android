package com.jimchen.kanditag;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.app.Activity;
import android.os.Handler;
import android.content.Intent;
import android.util.Log;
import android.view.WindowManager;

import com.facebook.Session;


public class SplashActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        //getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);


        int myTimer = 1200;
        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                Intent i = new Intent(SplashActivity.this, LoginActivity.class);
                startActivity(i);
                finish(); // close this activity
            }
        }, myTimer);

    }

}
