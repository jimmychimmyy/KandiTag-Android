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
public class Res_End_Results_Deserializer implements JsonDeserializer<Res_End_Results> {

    private Boolean previous_user, sent;
    private int previousUserCount, placement;
    private Records[] records;
    private String qrCode, kt_id, fb_id, user_name, _id, kandiName, date, message;
    private String error;

    private String gmMessage, gmFromID, gmFromName, gmKandiGroup, gmKandiName, gmTime;

    private Boolean success;

    public Res_End_Results deserialize (JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException {

        JsonObject jsonObject = json.getAsJsonObject();
        try {
            gmMessage = jsonObject.get("message").getAsString();
            gmFromID = jsonObject.get("from_id").getAsString();
            gmFromName = jsonObject.get("from_name").getAsString();
            gmKandiGroup = jsonObject.get("kandi_group").getAsString();
            gmKandiName = jsonObject.get("kandi_name").getAsString();
            gmTime = jsonObject.get("time").getAsString();
        } catch (NullPointerException nullEx) {}
        try {
            success = jsonObject.get("success").getAsBoolean();
        } catch (NullPointerException nullEx) {}

        try {
            sent = jsonObject.get("sent").getAsBoolean();
            date = jsonObject.get("date").getAsString();
            message = jsonObject.get("msg").getAsString();
        } catch (NullPointerException nullEx) {}

        try {
            qrCode = jsonObject.get("qrcode").getAsString();
            kt_id = jsonObject.get("kt_id").getAsString();
            fb_id = jsonObject.get("fb_id").getAsString();
            user_name = jsonObject.get("username").getAsString();
            placement = jsonObject.get("placement").getAsInt();
            kandiName = jsonObject.get("kandi_name").getAsString();

        } catch (NullPointerException nullEx) {}

        try {
            error = jsonObject.get("error").getAsString();
        } catch (NullPointerException nullEx) {}

        try {
            records = context.deserialize(jsonObject.get("records"), Records[].class);
        } catch (NullPointerException nullEx) {}

        try {
            previous_user = jsonObject.get("previous_user").getAsBoolean();
            previousUserCount = jsonObject.get("previous_userCount").getAsInt();
        } catch (NullPointerException nullEx) {}

//*******************

        Res_End_Results resEndObj = new Res_End_Results();
        try {
            resEndObj.setGmMessage(gmMessage);
            resEndObj.setGmFrom_id(gmFromID);
            resEndObj.setGmFrom_name(gmFromName);
            resEndObj.setGmKandi_group(gmKandiGroup);
            resEndObj.setGmKandi_name(gmKandiName);
            resEndObj.setGmTime(gmTime);
        } catch (NullPointerException nullEx) {}
        try {
            resEndObj.setSuccess(success);
        } catch (NullPointerException nullEx) {}
        try {
            resEndObj.setMessage(message);
            resEndObj.setDate(date);
            resEndObj.setSent(sent);
        } catch (NullPointerException nullEx) {}
        try {
            resEndObj.setPrevious_user(previous_user);
            resEndObj.setPrevious_userCount(previousUserCount);
        } catch (NullPointerException nullEx) {}
        try {
            resEndObj.setQrCode(qrCode);
            resEndObj.setKt_id(kt_id);
            resEndObj.setFb_id(fb_id);
            resEndObj.setUser_name(user_name);
            resEndObj.setPlacement(placement);
            resEndObj.setKandiName(kandiName);
        } catch (NullPointerException nullEx) {}
        try {
            resEndObj.setRecords(records);
        } catch (NullPointerException nullEx) {}

        return resEndObj;
    }
}
