package com.example.jester;

import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.os.Environment;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import androidx.constraintlayout.widget.ConstraintLayout;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Random;

public class WorkspaceView extends View {

    private static final String TAG = "WorkspaceView";

    /** Main bitmap */
    private Bitmap mainBitmap = null;

    private Rect mMeasuredRect = new Rect(0, 0, getMeasuredWidth(), getMeasuredHeight());

    /** Paint to draw */
    private Paint paint;

    /** All available texts */
    private HashSet<TextArea> texts = new HashSet<>();
    private SparseArray<TextArea> textPointers = new SparseArray<>();

    /** Stores data about single text */
    private static class TextArea {

        String text;
        int x, y;
        int width, height;
        TextArea(String text, int x, int y, int width, int height) {
            this.text = text;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }

        @Override
        @SuppressWarnings("NullableProblems")
        public String toString() {
            return "Text[x: " + x + ", y: " + y + ", width: " + width + ", height: " + height + "]";
        }

    }

    /**
     * Default constructor
     *
     * @param context {@link android.content.Context}
     */
    public WorkspaceView(final Context context) {
        this(context, null);
    }

    public WorkspaceView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);

        init(context);
    }

    public WorkspaceView(final Context context, final AttributeSet attributeSet, final int defStyle) {
        super(context, attributeSet, defStyle);

        init(context);
    }

    private void init(final Context ct) {
        // Generate bitmap used for background

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inMutable = true;
        mainBitmap = BitmapFactory.decodeResource(ct.getResources(), R.drawable.android, options);

        paint = new Paint();

        paint.setColor(Color.BLUE);
        paint.setTextSize(100);
        paint.setTextAlign(Paint.Align.LEFT);
        paint.setStrokeWidth(40);
        paint.setStyle(Paint.Style.FILL);
    }

    @Override
    public void onDraw(Canvas canvas) {
        // background bitmap to cover all area
        canvas.drawBitmap(mainBitmap, null, mMeasuredRect, null);

        // TODO create own paint for each text
        for (TextArea textArea : texts) {
            paint.setColor(Color.BLUE);
            canvas.drawRect(textArea.x, textArea.y - textArea.height, textArea.x + textArea.width, textArea.y, paint);
            paint.setColor(Color.WHITE);
            canvas.drawText(textArea.text, textArea.x, textArea.y, paint);
        }

        CharSequence s = getMeasuredWidth() + ", " + getMeasuredHeight() +
                ":" + getWidth() + ", " + getHeight();
        Toast.makeText(getContext(), s, Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onTouchEvent(final MotionEvent event) {
        boolean handled = false;

        TextArea touchedCircle;
        int xTouch;
        int yTouch;
        int pointerId;
        int actionIndex = event.getActionIndex();

        // get touch event coordinates and make transparent circle from it
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                // it's the first pointer, so clear all existing pointers data
                clearCirclePointer();

                xTouch = (int) event.getX(0);
                yTouch = (int) event.getY(0);

                // check if we've touched inside some circle
                touchedCircle = obtainTouchedText(xTouch, yTouch);
                touchedCircle.x = xTouch;
                touchedCircle.y = yTouch;
                textPointers.put(event.getPointerId(0), touchedCircle);

                invalidate();
                handled = true;
                break;

            case MotionEvent.ACTION_POINTER_DOWN:
                Log.w(TAG, "Pointer down");
                // It secondary pointers, so obtain their ids and check circles
                pointerId = event.getPointerId(actionIndex);

                xTouch = (int) event.getX(actionIndex);
                yTouch = (int) event.getY(actionIndex);

                // check if we've touched inside some circle
                touchedCircle = obtainTouchedText(xTouch, yTouch);

                textPointers.put(pointerId, touchedCircle);
                touchedCircle.x = xTouch;
                touchedCircle.y = yTouch;
                invalidate();
                handled = true;
                break;

            case MotionEvent.ACTION_MOVE:
                final int pointerCount = event.getPointerCount();

                Log.w(TAG, "Move");

                for (actionIndex = 0; actionIndex < pointerCount; actionIndex++) {
                    // Some pointer has moved, search it by pointer id
                    pointerId = event.getPointerId(actionIndex);

                    xTouch = (int) event.getX(actionIndex);
                    yTouch = (int) event.getY(actionIndex);

                    touchedCircle = textPointers.get(pointerId);

                    if (null != touchedCircle) {
                        touchedCircle.x = xTouch;
                        touchedCircle.y = yTouch;
                    }
                }

                invalidate();
                handled = true;
                break;

            case MotionEvent.ACTION_UP:
                clearCirclePointer();
                invalidate();
                handled = true;
                break;

            case MotionEvent.ACTION_POINTER_UP:
                // not general pointer was up
                pointerId = event.getPointerId(actionIndex);

                textPointers.remove(pointerId);
                invalidate();
                handled = true;
                break;

            case MotionEvent.ACTION_CANCEL:
                handled = true;
                break;

            default:
                // do nothing
                break;
        }

        return super.onTouchEvent(event) || handled;
    }

    /**
     * Clears all TextArea - pointer id relations
     */
    private void clearCirclePointer() {
        Log.w(TAG, "clearCirclePointer");

        textPointers.clear();
    }

    /**
     * Search and creates new (if needed) circle based on touch area
     *
     * @param xTouch int x of touch
     * @param yTouch int y of touch
     *
     * @return obtained {@link TextArea}
     */
    private TextArea obtainTouchedText(final int xTouch, final int yTouch) {
        TextArea touchedText = getTouchedText(xTouch, yTouch);

        if (null == touchedText) {
            String test = "test";
            Rect rect = new Rect();
            paint.getTextBounds(test, 0, test.length(), rect);
            float width = paint.measureText(test);
            float height = rect.bottom - rect.top;
            touchedText = new TextArea(test, xTouch, yTouch, (int)width, (int)height);

            Log.w(TAG, "Added circle " + touchedText);
            texts.add(touchedText);
        }

        return touchedText;
    }

    /**
     * Determines touched circle
     *
     * @param xTouch int x touch coordinate
     * @param yTouch int y touch coordinate
     *
     * @return {@link TextArea} touched circle or null if no circle has been touched
     */
    private TextArea getTouchedText(final int xTouch, final int yTouch) {
        TextArea touched = null;

        for (TextArea textArea : texts) {
            if (xTouch >= textArea.x && xTouch <= textArea.x + textArea.width &&
                    yTouch >= textArea.y - textArea.height && yTouch <= textArea.y) {

                touched = textArea;
                break;
            }
        }

        return touched;
    }

    // TODO implement calculating bitmap size
    @Override
    protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mMeasuredRect = new Rect(0, 0, 300, 300);
    }

    // TODO understand layout() method!!!
    public Bitmap getMainBitmap() {
        Bitmap bitmap = Bitmap.createBitmap(getMeasuredWidth(),
                getMeasuredHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        this.draw(canvas);
        return bitmap;
    }
}