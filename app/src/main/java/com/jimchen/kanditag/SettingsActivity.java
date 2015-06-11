package com.jimchen.kanditag;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.facebook.Session;

/**
 * Created by Jim on 5/13/15.
 */
public class SettingsActivity extends Activity {

    public static final int SIGN_OUT_REQUEST = 0;
    public static final int RESULT_LOGGED_OUT = 9;

    private Button signOut;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);

        signOut = (Button) findViewById(R.id.Settings_signout);
        signOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                new AlertDialog.Builder(SettingsActivity.this)
                        .setTitle("KandiTag")
                        .setMessage("Are you sure you want to log out?")
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog, int whichButton) {

                                Intent return_intent = new Intent();
                                setResult(RESULT_LOGGED_OUT, return_intent);
                                finish();
                                overridePendingTransition(R.anim.right_slide_in, R.anim.abc_slide_out_bottom);

                            }})
                        .setNegativeButton(android.R.string.no, null).show();
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Intent result = new Intent();
        setResult(RESULT_CANCELED, result);
        finish();
        overridePendingTransition(R.anim.right_slide_in, R.anim.abc_slide_out_bottom);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent result = new Intent();
        setResult(RESULT_CANCELED, result);
        finish();
        overridePendingTransition(R.anim.right_slide_in, R.anim.abc_slide_out_bottom);
    }
}
