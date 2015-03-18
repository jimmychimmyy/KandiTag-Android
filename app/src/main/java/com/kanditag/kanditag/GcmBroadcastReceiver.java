package com.kanditag.kanditag;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

/**
 * Created by Jim on 3/11/15.
 */
public class GcmBroadcastReceiver extends WakefulBroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        //Explicitly specify that GcmBroadcastReceiver will handle the intent
        ComponentName comp = new ComponentName(context.getPackageName(), GcmIntentService.class.getName());
        //start the wakeful service, keeping the device awake while it is launching
        startWakefulService(context, (intent.setComponent(comp)));
        setResultCode(Activity.RESULT_OK);
    }
}
