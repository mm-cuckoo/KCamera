package com.sgf.kcamera.camera.info;

import android.graphics.Rect;
import android.hardware.camera2.CameraCharacteristics;
import android.util.Range;
import android.util.Size;

import com.sgf.kcamera.CameraID;
import com.sgf.kcamera.log.KLog;

public class CameraInfoManagerImpl implements CameraInfoManager {

    private static final String TAG = "CameraInfoManagerImpl";

    public static final CameraInfoManager CAMERA_INFO_MANAGER = new CameraInfoManagerImpl();
    private static final int MAX_ZOOM_VALUE = 100;

    private CameraInfo mCameraInfo;

    private CameraInfoManagerImpl(){}

    @Override
    public void initCameraInfo(CameraID cameraID) {
        mCameraInfo = CameraInfoHelper.getInstance().getCameraInfo(cameraID.ID);
    }

    @Override
    public boolean isAutoFocusSupported() {
        Float minFocusDist = getCharacteristics().get(CameraCharacteristics.LENS_INFO_MINIMUM_FOCUS_DISTANCE);
        KLog.d(TAG,"isAutoFocusSupported: minFocusDist:" + minFocusDist);
        return minFocusDist != null && minFocusDist > 0;
    }

    @Override
    public boolean isRawSupported() {
        boolean rawSupported = false;
        int[] modes = getCharacteristics().get(CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES);
        for (int mode : modes) {
            if (mode == CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES_RAW) {
                rawSupported = true;
                break;
            }
        }
        return rawSupported;
    }

    @Override
    public boolean isLegacyLocked() {
        Integer level = getCharacteristics().get(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL);
        KLog.d(TAG,"isLegacyLocked: INFO_SUPPORTED_HARDWARE_LEVEL:" + level);
        return level != null && level == CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LEGACY;
    }

    @Override
    public Size[] getPreviewSize(Class klass) {
        return mCameraInfo.getPreviewSize(klass);
    }

    @Override
    public Size[] getPictureSize(int format) {
        return mCameraInfo.getPictureSize(format);
    }

    @Override
    public int getSensorOrientation() {
        return mCameraInfo.getSensorOrientation();
    }

    @Override
    public int getValidAFMode(int targetMode) {
        int[] allAFMode = getCharacteristics().get(CameraCharacteristics.CONTROL_AF_AVAILABLE_MODES);
        for (int mode : allAFMode) {
            if (mode == targetMode) {
                return targetMode;
            }
        }
        KLog.d(TAG,"not support af mode:" + targetMode + " use mode:" + allAFMode[0]);
        return allAFMode[0];
    }

    @Override
    public int getValidAntiBandingMode(int targetMode) {
        int[] allABMode = getCharacteristics().get(CameraCharacteristics.CONTROL_AE_AVAILABLE_ANTIBANDING_MODES);
        for (int mode : allABMode) {
            if (mode == targetMode) {
                return targetMode;
            }
        }
        KLog.d(TAG,"not support anti banding mode:" + targetMode
                + " use mode:" + allABMode[0]);
        return allABMode[0];
    }

    @Override
    public boolean isMeteringSupport(boolean focusArea) {
        Integer regionNum;
        if (focusArea) {
            regionNum = getCharacteristics().get(CameraCharacteristics.CONTROL_MAX_REGIONS_AF);
        } else {
            regionNum = getCharacteristics().get(CameraCharacteristics.CONTROL_MAX_REGIONS_AE);
        }
        if (regionNum == null) {
            regionNum = 0;
        }
        return regionNum > 0;
    }

    @Override
    public float getMinimumDistance() {
        Float distance = getCharacteristics().get(
                CameraCharacteristics.LENS_INFO_MINIMUM_FOCUS_DISTANCE);
        if (distance == null) {
            return 0;
        }
        return distance;
    }

    @Override
    public boolean isFlashSupport() {
        Boolean support = getCharacteristics().get(CameraCharacteristics.FLASH_INFO_AVAILABLE);
        return support != null && support;
    }

    @Override
    public boolean canTriggerAf() {
        int[] allAFMode = getCharacteristics().get(CameraCharacteristics.CONTROL_AF_AVAILABLE_MODES);
        return  allAFMode != null && allAFMode.length > 1;
    }

    @Override
    public Range<Integer> getEvRange() {
        return internalGetEvRange(getCharacteristics());
    }

    @Override
    public Range<Integer> getEvRange(CameraID cameraID) {
        return internalGetEvRange(getCharacteristicsForCameraId(cameraID));
    }

    private Range<Integer> internalGetEvRange(CameraCharacteristics characteristics) {
        return characteristics.get(CameraCharacteristics.CONTROL_AE_COMPENSATION_RANGE);
    }

    @Override
    public Range<Float> getFocusRange() {
        return internalGetFocusRange(getCharacteristics());
    }

    @Override
    public Range<Float> getFocusRange(CameraID cameraID) {
        return internalGetFocusRange(getCharacteristicsForCameraId(cameraID));
    }

    private Range<Float> internalGetFocusRange(CameraCharacteristics characteristics) {
        Float minFocus = characteristics.get(CameraCharacteristics.LENS_INFO_MINIMUM_FOCUS_DISTANCE);
        Float maxFocus = characteristics.get(CameraCharacteristics.LENS_INFO_HYPERFOCAL_DISTANCE);
        return new Range<>(minFocus, maxFocus);
    }


    /**
     * 在这里拿到的值就是zoom 范围
     *  1 ~ MaxZoom
     * @return max zoom size
     */
    @Override
    public int getMaxZoom() {
        return MAX_ZOOM_VALUE;
    }

    @Override
    public int getSensorMaxZoom() {
        return internalGetMaxZoom(getCharacteristics());
    }

    @Override
    public Size getSensorPixelArraySize() {
        return internalGetSensorPixelArraySize(getCharacteristics());
    }

    @Override
    public Rect getSensorActiveArraySize() {
        return internalGetSensorActiveArraySize(getCharacteristics());
    }

    private Size internalGetSensorPixelArraySize(CameraCharacteristics characteristics) {
        return characteristics.get(CameraCharacteristics.SENSOR_INFO_PIXEL_ARRAY_SIZE);
    }


    private Rect internalGetSensorActiveArraySize(CameraCharacteristics characteristics) {
        return characteristics.get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE);
    }

    private int internalGetMaxZoom(CameraCharacteristics characteristics) {
        Float maxZoom = characteristics.get(CameraCharacteristics.SCALER_AVAILABLE_MAX_DIGITAL_ZOOM);
        if (maxZoom == null) {
            maxZoom = 1f;
        }
        return maxZoom.intValue();
    }

    @Override
    public Rect getActiveArraySize() {
        return getCharacteristics().get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE);
    }

    @Override
    public Rect getActiveArraySize(CameraID cameraID) {
        return getCharacteristicsForCameraId(cameraID).get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE);
    }

    @Override
    public Integer getLensFacing() {
        return getCharacteristics().get(CameraCharacteristics.LENS_FACING);
    }

    @Override
    public CameraCharacteristics getCharacteristics() {
        return mCameraInfo.getCharacteristics();
    }

    private CameraCharacteristics getCharacteristicsForCameraId(CameraID cameraID) {
        return CameraInfoHelper.getInstance().getCameraInfo(cameraID.ID).getCharacteristics();
    }
}
