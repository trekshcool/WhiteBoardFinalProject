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
import android.widget.ImageView;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

/**
 * Manages the painting canvas View in the PaintingActivity.
 */
public class PaintView extends View {

    // Constant brush variables
    public static int DEFAULT_SIZE = 18;
    public static int SMALL_SIZE = 8;
    public static int LARGE_SIZE = 40;
    public static final int DEFAULT_COLOR = Color.BLACK;
    private static final float TOUCH_TOLERANCE = 4;

    // Stroke path variables
    private float mX, mY;
    private Path path;
    private StrokePath strokePath;
    private Paint paint;
    private ArrayList<StrokePath> paths = new ArrayList<>();
    private int currentColor;
    private int strokeWidth;

    // PaintView/canvas variables
    private boolean isScaled = false;
    private Bitmap bitmap;
    private Canvas canvas;
    private Paint bitmapPaint = new Paint(Paint.DITHER_FLAG);

    // Variables for up/downloading database information
    private Whiteboard whiteboard;
    private String userId;
    private DatabaseReference dbReference;
    private DatabaseReference curPathReference;

    // Other
    private ImageView imageInk;
    private boolean canDraw = true;

    ValueEventListener dataSetupListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            // If the database does not have stroke data for this whiteboard
            if (!dataSnapshot.hasChild(whiteboard.getName())) {
                // Create an empty StrokePath to get it started
                StrokePath emptyPath = new StrokePath(0xFFFFFFFF, 0);
                emptyPath.addPathPoint(0, 0);
                emptyPath.addPathPoint(1, 1);
                dbReference
                        .child("stroke-data")
                        .child(whiteboard.getName())
                        .push()
                        .setValue(emptyPath);
            }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {}
    };

    ValueEventListener pathListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            // Reset drawing paths
            paths = new ArrayList<>();

            // Iterate over all children of the node
            for (DataSnapshot ds : dataSnapshot.getChildren()) {
                // Ignore if ds is current path
                if (curPathReference != null && curPathReference.getKey().equals(ds.getKey())) {
                    continue;
                }

                // Get the data as a StrokePath and add it to the drawing list
                StrokePath sp = ds.getValue(StrokePath.class);
                paths.add(sp);
            }

            // If drawing, add current path to list of paths to draw
            if (strokePath != null) {
                paths.add(strokePath);
            }

            // Invalidate canvas to redraw
            invalidate();
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {}
    };

    // Listener for changes in ink level
    ValueEventListener inkListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            Float newInk = dataSnapshot.getValue(Float.class);
            if (newInk != null) {
                whiteboard.setInkLevel(newInk);
                updateInk();
            }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {}
    };

    /**
     * Construct a PaintView with the given context.
     * @param context the context the new PaintView is in.
     */
    public PaintView(Context context) {
        this(context, null);
    }

    /**
     * Construct a PaintView with the given context and attributes.
     * @param context the context the new PaintView is in.
     * @param attrs the View's attributes.
     */
    public PaintView(Context context, AttributeSet attrs) {
        super(context, attrs);

        // Setup paint
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

    /**
     * Creates the canvas and initializes the paint to black.
     * @param width the width of the canvas in pixels.
     * @param height the height of the canvas in pixels.
     * @param whiteboard a reference to the Whiteboard to draw on (needed for database up/download).
     * @param userId the ID of the current user.
     */
    public void init(int width, int height, Whiteboard whiteboard, String userId) {
        // Find views
        imageInk = ((View)getParent()).findViewById(R.id.img_ink_meter);

        // Create canvas from a bitmap
        bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        canvas = new Canvas(bitmap);

        // Initialize brush
        currentColor = DEFAULT_COLOR;
        strokeWidth = DEFAULT_SIZE;

        // Whiteboard and database setup
        this.whiteboard = whiteboard;
        this.userId = userId;
        dbReference = FirebaseDatabase.getInstance().getReference();

        // Add child for this Whiteboard if none exists
        dbReference
                .child("stroke-data")
                .addListenerForSingleValueEvent(dataSetupListener);

        // Listen to changes in the stroke data
        dbReference
                .child("stroke-data")
                .child(whiteboard.getName())
                .addValueEventListener(pathListener);

        // Listen to changes in ink level
        dbReference
                .child("users")
                .child(userId)
                .child(whiteboard.getName())
                .addValueEventListener(inkListener);
    }

    /**
     * Fits the canvas's specific bitmap size onto the space available on the phone
     */
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

    /**
     * Called whenever the canvas View is invalidated. Draws the canvas with all the existing paths.
     * @param canvas what Canvas view is being re-drawn.
     */
    @Override
    protected void onDraw(Canvas canvas) {
        // Re-scale the canvas, if it hasn't been done yet
        if (!isScaled) {
            isScaled = true;
            rescaleCanvas();
        }

        canvas.save();

        for (StrokePath sp : paths) {
            paint.setColor(sp.getColor());
            paint.setStrokeWidth(sp.getStrokeWidth());
            paint.setMaskFilter(null);

            canvas.drawPath(sp.getPath(), paint);
        }

        canvas.drawBitmap(bitmap, 0, 0, bitmapPaint);
        canvas.restore();
    }

    void updateInk() {
        // Set the image based on the ink level
        imageInk.setImageResource(whiteboard.getInkDrawable());

        // Stop drawing if depleted
        if (whiteboard.getInkLevel() <= 0f) {
            canDraw = false;
            whiteboard.setInkLevel(0f);
        }
    }

    /**
     * Called when a finger first touches the screen. Begins a new drawing path.
     * @param x the x-coordinate of the touch.
     * @param y the y-coordinate of the touch.
     */
    private void touchStart(float x, float y) {
        // Create the StrokePath and add it to the list to be drawn
        path = new Path();
        strokePath = new StrokePath(currentColor, strokeWidth);
        paths.add(strokePath);

        // Initialize path location
        path.reset();
        path.moveTo(x, y);
        mX = x;
        mY = y;
        strokePath.addPathPoint(x, y);

        // Start path in database
        curPathReference = addPathToDB(strokePath);
    }

    /**
     * Called when a finger moves while touching the screen. Adds another step to the stroke.
     * @param x the x-coordinate of the touch.
     * @param y the y-coordinate of the touch.
     */
    private void touchMove(float x, float y) {
        float dx = Math.abs(x - mX);
        float dy = Math.abs(y - mY);

        // Deplete ink
        float spentInk = (float)Math.sqrt(dx * dx + dy * dy) * strokeWidth;
        float newInkLevel = whiteboard.getInkLevel() - spentInk;
        whiteboard.setInkLevel(newInkLevel);
        dbReference
                .child("users")
                .child(userId)
                .child(whiteboard.getName())
                .setValue(whiteboard.getInkLevel());

        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
            strokePath.addPathPoint((x + mX) / 2, (y + mY) / 2);
            mX = x;
            mY = y;
        }

        // Update StrokePath data
        curPathReference.setValue(strokePath);

        updateInk();
    }

    /**
     * Called when a finger lifts up off the screen. Ends the drawing path with a final line
     * to the last known finger location. Re-enables drawing if possible.
     */
    private void touchUp() {
        // Update StrokePath data
        if (curPathReference != null) {
            curPathReference.setValue(strokePath);
        }
        strokePath = null;
        curPathReference = null;

        // Re-enable drawing if possible
        if (whiteboard.getInkLevel() > 0f) {
            canDraw = true;
        }
    }

    /**
     * Called with any touch input in this View.
     * @param event information about the touch event.
     * @return true if handled, false otherwise.
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_UP:
                touchUp();
                invalidate();
                break;
            case MotionEvent.ACTION_DOWN:
                if (canDraw) {
                    touchStart(x, y);
                    invalidate();
                } else {
                    // Touch event failed (out of ink)
                    strokePath = null;
                    curPathReference = null;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (canDraw) {
                    touchMove(x, y);
                    invalidate();
                } else {
                    // Touch event failed (out of ink)
                    strokePath = null;
                    curPathReference = null;
                }
                break;
        }

        // Event handled
        return true;
    }

    /**
     * Creates a new DatabaseReference where the path will be stored and stores its data there.
     * @param sp the StrokePath to add to the database.
     * @return a reference to the StrokePath.
     */
    private DatabaseReference addPathToDB(StrokePath sp) {
        // Get a new identifier for the current path
        DatabaseReference pathReference =  dbReference
                .child("stroke-data")
                .child(whiteboard.getName())
                .push();

        // Set the path data in the database
        pathReference.setValue(sp);
        return pathReference;
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
}
