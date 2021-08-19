package com.sk7software.map2hand.db;

import android.content.Context;
import android.content.SharedPreferences;

public class PreferencesUtil {
    public static final String APP_PREFERENCES_KEY = "SK7_MAP2HAND_PREFS";
    public static final String PREFERNECE_GPS = "PREF_GPS";
    public static final String PREFERNECE_ZOOM = "PREF_ZOOM";
    public static final String PREFERNECE_BEARING = "PREF_BEARING";
    public static final String PREFERNECE_ROUTE_TRANSPARENCY = "PREF_ROUTE_TRANSPARENCY";
    public static final String PREFERNECE_ROUTE_WIDTH = "PREF_ROUTE_WIDTH";
    public static final String PREFERNECE_MARKER_SIZE = "PREF_MARKER_SIZE";
    public static final String PREFERENCES_INIT = "PREFS_INIT";
    public static final String PREFS_SET = "Y";

    private static PreferencesUtil instance;
    private final SharedPreferences prefs;

    private PreferencesUtil(Context context) {
        prefs = context.getSharedPreferences(APP_PREFERENCES_KEY, Context.MODE_PRIVATE);
    }

    public synchronized static void init(Context context) {
        if (instance == null) {
            instance = new PreferencesUtil(context);
        }
    }

    public static PreferencesUtil getInstance() {
        if (instance == null) {
            throw new IllegalStateException("Preferences not initialised");
        } else {
            return instance;
        }
    }

    public static boolean isPrefsSet() {
        return PREFS_SET.equals(getInstance().getStringPreference(PREFERENCES_INIT));
    }

    public void addPreference(String name, String value) {
        prefs.edit().putString(name, value).commit();
    }

    public void addPreference(String name, int value) {
        prefs.edit().putInt(name, value).commit();
    }

    public void addPreference(String name, boolean value) {
        prefs.edit().putBoolean(name, value).commit();
    }

    public String getStringPreference(String name) {
        return prefs.getString(name, "");
    }

    public int getIntPreference(String name) {
        return prefs.getInt(name, 0);
    }

    public void clearAllPreferences() {
        prefs.edit().clear().commit();
    }

    public static void reset() {
        instance = null;
    }

    public boolean getBooleanPreference(String name) {
        return prefs.getBoolean(name, false);
    }

    public void clearStringPreference(String name) {
        prefs.edit().putString(name, "").commit();
    }

}
