package com.sk7software.map2hand;

import android.app.Application;
import android.content.Context;
import android.os.Environment;

public class ApplicationContextProvider extends Application {
	/**
     * Keeps a reference of the application context
     */
    private static Context sContext;
    public static final String EXT_SD_DIR = Environment.getExternalStorageDirectory().toString();
 
    @Override
    public void onCreate() {
        super.onCreate();
 
        sContext = getApplicationContext();
 
    }
 
    /**
     * Returns the application context
     *
     * @return application context
     */
    public static Context getContext() {
        return sContext;
    }
 
}
