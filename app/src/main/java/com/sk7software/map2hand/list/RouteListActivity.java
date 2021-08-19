package com.sk7software.map2hand.list;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.sk7software.map2hand.ActivityUpdateInterface;
import com.sk7software.map2hand.ApplicationContextProvider;
import com.sk7software.map2hand.R;
import com.sk7software.map2hand.db.GPXFile;
import com.sk7software.map2hand.db.GPXFiles;
import com.sk7software.map2hand.net.NetworkRequest;

public class RouteListActivity extends Activity implements ActivityUpdateInterface {
    private AlertDialog.Builder progressDialogBuilder;
    private Dialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_list);
        // Show the Up button in the action bar.
        setupActionBar();

        // Create progress dialog for use later
        progressDialogBuilder = new AlertDialog.Builder(RouteListActivity.this);
        progressDialogBuilder.setView(R.layout.progress);

        initGPXFileList(this);
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

    private void initGPXFileList(final Activity activity) {
        setProgress(true, "Fetching Routes");
        final GPXFiles files = new GPXFiles();
        files.addLocalFiles();

        NetworkRequest.fetchGPXFiles(ApplicationContextProvider.getContext(), this, new NetworkRequest.NetworkCallback() {
            @Override
            public void onRequestCompleted(Object callbackData) {
                files.addFiles((GPXFiles)callbackData);
                setUpList(activity, files);
            }

            @Override
            public void onError(Exception e) {
                setUpList(activity, files);
            }
        });
    }

    private void setUpList(final Activity activity, final GPXFiles files) {
        if (files != null && files.getFiles() != null && files.getFiles().size() > 0) {
            final ListView lv = (ListView) findViewById(R.id.mapList);
            GPXFile[] fileItems = files.getFiles().toArray(new GPXFile[0]);

            ArrayAdapter<GPXFile> adapter = new ArrayAdapter<GPXFile>(activity, android.R.layout.simple_list_item_1, android.R.id.text1, fileItems);
            lv.setAdapter(adapter);
            lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Intent i = new Intent();
                    i.putExtra("route", fileItems[position].getDescription());
                    i.putExtra("routeFile", fileItems[position].getName());
                    i.putExtra("local", fileItems[position].isLocal());
                    setResult(Activity.RESULT_OK, i);
                    finish();
                }
            });
        }
    }

    @Override
    public void setProgress(boolean showProgressDialog, String progressMessage) {
        if (showProgressDialog) {
            progressDialog = progressDialogBuilder
                    .setMessage(progressMessage)
                    .create();
            progressDialog.show();
        } else {
            if (progressDialog != null) {
                progressDialog.dismiss();
            }
        }
    }
}
