package com.jimchen.kanditag;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by Jim on 1/28/15.
 */
public class KtDatabase extends SQLiteOpenHelper {

    SharedPreferences sharedPreferences;
    public static final String MyPreferences = "MyPrefs";
    public static final String Name = "nameKey";
    public static final String FbId = "fbidKey";
    public static final String UserId = "userIdKey";

    private static final String TAG = "KTDatabase";
    private String MY_KT_ID, MY_FB_ID, MY_USER_NAME;

    private static final String DATABASE_NAME = "kt_database";
    private static final int DATABASE_VERSION = 2;

    private static final String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS ";

    private static final String KANDI_TABLE = "kt_kandi";
    private static final String KANDI_COLUMN_QR = "qrcode";
    private static final String KANDI_COLUMN_GROUP_NAME = "group_name";

    private static final String MESSAGE_TABLE = "kt_message";
    private static final String MESSAGE_COLUMN_MSSG = "message";
    private static final String MESSAGE_COLUMN_FROM_ID = "from_id";
    private static final String MESSAGE_COLUMN_FROM_NAME = "from_name";
    private static final String MESSAGE_COLUMN_TO_ID = "to_id";
    private static final String MESSAGE_COLUMN_TO_NAME = "to_name";
    private static final String MESSAGE_COLUMN_TIME = "time";

    private static final String GROUP_MESSAGE_TABLE = "kt_group_message";
    private static final String GROUP_MESSAGE_COLUMN_MSSG = "message";
    private static final String GROUP_MESSAGE_COLUMN_FROM_ID = "from_id";
    private static final String GROUP_MESSAGE_COLUMN_FROM_NAME = "from_name";
    private static final String GROUP_MESSAGE_COLUMN_GROUP = "kt_group";
    private static final String GROUP_MESSAGE_COLUMN_TIME = "time";

    private static final String KT_USERS_TABLE = "kt_users";
    private static final String KT_USERS_COLUMN_KT_ID = "kt_id";
    private static final String KT_USERS_COLUMN_FB_ID = "fb_id";
    private static final String KT_USERS_COLUMN_USERNAME = "username";
    private static final String KT_USERS_COLUMN_KANDI_GROUP = "kt_group";
    private static final String KT_USERS_COLUMN_PLACEMENT = "placement";



    KtDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.i(TAG, "onCreate");
        db.execSQL(CREATE_TABLE + KANDI_TABLE + " (_id integer primary key, qrcode VARCHAR(32), group_name VARCHAR(32));");
        db.execSQL(CREATE_TABLE + MESSAGE_TABLE + " (_id integer primary key, message text, from_id VARCHAR(32), from_name VARCHAR(32), to_id VARCHAR(32), to_name VARCHAR(32), time integer);");
        db.execSQL(CREATE_TABLE + GROUP_MESSAGE_TABLE + " (_id integer primary key, message text, from_id VARCHAR(32), from_name VARCHAR(32), kt_group VARCHAR(32), time integer);");
        db.execSQL(CREATE_TABLE + KT_USERS_TABLE + " (_id integer primary key, kt_id VARCHAR(32), fb_id VARCHAR(32), username VARCHAR(32), kt_group VARCHAR(32), placement integer);");


