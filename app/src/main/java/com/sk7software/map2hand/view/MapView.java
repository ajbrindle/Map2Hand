package com.sk7software.map2hand.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;

import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.sk7software.map2hand.R;
import com.sk7software.map2hand.db.PreferencesUtil;
import com.sk7software.map2hand.geo.GPXRoute;
import com.sk7software.map2hand.geo.GeoLocation;

import java.util.Calendar;

/**
 * TODO: document your custom view class.
 */
public class MapView extends SubsamplingScaleImageView {

    private GeoLocation mapGPSLocation = new GeoLocation();
    private GPXRoute route;
    private long panDelay;

    private static final String TAG = MapView.class.getSimpleName();

    public interface OnReadyCallback {
        public void ready();
    }

    private OnReadyCallback onReadyFunction;

    public MapView(Context context) {
        super(context);
    }
    public MapView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setMapGPSLocation(GeoLocation loc) {
        mapGPSLocation = loc;
    }

    public void setRoute(GPXRoute route) {
        this.route = route;
    }

    public void setOnReadyFunction(OnReadyCallback ready) {
        this.onReadyFunction = ready;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (route != null) {
            int alpha =
                    PreferencesUtil.getInstance().getIntPreference(PreferencesUtil.PREFERNECE_ROUTE_TRANSPARENCY) * 255 / 100;
            int routeWidth =
                    PreferencesUtil.getInstance().getIntPreference(PreferencesUtil.PREFERNECE_ROUTE_WIDTH);

            Paint routePaint = new Paint();
            routePaint.setColor(Color.BLUE);
            routePaint.setAlpha(alpha);
            routePaint.setStyle(Paint.Style.STROKE);
            routePaint.setStrokeJoin(Paint.Join.MITER);
            routePaint.setStrokeWidth(routeWidth * getScale());
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

        PointF pt = sourceToViewCoord(new PointF((float)mapGPSLocation.getEasting(), (float)mapGPSLocation.getNorthing()));

        if (pt != null && pt.x >= 0 && pt.y >= 0) {
            boolean showBearing = PreferencesUtil.getInstance().getBooleanPreference(PreferencesUtil.PREFERNECE_BEARING);
            int radius = PreferencesUtil.getInstance().getIntPreference(PreferencesUtil.PREFERNECE_MARKER_SIZE);
            double bearing = mapGPSLocation.getBearing();

            if (showBearing && bearing >= 0) {
                Paint bearPaint = new Paint();
                bearPaint.setColor(Color.BLUE);
                bearPaint.setStyle(Paint.Style.FILL_AND_STROKE);
                canvas.drawPath(calcBearingPath(pt, bearing, radius), bearPaint);
            }

            Paint p = new Paint();
            p.setStrokeWidth(2.0f);
            p.setColor(Color.BLUE);
            canvas.drawCircle(pt.x, pt.y, radius, p);
            radius = (2*radius)/3;
            p.setColor(Color.YELLOW);
            canvas.drawCircle(pt.x, pt.y, radius, p);
            radius = radius/2;
            p.setColor(Color.RED);
            canvas.drawCircle(pt.x, pt.y, radius, p);
        }
    }

    private Path calcBearingPath(PointF pt, double bearing, int radius) {
        float triangleHt = (float)radius * (float)1.8;
        PointF triangleTip = new PointF();
        Path path = new Path();
        bearing = bearing / 180.0 * Math.PI;

        triangleTip.x = pt.x + (float)(triangleHt * Math.sin(bearing));
        triangleTip.y = pt.y - (float)(triangleHt * Math.cos(bearing));
        path.moveTo(triangleTip.x, triangleTip.y);

        PointF triangleVertex = new PointF();
        triangleVertex.x = pt.x - (float)((float)radius * Math.cos(bearing));
        triangleVertex.y = pt.y - (float)((float)radius * Math.sin(bearing));
        path.lineTo(triangleVertex.x, triangleVertex.y);

        triangleVertex.x = pt.x + (float)((float)radius * Math.cos(bearing));
        triangleVertex.y = pt.y + (float)((float)radius * Math.sin(bearing));
        path.lineTo(triangleVertex.x, triangleVertex.y);
        path.lineTo(triangleTip.x, triangleTip.y);
        return path;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_MOVE) {
            panDelay = Calendar.getInstance().getTimeInMillis() + 5000;
        }
        return super.onTouchEvent(event);
    }

    @Override
    public void onReady() {
        Log.d(TAG, "Map is ready");
        super.onReady();
        onReadyFunction.ready();
    }

    public long getPanDelay() {
        return panDelay;
    }
}