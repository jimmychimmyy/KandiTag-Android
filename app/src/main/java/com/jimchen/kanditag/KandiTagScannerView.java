package com.jimchen.kanditag;

import android.content.Context;

import eu.livotov.zxscan.ScannerView;

/**
 * Created by Jim on 6/10/15.
 */
public class KandiTagScannerView extends ScannerView{

    public KandiTagScannerView(Context context) {
        super(context);
    }

    @Override
    protected int getScannerLayoutResource() {
        return R.layout.kanditag_scannerview;
    }
}
