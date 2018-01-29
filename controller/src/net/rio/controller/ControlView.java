/*
 * Author: Rio
 * Date: 2017/12/15
 */

package net.rio.controller;

import net.rio.wifi.RobotClient;

import android.content.Context;
import android.graphics.*; // Bitmap{,Factory}, Canvas, Paint, Matrix, PorterDuff, RectF
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class ControlView extends View implements RobotClient.OnReceiveListener {

    private OnMoveListener listener;

    private Paint paint = new Paint();

    private final static float RANGE = 150f;
    private float sx, sy, cx, cy;

    private Bitmap cameraBitmap;
    private Matrix imgMatrix = new Matrix();
    private int disW, disH, imgRot;

    public ControlView(Context context, AttributeSet attributes) {
        super(context, attributes);

        paint.setAntiAlias(true);
        paint.setStrokeWidth(5f);
        paint.setARGB(0xff, 0xff, 0xff, 0xff);
    }

    public void setOnMoveListener(OnMoveListener listener) {
        this.listener = listener;
    }

    public void setCameraAngle(int angle) {
        imgRot = angle;
    }

    public void clearImage() {
        cameraBitmap = null;
    }

    @Override
    public void onSizeChanged(int w, int h, int oldw, int oldh) {
        disW = w;
        disH = h;
    }

    @Override
    public boolean onTouchEvent(MotionEvent eve) {
        switch(eve.getAction()) {
            case MotionEvent.ACTION_DOWN:
                cx = sx = eve.getX();
                cy = sy = eve.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                cx = eve.getX();
                cy = eve.getY();

                double dx = cx - sx;
                double dy = cy - sy;

                double distance = Math.hypot(dx, dy);

                if(distance > RANGE) {
                    cx = (float) (sx + dx / distance * RANGE);
                    cy = (float) (sy + dy / distance * RANGE);
                }
                break;
            case MotionEvent.ACTION_UP:
                sx = sy = cx = cy = 0;
                break;
            default:
                return false;
        }

        if(listener != null)
            listener.onMove((cx - sx) / RANGE, (cy - sy) / RANGE);

        invalidate();
        return true;
    }

    @Override
    public void onDraw(Canvas can) {

        can.drawColor(0, PorterDuff.Mode.CLEAR);

        if(cameraBitmap != null) {
            int imgW = cameraBitmap.getWidth();
            int imgH = cameraBitmap.getHeight();

            imgMatrix.setRectToRect(
                    new RectF(0, 0, imgW, imgH), new RectF(0, 0, disW, disH),
                    Matrix.ScaleToFit.FILL);
            imgMatrix.preRotate(imgRot, imgW / 2f, imgH / 2f);

            can.drawBitmap(cameraBitmap, imgMatrix, paint);
        }

        if(sx != 0 && sy != 0) {
            paint.setStyle(Paint.Style.STROKE);
            can.drawCircle(sx, sy, RANGE, paint);
            paint.setStyle(Paint.Style.FILL);
            can.drawCircle(cx, cy, RANGE / 3, paint);
        }
    }

    @Override
    public void onReceive(byte[] data) {
        cameraBitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
        postInvalidate();
    }

    public static interface OnMoveListener {
        public void onMove(float x, float y);
    }
}
