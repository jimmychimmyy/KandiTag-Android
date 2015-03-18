package com.kanditag.kanditag;

import java.util.ArrayList;

/**
 * Created by Jim on 3/4/15.
 */
public class KandiGroupObject {

    private String qrCode, kandi_name;
    private ArrayList<KtUserObject> users;

    public KandiGroupObject() {}

    public KandiGroupObject(String kandi, String kandi_name, ArrayList<KtUserObject> list) {
        this.qrCode = kandi;
        this.kandi_name = kandi_name;
        this.users = list;
    }

    public void setQrCode(String kandi) {
        this.qrCode = kandi;
    }

    public void setKandi_name(String name) {
        this.kandi_name = name;
    }

    public void setUsers(ArrayList<KtUserObject> list) {
        this.users = list;
    }

    public String getQrCode() {
        return qrCode;
    }

    public String getKandi_name() {
        return kandi_name;
    }

    public ArrayList<KtUserObject> getUsers() {
        return users;
    }
}
