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


    private KtMessageObject ktMessageObject;

    //kandi vars
    private String kandiMsg;

    //group message var
    private String kandi_id;
    private GroupMessageItem groupMessageItem;

    // KtUser for saving into db
    private KtUserObject ktUserObject;

    // response variables
    private String kt_id, username, kandi_name;
    private int placement;
    private String message, to_id, from_id, to_name, from_name, to_kandi_id, to_kandi_name, timestamp;

    public GcmIntentService() {
        super("GcmIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle extras = intent.getExtras();

        try {
            kt_id = intent.getStringExtra("kt_id");
            username = intent.getStringExtra("username");
            kandi_name = intent.getStringExtra("kandi_name");
            placement = intent.getIntExtra("placement", -1);
            message = intent.getStringExtra("message");
            from_id = intent.getStringExtra("from_id");
            from_name = intent.getStringExtra("from_name");
            to_id = intent.getStringExtra("to_id");
            to_name = intent.getStringExtra("to_name");
            to_kandi_id = intent.getStringExtra("to_kandi_id");
            to_kandi_name = intent.getStringExtra("to_kandi_name");
            timestamp = intent.getStringExtra("timestamp");

            // save message
            if (to_id != null && to_name != null) {
                ktMessageObject = new KtMessageObject();
                ktMessageObject.setMessage(message);
                ktMessageObject.setTo_id(to_id);
                ktMessageObject.setTo_name(to_name);
                ktMessageObject.setFrom_id(from_id);
                ktMessageObject.setFrom_name(from_name);
                ktMessageObject.setTimestamp(timestamp);
                myDatabase = new KtDatabase(getApplicationContext());
                myDatabase.saveMessage(ktMessageObject);
            }

            // save group message
            if (to_kandi_id != null && to_kandi_name != null) {
                ktMessageObject = new KtMessageObject();
                ktMessageObject.setMessage(message);
                ktMessageObject.setTo_Kandi_Id(to_kandi_id);
                ktMessageObject.setTo_Kandi_Name(to_kandi_name);
                ktMessageObject.setFrom_id(from_id);
                ktMessageObject.setFrom_name(from_name);
                ktMessageObject.setTimestamp(timestamp);
                myDatabase = new KtDatabase(getApplicationContext());
                myDatabase.saveGroupMessage(ktMessageObject);
            }

            // save kt_user
            if (kt_id != null && kandi_name != null && placement >= 0) {
                ktUserObject = new KtUserObject();
                ktUserObject.setKt_id(kt_id);
                ktUserObject.setUsername(username);
                ktUserObject.setKandiId(kandi_id);
                ktUserObject.setPlacement(placement);

                // will probably want to do all of this on seperate threads
                myDatabase = new KtDatabase(getApplicationContext());
                boolean exists = myDatabase.checkForExistingKtUser(ktUserObject);
                if (!exists) {
                    myDatabase.saveKtUser(ktUserObject);
                }
            }

        } catch (NullPointerException e) {

        }

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
                if (to_id != null) {
                    newMessageNotification(ktMessageObject);
                } else if (kt_id != null) {
                    newKandiTagUserNotification(username, kandi_name);
                } else if (to_kandi_id != null) {
                    newGroupMessageNotification(ktMessageObject);
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

    private void newKandiTagUserNotification(String username, String kandi_name) {
        myNotificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        //PendingIntent contentIntent = PendingIntent.getActivity(this, 0, null, 0);

        NotificationCompat.Builder myBuilder = new NotificationCompat.Builder(this).setSmallIcon(R.drawable.splash_screen_kt_logo_universal).
                setContentTitle("KandiTag").
                setStyle(new NotificationCompat.BigTextStyle().bigText(username + " has joined " + kandi_name)).setContentText(username + " has joined " + kandi_name);

        //myBuilder.setContentIntent(contentIntent);
        // vibrate delay, vibrate length, vibrate delay, vibrate length
        myBuilder.setVibrate(new long[]{100, 200, 100, 500});
        myBuilder.setLights(Color.YELLOW, 3000, 3000);
        myBuilder.setColor(Color.BLACK);
        myBuilder.setWhen(System.currentTimeMillis());
        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        myBuilder.setSound(alarmSound);

        myBuilder.setAutoCancel(true);

        // TODO will need to set the notification on a click listener to open an activity

        myNotificationManager.notify(NOTIFICATION_ID, myBuilder.build());

    }

    private void newGroupMessageNotification(KtMessageObject message) {
        myNotificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);

        //Intent toGroupMessageIntent = new Intent(getApplicationContext(), Message.class);
        //toGroupMessageIntent.putExtra("kandi_id", message.getKandi_id());

        //PendingIntent contentIntent = PendingIntent.getActivity(this, 0, toGroupMessageIntent, 0);

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
