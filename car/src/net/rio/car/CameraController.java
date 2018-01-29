package net.rio.car;

import android.app.Activity;
import android.graphics.*; // ImageFormat, YuvImage, Rect
import android.hardware.Camera;
import android.util.AttributeSet;
import android.util.Log;
import android.view.*; // SurfaceView, SurfaceHolder

import java.io.IOException;
import java.io.ByteArrayOutputStream;

@SuppressWarnings("deprecation") // Camera is deprecated after API 23
public class CameraController implements SurfaceHolder.Callback, Camera.PreviewCallback {

    private Camera camera;
    private SurfaceView cameraView;
    private OnCameraDataListener cameraListener;

    private ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
    private Rect preRect;

    public CameraController(Activity activity, OnCameraDataListener cameraListener) {
        this.cameraListener = cameraListener;

        cameraView = (SurfaceView) activity.findViewById(R.id.camera_view);
        cameraView.getHolder().addCallback(this);
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

                preRect = new Rect(0, 0, size.width, size.height);

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
        YuvImage frameImage = new YuvImage(data, ImageFormat.NV21, preRect.right, preRect.bottom, null);
        frameImage.compressToJpeg(preRect, 50, byteStream);
        cameraListener.onCameraData(byteStream.toByteArray());
        byteStream.reset();
        camera.addCallbackBuffer(data);
    }


    public interface OnCameraDataListener {
        public void onCameraData(byte[] data);
    }
}
