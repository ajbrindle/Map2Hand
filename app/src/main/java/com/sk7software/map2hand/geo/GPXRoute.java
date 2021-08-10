package com.sk7software.map2hand.geo;

import android.graphics.PointF;

import com.sk7software.map2hand.MapFile;

import java.util.ArrayList;
import java.util.List;

public class GPXRoute {
    private String name;
    private List<GPXLocation> points;
    private List<PointF> mapPoints;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<GPXLocation> getPoints() {
        return points;
    }

    public void setPoints(List<GPXLocation> points) {
        this.points = points;
    }

    public List<PointF> getMapPoints() {
        return mapPoints;
    }

    public void setMapPoints(List<PointF> mapPoints) {
        this.mapPoints = mapPoints;
    }

    public void calcXY(MapFile map, int zone) {
        mapPoints = new ArrayList<>();

        for (GPXLocation ll : getPoints()) {
            GeoLocation geoLoc = new GeoLocation();
            geoLoc.setLatitude(ll.getLat());
            geoLoc.setLongitude(ll.getLon());
            geoLoc = GeoConvert.ConvertLLToGrid(map.getProjection(), geoLoc, 0);

            // Calculate map x, y
            PointF mapPoint = new PointF();
            mapPoint.x = (float) ((geoLoc.getEasting() - map.getTopLeftE()) / map.getResolution());
            mapPoint.y = (float) ((map.getTopLeftN() - geoLoc.getNorthing()) / map.getResolution());
            mapPoints.add(mapPoint);
        }
    }
}
