package com.sk7software.map2hand.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintSet;

import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.sk7software.map2hand.ApplicationContextProvider;
import com.sk7software.map2hand.R;
import com.sk7software.map2hand.geo.GPXRoute;
import com.sk7software.map2hand.net.NetworkRequest;

import java.util.Calendar;

/**
 * TODO: document your custom view class.
 */
public class MapView extends SubsamplingScaleImageView {

    private PointF geoLocation = new PointF();
    private GPXRoute route;
    private long panDelay;

    private static final String TAG = MapView.class.getSimpleName();

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

    public void setRoute(GPXRoute route) {
        this.route = route;
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

        if (route != null) {
            Log.d(TAG, "Route: " + route.getName());

            Paint routePaint = new Paint();
            routePaint.setColor(Color.BLUE);
            routePaint.setAlpha(150);
            routePaint.setStyle(Paint.Style.STROKE);
            routePaint.setStrokeJoin(Paint.Join.MITER);
            routePaint.setStrokeWidth(10 * getScale());
            Path path = new Path();
            boolean first = true;
            for (PointF point : route.getMapPoints()) {
                PointF mapPoint = sourceToViewCoord(point);
                if (mapPoint != null) {
                    if (first) {
                        path.moveTo(mapPoint.x, mapPoint.y);
                        first = false;
                    } else {
                        path.lineTo(mapPoint.x, mapPoint.y);
                    }
                }
            }
            canvas.drawPath(path, routePaint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_MOVE) {
            panDelay = Calendar.getInstance().getTimeInMillis() + 5000;
        }
        return super.onTouchEvent(event);
    }

    public long getPanDelay() {
        return panDelay;
    }
}