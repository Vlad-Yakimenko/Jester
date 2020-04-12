package com.example.jester;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.view.MotionEvent;
import android.view.View;

public class CustomView extends View {

    Paint paint;
    Path path;
    float x = 300, y = 300;
    float side;
    String text = "Test";

    boolean drag = false;
    float dragX = 0, dragY = 0;

    public CustomView(Context context) {
        super(context);
        paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setTextSize(100);
        side = paint.measureText(text);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawText(text, x, y, paint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float evX = event.getX();
        float evY = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (evX >= x && evX <= x + side && evY >= y && evY <= y + side) {
                    drag = true;
                    dragX = evX - x;
                    dragY = evY - y;
                }
                break;

            case MotionEvent.ACTION_MOVE:
                if (drag) {
                    x = evX - dragX;
                    y = evY - dragY;
                    invalidate();
                }
                break;

            case MotionEvent.ACTION_UP:
                drag = false;
                break;
        }

        return true;
    }
}
