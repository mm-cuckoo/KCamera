package com.sgf.demo.utils;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;

public class OrientationFilter implements SensorEventListener {

    private final OrientationSensorManager mSensorManager;
    private OrientationChangeListener mChangeListener;
    private Float mSensorX;
    private Float mSensorY;
    private boolean isRecord = false;
    public interface OrientationChangeListener {
        void onChanged();
    }

    public OrientationFilter(OrientationSensorManager sensorManager) {
        this.mSensorManager = sensorManager;
        this.mSensorManager.setSensorListener(this);
    }


    public void onResume() {
        mSensorManager.register();
    }

    public void onPause() {
        mSensorManager.unregister();
    }

    public void setOnceListener(OrientationChangeListener listener) {
        mChangeListener = listener;
        isRecord = true;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        // 0 是沿着y轴旋转
        // 1 是 x 轴旋转
        // 2 是 z 轴旋转

        if (isRecord) {
            mSensorX = event.values[1];
            mSensorY = event.values[0];
            isRecord = false;
            return;
        }

        if (mChangeListener != null && mSensorX != null && mSensorY != null) {
            float sensorY = Math.abs(Math.round((event.values[0] - mSensorY) * 100));
            float sensorX = Math.abs(Math.round((event.values[1] - mSensorX) * 100));
            if (sensorX > 20 * 100 || sensorY > 20 * 100) {
                mChangeListener.onChanged();
                mChangeListener = null;
                mSensorX = null;
                mSensorY = null;
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public void release() {
        mChangeListener = null;
    }
}
