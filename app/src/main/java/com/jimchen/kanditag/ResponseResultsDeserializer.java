package com.jimchen.kanditag;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

/**
 * Created by Jim on 6/9/15.
 */
public class ResponseResultsDeserializer implements JsonDeserializer<ResponseResults> {

    // KtUser
    private String kt_id, username, kandi_id;
    private int placement;

    // message && group message
    private String message, from_id, from_name, timestamp;
    private String to_id, to_name;
    private String to_kandi_id, to_kandi_name;

    public ResponseResults deserialize (JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException {

        JsonObject object = json.getAsJsonObject();

        try {
            kt_id = object.get("kt_id").getAsString();
            username = object.get("username").getAsString();
            kandi_id = object.get("kandi_id").getAsString();
            placement = object.get("placement").getAsInt();
        } catch (NullPointerException e) {

        }

        try {
            message = object.get("message").getAsString();
            from_id = object.get("from_id").getAsString();
            from_name = object.get("from_name").getAsString();
            timestamp = object.get("timestamp").getAsString();
        } catch (NullPointerException e) {

        }

        try {
            to_id = object.get("to_id").getAsString();
            to_name = object.get("to_name").getAsString();
        } catch (NullPointerException e) {

        }

        try {
            to_kandi_id = object.get("to_kandi_id").getAsString();
            to_kandi_name = object.get("to_kandi_name").getAsString();
        } catch (NullPointerException e) {

        }


        ResponseResults results = new ResponseResults();
        try {
            results.setKt_id(kt_id);
            results.setUsername(username);
            results.setKandi_id(kandi_id);
            results.setPlacement(placement);
        } catch (NullPointerException e) {

        }

        try {
            results.setMessage(message);
            results.setFrom_id(from_id);
            results.setFrom_name(from_name);
            results.setTimestamp(timestamp);
        } catch (NullPointerException e) {

        }

        try {
            results.setTo_id(to_id);
            results.setTo_name(to_name);
        } catch (NullPointerException e) {

        }

        try {
            results.setTo_kandi_id(to_kandi_id);
            results.setTo_kandi_name(to_kandi_name);
        } catch (NullPointerException e) {

        }

        return results;
    }
}
