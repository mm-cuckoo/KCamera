package com.sgf.kcamera.camera.info;

import android.graphics.Rect;
import android.hardware.camera2.CameraCharacteristics;
import android.util.Range;
import android.util.Size;
import android.util.SizeF;

import com.sgf.kcamera.CameraID;


public interface CameraInfoManager {

    void initCameraInfo(CameraID cameraID);

    boolean isAutoFocusSupported();

    boolean isRawSupported();

    boolean isLegacyLocked();

    int getSensorOrientation();

    int getValidAFMode(int targetMode);

    int getValidAntiBandingMode(int targetMode);

    boolean isMeteringSupport(boolean focusArea);

    float getMinimumDistance();

    boolean isFlashSupport();

    boolean canTriggerAf();

    Size[] getPictureSize(int format);

    Size[] getPreviewSize(Class cls);

    Range<Integer> getEvRange();

    Range<Integer> getEvRange(CameraID cameraID);

    Range<Float> getFocusRange();

    Range<Float> getFocusRange(CameraID cameraID);

    int getMaxZoom();

    int getSensorMaxZoom();

    Size getSensorPixelArraySize();

    Rect getSensorActiveArraySize();

    Rect getActiveArraySize();

    Rect getActiveArraySize(CameraID cameraID);

    CameraCharacteristics getCharacteristics();

}
