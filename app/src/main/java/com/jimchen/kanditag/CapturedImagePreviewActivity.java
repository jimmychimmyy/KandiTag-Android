package com.jimchen.kanditag;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.FormatException;
import com.google.zxing.LuminanceSource;
import com.google.zxing.NotFoundException;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Reader;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;


public class CapturedImagePreviewActivity extends Activity {

    public static final String CAPTURED_IMAGE = "com.jimchen.kanditag.extras.CAPTURED_IMAGE";

    private byte[] image;
    private Bitmap bitmap;

    // xml
    private ImageView imageContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_captured_image_preview);

        try {
            image = getIntent().getExtras().getByteArray(CAPTURED_IMAGE);
            imageContainer.setImageBitmap(convertByteToBitmap(image));
        } catch (Exception e) {}
    }

    private Bitmap convertByteToBitmap(byte[] data) {

        int screenWidth = getApplicationContext().getResources().getDisplayMetrics().widthPixels;
        int screenHeight = getApplicationContext().getResources().getDisplayMetrics().heightPixels;
        bitmap = BitmapFactory.decodeByteArray(data, 0, (data != null) ? data.length : 0);

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            Bitmap scaledB = Bitmap.createScaledBitmap(bitmap, bitmap.getWidth() / 2, bitmap.getHeight() / 2, true);
            int width = scaledB.getWidth();
            int height = scaledB.getHeight();

            //rotate 90 degrees with matrix
            Matrix matrix = new Matrix();
            matrix.postRotate(90);
            bitmap = Bitmap.createBitmap(scaledB, 0, 0, width, height, matrix, true);


        } else {
            //landscape
            Bitmap scaled = Bitmap.createScaledBitmap(bitmap, screenWidth, screenHeight, true);
            bitmap = scaled;
        }

        return bitmap;
    }

}
