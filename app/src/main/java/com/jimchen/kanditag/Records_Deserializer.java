package com.jimchen.kanditag;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

/**
 * Created by Jim on 3/1/15.
 */
class Records_Deserializer implements JsonDeserializer<Records> {

    private String group;

    private String message, from_id, to_id;
    private boolean sent;

    private String kt_id, fb_id, username, _id;
    private int placement;
    private String from_name, to_name;

    private String timestamp;

    private String kandi_name, kandi_id;

    public Records deserialize (JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();

        try {
            to_name = jsonObject.get("to_name").getAsString();
        } catch (NullPointerException nullEx) {
            nullEx.printStackTrace();
        }

        try {
            message = jsonObject.get("message").getAsString();
        } catch (NullPointerException nulLEx) {}

        try {
            username = jsonObject.get("username").getAsString();
        } catch (NullPointerException nullEx) {}

        try {
            fb_id = jsonObject.get("fb_id").getAsString();
        } catch (NullPointerException nullEx) {}

        try {
            kt_id = jsonObject.get("kt_id").getAsString();
        } catch (NullPointerException nullEx) {}
        try {
            kandi_id = jsonObject.get("kandi_id").getAsString();
        } catch (NullPointerException nullEx) {}
        try {
            kandi_name = jsonObject.get("kandi_name").getAsString();
        } catch (NullPointerException nullEx) {}
        try {
            timestamp = jsonObject.get("timestamp").getAsString();
        } catch (NullPointerException nullEx) {}

        try {
            from_name = jsonObject.get("from_name").getAsString();
        } catch (NullPointerException nullEx) {
            nullEx.printStackTrace();
        }
        try {
            from_id = jsonObject.get("from_id").getAsString();
            to_id = jsonObject.get("to_id").getAsString();
            sent = jsonObject.get("sent").getAsBoolean();
        } catch (NullPointerException nullEx) {}
        try {
            _id = jsonObject.get("_id").getAsString();
            placement = jsonObject.get("placement").getAsInt();
        } catch (NullPointerException nullEx) {}
        try {
            group = jsonObject.get("group").getAsString();
        } catch (NullPointerException nullEx) {}

        Records recordsObj = new Records();

        try {
            recordsObj.setKandiID(kandi_id);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            recordsObj.setKandi_name(kandi_name);
        } catch (NullPointerException nullEx) {}
        try {
            recordsObj.setTimestamp(timestamp);
        } catch (NullPointerException nullEx) {}
        try {
            recordsObj.setFrom_name(from_name);
            recordsObj.setTo_name(to_name);
        } catch (NullPointerException nullEx) {}
        try {
            recordsObj.setMessage(message);
            recordsObj.setFrom_id(from_id);
            recordsObj.setTo_id(to_id);
            recordsObj.setSent(sent);
        } catch (NullPointerException nullEx) {}
        try {
            recordsObj.setQrcode(kandi_id);
            recordsObj.setKt_id(kt_id);
            recordsObj.setFb_id(fb_id);
            recordsObj.setUsername(username);
            recordsObj.set_id(_id);
            recordsObj.setPlacement(placement);
        } catch (NullPointerException nullEx) {}
        try {
            recordsObj.setGroup(group);
        } catch (NullPointerException nullEx) {}

        return recordsObj;
    }
}
