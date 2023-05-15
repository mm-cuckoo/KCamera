package com.sgf.kcamera.business.session;

import android.graphics.Rect;

import com.sgf.kcamera.camera.info.CameraInfoManager;
import com.sgf.kcamera.log.KLog;

public class ZoomHelper {
    private final CameraInfoManager mCameraInfoManager;
    public static final int ZOOM_STEP_SIZE = 100;
    private Rect mZoomRect;
    private int mStepW;
    private int mStepH;

    public ZoomHelper(CameraInfoManager manager) {
        this.mCameraInfoManager = manager;
    }

    public void init() {
        float maxZoomValue = mCameraInfoManager.getMaxZoom();
        mZoomRect = mCameraInfoManager.getActiveArraySize();

        int minW = (int) (mZoomRect.width() / maxZoomValue);
        int minH = (int) (mZoomRect.height() / maxZoomValue);
        int difW = mZoomRect.width() - minW;
        int difH = mZoomRect.height() - minH;

        mStepW = difW / ZOOM_STEP_SIZE;
        mStepH = difH / ZOOM_STEP_SIZE;
    }

    public Rect getZoomRect(float zoomValue) {
        if (zoomValue > 100) {
            zoomValue = 100;
        }

        if (zoomValue < 0) {
            zoomValue = 0;
        }

        int cropW = mStepW * (int)zoomValue;
        int cropH = mStepH *(int)zoomValue;
        cropW = cropW / 2;
        cropH = cropH / 2;
        KLog.d("zoom value3 : --> cropW:" + cropW  + "  cropH:" + cropH);
        Rect zoom = new Rect(cropW, cropH, mZoomRect.width() - cropW, mZoomRect.height() - cropH);
        KLog.i("zoom value :" + zoomValue  + "  base zoomRect:" + mZoomRect  +" zoom rect:" + zoom);
        return zoom;
    }
}
