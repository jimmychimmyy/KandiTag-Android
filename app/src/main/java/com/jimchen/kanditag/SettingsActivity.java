package com.jimchen.kanditag;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.facebook.Session;

/**
 * Created by Jim on 5/13/15.
 */
public class SettingsActivity extends Activity {

    private static final int RESULT_LOGGED_OUT = 9;
    private Button signOut;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);

        signOut = (Button) findViewById(R.id.Settings_signout);
        signOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Session session = Session.getActiveSession();
                session.closeAndClearTokenInformation();
                Intent return_intent = new Intent();
                setResult(RESULT_LOGGED_OUT, return_intent);
                finish();
                overridePendingTransition(R.anim.right_slide_in, R.anim.abc_fade_out);
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Intent result = new Intent();
        setResult(RESULT_OK, result);
        finish();
    }
}
