package com.kanditag.kanditag;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

/**
 * Created by Jim on 2/22/15.
 */
public class TakenPictureContainer extends Activity {

    View rootView;
    private ImageView pictureContainer;
    private byte[] imageData;

    KtDatabase myDatabase;

    public TakenPictureContainer() {}

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_taken_picture_container);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        myDatabase = new KtDatabase(this);

        pictureContainer = (ImageView) findViewById(R.id.takenPictureContainer_container);

        Bundle params = getIntent().getExtras();
        imageData = params.getByteArray("image");

        Bitmap bitmap = BitmapFactory.decodeByteArray(imageData, 0, imageData.length);

        pictureContainer.setImageBitmap(bitmap);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

}
