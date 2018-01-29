/*
 * Author: Rio
 * Date: 2017/12/15
 */

package net.rio.controller;

import net.rio.wifi.RobotClient;

import android.content.Context;
import android.graphics.*; // Bitmap{,Factory}, Canvas, Paint, Rect
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class ControlView extends View implements RobotClient.OnReceiveListener {

    private OnMoveListener listener;

    private Paint paint = new Paint();
    private Bitmap cameraBitmap;

    private final static float RANGE = 150f;
    private float sx, sy, cx, cy;

    private Rect viewRect = new Rect();

    public ControlView(Context context, AttributeSet attributes) {
        super(context, attributes);

        paint.setAntiAlias(true);
        paint.setStrokeWidth(5f);
    }

    public void setOnMoveListener(OnMoveListener listener) {
        this.listener = listener;
    }

    @Override
    public void onSizeChanged(int w, int h, int oldw, int oldh) {
        viewRect.right = w;
        viewRect.bottom = h;
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
        paint.setStyle(Paint.Style.STROKE);
        if(cameraBitmap != null)
            can.drawBitmap(cameraBitmap, null, viewRect, paint);
        if(sx != 0 && sy != 0) {
            paint.setStyle(Paint.Style.STROKE);
            paint.setARGB(0xff, 0xff, 0xff, 0xff);
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
