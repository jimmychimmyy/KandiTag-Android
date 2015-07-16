package com.jimchen.kanditag;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;


public class ImagePreview extends Activity {

    private static final int REQUEST_CODE_POST_OPTIONS = 5;

    private ImageView cancel, post, download, share;
    private ImageView imageContainer;

    private String filename, filepath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_preview);

        connectXML();

        getImage();
    }

    private void connectXML() {

        imageContainer = (ImageView) findViewById(R.id.ImagePreview_ImageContainer);
        cancel = (ImageView) findViewById(R.id.ImagePreview_Cancel);
        post = (ImageView) findViewById(R.id.ImagePreview_Post);
        download = (ImageView) findViewById(R.id.ImagePreview_Download);
        share = (ImageView) findViewById(R.id.ImagePreview_Share);

        post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ImagePreview.this, PostOptions.class);
                startActivityForResult(intent, REQUEST_CODE_POST_OPTIONS);
            }
        });
    }

    private void getImage() {
        try {
            Bundle extras = getIntent().getExtras();
            //filename = extras.getString("filename");
            filepath = extras.getString("filepath");
            loadImageFromStorage(filepath, filename);
        } catch (Exception e) {}
    }

    private void loadImageFromStorage(String path, String filename) {
        try {
            File file = new File(filepath);
            //File file = new File(path, filename);
            Bitmap bitmap = BitmapFactory.decodeStream(new FileInputStream(file));
            imageContainer.setImageBitmap(bitmap);
        } catch (FileNotFoundException e) {}
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == REQUEST_CODE_POST_OPTIONS) {
            if (resultCode == RESULT_OK) {

            }
        }
    }
}
