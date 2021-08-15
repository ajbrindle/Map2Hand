package com.sk7software.map2hand;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.graphics.PointF;
import android.util.Log;

import com.sk7software.map2hand.db.Database;
import com.sk7software.map2hand.geo.GeoLocation;

public class MapController {

	private static final String CALIB_FILE = "calibration.csv";
	private static final String TAG = MapController.class.getSimpleName();
	
	private static List<MapFile> maps = new ArrayList<MapFile>();

	public static void loadMaps() {
		Database db = Database.getInstance();
		// Loop round every row in the csv file
		BufferedReader calibFile = null;
		try {
			calibFile = new BufferedReader(new FileReader(new File(MapFile.MAP_DIR + CALIB_FILE)));
			String line;
			db.clearMapTables(db.getReadableDatabase());
			while ((line = calibFile.readLine()) != null) {
				if (line.charAt(0) != '#') {
					Log.d(TAG, line);
					MapFile map = new MapFile(line);
					maps.add(map);
				}
			}
		} catch (IOException e) {
			Log.d(TAG, "Error processing calibration file: " + e.getMessage());
		} finally {
			if (calibFile != null) {
				try {
					calibFile.close();
				} catch (IOException e) {
					Log.d(TAG, "Error closing calibration file: " + e.getMessage());					
				}
			}
		}
	}
	
	public static MapFile getBestMap(GeoLocation geoLoc, MapFile currentMap, boolean autoZoom) {
		double minRes = Double.MAX_VALUE;
		MapFile bestMap = null;

		// If not auto-zooming, return current map if point is on it
		if (!autoZoom && isPointOnMap(geoLoc, currentMap)) {
			return currentMap;
		}

		// Find lowest resolution map that contains the point
		for (MapFile m : maps) {
			if (isPointOnMap(geoLoc, m) && m.getResolution() < minRes) {
				bestMap = m;
				minRes = m.getResolution();
			}
		}

		// Don't switch maps if the point is on both and they are the same resolution
		if (bestMap != null &&
				!bestMap.equals(currentMap) &&
				isPointOnMap(geoLoc, currentMap) &&
				!isDifferentResolution(currentMap.getResolution(), bestMap.getResolution())) {
			bestMap = currentMap;
		}
		return bestMap;		
	}

	public static MapFile hasMap(PointF point, MapFile currentMap) {
		for (MapFile m : maps) {
//			Log.d(TAG, m.getName() + ": " + m.getTopLeftE() + "," + m.getTopLeftN() +
//					"; " + m.getBotRightE() + "," + m.getBotRightN());

			if (!m.equals(currentMap) &&
					isPointOnMap(point, m) &&
					!isDifferentResolution(currentMap.getResolution(), m.getResolution())) {
				return m;
			}
		}
		return null;
	}

	public static MapFile getNearestMap(PointF point, MapFile currentMap, boolean zoomIn) {
		MapFile nearestMap = null;
		double minResDiff = Double.MAX_VALUE;

		for (MapFile m : maps) {
			if (!m.equals(currentMap) &&
					isPointOnMap(point, m) &&
					isDifferentResolution(currentMap.getResolution(), m.getResolution()) &&
					(zoomIn ? m.getResolution() < currentMap.getResolution() : m.getResolution() > currentMap.getResolution())) {
				double resDiff = Math.abs(m.getResolution() - currentMap.getResolution());
				if (resDiff < minResDiff) {
					minResDiff = resDiff;
					nearestMap = m;
				}
			}
		}
		return nearestMap;
	}

	private static boolean isPointOnMap(GeoLocation geoLoc, MapFile map) {
		return map.getTopLeftE() < geoLoc.getEasting() &&
				map.getTopLeftN() > geoLoc.getNorthing() &&
				map.getBotRightE() > geoLoc.getEasting() &&
				map.getBotRightN() < geoLoc.getNorthing();
	}

	public static boolean isPointOnMap(PointF point, MapFile map) {
//		Log.d(TAG, "Current: " + map.getTopLeftE() + "," + map.getTopLeftN() +
//				"; " + map.getBotRightE() + "," + map.getBotRightN());
		return map.getTopLeftE() < point.x &&
				map.getTopLeftN() > point.y &&
				map.getBotRightE() > point.x &&
				map.getBotRightN() < point.y;
	}

	public static boolean isDifferentResolution(double res1, double res2) {
		// Check resolution is at least 5% different
		double resDiff = Math.abs(res1 - res2);
		return (resDiff / res1) > 0.05;
	}
	public static MapFile getMapByName(String mapName) {
		if (mapName == null || mapName.length() == 0) return null;
		
		for (MapFile m : maps) {
			if (mapName.equals(m.getName())) {
				return m;
			}
		}
		return null;
	}
}
