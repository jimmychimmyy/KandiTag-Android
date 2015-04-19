package com.jimchen.kanditag;

/**
 * Created by Jim on 3/1/15.
 */
public class KandiObject {

    private String kandi_id, kandi_name;

    public  KandiObject() {}

    public KandiObject(String qr, String name) {
        this.kandi_id = qr;
        this.kandi_name = name;
    }

    public void setKandi_id(String qr) {
        this.kandi_id = qr;
    }

    public void setKandi_name(String name) {
        this.kandi_name = name;
    }

    public String getKandi_id() {
        return kandi_id;
    }

    public String getKandi_name() {
        return kandi_name;
    }

}
