package com.sk7software.map2hand.net;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.sk7software.map2hand.ApplicationContextProvider;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

public class WebRequest extends AsyncTask<String, String, String>{

    private static final String TAG = WebRequest.class.getSimpleName();
    private static RequestQueue queue;

    @Override
    protected String doInBackground(String... uri) {
        try {
            StringRequest webRequest = new StringRequest
                    (Request.Method.GET, uri[0],
                            new Response.Listener<String>() {
                                @Override
                                public void onResponse(String response) {
                                    Log.d(TAG, "Completed upload: " + response);
                                }
                            },
                            new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    Log.d(TAG, "Error => " + error.toString());
                                }
                            }
                    );
            webRequest.setRetryPolicy(new DefaultRetryPolicy(5000, 3, 1));
            getQueue(ApplicationContextProvider.getContext()).add(webRequest);
        } catch (Exception e) {
            Log.d(TAG, "Error fetching dev messages: " + e.getMessage());
        }
        return null;
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        //Do anything with response..
    }

    private synchronized static RequestQueue getQueue(Context context) {
        if (queue == null) {
            queue = Volley.newRequestQueue(context);
        }
        return queue;
    }
}