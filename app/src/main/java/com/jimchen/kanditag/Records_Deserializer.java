package com.jimchen.kanditag;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

/**
 * Created by Jim on 3/1/15.
 */
class Records_Deserializer implements JsonDeserializer<Records> {

    private String group;

    private String msg, date, fID, tID;
    private boolean sent;

    private String qrcode, kt_id, fb_id, username, _id;
    private int placement;
    private String fromName, toName;

    private String time;

    private String kandiName;

    public Records deserialize (JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();

        try {
            toName = jsonObject.get("toName").toString();
        } catch (NullPointerException nullEx) {
            nullEx.printStackTrace();
        }

        try {
            msg = jsonObject.get("msg").getAsString();
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
            qrcode = jsonObject.get("qrcode").getAsString();
        } catch (NullPointerException nullEx) {}
        try {
            kandiName = jsonObject.get("kandi_name").getAsString();
        } catch (NullPointerException nullEx) {}
        try {
            time = jsonObject.get("date").getAsString();
        } catch (NullPointerException nullEx) {}

        try {
            fromName = jsonObject.get("fromName").getAsString();
        } catch (NullPointerException nullEx) {
            nullEx.printStackTrace();
        }
        try {
            fID = jsonObject.get("fID").getAsString();
            tID = jsonObject.get("tID").getAsString();
            date = jsonObject.get("date").getAsString();
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
            recordsObj.setKandiName(kandiName);
        } catch (NullPointerException nullEx) {}
        try {
            recordsObj.setTime(time);
        } catch (NullPointerException nullEx) {}
        try {
            recordsObj.setFromName(fromName);
            recordsObj.setToName(toName);
        } catch (NullPointerException nullEx) {}
        try {
            recordsObj.setMsg(msg);
            recordsObj.setFID(fID);
            recordsObj.setTID(tID);
            recordsObj.setDate(date);
            recordsObj.setSent(sent);
        } catch (NullPointerException nullEx) {}
        try {
            recordsObj.setQrcode(qrcode);
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
