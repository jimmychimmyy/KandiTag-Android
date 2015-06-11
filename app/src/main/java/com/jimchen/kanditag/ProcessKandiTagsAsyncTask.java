package com.jimchen.kanditag;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.widget.Toast;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.LuminanceSource;
import com.google.zxing.Reader;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;

import eu.livotov.zxscan.decoder.zxing.ZXRGBLuminanceSource;

/**
 * Created by Jim on 6/11/15.
 */
public class ProcessKandiTagsAsyncTask extends AsyncTask<Void, Void, String> {

    private String decodedKandiTag;
    private Camera camera;
    private byte[] data;

    public ProcessKandiTagsAsyncTask(byte[] data, Camera camera) {
        this.data = data;
        this.camera = camera;
    }

    @Override
    protected String doInBackground(Void... params) {


        FileOutputStream outputStream = null;
        try {
            YuvImage yuvImage = new YuvImage(data, ImageFormat.NV21, camera.getParameters().getPreviewSize().width, camera.getParameters().getPreviewSize().height, null);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            yuvImage.compressToJpeg(new Rect(0, 0, camera.getParameters().getPreviewSize().width, camera.getParameters().getPreviewSize().height), 80, baos);
            Bitmap bitmap = BitmapFactory.decodeByteArray(baos.toByteArray(), 0, baos.toByteArray().length);
            if (bitmap != null) {
                LuminanceSource source = new ZXRGBLuminanceSource(bitmap);
                BinaryBitmap binaryBitmap = new BinaryBitmap(new HybridBinarizer(source));
                Reader reader = new QRCodeReader();

                try {
                    Result result = reader.decode(binaryBitmap);
                    decodedKandiTag = result.getText();
                    //Toast.makeText(Main.this, decodedKandiTag, Toast.LENGTH_SHORT).show();

                    // TODO add profile view (list view maybe?)
                    // when closing profile view set decodedKandiID to ""

                } catch (Exception e) {
                }
            }
        } catch (Exception e) {
        }

        return decodedKandiTag;
    }

    @Override
    protected void onPostExecute(String decodedKandiTag) {
        System.out.println(decodedKandiTag);
    }
}
