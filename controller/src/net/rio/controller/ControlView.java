/*
 * Author: Rio
 * Date: 2017/12/15
 */

package net.rio.controller;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class ControlView extends View {

    private Paint paint = new Paint();
    private OnMoveListener listener;
    private final static float RANGE = 150f;
    private float sx, sy, cx, cy;

    public ControlView(Context context, AttributeSet attributes) {
        super(context, attributes);
    }

    public void setOnMoveListener(OnMoveListener listener) {
        this.listener = listener;
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
        if(sx != 0 && sy != 0) {
            paint.setAntiAlias(true);
            paint.setStrokeWidth(5f);
            paint.setStyle(Paint.Style.STROKE);
            paint.setARGB(0xff, 0xff, 0xff, 0xff);
            can.drawCircle(sx, sy, RANGE, paint);
            paint.setStyle(Paint.Style.FILL);
            can.drawCircle(cx, cy, RANGE / 3, paint);
        }
    }

    public static interface OnMoveListener {
        public void onMove(float x, float y);
    }
}
