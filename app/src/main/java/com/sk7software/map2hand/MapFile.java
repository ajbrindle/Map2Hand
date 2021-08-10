package com.sk7software.map2hand;

import android.graphics.BitmapFactory;
import android.util.Log;

import com.sk7software.map2hand.db.Database;
import com.sk7software.map2hand.geo.Projection;

public class MapFile {
	private String name;
	private int topLeftE;
	private int topLeftN;
	private int botRightE;
	private int botRightN;
	private double resolution;
	private Projection projection;

	//public static final String MAP_DIR = android.os.Environment.getExternalStorageDirectory().getPath() + "/Android/data/com.sk7software/";
	//public static final String MAP_DIR = "/mnt/extSdCard/Android/data/com.sk7software/";
	//public static final String MAP_DIR = "/Android/data/com.sk7software/";
	// public static final String MAP_DIR = "/storage/sdcard1/Android/data/com.sk7software/";
	//public static final String MAP_DIR = "/data/data/com.sk7software.map2hand/";
	public static final String MAP_DIR = "/sdcard/Android/data/com.sk7software.map2hand/";

	private static final String JPG_EXT = ".jpg";
	private static final String TAG = MapFile.class.getSimpleName();
	
	private static final int FIELD_NAME = 0;
	private static final int FIELD_PROJ_ID = 1;
	private static final int FIELD_E1 = 2;
	private static final int FIELD_N1 = 3;
	private static final int FIELD_X1 = 4;
	private static final int FIELD_Y1 = 5;
	private static final int FIELD_RES = 6;
	
	public MapFile() {	
	}

	public MapFile(String calibLine) {
		// Read in calibration info
		String[] fields = calibLine.split(",");
		name = fields[FIELD_NAME];
		projection = Database.getProjection(Integer.parseInt(fields[FIELD_PROJ_ID]));
		double E1 = Double.parseDouble(fields[FIELD_E1]);
		double N1 = Double.parseDouble(fields[FIELD_N1]);
		int x1 = Integer.parseInt(fields[FIELD_X1]);
		int y1 = Integer.parseInt(fields[FIELD_Y1]);
		
		// Locate the corresponding jpg file
		String filename = MAP_DIR + name + JPG_EXT;
		Log.d(TAG, filename);
		
		// Decode bounds to get width and height of image
		BitmapFactory.Options opts = new BitmapFactory.Options();
		opts.inJustDecodeBounds = true;
		
		BitmapFactory.decodeFile(filename, opts);
		int wid = opts.outWidth;
		int ht = opts.outHeight;
		Log.d(TAG, wid + "," + ht);
		resolution = Double.parseDouble(fields[FIELD_RES]);
		
		// Insert map into database
		Database db = Database.getInstance();
		db.insertMap(name, E1, N1, x1, y1, wid, ht, resolution);
		setCorners(E1, N1, x1, y1, wid, ht, resolution);
		Log.d(TAG, name + " added");		
	}
	
	public int getTopLeftE() {
		return topLeftE;
	}

	public void setTopLeftE(int topLeftE) {
		this.topLeftE = topLeftE;
	}

	public int getTopLeftN() {
		return topLeftN;
	}

	public void setTopLeftN(int topLeftN) {
		this.topLeftN = topLeftN;
	}

	public int getBotRightE() {
		return botRightE;
	}

	public void setBotRightE(int botRightE) {
		this.botRightE = botRightE;
	}

	public int getBotRightN() {
		return botRightN;
	}

	public void setBotRightN(int botRightN) {
		this.botRightN = botRightN;
	}

	public double getResolution() {
		return resolution;
	}

	public void setResolution(double resolution) {
		this.resolution = resolution;
	}

	public String getName() {
		return name;
	}
	
	public int getWidthPix() {
		return (int)(((double)botRightE-(double)topLeftE)/resolution);
	}

	public int getHeightPix() {
		return (int)(((double)topLeftN-(double)botRightN)/resolution);
	}
	
	public Projection getProjection() {
		return projection;
	}

	public void setProjection(Projection projection) {
		this.projection = projection;
	}

	private void setCorners(double E1, double N1, int x1, int y1, int wid, int ht, double resolution) {
		// Calculate corners
		topLeftE = (int)(E1 - (x1*resolution));
		topLeftN = (int)(N1 + (y1*resolution));
		botRightE = (int)(topLeftE + (wid*resolution));
		botRightN = (int)(topLeftN - (ht*resolution));
		Log.d(TAG, "MapFile: " + topLeftE + "," + topLeftN + "; " + botRightE + "," + botRightN);
	}
	
	public boolean equals(MapFile m) {
		if (m == null) return false;
		
		return (name.equals(m.getName()) && 
				topLeftE == m.getTopLeftE() && topLeftN == m.getTopLeftN() &&
				botRightE == m.getBotRightE() && botRightN == m.getBotRightN());
	}
	
	public int hashCode() {
		int code = name.hashCode();
		code += topLeftE + topLeftN + botRightE + botRightN;
		return code;
	}
	
	public String getFullPath() {
		return MAP_DIR + name + JPG_EXT;		
	}
	
	public void draw() {
		
	}
	
}
