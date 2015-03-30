package com.kanditag.kanditag;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.hardware.Camera;
import android.hardware.camera2.CameraDevice;
import android.nfc.Tag;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Size;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup;
import android.widget.Toast;

//import com.dlazaro66.qrcodereaderview.QRCodeReaderView;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.FormatException;
import com.google.zxing.NotFoundException;
import com.google.zxing.PlanarYUVLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.ResultPoint;
//import com.google.zxing.client.android.camera.open.CameraManager;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * Created by Jim on 2/3/15.
 */
class Preview extends SurfaceView implements SurfaceHolder.Callback {

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        try {
            myCamera.setPreviewDisplay(holder);
            myCamera.setDisplayOrientation(90);
            myCamera.startPreview();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } catch (NullPointerException nullEx) {
            System.out.println(nullEx.toString());
        }
    }

    @Override
    public void onLayout(boolean changed, int x, int y, int z, int a) {

    }

    public static final String TAG = "Preview";

    private SurfaceHolder myHolder;
    public Camera myCamera;
    private Context myContext;
    private SurfaceView mySurfaceView;
    private Size myPreviewSize;
    private List<String> mySupportedFlashModes;
    private List<Camera.Size> mySupportedPreviewSizes;

    /**
     * Preview (Context context) {
     * super(context);
     * myHolder = getHolder();
     * myHolder.addCallback(this);
     * myHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
     * myCamera = openBackFacingCamera();
     * //myCamera.setDisplayOrientation(90);
     * }
     * <p/>
     * /**
     * public Preview(Context context, AttributeSet attrs) {
     * super(context, attrs);
     * myContext = context;
     * }
     */

    public Preview(Context context, Camera camera) {
        super(context);
        myCamera = camera;
        myHolder = getHolder();
        myHolder.addCallback(this);
        myHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

    }


    public void setCamera(Camera camera) {
        if (myCamera == camera) {
            return;
        }

        stopPreviewAndFreeCamera();

        myCamera = camera;

        if (myCamera != null) {
            List<Camera.Size> localSizes = myCamera.getParameters().getSupportedPreviewSizes();
            mySupportedPreviewSizes = localSizes;
            requestLayout();

            try {
                myCamera.setPreviewDisplay(myHolder);
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }

            myCamera.startPreview();
        }
    }

    private void stopPreviewAndFreeCamera() {
        if (myCamera != null) {
            myCamera.stopPreview();
            myCamera.release();
            myCamera = null;
        }
    }

    public Size getPreviewSize() {
        return myPreviewSize;
    }

    public void surfaceDestroyed(SurfaceHolder holder) {

    }

   public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
       if (myHolder.getSurface()== null) {
           return;
       }

       try {
           myCamera.stopPreview();
       } catch (Exception e) {

       }

       try {
           myCamera.setPreviewDisplay(myHolder);
           myCamera.startPreview();
       } catch (Exception e) {
           e.printStackTrace();
       }
   }
}

    /**

    public void surfaceCreated(SurfaceHolder holder) {
        //
        //
       // myCamera = Camera.open();

        try {
            myCamera.setPreviewDisplay(holder);
            myCamera.setPreviewCallback(new Camera.PreviewCallback() {
                @Override
                public void onPreviewFrame(byte[] data, Camera camera) {
                    /**
                    FileOutputStream outputStream = null;
                    try {
                        outputStream = new FileOutputStream(String.format("/sdcard/%d.png", System.currentTimeMillis()));
                        outputStream.write(data);
                        outputStream.close();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                    }

                    Preview.this.invalidate();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        if (myCamera != null) {
            myCamera.stopPreview();
        }
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Camera.Parameters parameters = myCamera.getParameters();
        List<Camera.Size> sizes = parameters.getSupportedPreviewSizes();
        Camera.Size cs = sizes.get(0);
        parameters.setPreviewSize(cs.width, cs.height);
        myCamera.setDisplayOrientation(90);
        myCamera.setParameters(parameters);
        myCamera.startPreview();
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        Paint p = new Paint(Color.RED);
        Log.d(TAG, "draw");
        canvas.drawText("PREVIEW", canvas.getWidth() / 2, canvas.getHeight() / 2, p);
    }

    private Camera openBackFacingCamera() {
        int cameraCount = 0;
        Camera camera = null;
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        cameraCount = Camera.getNumberOfCameras();
        for (int camIdx = 0; camIdx < cameraCount; camIdx++) {
            Camera.getCameraInfo(camIdx, cameraInfo);
            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                try {
                    camera = Camera.open(camIdx);
                } catch (RuntimeException e) {
                    Log.e(TAG, "Camera Failed to Open: " + e.getLocalizedMessage());
                }
            }
        }
        return camera;
    }
}

     **/
