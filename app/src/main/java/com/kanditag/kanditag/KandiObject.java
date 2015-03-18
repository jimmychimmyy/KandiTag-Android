package com.kanditag.kanditag;

/**
 * Created by Jim on 3/1/15.
 */
public class KandiObject {

    private String qrCode, kandi_name;

    public  KandiObject() {}

    public KandiObject(String qr, String name) {
        this.qrCode = qr;
        this.kandi_name = name;
    }

    public void setQrCode(String qr) {
        this.qrCode = qr;
    }

    public void setKandi_name(String name) {
        this.kandi_name = name;
    }

    public String getQrCode() {
        return qrCode;
    }

    public String getKandi_name() {
        return kandi_name;
    }

}
