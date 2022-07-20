package com.sgf.kcamera.business.session;


import android.hardware.camera2.CaptureRequest;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class RequestCache {

    private final Map<CaptureRequest.Key<?>, Object> mMapObject = new HashMap<>();

    public <T> void put(CaptureRequest.Key<T> key, T value) {
        mMapObject.put(key,value);
    }


    public <T> T get(CaptureRequest.Key<T> key) {
        if (mMapObject.containsKey(key) ) {
            return (T) mMapObject.get(key);
        }
        return null;
    }

    public <T> T get(CaptureRequest.Key<T> key, T def) {
        if (mMapObject.containsKey(key) ) {
            return (T) mMapObject.get(key);
        }
        return def;
    }

    public Set<CaptureRequest.Key<?>> getKeySet() {
        return mMapObject.keySet();
    }

    public void reset() {
        mMapObject.clear();
    }



}
