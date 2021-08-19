package com.sk7software.map2hand.db;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.sk7software.map2hand.ApplicationContextProvider;
import com.sk7software.map2hand.geo.Ellipsoid;
import com.sk7software.map2hand.geo.Projection;

public class Database extends SQLiteOpenHelper {
	
	public static final int DATABASE_VERSION = 1;
	public static final String DATABASE_NAME = "com.sk7software.map2hand.db";
	private static final String TAG = "Database";
	
	private static Database dbInstance;
	private int maxId = 1;
	
	public static Database getInstance() {
		if (dbInstance == null) {
			dbInstance = new Database(ApplicationContextProvider.getContext());
		}
		
		return dbInstance;
	}
	
	private Database(Context context)
	{
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		Log.d(TAG,"DB constructor");
	}

	
	@Override
	public void onCreate(SQLiteDatabase db) {
		Log.d(TAG, "DB onCreate()");
		initialise(db);
	}
	
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldv, int newv) {
		Log.d(TAG, "DB onUpgrade()");		
	}
	
	
	private void initialise(SQLiteDatabase db) {
		Log.d(TAG, "DB initialise()");
		String createEllipsoid = 
				"CREATE TABLE ELLIPSOID (" +
				"_ID INTEGER PRIMARY KEY," +
			    "NAME TEXT," +
				"RADIUS_A REAL," +
			    "RADIUS_B REAL);";
		db.execSQL(createEllipsoid);
		
		int id = 1;
		String insertStart = "INSERT INTO ELLIPSOID VALUES(";
		String insertEnd = ");";
		
		List<String> ellip = new ArrayList<String>();
		ellip.add(id++ + ",'Clarke 1866', 6378206.4, 6356583.8");
		ellip.add(id++ + ",'Clarke 1880', 6378249.145, 6356514.86955");	// 2
		ellip.add(id++ + ",'Bessel 1841', 6377397.155, 6356078.96284");	// 3
		ellip.add(id++ + ",'Airy 1830', 6377563.396, 6356256.91");	// 4
		ellip.add(id++ + ",'New International 1967', 6378157.5, 6356772.2");		// 5
		ellip.add(id++ + ",'International 1924', 6378388.0, 6356911.94613");	// 6
		ellip.add(id++ + ",'WGS 1972', 6378135.0, 6356750.519915");	// 7
		ellip.add(id++ + ",'Everest 1830', 6377276.3452, 6356075.4133");	// 8
		ellip.add(id++ + ",'WGS 1966', 6378145.0, 6356759.769356");	// 9
		ellip.add(id++ + ",'GRS 1980', 6378137.0, 6356752.31414");	// 10
		ellip.add(id++ + ",'Everest 1948', 6377304.063, 6356103.039");	// 11
		ellip.add(id++ + ",'Modified Airy', 6377340.189, 6356034.448");	// 12
		ellip.add(id++ + ",'WGS 1984', 6378137.0, 6356752.314245");	// 13
		ellip.add(id++ + ",'Modified Fisher 1960', 6378155.0, 6356773.3205");	// 14
		ellip.add(id++ + ",'Australian Nat 1965', 6378160.0, 6356774.719");	// 15
		ellip.add(id++ + ",'Krassovsky 1940', 6378245.0, 6356863.0188");	// 16
		ellip.add(id++ + ",'Hough 1960', 6378270.0, 6356794.343479");	// 17
		ellip.add(id++ + ",'Fisher 1960', 6378166.0, 6356784.283666");	// 18
		ellip.add(id++ + ",'Fisher 1968', 6378150.0, 6356768.337303");	// 19
		ellip.add(id++ + ",'Normal Sphere', 6370997.0, 6370997.0");		// 20
		ellip.add(id++ + ",'Indonesian 1974', 6378160.0, 6356774.504086");	// 21
		ellip.add(id++ + ",'Everest (Pakistan)', 6377309.613, 6356108.570542");	// 22
		ellip.add(id++ + ",'Bessel 1841 (Japan)', 6377397.155, 6356078.963");	// 23
		ellip.add(id++ + ",'Bessel 1841 (Namibia)', 6377483.865, 6356165.382966");	// 24
		ellip.add(id++ + ",'Everest 1956', 6377301.243, 6356100.228368");	// 25
		ellip.add(id++ + ",'Everest 1969', 6377295.664, 6356094.667915");	// 26
		ellip.add(id++ + ",'Everest', 6377298.556, 6356097.550301");	// 27
		ellip.add(id++ + ",'Helmert 1906', 6378200.0, 6356818.169628");	// 28
		ellip.add(id++ + ",'SGS 85', 6378136.0, 6356751.301569");	// 29
		ellip.add(id++ + ",'WGS 60', 6378165.0, 6356783.286959");	// 30
		ellip.add(id++ + ",'South American 1969',6378160.0, 6356774.719");	// 31
		ellip.add(id++ + ",'ATS77',	6378135.0, 6356750.304922");	// 32

		for (String s : ellip) {
			String insertEllipsoid = insertStart + s + insertEnd;
			Log.d(TAG, insertEllipsoid);
			db.execSQL(insertEllipsoid);
		}

		String createProjection = 
				"CREATE TABLE PROJECTION (" +
				"_ID INTEGER PRIMARY KEY," +
			    "NAME TEXT," +
				"FALSE_E REAL," +
			    "FALSE_N REAL, " +
				"LAT0 REAL, " +
			    "LON0 REAL, " +
				"K0 REAL, " +
			    "ELLIPSOID_ID INTEGER, " +
				"PROJ_TYPE INTEGER);";
		db.execSQL(createProjection);
		
		id = 1;
		insertStart = "INSERT INTO PROJECTION VALUES(";
		insertEnd = ");";
		
		List<String> proj = new ArrayList<String>();
		proj.add(id++ + ", 'UK National Grid', 400000.0, -100000.0, " + (49.0*Math.PI/180.0) + ", " + (-2.0*Math.PI/180.0) + ", 0.9996013, 10, " + Projection.SYS_TYPE_TM);
		proj.add(id++ + ", 'UTM (WGS 1984)', 500000.0, 0.0, 0.0, 0.0, 0.9996, 13, " + Projection.SYS_TYPE_UTM);
		proj.add(id++ + ", 'UTM (Intl 1924)', 500000.0, 0.0, 0.0, 0.0, 0.9996, 6, " + Projection.SYS_TYPE_UTM);
		proj.add(id++ + ", 'UTM (Clarke 1866)', 500000.0, 0.0, 0.0, 0.0, 0.9996, 1, " + Projection.SYS_TYPE_UTM);
		proj.add(id++ + ", 'UTM (Clarke 1866)', 500000.0, 0.0, 0.0, 0.0, 0.9996, 2, " + Projection.SYS_TYPE_UTM);
		proj.add(id++ + ", 'UTM (Bessel 1841)', 500000.0, 0.0, 0.0, 0.0, 0.9996, 3, " + Projection.SYS_TYPE_UTM);
		proj.add(id++ + ", 'UTM (Airy 1830)', 500000.0, 0.0, 0.0, 0.0, 0.9996, 4, " + Projection.SYS_TYPE_UTM);
		proj.add(id++ + ", 'UTM (New Intl 1967)', 500000.0, 0.0, 0.0, 0.0, 0.9996, 5, " + Projection.SYS_TYPE_UTM);
		proj.add(id++ + ", 'UTM (WGS 1972)', 500000.0, 0.0, 0.0, 0.0, 0.9996, 7, " + Projection.SYS_TYPE_UTM);
		proj.add(id++ + ", 'UTM (Everest 1830)', 500000.0, 0.0, 0.0, 0.0, 0.9996, 8, " + Projection.SYS_TYPE_UTM);
		proj.add(id++ + ", 'UTM (WGS 1966)', 500000.0, 0.0, 0.0, 0.0, 0.9996, 9, " + Projection.SYS_TYPE_UTM);
		proj.add(id++ + ", 'UTM (GRS 1980)', 500000.0, 0.0, 0.0, 0.0, 0.9996, 10, " + Projection.SYS_TYPE_UTM);
		proj.add(id++ + ", 'UTM (Everest 1948)', 500000.0, 0.0, 0.0, 0.0, 0.9996, 11, " + Projection.SYS_TYPE_UTM);
		proj.add(id++ + ", 'UTM (Modified Airy)', 500000.0, 0.0, 0.0, 0.0, 0.9996, 12, " + Projection.SYS_TYPE_UTM);
		proj.add(id++ + ", 'UTM (Modified Fisher 1960)', 500000.0, 0.0, 0.0, 0.0, 0.9996, 14, " + Projection.SYS_TYPE_UTM);
		proj.add(id++ + ", 'UTM (Australian Nat 1965)', 500000.0, 0.0, 0.0, 0.0, 0.9996, 15, " + Projection.SYS_TYPE_UTM);
		proj.add(id++ + ", 'UTM (Krassovsky 1940)', 500000.0, 0.0, 0.0, 0.0, 0.9996, 16, " + Projection.SYS_TYPE_UTM);
		proj.add(id++ + ", 'UTM (Hough 1960)', 500000.0, 0.0, 0.0, 0.0, 0.9996, 17, " + Projection.SYS_TYPE_UTM);
		proj.add(id++ + ", 'UTM (Fisher 1960)', 500000.0, 0.0, 0.0, 0.0, 0.9996, 18, " + Projection.SYS_TYPE_UTM);
		proj.add(id++ + ", 'UTM (Fisher 1968)', 500000.0, 0.0, 0.0, 0.0, 0.9996, 19, " + Projection.SYS_TYPE_UTM);
		proj.add(id++ + ", 'UTM (Indonesian 1974)', 500000.0, 0.0, 0.0, 0.0, 0.9996, 21, " + Projection.SYS_TYPE_UTM);
		proj.add(id++ + ", 'UTM (Everest Pakistan)', 500000.0, 0.0, 0.0, 0.0, 0.9996, 22, " + Projection.SYS_TYPE_UTM);
		proj.add(id++ + ", 'UTM (Bessel 1841 Japan)', 500000.0, 0.0, 0.0, 0.0, 0.9996, 23, " + Projection.SYS_TYPE_UTM);
		proj.add(id++ + ", 'UTM (Bessel 1841 Namibia)', 500000.0, 0.0, 0.0, 0.0, 0.9996, 24, " + Projection.SYS_TYPE_UTM);
		proj.add(id++ + ", 'UTM (Everest 1956)', 500000.0, 0.0, 0.0, 0.0, 0.9996, 25, " + Projection.SYS_TYPE_UTM);
		proj.add(id++ + ", 'UTM (Everest 1969)', 500000.0, 0.0, 0.0, 0.0, 0.9996, 26, " + Projection.SYS_TYPE_UTM);
		proj.add(id++ + ", 'UTM (Everest)', 500000.0, 0.0, 0.0, 0.0, 0.9996, 27, " + Projection.SYS_TYPE_UTM);
		proj.add(id++ + ", 'UTM (Helmert 1906)', 500000.0, 0.0, 0.0, 0.0, 0.9996, 28, " + Projection.SYS_TYPE_UTM);
		proj.add(id++ + ", 'UTM (SGS 85)', 500000.0, 0.0, 0.0, 0.0, 0.9996, 29, " + Projection.SYS_TYPE_UTM);
		proj.add(id++ + ", 'UTM (WGS 60)', 500000.0, 0.0, 0.0, 0.0, 0.9996, 30, " + Projection.SYS_TYPE_UTM);
		proj.add(id++ + ", 'UTM (South American 1969)', 500000.0, 0.0, 0.0, 0.0, 0.9996, 31, " + Projection.SYS_TYPE_UTM);
		proj.add(id++ + ", 'UTM (ATS77)', 500000.0, 0.0, 0.0, 0.0, 0.9996, 32, " + Projection.SYS_TYPE_UTM);

		for (String s : proj) {
			String insertProjection = insertStart + s + insertEnd;
			Log.d(TAG, insertProjection);
			db.execSQL(insertProjection);
		}

		String createMap = 
				"CREATE TABLE MAP (" +
				"_ID INTEGER PRIMARY KEY," +
			    "NAME TEXT," +
				"E1 INTEGER," +
			    "N1 INTEGER, " +
				"X1 INTEGER," +
			    "Y1 INTEGER, " +
				"WIDTH INTEGER, " +
			    "HEIGHT INTEGER, " +
				"RESOLUTION REAL);";
		db.execSQL(createMap);

		String createMapCache = 
				"CREATE TABLE MAP_CACHE (" +
				"_ID INTEGER PRIMARY KEY," +
			    "NAME TEXT," +
				"E0 INTEGER," +
			    "N0 INTEGER, " +
				"RESOLUTION REAL, " +
			    "E1 INTEGER, " +
				"N1 INTEGER, " +
			    "WIDTH INTEGER, " +
				"HEIGHT INTEGER" +
			    ");";
		db.execSQL(createMapCache);
		
		//String insertMap = "INSERT INTO MAP VALUES(1,'GB_Overview',400083,500249,2386,3591,4087,6577,166.666667);";
		//Log.d(TAG, insertMap);
		//db.execSQL(insertMap);
//		insertMap(db, 1, 400083, 500249, 2386, 3591, 4087, 6577, 166.666667);
//		insertMap = "INSERT INTO MAP VALUES(2,'OS_GManc', 361000, 413000, 240, 434, 8226, 7506, 5);";
//		insertMapCache(db, 2, 361000, 413000, 240, 434, 8226, 7506, 5);
	}
	
	public void clearMapTables(SQLiteDatabase db) {
		String sql = "DELETE FROM MAP;";
		db.execSQL(sql);
		sql = "DELETE FROM MAP_CACHE;";
		db.execSQL(sql);
	}
	
	public void insertMap(String name, double e, double n, int x, int y, int wid, int ht, double res) {
		SQLiteDatabase db = this.getReadableDatabase();
		String insertMap;
		int id;
		
		Log.d(TAG, "findMap: " + name);
		id = findMap(name);
		Log.d(TAG, "id: " + id);

		if (id == -1) {
			insertMap = "INSERT INTO MAP VALUES(" + maxId + ",'" + 
				    name + "'," + e + "," + n + "," +
				    x + "," + y + "," +
				    wid + "," + ht + "," + res + ")";
			Log.d(TAG, insertMap);
			db.execSQL(insertMap);
			maxId++;
		}
	}
	
	public static Ellipsoid getEllipsoid(int id) {
		Cursor cursor = null;
		SQLiteDatabase db = Database.getSQLiteDatabase();
		
		try {
			cursor = db.query("ELLIPSOID", new String[] {"NAME", "RADIUS_A", "RADIUS_B"}, "_id=?", 
							  new String[] {String.valueOf(id)}, null, null, null, null);
			if (cursor != null) {
				cursor.moveToFirst();
				return new Ellipsoid(id, cursor.getString(0), cursor.getDouble(1), cursor.getDouble(2));
			} 
		} finally {
			cursor.close();
		}
		
		return null;
	}
	
	public int findMap(String name) {
		Cursor cursor = null;
		SQLiteDatabase db = this.getReadableDatabase();
		
		try {
			Log.d(TAG, "Set up cursor");
			cursor = db.query("MAP", new String[] {"_ID"}, "name=?", 
							  new String[] {name}, null, null, null, null);
			if (cursor != null && cursor.getCount() > 0) {
				Log.d(TAG, "Found " + cursor.getCount());
				cursor.moveToFirst();
				Log.d(TAG, "Id: " + cursor.getInt(0));
				return cursor.getInt(0);
			} 
		} finally {
			cursor.close();
		}
		
		return -1;		
	}
	
	public static String[] getMapsList() {
		Cursor cursor = null;
		SQLiteDatabase db = Database.getSQLiteDatabase();
		
		try {
			cursor = db.query("MAP", new String[] {"NAME"}, null,null, null, null, null, null);
			if (cursor != null) {
				String[] maps = new String[cursor.getCount()];
				int i=0;
				cursor.moveToFirst();
				do {
					maps[i++] = cursor.getString(0);					
				} while (cursor.moveToNext());
				
				return maps;
			} 
		} finally {
			cursor.close();
		}
		
		return null;
		
	}
	
	public static Projection getProjection(int id) {
		Cursor cursor = null;
		SQLiteDatabase db = Database.getSQLiteDatabase();
		
		try {
			cursor = db.query("PROJECTION", new String[] {"NAME", "FALSE_E", "FALSE_N", "LAT0", "LON0", "K0", "ELLIPSOID_ID", "PROJ_TYPE"}, "_id=?", 
							  new String[] {String.valueOf(id)}, null, null, null, null);
			if (cursor != null) {
				cursor.moveToFirst();
				return new Projection(id, cursor.getString(0), cursor.getDouble(1), cursor.getDouble(2),
									  cursor.getDouble(3), cursor.getDouble(4), cursor.getDouble(5),
									  getEllipsoid(cursor.getInt(6)), cursor.getInt(7));
			} 
		} finally {
			cursor.close();
		}
		
		return null;
		
	}
	
	public static SQLiteDatabase getSQLiteDatabase() {
		return getInstance().getReadableDatabase();
	}
	
	public static Cursor getMapList() {
		return getSQLiteDatabase().query("MAP", new String[] {"NAME"}, null,null, null, null, null, null);
	}
	
//	public static MapFile getMapInfo(String mapName) {
//		SQLiteDatabase db = getSQLiteDatabase();
//		MapFile mf;
//		Cursor cursor = null;
//		
//		try {
//
//			cursor = db.query("MAP_CACHE", new String[] {"E0", "N0", "E1", "N1", "RESOLUTION"}, "NAME=?", 
//							  new String[] {mapName}, null, null, null, null);
//			if (cursor != null) {
//				cursor.moveToFirst();
//				return new MapFile(cursor.getInt(0), cursor.getInt(1), cursor.getInt(2),
//								   cursor.getInt(3), cursor.getDouble(4));
//			} 
//		} finally {
//			cursor.close();
//		}
//		
//		return null;
//	}
	
}
