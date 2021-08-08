package com.sk7software.map2hand;

import java.util.ArrayList;
import java.util.HashMap;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.PointF;
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

public class Map2HandActivity extends Activity {

	private ArrayList<HashMap<String,String>> list = new ArrayList<HashMap<String,String>>();
	private int selectedItem;
	private String currentMap;
	private ListView lv;
	private SimpleAdapter adapter;

	private static final String STATE_MAP_NAME = "mapName";
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

		lv = (ListView) findViewById(R.id.listView1);
		HashMap<String,String> h = new HashMap<String,String>();
		h.put("name", "Selected Map");
		h.put("value", (currentMap != null && currentMap.length() > 0 ? currentMap : "Tap to choose"));
		list.add(h);
		
		adapter = new SimpleAdapter(this, list, R.layout.list_item, 
									new String[]{"name", "value"}, new int[]{R.id.firstLine, R.id.secondLine});

        lv.setAdapter(adapter);	
        lv.setOnItemClickListener(new OnItemClickListener(){
        	    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        	    	selectedItem = position;
        	    	Intent i = new Intent(ApplicationContextProvider.getContext(), MapListActivity.class);
        	    	startActivityForResult(i, REQUEST_MAP);
        	    }
        });

        Button b = (Button)findViewById(R.id.button1);
        b.setOnClickListener(new OnClickListener(){
        	public void onClick(View v){
        		// Store map position in list (so it can be updated on return)
        		selectedItem = LIST_POS_MAP;
        		
        		// Show map view
        		Intent i = new Intent(ApplicationContextProvider.getContext(), MapActivity.class);
        		i.putExtra("map", currentMap);
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
	        	Log.d(TAG, "Returned to form: " + selectedItem);
	        	
	        	// Get extra data
	        	String selectedMap = data.getStringExtra("map");
	        	HashMap<String, String> h = new HashMap<String, String>();
	        	h = list.get(selectedItem);
	        	h.put("value", selectedMap);
	        	currentMap = new String(selectedMap);
	        	adapter.notifyDataSetChanged();
	        }
	    }
	}
	
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d(TAG, "onSaveInstanceState");

        outState.putString(STATE_MAP_NAME, currentMap);
    }
    
    protected void onRestoreInstanceState(Bundle savedState) {		
		Log.i(TAG, "onRestoreInstanceState");
		//HashMap<String, String> h = new HashMap<String, String>();
		for (HashMap<String, String> h : list) {
			if (h.get("value") != null) {
				h.put("value", currentMap);
				adapter.notifyDataSetChanged();
			}
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
