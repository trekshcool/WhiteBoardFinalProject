package com.example.multiboard;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;


public class PaintView extends View {

    public static int DEFAULT_SIZE = 8;
    public static final int DEFAULT_COLOR = Color.RED;
    private static final float TOUCH_TOLERANCE = 4;
    private float mX, mY;
    private Path path;
    private Paint paint;
    private ArrayList<StrokePath> paths = new ArrayList<>();
    private int currentColor;
    private int strokeWidth;
    private boolean isScaled = false;
    private Bitmap bitmap;
    private Canvas canvas;
    private Paint bitmapPaint = new Paint(Paint.DITHER_FLAG);


    public PaintView(Context context) {
        this(context, null);
    }

    public PaintView(Context context, AttributeSet attrs) {
        super(context, attrs);
        paint = new Paint();
        paint.setAntiAlias(false);
        paint.setDither(true);
        paint.setColor(DEFAULT_COLOR);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setXfermode(null);
        paint.setAlpha(0xff);
    }

    public void init(int width, int height) {
        // Create canvas from a bitmap
        bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        canvas = new Canvas(bitmap);

        // Initialize brush
        currentColor = DEFAULT_COLOR;
        strokeWidth = DEFAULT_SIZE;
    }

    public void rescaleCanvas() {
        // Get dimensions
        int screenWidth = getMeasuredWidth();
        int screenHeight = getMeasuredHeight();
        int canvasWidth = canvas.getWidth();
        int canvasHeight = canvas.getHeight();

        // Scale canvas to fit Whiteboard onto the screen
        setScaleX((float) screenWidth / canvasWidth);
        setScaleY((float) screenHeight / canvasHeight);

        // Resize canvas to match Whiteboard dimensions
        ViewGroup.LayoutParams layoutParams = getLayoutParams();
        layoutParams.width = canvasWidth;
        layoutParams.height = canvasHeight;
        setLayoutParams(layoutParams);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // Re-scale the canvas, if it hasn't been done yet
        if (!isScaled) {
            isScaled = true;
            rescaleCanvas();
        }

        canvas.save();
        //canvas.drawColor(backgroundColor);

        for (StrokePath fp : paths) {
            paint.setColor(fp.getColor());
            paint.setStrokeWidth(fp.getStrokeWidth());
            paint.setMaskFilter(null);

            canvas.drawPath(fp.getPath(), paint);
        }

        canvas.drawBitmap(bitmap, 0, 0, bitmapPaint);
        canvas.restore();
    }

    private void touchStart(float x, float y) {
        path = new Path();
        StrokePath fp = new StrokePath(currentColor, strokeWidth, path);
        paths.add(fp);

        path.reset();
        path.moveTo(x, y);
        mX = x;
        mY = y;
    }

    private void touchMove(float x, float y) {
        float dx = Math.abs(x - mX);
        float dy = Math.abs(y - mY);

        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
            path.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);
            mX = x;
            mY = y;
        }
    }

    private void touchUp() {
        path.lineTo(mX, mY);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch(event.getAction()) {
            case MotionEvent.ACTION_DOWN :
                touchStart(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE :
                touchMove(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_UP :
                touchUp();
                invalidate();
                break;
        }

        return true;
    }

    /**
     * Sets the color of the brush to paint with.
     * @param color the int color of the brush.
     */
    public void setColor(int color) {
        currentColor = color;
        bitmapPaint.setColor(color);
    }

    /**
     * Sets the width of the brush to paint with.
     * @param width the radius of the brush stroke.
     */
    public void setStrokeWidth(int width) {
        strokeWidth = width;
        bitmapPaint.setStrokeWidth(width);
    }
    public float getStrokeWidth() {
        return bitmapPaint.getStrokeWidth();
    }
    public int getColor() {
        return bitmapPaint.getColor();
    }
}
