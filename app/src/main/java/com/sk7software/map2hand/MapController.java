package com.sk7software.map2hand;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.Environment;
import android.util.Log;

public class MapController {

	private static final String CALIB_FILE = "calibration.csv";
	private static final String TAG = MapController.class.getSimpleName();
	
	private static List<MapFile> maps = new ArrayList<MapFile>();
	//private static MapFile currentMap = null;
	
	public static void loadMaps() {
		Database db = Database.getInstance();
		// Loop round every row in the csv file
		BufferedReader calibFile = null;
		try {
			calibFile = new BufferedReader(new FileReader(new File(MapFile.MAP_DIR + CALIB_FILE)));
			String line;
			db.clearMapTables(db.getReadableDatabase());
			Log.d(TAG, "Tables cleared");
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
	
	public static MapFile getBestMap(GeoLocation geoLoc) {
		double minRes = Double.MAX_VALUE;
		MapFile bestMap = null;
		
		for (MapFile m : maps) {
			if (m.getTopLeftE() < geoLoc.getEasting() && m.getTopLeftN() > geoLoc.getNorthing() &&
				m.getBotRightE() > geoLoc.getEasting() && m.getBotRightN() < geoLoc.getNorthing()) {
				if (m.getResolution() < minRes) {
					bestMap = m;
					minRes = m.getResolution();
				}
			}
		}
		
		return bestMap;		
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
	
//	public static void setCurrentMap(MapFile m) {
//		currentMap = m;
//	}
//	
//	public static MapFile getCurrentMap() {
//		return currentMap;
//	}
}
