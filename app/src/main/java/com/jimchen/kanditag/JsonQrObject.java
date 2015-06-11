package com.jimchen.kanditag;

import android.graphics.Bitmap;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Dictionary;

/**
 * Created by Jim on 3/1/15.
 */
public class JsonQrObject extends JSONObject {

    private String kandi_id, kt_id, fb_id, user_name, kandi_name, token, img_caption;
    private Bitmap img;
    private byte[] image;
    private ArrayList<String> tags;

    // for message
    private String message, from_id, from_name;
    private String to_id, to_name, to_kandi_id, to_kandi_name;

    public void setMessage(String message) {
        this.message = message;
        try {
            this.put("message", message);
        } catch (JSONException e) {

        }
    }

    public void setFrom_id(String from_id) {
        this.from_id = from_id;
        try {
            this.put("from_id", from_id);
        } catch (JSONException e) {

        }
    }

    public void setFrom_name(String from_name) {
        this.from_name = from_name;
        try {
            this.put("from_name", from_name);
        } catch (JSONException e) {

        }
    }

    public void setTo_id(String to_id) {
        this.to_id = to_id;
        try {
            this.put("to_id", to_id);
        } catch (JSONException e) {

        }
    }

    public void setTo_name(String to_name) {
        this.to_name = to_name;
        try {
            this.put("to_name", to_name);
        } catch (JSONException e) {

        }
    }

    public void setTo_kandi_id(String kandi_id) {
        this.to_kandi_id = kandi_id;
        try {
            this.put("to_kandi_id", to_kandi_id);
        } catch (JSONException e) {

        }
    }

    public void setTo_kandi_name(String kandi_name) {
        this.to_kandi_name = kandi_name;
        try {
            this.put("to_kandi_name", to_kandi_name);
        } catch (JSONException e) {

        }
    }

    public JsonQrObject() {}

    //this is to check for the qr
    public JsonQrObject(String qr) {
        this.kandi_id = qr;
        try {
            this.put("qrcode", kandi_id);
        } catch (JSONException jsonEx) {}
    }

    public void setImage(byte[] img) {
        this.image = img;
        try {
            this.put("image", image);
        } catch (JSONException e) {

        }
    }

    public byte[] getImage() {
        return image;
    }

    public void setTags(ArrayList<String> list) {
        this.tags = list;
        try {
            this.put("tags", tags);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public ArrayList<String> getTags() {
        return tags;
    }

    public void setImgCaption(String caption) {
        this.img_caption = caption;
        try {
            this.put("img_caption", img_caption);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public String getImgCaption() {
        return img_caption;
    }

    public void setImg(Bitmap image) {
        this.img = image;
        try {
            this.put("img", img);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public Bitmap getImg() {
        return img;
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

    // for posting images
    public JsonQrObject(String kt_id, Bitmap img) {
        this.kt_id = kt_id;
        this.img = img;
        try {
            this.put("kt_id", kt_id);
            this.put("img", img);
        } catch (JSONException e) {
            e.printStackTrace();
        }
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
