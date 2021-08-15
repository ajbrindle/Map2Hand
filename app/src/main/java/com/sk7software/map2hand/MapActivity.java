package com.sk7software.map2hand;

import java.util.Calendar;
import java.util.Date;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.PointF;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Switch;

import androidx.core.app.ActivityCompat;

import com.davemorrissey.labs.subscaleview.ImageSource;
import com.sk7software.map2hand.db.PreferencesUtil;
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
	private Button menuButton;
	private LinearLayout menuPanel;
	private boolean gpsListen = true;
	private boolean autoZoom = true;

	private Button upButton;
	private Button downButton;
	private Button leftButton;
	private Button rightButton;
	private Button zoomInButton;
	private Button zoomOutButton;

	private MapView mapView = null;

	private enum MapAction {
		DOWN,
		UP,
		LEFT,
		RIGHT,
		ZOOM_IN,
		ZOOM_OUT,
		CHECK_ONLY
	}

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

		menuPanel = (LinearLayout)findViewById(R.id.slideMenu);
		menuButton = (Button)findViewById(R.id.btnMenu);
		menuButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (menuPanel.getVisibility() == View.INVISIBLE) {
					initialisePanel();
					TranslateAnimation animate = new TranslateAnimation(
							-menuPanel.getWidth(),
							0,
							0,
							0);
					animate.setDuration(250);
					menuPanel.startAnimation(animate);
					menuPanel.setVisibility(View.VISIBLE);
				} else {
					TranslateAnimation animate = new TranslateAnimation(
							0,
							-menuPanel.getWidth(),
							0,
							0);
					animate.setDuration(250);
					menuPanel.startAnimation(animate);
					menuPanel.setVisibility(View.INVISIBLE);
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

		// Initialise map view
		mapView = (MapView) findViewById(R.id.mapView);
		mapView.setMaxScale(50);
		mapView.setOnReadyFunction(new MapView.OnReadyCallback() {
			@Override
			public void ready() {
				Log.d(TAG, "OnReady callback");
				checkEdges(MapAction.CHECK_ONLY);
			}
		});
		refreshDisplay(null, currentMap, false, false);

		String routeName = mapIntent.getStringExtra("route");
		setCurrentRoute(routeName);

		if (restoreState) {
			mapView.setScaleAndCenter(savedInstanceState.getFloat(STATE_SCALE),
					new PointF(savedInstanceState.getFloat(STATE_CENTER_X), savedInstanceState.getFloat(STATE_CENTER_Y)));
		}

		initialisePanel();

		upButton = (Button)findViewById(R.id.upButton);
		setChangeMapAction(upButton, MapAction.UP);
		downButton = (Button)findViewById(R.id.downButton);
		setChangeMapAction(downButton, MapAction.DOWN);
		leftButton = (Button)findViewById(R.id.leftButton);
		setChangeMapAction(leftButton, MapAction.LEFT);
		rightButton = (Button)findViewById(R.id.rightButton);
		setChangeMapAction(rightButton, MapAction.RIGHT);
		zoomInButton = (Button)findViewById(R.id.zoomInButton);
		setChangeMapAction(zoomInButton, MapAction.ZOOM_IN);
		zoomOutButton = (Button)findViewById(R.id.zoomOutButton);
		setChangeMapAction(zoomOutButton, MapAction.ZOOM_OUT);
	}

	private void setChangeMapAction(final Button button, final MapAction action) {
		button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				checkEdges(action);
			}
		});
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

		mapView.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_MOVE) {
					checkEdges(MapAction.CHECK_ONLY);
				}
				return false;
			}
		});
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

	private void initialisePanel() {
		SeekBar seekTransparency = (SeekBar)findViewById(R.id.seekTransparency);
		seekTransparency.setProgress(PreferencesUtil.getInstance().getIntPreference(PreferencesUtil.PREFERNECE_ROUTE_TRANSPARENCY));
		seekTransparency.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			int progressChangedValue = 0;

			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				PreferencesUtil.getInstance().addPreference(PreferencesUtil.PREFERNECE_ROUTE_TRANSPARENCY, progress);
				mapView.invalidate();
			}
			public void onStartTrackingTouch(SeekBar seekBar) {
			}

			public void onStopTrackingTouch(SeekBar seekBar) {
			}
		});

		SeekBar seekWidth = (SeekBar)findViewById(R.id.seekWidth);
		seekWidth.setProgress(PreferencesUtil.getInstance().getIntPreference(PreferencesUtil.PREFERNECE_ROUTE_WIDTH));
		seekWidth.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			int progressChangedValue = 0;

			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				PreferencesUtil.getInstance().addPreference(PreferencesUtil.PREFERNECE_ROUTE_WIDTH, progress);
				mapView.invalidate();
			}
			public void onStartTrackingTouch(SeekBar seekBar) {
			}

			public void onStopTrackingTouch(SeekBar seekBar) {
			}
		});

		Switch swiGPS = (Switch)findViewById(R.id.swiGPS);
		gpsListen = PreferencesUtil.getInstance().getBooleanPreference(PreferencesUtil.PREFERNECE_GPS);
		swiGPS.setChecked(gpsListen);
		swiGPS.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				PreferencesUtil.getInstance().addPreference(PreferencesUtil.PREFERNECE_GPS, isChecked);
				gpsListen = isChecked;
			}
		});

		Switch swiZoom = (Switch)findViewById(R.id.swiZoom);
		autoZoom = PreferencesUtil.getInstance().getBooleanPreference(PreferencesUtil.PREFERNECE_ZOOM);
		swiZoom.setChecked(autoZoom);
		swiZoom.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				PreferencesUtil.getInstance().addPreference(PreferencesUtil.PREFERNECE_ZOOM, isChecked);
				autoZoom = isChecked;
			}
		});
	}

	private void checkEdges(MapAction action) {
//		Log.d(TAG, "Check edges: " + action.name());
		PointF viewTopLeft = mapView.viewToSourceCoord(0, 0);
		PointF viewBottomRight = mapView.viewToSourceCoord(mapView.getWidth(), mapView.getHeight());

		if (viewTopLeft != null && viewBottomRight != null) {
			// Get grid coordinates at edges of display
			float leftE = currentMap.getTopLeftE() + (float) (viewTopLeft.x * currentMap.getResolution());
			float rightE = currentMap.getTopLeftE() + (float) (viewBottomRight.x * currentMap.getResolution());
			float midE = (rightE + leftE) / 2;
			float topN = currentMap.getTopLeftN() - (float) (viewTopLeft.y * currentMap.getResolution());
			float bottomN = currentMap.getTopLeftN() - (float) (viewBottomRight.y * currentMap.getResolution());
			float midN = (topN + bottomN) / 2;
			float offset = 5 * (float) currentMap.getResolution();

			PointF checkUp = new PointF(midE, topN + offset);
			PointF checkDown = new PointF(midE, bottomN - offset);
			PointF checkLeft = new PointF(leftE - offset, midN);
			PointF checkRight = new PointF(rightE + offset, midN);
			PointF checkZoom = new PointF(midE, midN);

//		Log.d(TAG, "Up: " + checkUp.x + "," + checkUp.y);
//		Log.d(TAG, "Down: " + checkDown.x + "," + checkDown.y);
//		Log.d(TAG, "Left: " + checkLeft.x + "," + checkLeft.y);
//		Log.d(TAG, "Right: " + checkRight.x + "," + checkRight.y);
			if (!checkAndDoAction(MapAction.UP, checkUp, upButton, action))
				if (!checkAndDoAction(MapAction.DOWN, checkDown, downButton, action))
					if (!checkAndDoAction(MapAction.LEFT, checkLeft, leftButton, action))
						if (!checkAndDoAction(MapAction.RIGHT, checkRight, rightButton, action))
							if (!checkAndDoAction(MapAction.ZOOM_IN, checkZoom, zoomInButton, action))
								checkAndDoAction(MapAction.ZOOM_OUT, checkZoom, zoomOutButton, action);
		}
	}

	private boolean checkAndDoAction(MapAction buttonAction, PointF point, Button button, MapAction action) {
		MapFile map = null;
		boolean keepScale = true;
		boolean zooming = (buttonAction == MapAction.ZOOM_IN || buttonAction == MapAction.ZOOM_OUT);

		if (!MapController.isPointOnMap(point, currentMap)) {
			map = MapController.hasMap(point, currentMap);
		} else if (zooming) {
//			Log.d(TAG, "Check zoom " + buttonAction.name());
			map = MapController.getNearestMap(point, currentMap, buttonAction == MapAction.ZOOM_IN);
			keepScale = false;
		} else {
			button.setVisibility(View.INVISIBLE);
			return false;
		}

		if (map != null) {
			button.setVisibility(View.VISIBLE);
			if (buttonAction == action) {
				// Disable auto-zoom if zooming
				if (zooming) {
					autoZoom = false;
					PreferencesUtil.getInstance().addPreference(PreferencesUtil.PREFERNECE_ZOOM, false);
				}
				refreshDisplay(point, map, keepScale, false);
				return true;
			}
		} else {
			button.setVisibility(View.INVISIBLE);
		}

		return false;
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
	        			boolean mapChanged = false;
		                Log.d(TAG, "Location changed : Lat: " + loc.getLatitude() + 
			                    " Lng: " + loc.getLongitude());
		                
		                GeoLocation geoLoc = new GeoLocation();
		                geoLoc.setLatitude(loc.getLatitude());
		                geoLoc.setLongitude(loc.getLongitude());
		                geoLoc = GeoConvert.ConvertLLToGrid(currentMap.getProjection(), geoLoc, 0);
						PointF mapPoint = new PointF((float)geoLoc.getEasting(), (float)geoLoc.getNorthing());

		                // Check for better map
						MapFile newMap = MapController.getBestMap(geoLoc, currentMap, autoZoom);
						if (newMap != null && !newMap.equals(currentMap)) {
							Log.d(TAG, "Map changed to: " + newMap.getName());
							boolean resChanged = MapController.isDifferentResolution(currentMap.getResolution(), newMap.getResolution());
							refreshDisplay(mapPoint, newMap, !resChanged, true);
						} else if (Calendar.getInstance().getTimeInMillis() > mapView.getPanDelay()) {
							// Show location on map
							refreshDisplay(mapPoint, null, true, true);
							uploadPosition(loc);
						}
						checkEdges(MapAction.CHECK_ONLY);
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
    }

    // Scenarios for refreshing display
	// 1 - location changed, no change of map
	//		centre = E/N
	//		newMap = null
	//		keepScale = true
	//		locIsGPS = true
	// 2 - location changed, change of map
	//		centre = E/N
	//		newMap = new map to load
	//		keepScale = true if same res as old map, otherwise false
	//		locIsGPS = true
	// 3 - manual switch to adjacent map
	//		centre = edge point on adjacent map
	//		newMap = adjacent map
	//		keepScale = true
	//		locIsGPS = false
	// 4 - zooming to new map
	//		centre = centre of current map
	//		newMap = new map to zoom to
	//		keepScale = false
	//		locIsGPS = false
    private void refreshDisplay(PointF centre, MapFile newMap, boolean keepScale, boolean locIsGPS) {
//    	Log.d(TAG, "New map: " + (newMap != null ? newMap.getName() : "null"));
//    	Log.d(TAG, "Point: " + (centre != null ? centre.x + "," + centre.y : "null"));
//    	Log.d(TAG, "Keep scale: " + keepScale);
//    	Log.d(TAG, "Location is GPS: " + locIsGPS);

    	float currentScale = mapView.getScale();

		if (newMap != null) {
    		mapView.setImage(ImageSource.uri(newMap.getFullPath()));
    		currentMap = newMap;
    		if (currentRoute != null) {
				currentRoute.calcXY(currentMap, 0);
			}
		}

		PointF mapPoint = new PointF();
		if (centre == null) {
			mapPoint.x = currentMap.getWidthPix()/2;
			mapPoint.y = currentMap.getHeightPix()/2;
		} else {
			mapPoint.x = (float) ((centre.x - currentMap.getTopLeftE()) / currentMap.getResolution());
			mapPoint.y = (float) ((currentMap.getTopLeftN() - centre.y) / currentMap.getResolution());
			Log.d(TAG, "Map point: " + mapPoint.x + "," + mapPoint.y);
			if (locIsGPS) {
				mapView.setMapGPSLocation(mapPoint);
			}
		}

		if (keepScale) {
			mapView.setScaleAndCenter(currentScale, mapPoint);
		} else {
			mapView.setScaleAndCenter(1.0F, mapPoint);
		}
		mapView.invalidate();
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