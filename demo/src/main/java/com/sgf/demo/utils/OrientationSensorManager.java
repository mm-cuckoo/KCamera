package com.sgf.demo.utils;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class OrientationSensorManager implements SensorEventListener {

    private static final OrientationSensorManager sManager = new OrientationSensorManager();
    private SensorEventListener mListener;
    private SensorManager mSensorManager;

    private OrientationSensorManager(){}

    public void init(Context context) {
        mSensorManager = (SensorManager) context.getApplicationContext().getSystemService(Context.SENSOR_SERVICE);
    }

    public static OrientationSensorManager getInstance() {
        return sManager;
    }


    public void setSensorListener(SensorEventListener listener) {
        this.mListener = listener;
    }

    public void register() {
        Sensor sensorOrientation = mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
        mSensorManager.registerListener(this, sensorOrientation, SensorManager.SENSOR_DELAY_UI);
    }


    public void unregister() {
        mSensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (mListener != null) {
            this.mListener.onSensorChanged(event);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
