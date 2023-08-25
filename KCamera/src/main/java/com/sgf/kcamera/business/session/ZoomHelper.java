package com.sgf.kcamera.business.session;

import android.graphics.Rect;

import com.sgf.kcamera.camera.info.CameraInfoManager;
import com.sgf.kcamera.log.KLog;

public class ZoomHelper {
    private final CameraInfoManager mCameraInfoManager;

    private int mMaxZoomValue;
    private Rect mZoomRect;
//    private int mMinHeight;
    private int mCropStepWidth;
    public ZoomHelper(CameraInfoManager manager) {
        this.mCameraInfoManager = manager;

    }

    public void init() {
        float sensorMaxZoomValue = mCameraInfoManager.getSensorMaxZoom();
        mMaxZoomValue = mCameraInfoManager.getMaxZoom();
        mZoomRect = mCameraInfoManager.getActiveArraySize();
        int minWidth = (int) (mZoomRect.width() / sensorMaxZoomValue);// 最小zoom宽度
//        mMinHeight = (int) (mZoomRect.height() / sensorMaxZoomValue);// 最小zoom高度
        int difW = mZoomRect.width() - minWidth; // zoom 范围， 宽度
        mCropStepWidth = difW / mMaxZoomValue;
        KLog.d("init:mSensorMaxZoomValue :" + sensorMaxZoomValue + "  mMaxZoomValue:" + mMaxZoomValue + " mZoomRect:" + mZoomRect);
    }

    public Rect getZoomRect(float zoomValue) {

        if (zoomValue > mMaxZoomValue) {
            zoomValue = mMaxZoomValue;
        }

        if (zoomValue < 0) {
            zoomValue = 0;
        }

        int cropWidthSize = mCropStepWidth * (int)zoomValue;
        int cropWidth = mZoomRect.width() - cropWidthSize;
        int cropHeight = cropWidth * mZoomRect.height() / mZoomRect.width();
        int cropHeightSize = mZoomRect.height() - cropHeight;
//        KLog.d("cropWidth:" + cropWidth  + "   cropHeight:" + cropHeight);
        cropWidthSize = cropWidthSize / 2;
        cropHeightSize = cropHeightSize / 2;
//        KLog.d("w:h:" + ((float)mZoomRect.width() / (float)mZoomRect.height())   + "   --> " + ((float)cropWidth ) / ((float)cropHeight) );
//        KLog.d("cropWidthSize:" + cropWidthSize   + "   --> " + cropHeightSize );
        Rect zoom = new Rect(cropWidthSize, cropHeightSize, cropWidth + cropWidthSize, cropHeight + cropHeightSize);
        KLog.i("zoom value :" + zoomValue  + " zoom rect:" + zoom);
        return zoom;
    }
}
