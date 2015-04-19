package com.jimchen.kanditag;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;

/**
 * Created by Jim on 3/11/15.
 */
public class GcmIntentService extends IntentService {

    public static final int NOTIFICATION_ID = 1;
    private static final String TAG = "GcmIntentService";
    private NotificationManager myNotificationManager;
    NotificationCompat.Builder builder;

    private KtDatabase myDatabase;

    //message vars
    private String msg, from_id, from_name, to_id, to_name, timestamp;
    private KtMessageObject ktMessageObject;

    //kandi vars
    private String kandiMsg;

    //group message var
    private String kandi_group, kandi_name;
    private GroupMessageItem groupMessageItem;

    public GcmIntentService() {
        super("GcmIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        try {
            msg = intent.getStringExtra("msg");
            from_id = intent.getStringExtra("from_id");
            from_name = intent.getStringExtra("from_name");
            to_id = intent.getStringExtra("to_id");
            to_name = intent.getStringExtra("to_name");
            //TODO change the server key to timestamp
            timestamp = intent.getStringExtra("time");

            ktMessageObject = new KtMessageObject(msg, to_id, to_name, from_id, from_name, timestamp);

            myDatabase = new KtDatabase(getApplicationContext());
            myDatabase.saveMessage(ktMessageObject);

        } catch (NullPointerException nullEx) {}

        try {
            kandiMsg = intent.getStringExtra("kandi");
        } catch (NullPointerException nullEx) {}

        try {
            msg = intent.getStringExtra("msg");
            from_id = intent.getStringExtra("from_id");
            from_name = intent.getStringExtra("from_name");
            kandi_group = intent.getStringExtra("kandi_group");
            kandi_name = intent.getStringExtra("kandi_name");
            timestamp = intent.getStringExtra("time");

            groupMessageItem = new GroupMessageItem(msg, from_id, from_name, kandi_group, timestamp);

            myDatabase = new KtDatabase(getApplicationContext());
            myDatabase.saveGroupMessage(groupMessageItem);

        } catch (NullPointerException nullEx) {}

        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);

        String messageType = gcm.getMessageType(intent);
        System.out.println("messageType: " + messageType);

        if (!extras.isEmpty()) {
            if (GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR.equals(messageType)) {
                sendNotification("Send Error: " + extras.toString());
            } else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED.equals(messageType)) {
                sendNotification("Deleted messages on server: " + extras.toString());
            } else if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) {
                for (int i = 0; i <5; i++) {
                    Log.i(TAG, "Working..." + (i+1) + "/5 @ " + SystemClock.elapsedRealtime());
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException interruptedEx) {

                    }
                }
                Log.i(TAG, "Completed Work @ " + SystemClock.elapsedRealtime());
                if (ktMessageObject != null) {
                    newMessageNotification(ktMessageObject);
                } else if (kandiMsg != null) {
                    newKandiUserNotification(kandiMsg);
                } else if (kandi_group != null) {
                    newGroupMessageNotification(groupMessageItem);
                } else {
                    sendNotification(extras.toString());
                }
                //TODO clean up the notification with ui
                //Log.i(TAG, extras.toString());
            }
        }
        GcmBroadcastReceiver.completeWakefulIntent(intent);
    }

    private void sendNotification(String mssg) {
        myNotificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), 0);
        NotificationCompat.Builder myBuilder = new NotificationCompat.Builder(this).setSmallIcon(R.drawable.splash_screen_kt_logo_universal).
                setContentTitle("KandiTag").
                setStyle(new NotificationCompat.BigTextStyle().bigText(mssg)).setContentText(mssg);
        myBuilder.setContentIntent(contentIntent);
        // vibrate delay, vibrate length, vibrate delay, vibrate length
        myBuilder.setVibrate(new long[]{100, 200, 100, 500});
        myBuilder.setLights(Color.YELLOW, 3000, 3000);
        myBuilder.setColor(Color.BLACK);
        myBuilder.setWhen(System.currentTimeMillis());
        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        myBuilder.setSound(alarmSound);
        myNotificationManager.notify(NOTIFICATION_ID, myBuilder.build());
    }

    private void newKandiUserNotification(String mssg) {
        myNotificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, null, 0);

        NotificationCompat.Builder myBuilder = new NotificationCompat.Builder(this).setSmallIcon(R.drawable.splash_screen_kt_logo_universal).
                setContentTitle("KandiTag").
                setStyle(new NotificationCompat.BigTextStyle().bigText(mssg)).setContentText(mssg);

        myBuilder.setContentIntent(contentIntent);
        // vibrate delay, vibrate length, vibrate delay, vibrate length
        myBuilder.setVibrate(new long[]{100, 200, 100, 500});
        myBuilder.setLights(Color.YELLOW, 3000, 3000);
        myBuilder.setColor(Color.BLACK);
        myBuilder.setWhen(System.currentTimeMillis());
        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        myBuilder.setSound(alarmSound);

        myBuilder.setAutoCancel(true);

        myNotificationManager.notify(NOTIFICATION_ID, myBuilder.build());

    }

    private void newGroupMessageNotification(GroupMessageItem message) {
        myNotificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);

        //Intent toGroupMessageIntent = new Intent(getApplicationContext(), Message.class);
        //toGroupMessageIntent.putExtra("kandi_group", message.getKandi_id());

        //PendingIntent contentIntent = PendingIntent.getActivity(this, 0, toGroupMessageIntent, 0);

        NotificationCompat.Builder myBuilder = new NotificationCompat.Builder(this).setSmallIcon(R.drawable.splash_screen_kt_logo_universal).
                setContentTitle(message.getFromName()).
                setStyle(new NotificationCompat.BigTextStyle().bigText(message.getMessage())).setContentText(message.getMessage());

        //myBuilder.setContentIntent(contentIntent);
        // vibrate delay, vibrate length, vibrate delay, vibrate length
        myBuilder.setVibrate(new long[]{100, 200, 100, 500});
        myBuilder.setLights(Color.YELLOW, 3000, 3000);
        myBuilder.setColor(Color.BLACK);
        myBuilder.setWhen(System.currentTimeMillis());
        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        myBuilder.setSound(alarmSound);

        myBuilder.setAutoCancel(true);

        myNotificationManager.notify(NOTIFICATION_ID, myBuilder.build());

    }

    private void newMessageNotification(KtMessageObject message) {
        myNotificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);

        //Intent toMessageIntent = new Intent(getApplicationContext(), Message.class);
        //toMessageIntent.putExtra("fb_id", message.getFrom_id());

        //PendingIntent contentIntent = PendingIntent.getActivity(this, 0, toMessageIntent, 0);

        NotificationCompat.Builder myBuilder = new NotificationCompat.Builder(this).setSmallIcon(R.drawable.splash_screen_kt_logo_universal).
                setContentTitle(message.getFrom_name()).
                setStyle(new NotificationCompat.BigTextStyle().bigText(message.getMessage())).setContentText(message.getMessage());

        //myBuilder.setContentIntent(contentIntent);
        // vibrate delay, vibrate length, vibrate delay, vibrate length
        myBuilder.setVibrate(new long[]{100, 200, 100, 500});
        myBuilder.setLights(Color.YELLOW, 3000, 3000);
        myBuilder.setColor(Color.BLACK);
        myBuilder.setWhen(System.currentTimeMillis());
        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        myBuilder.setSound(alarmSound);

        myBuilder.setAutoCancel(true);

        myNotificationManager.notify(NOTIFICATION_ID, myBuilder.build());
    }
}
