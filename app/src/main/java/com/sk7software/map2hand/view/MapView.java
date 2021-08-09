package com.sk7software.map2hand.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;

import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.sk7software.map2hand.ApplicationContextProvider;
import com.sk7software.map2hand.R;

/**
 * TODO: document your custom view class.
 */
public class MapView extends SubsamplingScaleImageView {

    private PointF geoLocation = new PointF();

    public MapView(Context context) {
        super(context);
    }

    public MapView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setGeoLocation(PointF p) {
        geoLocation.x = p.x;
        geoLocation.y = p.y;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        PointF pt = sourceToViewCoord(geoLocation);

        if (pt != null) {
            Paint p = new Paint();
            p.setStrokeWidth(2.0f);
            int radius = 30;
            p.setColor(Color.BLUE);
            canvas.drawCircle(pt.x, pt.y, radius, p);
            radius -= 10;
            p.setColor(Color.YELLOW);
            canvas.drawCircle(pt.x, pt.y, radius, p);
            radius -= 10;
            p.setColor(Color.RED);
            canvas.drawCircle(pt.x, pt.y, radius, p);
        }
    }
}