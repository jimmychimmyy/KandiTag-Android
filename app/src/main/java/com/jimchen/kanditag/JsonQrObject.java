package com.jimchen.kanditag;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Jim on 3/1/15.
 */
public class JsonQrObject extends JSONObject {

    private String kandi_id, kt_id, fb_id, user_name, kandi_name, token;

    public JsonQrObject() {}

    //this is to check for the qr
    public JsonQrObject(String qr) {
        this.kandi_id = qr;
        try {
            this.put("qrcode", kandi_id);
        } catch (JSONException jsonEx) {}
    }

    public void setKt_id(String kt_id) {
        this.kt_id = kt_id;
        try {
            this.put("kt_id", kt_id);
        } catch (JSONException jsonEx) {}
    }

    public String getKt_id() {
        return kt_id;
    }

    public void setKandi_id(String kandi_id) {
        this.kandi_id = kandi_id;
        try {
            this.put("qrcode", kandi_id);
        } catch (JSONException jsonEx) {}
    }

    public String getKandi_id() {
        return kandi_id;
    }

    public void setFb_id(String fb_id) {
        this.fb_id = fb_id;
        try {
            this.put("fb_id", fb_id);
        } catch (JSONException jsonEx) {}
    }

    public void setToken(String token) {
        this.token = token;
        try {
            this.put("token", token);
        } catch (JSONException jsonEx) {}
    }

    public String getToken() {
        return token;
    }

    public void setUser_name(String name) {
        this.user_name = name;
        try {
            this.put("user_name", name);
        } catch (JSONException jsonEx) {}
    }

    public String getUser_name() {
        return user_name;
    }

    public String getFb_id() {
        return fb_id;
    }

    public JsonQrObject(String qr, String kt, String fb, String name) {
        this.kandi_id = qr;
        this.kt_id = kt;
        this.fb_id = fb;
        this.user_name = name;
        try {
            this.put("qrcode", kandi_id);
            this.put("kt_id", kt_id);
            this.put("fb_id", fb_id);
            this.put("username", user_name);
        } catch (JSONException jsonEx) {
        }
    }

    //this is to actually save the qr
    public JsonQrObject(String qr, String kt, String fb, String name, String kandi_name) {
        this.kandi_id = qr;
        this.kt_id = kt;
        this.fb_id = fb;
        this.user_name = name;
        this.kandi_name = kandi_name;
        try {
            this.put("qrcode", kandi_id);
            this.put("kt_id", kt_id);
            this.put("fb_id", fb_id);
            this.put("username", user_name);
            this.put("kandi_name", kandi_name);
        } catch (JSONException jsonEx) {
        }
    }

    //TODO add getters and setters, but maybe dont need to

}