        //will not be needing these tables anymore but double check to make sure that the getters and setters for these tables are not being used
        /**
        db.execSQL(CREATE_TABLE + FOLLOWERS_TABLE + " (_id integer primary key, user_id VARCHAR(32), facebook_id VARCHAR(32), kt_name VARCHAR(32));");
        db.execSQL(CREATE_TABLE + FOLLOWING_TABLE + " (_id integer primary key, user_id VARCHAR(32), facebook_id VARCHAR(32), kt_name VARCHAR(32));");
        db.execSQL(CREATE_TABLE + TEMP_IMAGE_TABLE + " (_id integer primary key, image BLOB);");
         **/
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
        onCreate(database);
    }

    public boolean checkIfGroupMessageExists(GroupMessageItem item) {
        System.out.println("KtDatabase.checkIfGroupMessageExists");
        boolean exists = false;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("select * from kt_group_message where from_id='" + item.getFromID() + "' and from_name='" + item.getFromName() + "' and kt_group='" + item.getQrCode() + "' and time='" + item.getTime() + "'", null);
        res.moveToFirst();
        if (res.getCount() == 1) {
            System.out.println("count = 1");
            exists = true;
        } else if (res.getCount() > 1) {
            exists = true;
            System.out.println("count exceeds 1, error check db");
        } else if (res.getCount() < 1) {
            exists = false;
            System.out.println("count < 1");
        }
        return exists;
    }


    public boolean checkIfKtUserExists(KtUserObject user) {
        System.out.println("KtDatabase.checkIfKtUserExists");
        boolean exists = false;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("select * from kt_users where kt_id='" + user.getKt_id() + "' and fb_id='" + user.getFb_id() + "' and username='" + user.getName() + "' and kt_group='" + user.getQrCode() + "' and placement=" + user.getPlacement() + "", null);
        res.moveToFirst();
        if (res.getCount() == 1) {
            System.out.println("count = 1");
            exists = true;
        } else if (res.getCount() > 1) {
            exists = true;
            System.out.println("count exceeds 1, error check db");
        } else if (res.getCount() < 1) {
            exists = false;
            System.out.println("count < 1");
        }
        return exists;
    }

    public boolean saveKtUser(KtUserObject user) {
        System.out.println("KtDatabase.saveKtUser");
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(KT_USERS_COLUMN_KT_ID, user.getKt_id());
        contentValues.put(KT_USERS_COLUMN_FB_ID, user.getFb_id());
        contentValues.put(KT_USERS_COLUMN_USERNAME, user.getName());
        contentValues.put(KT_USERS_COLUMN_KANDI_GROUP, user.getQrCode());
        contentValues.put(KT_USERS_COLUMN_PLACEMENT, user.getPlacement());
        db.insert(KT_USERS_TABLE, null, contentValues);
        return true;
    }

    public boolean checkIfKandiExists(KandiObject kandi) {
        boolean exists = false;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("select * from kt_kandi where qrcode='" + kandi.getQrCode() + "' and group_name='" + kandi.getKandi_name() + "'", null);
        res.moveToFirst();
        if (res.getCount() == 1) {
            System.out.println("count = 1");
            exists = true;
        } else if (res.getCount() > 1) {
            exists = true;
            System.out.println("count exceeds 1, error check db");
        } else if (res.getCount() < 1) {
            exists = false;
            System.out.println("count < 1");
        }
        return exists;
    }

    public boolean saveKandi(KandiObject item) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(KANDI_COLUMN_QR, item.getQrCode());
        contentValues.put(KANDI_COLUMN_GROUP_NAME, item.getKandi_name());
        db.insert(KANDI_TABLE, null, contentValues);
        return true;
    }

    public boolean saveGroupMessage(GroupMessageItem item) {
        System.out.println("KtDatabase.saveGroupMessage");
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(GROUP_MESSAGE_COLUMN_MSSG, item.getMessage());
        contentValues.put(GROUP_MESSAGE_COLUMN_FROM_ID, item.getFromID());
        contentValues.put(GROUP_MESSAGE_COLUMN_FROM_NAME, item.getFromName());
        contentValues.put(GROUP_MESSAGE_COLUMN_GROUP, item.getQrCode());
        contentValues.put(GROUP_MESSAGE_COLUMN_TIME, item.getTime());
        db.insert(GROUP_MESSAGE_TABLE, null, contentValues);
        return true;
    }

    public boolean saveMessage(KtMessageObject item) {
        System.out.println("KtDatabase.saveMessage");
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(MESSAGE_COLUMN_MSSG, item.getMessage());
        contentValues.put(MESSAGE_COLUMN_FROM_ID, item.getFrom_id());
        contentValues.put(MESSAGE_COLUMN_FROM_NAME, item.getFrom_name());
        contentValues.put(MESSAGE_COLUMN_TO_ID, item.getTo_id());
        contentValues.put(MESSAGE_COLUMN_TO_NAME, item.getTo_name());
        contentValues.put(MESSAGE_COLUMN_TIME, item.getTime());
        db.insert(MESSAGE_TABLE, null, contentValues);
        return true;
    }

    public boolean checkIfAlreadyExistsInKtMessage(KtMessageObject item) {
        System.out.println("KtDatabase.checkIfAlreadyExistsInKtMessage");
        boolean exists = false;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("select * from kt_message where from_id='" + item.getFrom_id() + "' and to_id='" + item.getTo_id() + "' and time='" + item.getTime() + "'", null);
        res.moveToFirst();
        if (res.getCount() == 1) {
            System.out.println("count = 1");
            exists = true;
        } else if (res.getCount() > 1) {
            exists = true;
            System.out.println("count exceeds 1, error check db");
        } else if (res.getCount() < 1) {
            exists = false;
            System.out.println("count < 1");
        }
        return exists;
    }

    /**
    public boolean saveMessage(ConversationListItem item) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(MESSAGE_COLUMN_MSSG, item.getMessage());
        contentValues.put(MESSAGE_COLUMN_FROM_ID, item.getSenderID());
        contentValues.put(MESSAGE_COLUMN_FROM_NAME, item.getSenderName());
        contentValues.put(MESSAGE_COLUMN_TO_ID, item.getRecipientID());
        contentValues.put(MESSAGE_COLUMN_TO_NAME, item.getRecipientName());
        contentValues.put(MESSAGE_COLUMN_TIME, item.getTime());
        db.insert(MESSAGE_TABLE, null, contentValues);
        return true;
    }
     **/

    //returns an array list of all users for new message selection in Message.java
    public ArrayList<KtUserObjectParcelable> getAllKtUserForNewMessage() {
        System.out.println("KtDatabase.getAllKtUserForNewMessage");
        ArrayList<KtUserObjectParcelable> ktUserArrayList = new ArrayList<>();
        ArrayList<String> kt_idArrayList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("select * from kt_users", null);
        res.moveToFirst();
        while (!res.isAfterLast()) {
            KtUserObjectParcelable tempKtUserObj = new KtUserObjectParcelable();
            tempKtUserObj.setKt_id(res.getString(res.getColumnIndex(KT_USERS_COLUMN_KT_ID)));
            tempKtUserObj.setFb_id(res.getString(res.getColumnIndex(KT_USERS_COLUMN_FB_ID)));
            tempKtUserObj.setUser_name(res.getString(res.getColumnIndex(KT_USERS_COLUMN_USERNAME)));
            // will not be needing the group or placement for individual users
            //tempKtUserObj.setQrCode(res.getString(res.getColumnIndex(KT_USERS_COLUMN_KANDI_GROUP)));
            //tempKtUserObj.setPlacement(res.getInt(res.getColumnIndex(KT_USERS_COLUMN_PLACEMENT)));

            if (!kt_idArrayList.contains(tempKtUserObj.getKt_id())) {
                kt_idArrayList.add(tempKtUserObj.getKt_id());
                ktUserArrayList.add(tempKtUserObj);
            }
            res.moveToNext();
        }
        return ktUserArrayList;
    }

    //get groups for new message selection in Message.java
    public ArrayList<KtUserObject> getGroupsForNewMessage(ArrayList<String> kandi) {
        ArrayList<KtUserObject> ktUserGroupArrayList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        for (int i = 0; i < kandi.size(); i++) {
            Cursor res = db.rawQuery("select * from kt_users where kt_group=" + kandi.get(i) + "", null);
            res.moveToFirst();
            while (!res.isAfterLast()) {
                KtUserObject tempKtUserObj = new KtUserObject();
                tempKtUserObj.setKt_id(res.getString(res.getColumnIndex(KT_USERS_COLUMN_KT_ID)));
                tempKtUserObj.setFb_id(res.getString(res.getColumnIndex(KT_USERS_COLUMN_FB_ID)));
                tempKtUserObj.setName(res.getString(res.getColumnIndex(KT_USERS_COLUMN_USERNAME)));
                tempKtUserObj.setQrCode(res.getString(res.getColumnIndex(KT_USERS_COLUMN_KANDI_GROUP)));
                ktUserGroupArrayList.add(tempKtUserObj);
                res.moveToNext();
            }
        }
        return ktUserGroupArrayList;
    }

    // get all messages for a single user
    // you will have the entire list of conversation but will show only the latest 10/12 and lazy load the rest
    //use the latest item in each of the returned array list to populate the message ui
    public ArrayList<ConversationListItem> getEntireConversationPrivateMessage(String fb_id) {
        System.out.println("KtDatabase.getEntireConversationPrivateMessage");
        ArrayList<ConversationListItem> messageArrayList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("select * from kt_message where from_id='" + fb_id + "' or to_id='" + fb_id + "'", null);
        res.moveToFirst();
        while (!res.isAfterLast()) {
            ConversationListItem tempConvoItem = new ConversationListItem();
            tempConvoItem.setMessage(res.getString(res.getColumnIndex(MESSAGE_COLUMN_MSSG)));
            tempConvoItem.setSenderID(res.getString(res.getColumnIndex(MESSAGE_COLUMN_FROM_ID)));
            tempConvoItem.setSenderName(res.getString(res.getColumnIndex(MESSAGE_COLUMN_FROM_NAME)));
            tempConvoItem.setRecipientID(res.getString(res.getColumnIndex(MESSAGE_COLUMN_TO_ID)));
            tempConvoItem.setRecipientName(res.getString(res.getColumnIndex(MESSAGE_COLUMN_FROM_NAME)));
            tempConvoItem.setTime(res.getString(res.getColumnIndex(MESSAGE_COLUMN_TIME)));
            messageArrayList.add(tempConvoItem);
            res.moveToNext();
        }
        return messageArrayList;
    }

    public ArrayList<ConversationListItem> getArrayListOfAllMessagesInKtMessage() {
        System.out.println("KtDatabase.getArrayListOfAllMessagesInKtMessage");
        ArrayList<ConversationListItem> messageArrayList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("select * from kt_message", null);
        res.moveToFirst();
        while (!res.isAfterLast()) {
            ConversationListItem tempConvoItem = new ConversationListItem();
            tempConvoItem.setMessage(res.getString(res.getColumnIndex(MESSAGE_COLUMN_MSSG)));
            tempConvoItem.setSenderID(res.getString(res.getColumnIndex(MESSAGE_COLUMN_FROM_ID)));
            tempConvoItem.setSenderName(res.getString(res.getColumnIndex(MESSAGE_COLUMN_FROM_NAME)));
            tempConvoItem.setRecipientID(res.getString(res.getColumnIndex(MESSAGE_COLUMN_TO_ID)));
            tempConvoItem.setRecipientName(res.getString(res.getColumnIndex(MESSAGE_COLUMN_FROM_NAME)));
            tempConvoItem.setTime(res.getString(res.getColumnIndex(MESSAGE_COLUMN_TIME)));
            messageArrayList.add(tempConvoItem);
            res.moveToNext();
        }
        return messageArrayList;
    }

    public ArrayList<GroupMessageItem> getMessagesForGroup(String ktGroup) {
        System.out.println("KtDatabase.getMessagesForGroup");
        ArrayList<GroupMessageItem> groupMessageItems = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("select * from kt_group_message where kt_group='" + ktGroup + "'", null);
        res.moveToFirst();
        while (!res.isAfterLast()) {
            GroupMessageItem groupMessageItem = new GroupMessageItem();
            groupMessageItem.setMessage(res.getString(res.getColumnIndex(GROUP_MESSAGE_COLUMN_MSSG)));
            groupMessageItem.setFromID(res.getString(res.getColumnIndex(GROUP_MESSAGE_COLUMN_FROM_ID)));
            groupMessageItem.setFromName(res.getString(res.getColumnIndex(GROUP_MESSAGE_COLUMN_FROM_NAME)));
            groupMessageItem.setQrCode(res.getString(res.getColumnIndex(GROUP_MESSAGE_COLUMN_GROUP)));
            groupMessageItem.setTime(res.getString(res.getColumnIndex(GROUP_MESSAGE_COLUMN_TIME)));
            groupMessageItems.add(groupMessageItem);
            res.moveToNext();
        }
        return groupMessageItems;
    }

    public ArrayList<ConversationListItem> getArrayListOfAllGroupKtMessages() {
        System.out.println("KtDatabase.getArrayListOfAllGroupKtMessages");
        ArrayList<ConversationListItem> groupMessageArrayList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("select * from kt_group_message", null);
        res.moveToFirst();
        while (!res.isAfterLast()) {
            ConversationListItem tempConvoItem = new ConversationListItem();
            tempConvoItem.setMessage(res.getString(res.getColumnIndex(GROUP_MESSAGE_COLUMN_MSSG)));
            tempConvoItem.setSenderID(res.getString(res.getColumnIndex(GROUP_MESSAGE_COLUMN_FROM_ID)));
            tempConvoItem.setSenderName(res.getString(res.getColumnIndex(GROUP_MESSAGE_COLUMN_FROM_NAME)));
            tempConvoItem.setRecipientID(res.getString(res.getColumnIndex(GROUP_MESSAGE_COLUMN_GROUP)));
            tempConvoItem.setTime(res.getString(res.getColumnIndex(GROUP_MESSAGE_COLUMN_TIME)));
            groupMessageArrayList.add(tempConvoItem);
            res.moveToNext();
        }

        return groupMessageArrayList;
    }

    //used in GetAllGroupsFromLocalDbAsyncTask
    //need to get all groups of kandi
    public ArrayList<KandiGroupObject> getKandiForGroups() {
        System.out.println("KtDatabase.getKandiForGroups");
        ArrayList<KandiGroupObject> kandiGroupObjectArrayList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("select * from kt_kandi", null);
        res.moveToFirst();
        while(!res.isAfterLast()) {
            KandiGroupObject kandiGroupObject = new KandiGroupObject();
            kandiGroupObject.setQrCode(res.getString(res.getColumnIndex(KANDI_COLUMN_QR)));
            kandiGroupObject.setKandi_name(res.getString(res.getColumnIndex(KANDI_COLUMN_GROUP_NAME)));
            //System.out.println("kandiGroupObject.getQr: " + kandiGroupObject.getQrCode());
            //System.out.println("kandiGroupObject.getKandi_name: " + kandiGroupObject.getKandi_name());
            res.moveToNext();
        }

        return kandiGroupObjectArrayList;
    }

    //used in GetAllGroupsFromLocalDbAsyncTask
    public ArrayList<KtUserObject> getKtUserObjectArrayForKandiGroupObjects(String qrCode) {
        System.out.println("KtDatabase.getKtUserObjectArrayForKandiGroupObjects");
        ArrayList<KtUserObject> ktUserObjectArrayList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("select * from kt_users where kt_group='" + qrCode + "'", null);
        res.moveToFirst();
        while(!res.isAfterLast()) {
            KtUserObject ktUserObject = new KtUserObject();
            ktUserObject.setKt_id(res.getString(res.getColumnIndex(KT_USERS_COLUMN_KT_ID)));
            ktUserObject.setFb_id(res.getString(res.getColumnIndex(KT_USERS_COLUMN_FB_ID)));
            ktUserObject.setName(res.getString(res.getColumnIndex(KT_USERS_COLUMN_USERNAME)));
            ktUserObject.setPlacement(res.getInt(res.getColumnIndex(KT_USERS_COLUMN_PLACEMENT)));
            ktUserObject.setQrCode(res.getString(res.getColumnIndex(KT_USERS_COLUMN_KANDI_GROUP)));
            ktUserObjectArrayList.add(ktUserObject);

            /**
            System.out.println("ktUserObject.getKtId: " + ktUserObject.getKt_id());
            System.out.println("ktUserObject.getFbId: " + ktUserObject.getFb_id());
            System.out.println("ktUserObject.getName: " + ktUserObject.getName());
            System.out.println("ktUserObject.getQrCode: " + ktUserObject.getQrCode());
            System.out.println("ktUserObject.getPlacement: " + ktUserObject.getPlacement());
             **/

            res.moveToNext();
        }
        return ktUserObjectArrayList;
    }

    public ArrayList<KandiObject> getKandi() {
        System.out.println("KtDatabase.getKandi");
        ArrayList<KandiObject> tempArray = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("select * from " + KANDI_TABLE + "", null);
        res.moveToFirst();
        while(!res.isAfterLast()) {
            KandiObject kandiObject = new KandiObject();
            kandiObject.setQrCode(res.getString(res.getColumnIndex(KANDI_COLUMN_QR)));
            kandiObject.setKandi_name(res.getString(res.getColumnIndex(KANDI_COLUMN_GROUP_NAME)));
            tempArray.add(kandiObject);

            //System.out.println("kandiObject.qrCode:" + kandiObject.getQrCode());
            //System.out.println("kandiObject.kandiName:" + kandiObject.getKandi_name());

            res.moveToNext();
        }

        return tempArray;
    }

    public ArrayList<String> getQrCodesFromKtUserTable() {
        ArrayList<String> tempArray = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("select * from kt_users", null);
        res.moveToFirst();
        while (!res.isAfterLast()) {
            String qrCode = res.getString(res.getColumnIndex(KT_USERS_COLUMN_KANDI_GROUP));
            if (!tempArray.contains(qrCode)) {
                tempArray.add(qrCode);
            }
            res.moveToNext();
        }
        return tempArray;
    }

    //gets a list of all other users' fb_ids
    public ArrayList<String> getFb_IdFromKtUserToDisplayLatestMessage() {
        ArrayList<String> fb_idArrayList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("select * from kt_users", null);
        res.moveToFirst();
        while (!res.isAfterLast()) {
            String fb_id = res.getString(res.getColumnIndex(KT_USERS_COLUMN_FB_ID));
            if (!fb_idArrayList.contains(fb_id)) {
                fb_idArrayList.add(fb_id);
                //System.out.println("getFbIdFromKtUser.fbid = " + fb_id);
            }
            res.moveToNext();
        }
        return fb_idArrayList;
    }

    public GroupMessageItem getLatestGroupMessage(String groupQr) {
        System.out.println("KtDatabase.getLatestGroupMessage");
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("select * from kt_group_message where kt_group='" + groupQr + "'", null);
        res.moveToLast();

        GroupMessageItem groupMessageItem = new GroupMessageItem();
        try {
            groupMessageItem.setMessage(res.getString(res.getColumnIndex(GROUP_MESSAGE_COLUMN_MSSG)));
            groupMessageItem.setFromID(res.getString(res.getColumnIndex(GROUP_MESSAGE_COLUMN_FROM_ID)));
            groupMessageItem.setFromName(res.getString(res.getColumnIndex(GROUP_MESSAGE_COLUMN_FROM_NAME)));
            groupMessageItem.setTime(res.getString(res.getColumnIndex(GROUP_MESSAGE_COLUMN_TIME)));
            groupMessageItem.setQrCode(res.getString(res.getColumnIndex(GROUP_MESSAGE_COLUMN_GROUP)));
        } catch (CursorIndexOutOfBoundsException cursorEx) {}
        return groupMessageItem;
    }

    public MessageListItem getLatestMessageBetween(String fb_id, String MY_FB_ID, String MY_USER_NAME) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("select * from kt_message where from_id='" + fb_id + "' or to_id='" + fb_id + "'", null);
        res.moveToLast();

        MessageListItem messageListItem = new MessageListItem();
        try {
            messageListItem.setSender(fb_id);
            messageListItem.setDescription(res.getString(res.getColumnIndex(MESSAGE_COLUMN_MSSG)));
        } catch (NullPointerException nullEx) {} catch (CursorIndexOutOfBoundsException cursorEx) {}

        try {
            if (MY_USER_NAME.equals(res.getString(res.getColumnIndex(MESSAGE_COLUMN_TO_NAME)))) {
                try {
                    messageListItem.setName(res.getString(res.getColumnIndex(MESSAGE_COLUMN_FROM_NAME)));
                } catch (NullPointerException nullEx) {
                } catch (CursorIndexOutOfBoundsException cursorEx) {
                }
            }
        } catch (CursorIndexOutOfBoundsException outOfBoundsEx) {
            outOfBoundsEx.printStackTrace();
        }


        try {
            if (MY_USER_NAME.equals(res.getString(res.getColumnIndex(MESSAGE_COLUMN_FROM_NAME)))) {
                try {
                    messageListItem.setName(res.getString(res.getColumnIndex(MESSAGE_COLUMN_TO_NAME)));
                } catch (NullPointerException nullEx) {
                } catch (CursorIndexOutOfBoundsException cursorEx) {
                }
            }
        } catch (CursorIndexOutOfBoundsException outOfBoundsEx) {
            outOfBoundsEx.printStackTrace();
        }

        //System.out.println("sender: " + messageListItem.getSender());
        //System.out.println("description: " + messageListItem.getDescription());
        //System.out.println("name: " + messageListItem.getName());

        return messageListItem;
    }

    public KtUserObject getSingleUserFromKtUser(String fb_id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("select * from kt_users where fb_id='" + fb_id + "'", null);
        res.moveToFirst();
        KtUserObject ktUserObject = new KtUserObject();
        ktUserObject.setName(res.getString(res.getColumnIndex(KT_USERS_COLUMN_USERNAME)));
        ktUserObject.setFb_id(res.getString(res.getColumnIndex(KT_USERS_COLUMN_FB_ID)));
        ktUserObject.setKt_id(res.getString(res.getColumnIndex(KT_USERS_COLUMN_KT_ID)));
        return ktUserObject;
    }

}
