package com.sgf.kcamera.business.session;

import android.graphics.Rect;

import com.sgf.kcamera.camera.info.CameraInfoManager;
import com.sgf.kcamera.log.KLog;

public class ZoomHelper {
    private final CameraInfoManager mCameraInfoManager;

    public ZoomHelper(CameraInfoManager manager) {
        this.mCameraInfoManager = manager;

    }

    public Rect getZoomRect(float zoomValue) {
        float maxZoomValue = mCameraInfoManager.getMaxZoom();
        Rect zoomRect = mCameraInfoManager.getActiveArraySize();
        KLog.d("zoom value :" + zoomValue  + " camera zoom rect:" + zoomRect);

        if (zoomValue > maxZoomValue) {
            zoomValue = maxZoomValue;
        }

        if (zoomValue < 1) {
            zoomValue = 1;
        }

        int minW = (int) (zoomRect.width() / maxZoomValue);
        int minH = (int) (zoomRect.height() / maxZoomValue);
        int difW = zoomRect.width() - minW;
        int difH = zoomRect.height() - minH;
        int cropW = difW / 100 * (int)zoomValue;
        int cropH = difH / 100 * (int)zoomValue;
        cropW -= cropW & 3;
        cropH -= cropH & 3;
        Rect zoom = new Rect(cropW, cropH, zoomRect.width() - cropW, zoomRect.height() - cropH);

        KLog.i("zoom value :" + zoomValue  + " zoom rect:" + zoom);
        return zoom;
    }
}
