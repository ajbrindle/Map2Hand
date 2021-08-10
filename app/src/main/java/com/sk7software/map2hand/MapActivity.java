package com.sk7software.map2hand;

import java.util.Calendar;
import java.util.Date;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;

import androidx.core.app.ActivityCompat;

import com.davemorrissey.labs.subscaleview.ImageSource;
import com.google.android.material.button.MaterialButton;
import com.sk7software.map2hand.geo.GPXRoute;
import com.sk7software.map2hand.geo.GeoConvert;
import com.sk7software.map2hand.geo.GeoLocation;
import com.sk7software.map2hand.net.NetworkRequest;
import com.sk7software.map2hand.net.WebRequest;
import com.sk7software.map2hand.util.SystemUiHider;
import com.sk7software.map2hand.view.MapView;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 *
 * @see SystemUiHider
 */
public class MapActivity extends Activity {

	private static final String STATE_SCALE = "state-scale";
	private static final String STATE_CENTER_X = "state-center-x";
	private static final String STATE_CENTER_Y = "state-center-y";
	private static final String STATE_MAP_NAME = "state-map";

	private static final String TAG = MapActivity.class.getSimpleName();

	// Time between updates to the location manager (in seconds)
	private static final int LM_UPDATE_INTERVAL = 2;

	// Network update intervals
	private static long lastUpdateTime = 0;
	private static final long UPDATE_INTERVAL_MS = 300000;
	private int newfile = 1;  // Indicates whether a new file should be created on the web server

	private LocationManager lm;
	private LocationListener locationListener;
	private MapFile currentMap;
	private GPXRoute currentRoute;
	protected PowerManager.WakeLock mWakeLock;
	private Button gpsButton;
	private boolean gpsListen = true;

	private MapView mapView = null;

	public static final int TOP_EDGE = 0x01;
	public static final int LEFT_EDGE = 0x02;
	public static final int BOTTOM_EDGE = 0x04;
	public static final int RIGHT_EDGE = 0x08;

