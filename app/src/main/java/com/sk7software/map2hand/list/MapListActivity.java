package com.sk7software.map2hand.list;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
//import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

import com.sk7software.map2hand.R;
import com.sk7software.map2hand.db.Database;

public class MapListActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_map_list);
		// Show the Up button in the action bar.
		setupActionBar();
		
		// Populate list
		final ListView lv = (ListView)findViewById(R.id.mapList);
		final String[] items = Database.getMapsList();
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, android.R.id.text1, items);
		lv.setAdapter(adapter);
        lv.setOnItemClickListener(new OnItemClickListener(){
    	    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
    	    	Intent i = new Intent();
    	    	i.putExtra("map", items[position]);
    	    	setResult(Activity.RESULT_OK, i);
    	    	finish();
    	    }
    });

	}

	/**
	 * Set up the {@link android.app.ActionBar}.
	 */
	private void setupActionBar() {
		getActionBar().setDisplayHomeAsUpEnabled(true);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.map_list, menu);
		return true;
	}
}
