package net.rio.car;

import android.content.Context;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.util.Log;
import android.view.*; // SurfaceView, SurfaceHolder

import java.io.IOException;

public class CameraView extends SurfaceView implements SurfaceHolder.Callback, Camera.PreviewCallback {

    private Camera camera;

    public CameraView(Context context, AttributeSet attributes) {
        super(context, attributes);

        getHolder().addCallback(this);;
    }

    public void openCamera() {
        Log.i(MainActivity.TAG, "Opening camera");
        try {
            camera = Camera.open(0);
        } catch(RuntimeException e) {
            Log.e(MainActivity.TAG, e.getMessage());
        }
    }

    public void releaseCamera() {
        if(camera != null) {
            Log.i(MainActivity.TAG, "Releasing camera");
            camera.stopPreview();
            camera.release();
            camera = null;
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        if(camera != null) {
            try {
                camera.setPreviewDisplay(holder);
                camera.setDisplayOrientation(90);
                camera.setPreviewCallbackWithBuffer(this);

                Camera.Parameters parameters = camera.getParameters();
                Camera.Size size = camera.getParameters().getPreviewSize();

                camera.addCallbackBuffer(
                        new byte[size.width * size.height *
                        ImageFormat.getBitsPerPixel(parameters.getPreviewFormat()) / 8]);

                camera.startPreview();
            } catch(IOException e) {
                Log.e(MainActivity.TAG, e.getMessage());
            }
        } else {
            Log.e(MainActivity.TAG, "Camera is null");
        }
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        Log.d(MainActivity.TAG, "Camera data " + data.length);
        camera.addCallbackBuffer(data);
    }
}
