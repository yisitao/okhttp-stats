package com.flipkart.flipperf.oldlib;

import android.content.Context;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.ByteArrayPool;
import com.android.volley.toolbox.HttpStack;
import com.flipkart.flipperf.oldlib.models.NetworkType;
import com.flipkart.flipperf.oldlib.trackers.APIEvent;
import com.flipkart.flipperf.oldlib.trackers.FlipperfNetworkStatManager;

/**
 * Created by nikhil.n on 19/01/15.
 */
public class FlipperfNetwork extends BasicNetwork {

    private Context context;

    public FlipperfNetwork(HttpStack httpStack, Context context) {
        super(httpStack);
        this.context = context;
    }

    public FlipperfNetwork(HttpStack httpStack, ByteArrayPool pool, Context context) {
        super(httpStack, pool);
        this.context = context;
    }

    @Override
    public NetworkResponse performRequest(Request<?> request) throws VolleyError {
        NetworkType networkType = FlipperfNetworkStatManager.getInstance(context).getCurrentNetworkType();
        APIEvent apiEvent = new APIEvent(context,networkType, request.getUrl());
        FlipperfEventManager<String> apiEventManager = FlipperfEventManager.getInstance(context);
        apiEventManager.startEvent(apiEvent);
        try {
            NetworkResponse networkResponse = super.performRequest(request);
            if(networkResponse.statusCode == 200) {
                if(networkResponse.data != null && networkResponse.data.length > 0)
                    apiEvent.setResponseSize(networkResponse.data.length);
                    apiEventManager.stopEvent(request.getUrl());
            } else {
                apiEventManager.removeEvent(request.getUrl());
            }
            return networkResponse;
        } catch (VolleyError e) {
            apiEventManager.removeEvent(request.getUrl());
            throw e;
        }
    }
}