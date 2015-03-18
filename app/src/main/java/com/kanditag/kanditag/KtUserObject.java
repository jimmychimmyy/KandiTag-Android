package com.kanditag.kanditag;

/**
 * Created by Jim on 2/27/15.
 */
public class KtUserObject {

    private String name, kt_id, fb_id, qrCode;
    private int placement;

    public KtUserObject() {}

    public KtUserObject(String nameString, String ktString, String fbString, String qrCode, int placement) {
        this.name = nameString;
        this.kt_id = ktString;
        this.fb_id = fbString;
        this.qrCode = qrCode;
        this.placement = placement;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setKt_id(String kt_id) {
        this.kt_id = kt_id;
    }

    public void setFb_id(String fb_id) {
        this.fb_id = fb_id;
    }


    public void setQrCode(String kandi) {
        this.qrCode = kandi;
    }

    public void setPlacement(int placement) {
        this.placement = placement;
    }



    public String getName() {
        return name;
    }

    public String getKt_id() {
        return kt_id;
    }

    public String getFb_id() {
        return fb_id;
    }

    public String getQrCode() {
        return qrCode;
    }

    public int getPlacement() {
        return placement;
    }
}
