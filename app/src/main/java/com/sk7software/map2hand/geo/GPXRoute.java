package com.sk7software.map2hand.geo;

import android.graphics.PointF;
import android.util.Log;

import com.google.gson.Gson;
import com.sk7software.map2hand.MapFile;
import com.sk7software.map2hand.db.GPXFile;
import com.sk7software.map2hand.db.GPXFiles;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

public class GPXRoute implements Serializable {
    private String name;
    private List<GPXLocation> points;
    private transient List<PointF> mapPoints;

    public static final String TAG = GPXRoute.class.getSimpleName();

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

    public void calcEN(MapFile map, int zone) {
        for (GPXLocation pt : getPoints()) {
            GeoLocation geoLoc = new GeoLocation();
            geoLoc.setLatitude(pt.getLat());
            geoLoc.setLongitude(pt.getLon());
            geoLoc = GeoConvert.ConvertLLToGrid(map.getProjection(), geoLoc, 0);
            pt.setEasting(geoLoc.getEasting());
            pt.setNorthing(geoLoc.getNorthing());
        }
    }

    public void calcXY(MapFile map, int zone) {
        mapPoints = new ArrayList<>();

        for (GPXLocation pt : getPoints()) {
            // Calculate map x, y
            PointF mapPoint = new PointF();
            mapPoint.x = (float) ((pt.getEasting() - map.getTopLeftE()) / map.getResolution());
            mapPoint.y = (float) ((map.getTopLeftN() - pt.getNorthing()) / map.getResolution());
            mapPoints.add(mapPoint);
        }
    }

    public static GPXRoute readFromFile(File f) {
        Gson gson = new Gson();
        try (BufferedReader br = new BufferedReader(new FileReader(new File(f.getAbsolutePath())))) {
            return gson.fromJson(br, GPXRoute.class);
        } catch (IOException e) {
            Log.d(TAG, "Error reading route file: " + f.getAbsolutePath() + " - " + e.getMessage());
            return null;
        }
    }

    public static void writeToFile(GPXRoute route, String fileName) {
        Gson gson = new Gson();
        File mhrFile = new File(MapFile.MAP_DIR + fileName.replace(".gpx", GPXFiles.ROUTE_EXT));
        try (Writer output = new BufferedWriter(new FileWriter(mhrFile))) {
            output.write(gson.toJson(route));
            Log.d(TAG, "Wrote local file: " + mhrFile.getName());
        } catch (IOException e) {
            Log.d(TAG, "Error storing route file: " + e.getMessage());
        }
    }
}
