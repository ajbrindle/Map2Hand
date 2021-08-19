package com.sk7software.map2hand.net;

import android.content.Context;
import android.util.Log;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sk7software.map2hand.ActivityUpdateInterface;
import com.sk7software.map2hand.db.GPXFile;
import com.sk7software.map2hand.db.GPXFiles;
import com.sk7software.map2hand.geo.GPXRoute;
import com.sk7software.map2hand.geo.GPXLocation;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Arrays;

public class NetworkRequest {
    private static RequestQueue queue;

    private static final String GPX_LOAD_URL = "http://www.sk7software.co.uk/gpxloader/gpxload.php";
    private static final String GPX_LIST_URL = "http://www.sk7software.co.uk/gpxloader/gpxlist.php";
    private static final String TAG = NetworkRequest.class.getSimpleName();

    public interface NetworkCallback {
        public void onRequestCompleted(Object callbackData);
        public void onError(Exception e);
    }

    private synchronized static RequestQueue getQueue(Context context) {
        if (queue == null) {
            queue = Volley.newRequestQueue(context);
        }
        return queue;
    }

    public static void fetchGPX(final Context context, String gpxFile, final NetworkCallback callback) {
        // Set start and end points before serialising and removing other points
        Log.d(TAG, "Fetching: " + gpxFile);
        try {
//            final Gson gson = new GsonBuilder()
//                    .create();

            JsonObjectRequest jsObjRequest = new JsonObjectRequest
                    (Request.Method.GET, GPX_LOAD_URL + "?name=" + gpxFile,
                            null,
                            new Response.Listener<JSONObject>() {
                                @Override
                                public void onResponse(JSONObject response) {
                                    try {
                                        ObjectMapper mapper = new ObjectMapper();
                                        GPXRoute route = mapper.readValue(response.toString(), GPXRoute.class);
                                        Log.d(TAG, route.getName());
//                                        for (GPXLocation ll : route.getPoints()) {
//                                            Log.d(TAG, "Lat:" + ll.getLat() + ", Lon: " + ll.getLon() );
//                                        }
                                        callback.onRequestCompleted(route);
                                    } catch (JsonProcessingException e) {
                                        Log.d(TAG, "Error getting dev messages: " + e.getMessage());
                                    }
                                }
                            },
                            new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    Log.d(TAG, "Error => " + error.toString());
                                    callback.onError(error);
                                }
                            }
                    );
            jsObjRequest.setRetryPolicy(new DefaultRetryPolicy(5000, 1, 1));
            getQueue(context).add(jsObjRequest);
        } catch (Exception e) {
            Log.d(TAG, "Error fetching GPX route: " + e.getMessage());
        }
    }

    public static void fetchGPXFiles(final Context context, ActivityUpdateInterface uiUpdate, final NetworkCallback callback) {
        // Set start and end points before serialising and removing other points
        Log.d(TAG, "Fetching GPX file list");
        try {
            JsonArrayRequest jsObjRequest = new JsonArrayRequest
                    (Request.Method.GET, GPX_LIST_URL,
                            null,
                            new Response.Listener<JSONArray>() {
                                @Override
                                public void onResponse(JSONArray response) {
                                    try {
                                        ObjectMapper mapper = new ObjectMapper();
                                        GPXFiles files = new GPXFiles();
                                        files.setFiles(Arrays.asList(mapper.readValue(response.toString(), GPXFile[].class)));
                                        uiUpdate.setProgress(false, null);
                                        callback.onRequestCompleted(files);
                                    } catch (JsonProcessingException e) {
                                        Log.d(TAG, "Error getting dev messages: " + e.getMessage());
                                    }
                                }
                            },
                            new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    Log.d(TAG, "Error => " + error.toString());
                                    uiUpdate.setProgress(false, null);
                                    callback.onError(error);
                                }
                            }
                    );
            jsObjRequest.setRetryPolicy(new DefaultRetryPolicy(5000, 1, 1));
            getQueue(context).add(jsObjRequest);
        } catch (Exception e) {
            uiUpdate.setProgress(false, null);
            Log.d(TAG, "Error fetching GPX route: " + e.getMessage());
        }
    }

}
