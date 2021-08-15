package com.sk7software.map2hand;

import java.util.ArrayList;
import java.util.HashMap;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import androidx.core.app.ActivityCompat;

import com.sk7software.map2hand.db.Database;
import com.sk7software.map2hand.db.PreferencesUtil;

public class Map2HandActivity extends Activity {

	private ArrayList<HashMap<String,String>> mapList = new ArrayList<HashMap<String,String>>();
	private int selectedMap;
	private String currentMap;
	private ListView mapListView;
	private SimpleAdapter mapAdapter;

	private ArrayList<HashMap<String,String>> routeList = new ArrayList<HashMap<String,String>>();
	private int selectedRoute;
	private String currentRoute;
	private ListView routeListView;
	private SimpleAdapter routeAdapter;

	private static final String STATE_MAP_NAME = "mapName";
	private static final String STATE_ROUTE_NAME = "routeName";
	private static final String TAG = Map2HandActivity.class.getSimpleName();
	
	private static final int REQUEST_MAP = 1;
	private static final int REQUEST_MAP_VIEW = 2;
	private static final int LIST_POS_MAP = 0;

	// Storage Permissions
	private static final int REQUEST_EXTERNAL_STORAGE = 1;
	private static String[] PERMISSIONS_STORAGE = {
			Manifest.permission.READ_EXTERNAL_STORAGE,
			Manifest.permission.WRITE_EXTERNAL_STORAGE
	};

    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_map2hand);
		
		if (savedInstanceState != null) {
			currentMap = savedInstanceState.getString(STATE_MAP_NAME);
		}

		initPrefs();

		mapListView = (ListView) findViewById(R.id.mapList);
		HashMap<String,String> mapMap = new HashMap<String,String>();
		mapMap.put("name", "Selected Map");
		mapMap.put("value", (currentMap != null && currentMap.length() > 0 ? currentMap : "Tap to choose"));
		mapList.add(mapMap);
		
		mapAdapter = new SimpleAdapter(this, mapList, R.layout.list_item,
									new String[]{"name", "value"}, new int[]{R.id.firstLine, R.id.secondLine});

        mapListView.setAdapter(mapAdapter);
        mapListView.setOnItemClickListener(new OnItemClickListener(){
        	    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        	    	selectedMap = position;
        	    	Intent i = new Intent(ApplicationContextProvider.getContext(), MapListActivity.class);
        	    	startActivityForResult(i, REQUEST_MAP);
        	    }
        });

        routeListView = (ListView) findViewById(R.id.routeList);
		HashMap<String,String> routeMap = new HashMap<String,String>();
		routeMap.put("name", "Selected Route");
		routeMap.put("value", (currentRoute != null && currentRoute.length() > 0 ? currentRoute : "Tap to choose"));
		routeList.add(routeMap);

		routeAdapter = new SimpleAdapter(this, routeList, R.layout.list_item,
				new String[]{"name", "value"}, new int[]{R.id.firstLine, R.id.secondLine});

		routeListView.setAdapter(routeAdapter);
		routeListView.setOnItemClickListener(new OnItemClickListener(){
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				selectedRoute = position;
				Intent i = new Intent(ApplicationContextProvider.getContext(), RouteListActivity.class);
				startActivityForResult(i, REQUEST_MAP);
			}
		});

		Button b = (Button)findViewById(R.id.button1);
        b.setOnClickListener(new OnClickListener(){
        	public void onClick(View v){
        		// Store map position in list (so it can be updated on return)
        		selectedMap = LIST_POS_MAP;
        		
        		// Show map view
        		Intent i = new Intent(ApplicationContextProvider.getContext(), MapActivity.class);
				i.putExtra("map", currentMap);
				i.putExtra("route", currentRoute);
        		startActivityForResult(i, REQUEST_MAP_VIEW);
        	}
        });

		verifyStoragePermissions(this);

        Log.d("sk7.debug", "Before DB");
        this.deleteDatabase(Database.DATABASE_NAME);
        Database db = Database.getInstance();
        db.getWritableDatabase();
        Log.d("sk7.debug", "After DB");
        MapController.loadMaps();
    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.map2_hand, menu);
		return true;
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	    // Check which request we're responding to
	    if (requestCode == REQUEST_MAP || requestCode == REQUEST_MAP_VIEW) {
	        // Make sure the request was successful
	        if (resultCode == RESULT_OK) {
	        	Log.d(TAG, "Returned to form: " + selectedMap);
	        	
	        	// Get extra data
				if (data.hasExtra("map")) {
					String selMap = data.getStringExtra("map");
					HashMap<String, String> h = new HashMap<String, String>();
					h = mapList.get(this.selectedMap);
					h.put("value", selMap);
					currentMap = new String(selMap);
					mapAdapter.notifyDataSetChanged();
				} else if (data.hasExtra("route")) {
					String selRoute = data.getStringExtra("routeFile");
					String selRouteDesc = data.getStringExtra("route");
					HashMap<String, String> h = new HashMap<String, String>();
					h = routeList.get(this.selectedRoute);
					h.put("value", selRouteDesc);
					currentRoute = new String(selRoute);
					routeAdapter.notifyDataSetChanged();
				}
	        }
	    }
	}
	
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d(TAG, "onSaveInstanceState");

		outState.putString(STATE_MAP_NAME, currentMap);
		outState.putString(STATE_ROUTE_NAME, currentRoute);
    }
    
    protected void onRestoreInstanceState(Bundle savedState) {		
		Log.i(TAG, "onRestoreInstanceState");
		for (HashMap<String, String> h : mapList) {
			if (h.get("value") != null) {
				h.put("value", currentMap);
				mapAdapter.notifyDataSetChanged();
			}
		}
		for (HashMap<String, String> h : routeList) {
			if (h.get("value") != null) {
				h.put("value", currentRoute);
				routeAdapter.notifyDataSetChanged();
			}
		}
    }

    private void initPrefs() {
		PreferencesUtil.init(getApplicationContext());
		if (!PreferencesUtil.isPrefsSet()) {
			// Set defaults
			PreferencesUtil.getInstance().addPreference(PreferencesUtil.PREFERNECE_GPS, true);
			PreferencesUtil.getInstance().addPreference(PreferencesUtil.PREFERNECE_ZOOM, true);
			PreferencesUtil.getInstance().addPreference(PreferencesUtil.PREFERNECE_ROUTE_WIDTH, 10);
			PreferencesUtil.getInstance().addPreference(PreferencesUtil.PREFERNECE_ROUTE_TRANSPARENCY, 30);
			PreferencesUtil.getInstance().addPreference(PreferencesUtil.PREFERENCES_INIT, PreferencesUtil.PREFS_SET);
		}
	}

	public static void verifyStoragePermissions(Activity activity) {
		// Check if we have write permission
		int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE);

		if (permission != PackageManager.PERMISSION_GRANTED) {
			// We don't have permission so prompt the user
			ActivityCompat.requestPermissions(
					activity,
					PERMISSIONS_STORAGE,
					REQUEST_EXTERNAL_STORAGE
			);
		}
	}
}