	//	/**
//     * Whether or not the system UI should be auto-hidden after
//     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
//     */
//    private static final boolean AUTO_HIDE = true;
//
//    /**
//     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
//     * user interaction before hiding the system UI.
//     */
//    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;
//
//    /**
//     * If set, will toggle the system UI visibility upon interaction. Otherwise,
//     * will show the system UI visibility upon interaction.
//     */
//    private static final boolean TOGGLE_ON_CLICK = true;
//
//    /**
//     * The flags to pass to {@link SystemUiHider#getInstance}.
//     */
//    private static final int HIDER_FLAGS = SystemUiHider.FLAG_HIDE_NAVIGATION;
//
//    /**
//     * The instance of the {@link SystemUiHider} for this activity.
//     */
//    private SystemUiHider mSystemUiHider;
//
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		boolean restoreState = false;
		Log.d(TAG, "onCreate");

		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_map);

		// Name of map
		Intent mapIntent = getIntent();
		String mapName = mapIntent.getStringExtra("map");
		currentMap = MapController.getMapByName(mapName);

		gpsButton = (Button)findViewById(R.id.gpsButton);
		gpsButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (gpsListen) {
					// Switch off
					gpsListen = false;
					gpsButton.setBackground(getDrawable(R.drawable.button_unselected));
					gpsButton.setTextColor(Color.BLACK);
				} else {
					// Switch on
					gpsListen = true;
					gpsButton.setBackground(getDrawable(R.drawable.button_selected));
					gpsButton.setTextColor(Color.WHITE);
				}
			}
		});

		// Restore state
		if (savedInstanceState != null &&
				savedInstanceState.containsKey(STATE_SCALE) &&
				savedInstanceState.containsKey(STATE_CENTER_X) &&
				savedInstanceState.containsKey(STATE_CENTER_Y)) {

			restoreState = true;
			currentMap = MapController.getMapByName(savedInstanceState.getString(STATE_MAP_NAME));
		}

		loadNewMap(currentMap, restoreState);

		String routeName = mapIntent.getStringExtra("route");
		setCurrentRoute(routeName);

		if (restoreState) {
			mapView.setScaleAndCenter(savedInstanceState.getFloat(STATE_SCALE),
					new PointF(savedInstanceState.getFloat(STATE_CENTER_X), savedInstanceState.getFloat(STATE_CENTER_Y)));
		}
	}

	@Override
	protected void onStart() {
		super.onStart();
	}

	@Override
	protected void onResume() {
		Log.d(TAG, "onResume");
		super.onResume();

		// Start location listener
		lm = (LocationManager) getSystemService(LOCATION_SERVICE);

		locationListener = new MapLocationListener();

		if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
			// TODO: Consider calling
			//    ActivityCompat#requestPermissions
			// here to request the missing permissions, and then overriding
			//   public void onRequestPermissionsResult(int requestCode, String[] permissions,
			//                                          int[] grantResults)
			// to handle the case where the user grants the permission. See the documentation
			// for ActivityCompat#requestPermissions for more details.
			return;
		}
		lm.requestLocationUpdates(
				LocationManager.GPS_PROVIDER,
				LM_UPDATE_INTERVAL * 1000,
				0,
				locationListener);
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "onPause");
        super.onPause();
        
        // Kill the location listener
        lm.removeUpdates(locationListener);
        lm = null;
    }
    
    @Override 
    public void onBackPressed() {
    	Log.d(TAG, "onBackPressed");
    	
        // Store the map name
    	Intent i = new Intent();
    	i.putExtra("map", currentMap.getName());
    	setResult(Activity.RESULT_OK, i);
    	finish();
    }
    

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d(TAG, "onSaveInstanceState");

        outState.putFloat(STATE_SCALE, mapView.getScale());
        outState.putString(STATE_MAP_NAME, currentMap.getName());
        PointF center = mapView.getCenter();
        if (center != null) {
            outState.putFloat(STATE_CENTER_X, center.x);
            outState.putFloat(STATE_CENTER_Y, center.y);
        }
    }

    private void setCurrentRoute(String routeName) {
		if (routeName == null || "".equals(routeName)) {
			return;
		}

		NetworkRequest.fetchGPX(ApplicationContextProvider.getContext(), routeName, new NetworkRequest.NetworkCallback() {
			@Override
			public void onRequestCompleted(Object callbackData) {
				currentRoute = (GPXRoute)callbackData;
				if (currentRoute != null) {
					currentRoute.calcXY(currentMap, 0);
					mapView.setRoute(currentRoute);
				}
			}

			@Override
			public void onError(Exception e) {

			}
		});
	}
	/************************************************************
	 * MyLocationListener nested class
	 * @author Andrew
	 *
	 */
    private class MapLocationListener implements LocationListener 
    {
        //@Override
        public synchronized void onLocationChanged(Location loc) {
            if (loc != null && mapView.isReady() && gpsListen) {
            	// Check lat and lon are > 0
            	if (Math.abs(loc.getLatitude()) > 0.001 && Math.abs(loc.getLongitude()) > 0.001) {
            		
	        		try {
	        			boolean newMapLoaded = false;	        			
		                Log.d(TAG, "Location changed : Lat: " + loc.getLatitude() + 
			                    " Lng: " + loc.getLongitude());
		                
		                GeoLocation geoLoc = new GeoLocation();
		                geoLoc.setLatitude(loc.getLatitude());
		                geoLoc.setLongitude(loc.getLongitude());
		                geoLoc = GeoConvert.ConvertLLToGrid(currentMap.getProjection(), geoLoc, 0);
		                
		                // Check for better map
		                MapFile newMap = MapController.getBestMap(geoLoc);
		                if (newMap != null && !newMap.equals(currentMap)) {
		                	Log.d(TAG, "Loading map: " + newMap.getName());
		                	currentMap = newMap;
		                	loadNewMap(newMap, false);
							if (currentRoute != null) {
								currentRoute.calcXY(currentMap, 0);
								mapView.setRoute(currentRoute);
							}
		                	newMapLoaded = true;
		                }
		                
						if (Calendar.getInstance().getTimeInMillis() > mapView.getPanDelay() || newMapLoaded) {
							// Show location on map
							PointF mapPoint = new PointF();
							mapPoint.x = (float) ((geoLoc.getEasting() - currentMap.getTopLeftE()) / currentMap.getResolution());
							mapPoint.y = (float) ((currentMap.getTopLeftN() - geoLoc.getNorthing()) / currentMap.getResolution());
							mapView.setGeoLocation(mapPoint);
							mapView.setScaleAndCenter(mapView.getScale(), mapPoint);
							mapView.invalidate();

							if (mapView != null) {
								if (newMapLoaded) {
									Log.d(TAG, "Rescaling new map");
									mapView.setScaleAndCenter(1.0F, mapPoint);
								}
								uploadPosition(loc);
							}
						}
	        		}
	        		
	        		
		        	catch (Exception e) {
		        		Log.d(TAG, "Exception: " + e.getMessage());
		        	}
	            }
            }
        }
        
        private final void uploadPosition(Location loc) {
        	Date now = new Date();
        	if (now.getTime() - lastUpdateTime > UPDATE_INTERVAL_MS) {
	            // Upload position to tracker
	            String urlBase = "http://www.sk7software.com/gmaps/trackloc.php?id=353719054232291&status=on&newfile=" + newfile;
	            urlBase += "&txt=" + android.text.format.DateFormat.format("ddMMyyyyHHmm", now);
	            urlBase += "&lat=" + loc.getLatitude();
	            urlBase += "&lon=" + loc.getLongitude();
	            urlBase += "&speed=" + loc.getSpeed();
	            urlBase += "&acc=" + loc.getAccuracy();
	            Log.d(TAG, "Upload: " + urlBase);
	            
	            new WebRequest().execute(urlBase);
	            newfile = 0;
	            lastUpdateTime = now.getTime();
        	}
        }

        
        //@Override
        public void onProviderDisabled(String provider) {
            // TODO Auto-generated method stub
        }

        //@Override
        public void onProviderEnabled(String provider) {
            // TODO Auto-generated method stub
        }

        //@Override
        public void onStatusChanged(String provider, int status, 
            Bundle extras) {
            // TODO Auto-generated method stub
            Log.d(TAG, "onStatusChanged");
        }
    }        

    public void loadNewMap(MapFile map, boolean restoreState) {
        try {
	        mapView = (MapView) findViewById(R.id.mapView);
	        mapView.setImage(ImageSource.uri(map.getFullPath()));
	        mapView.setMaxScale(50);

	        if (!restoreState) {
	        	mapView.setScaleAndCenter(1.0F, new PointF(map.getWidthPix()/2, map.getHeightPix()/2));
	        }
		} catch (Exception e) {
        	Log.d(MapActivity.class.getSimpleName(), "Could not load map", e);
        }       	
    }

    public void clearUpdating() {
    }
    
    @Override
    protected void onStop() {
        Log.d(TAG, "onStop");
    	super.onStop();
    }
    
    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestory");
        super.onDestroy();
    }
}

//@Override
//protected void onPostCreate(Bundle savedInstanceState) {
//  super.onPostCreate(savedInstanceState);
//
//  // Trigger the initial hide() shortly after the activity has been
//  // created, to briefly hint to the user that UI controls
//  // are available.
////  delayedHide(100);
//}
//
//
///**
//* Touch listener to use for in-layout UI controls to delay hiding the
//* system UI. This is to prevent the jarring behavior of controls going away
//* while interacting with activity UI.
//*/
////View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
////  @Override
////  public boolean onTouch(View view, MotionEvent motionEvent) {
////      if (AUTO_HIDE) {
////          delayedHide(AUTO_HIDE_DELAY_MILLIS);
////      }
////      return false;
////  }
////};
//
////Handler mHideHandler = new Handler();
////Runnable mHideRunnable = new Runnable() {
////  @Override
////  public void run() {
////      mSystemUiHider.hide();
////  }
////};
////
///**
//* Schedules a call to hide() in [delay] milliseconds, canceling any
//* previously scheduled calls.
//*/
////private void delayedHide(int delayMillis) {
////  mHideHandler.removeCallbacks(mHideRunnable);
////  mHideHandler.postDelayed(mHideRunnable, delayMillis);
////}
//