package com.sgf.kcamera.camera.info;

import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.util.Size;

public class CameraInfo {
    private final String mCameraId;
    private final CameraCharacteristics mCharacteristics;

    CameraInfo(String cameraId, CameraCharacteristics characteristics) {
        this.mCameraId = cameraId;
        this.mCharacteristics = characteristics;
    }

    public String getCameraId() {
        return mCameraId;
    }

    public CameraCharacteristics getCharacteristics() {
        return mCharacteristics;
    }

    /**
     * 获取设备支持的拍照分辨率
     *
     * @param format
     * @return
     */
    public Size[] getPictureSize(int format) {
        Size[] sizes = null;
        StreamConfigurationMap map = mCharacteristics
                .get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
        if (map != null) {
            sizes =  map.getOutputSizes(format);
        }
        return sizes;
    }

    public Size[] getPreviewSize(Class<?> klass) {
        Size[] sizes = null;
        StreamConfigurationMap map = mCharacteristics
                .get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
        if (map != null) {
            sizes = map.getOutputSizes(klass);
        }
        return sizes;
    }

    /**
     * 获取支持的预览分辨率
     * @return
     */
    public Size[] getPreviewSize() {
        return getPreviewSize(SurfaceTexture.class);
    }

    /**
     * 获取Camera sensor 中方向
     * @return ： Camera 物理角度
     */
    public int getSensorOrientation() {
        Integer orientation = mCharacteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
        return orientation == null ? 0 : orientation;
    }
}
