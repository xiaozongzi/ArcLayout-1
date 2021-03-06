package com.github.florent37.arclayout;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Outline;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.widget.FrameLayout;

public class ArcLayout extends FrameLayout {

    private ArcLayoutSettings settings;

    private int height = 0;

    private int width = 0;

    private Path clipPath;
    private Rect outlineRect;

    private Paint paint;

    private PorterDuffXfermode pdMode;

    public ArcLayout(Context context) {
        super(context);
        init(context, null);
    }

    public ArcLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public void init(Context context, AttributeSet attrs) {
        settings = new ArcLayoutSettings(context, attrs);
        settings.setElevation(ViewCompat.getElevation(this));

        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.WHITE);
        setLayerType(LAYER_TYPE_HARDWARE, paint);

        pdMode = new PorterDuffXfermode(PorterDuff.Mode.CLEAR);
    }

    private void calculateLayout() {
        if (settings == null) {
            return;
        }
        height = getMeasuredHeight();
        width = getMeasuredWidth();
        if (width > 0 && height > 0) {

            clipPath = createClipPath();
            ViewCompat.setElevation(this, settings.getElevation());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && settings.isCropInside()) {
                ViewCompat.setElevation(this, settings.getElevation());
                setOutlineProvider(new ViewOutlineProvider() {
                    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                    @Override
                    public void getOutline(View view, Outline outline) {
                        outline.setOval(outlineRect);
                    }
                });
            }
        }
    }

    private Path createClipPath() {
        final Path path = new Path();

        float verticalHeight = settings.getArcHeight();
        float horizontalPadding = settings.getArcPadding();
        final RectF arrowOval = new RectF();
        outlineRect = new Rect();

        final int left = (int) -horizontalPadding;
        final int right = (int) (width + horizontalPadding);

        if(settings.isCropInside()) {
            path.moveTo(0, height);
            path.lineTo(0, height - verticalHeight);

            final int top = (int)(height - verticalHeight * 2);
            final int bottom = height;

            arrowOval.set(left, top, right, bottom);
            outlineRect.set(left, top, right, bottom);

            path.arcTo(arrowOval, 180, -180, true);
            path.lineTo(width, height);
            path.lineTo(0, height);
        } else {
            path.moveTo(0, height);
            path.lineTo(0, height - verticalHeight);

            final int top = (int) (height - verticalHeight);
            final int bottom = (int) (height + verticalHeight);

            arrowOval.set(left, top, right, bottom);
            outlineRect.set(left, top, right, bottom);
            path.arcTo(arrowOval, -180, 180, true);
            path.lineTo(width, height);
            path.lineTo(0, height);
        }

        return path;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (changed) {
            calculateLayout();
        }
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);

        paint.setXfermode(pdMode);
        canvas.drawPath(clipPath, paint);
        paint.setXfermode(null);
    }
}
