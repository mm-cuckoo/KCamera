package com.sgf.kcamera.business.session;

import android.graphics.Rect;
import android.hardware.camera2.params.MeteringRectangle;
import android.util.Pair;
import android.util.Size;

import androidx.annotation.NonNull;

import com.sgf.kcamera.log.KLog;

public class FocusHelper {

    private static final String TAG = "FocusHelper";

    private static final int AF_AREA = 400;
    private static final int AF_AREA_WEIGHT = 1000;
    private static final int AE_AREA = 500;
    private static final int AE_AREA_WEIGHT = 1000;
    private int mPreviewOrientation;
    private Size mPreviewSize;
    private Rect mSensorPreviewRect;
    private Rect mZoomRect;

    public void init(@NonNull Size previewSize) {
        this.mPreviewSize = previewSize;
    }

    public void setZoomRect(@NonNull Rect zoomRect) {
        mZoomRect = zoomRect;

        int sensorW = mZoomRect.width();
        int sensorH = (int) (mZoomRect.width() * mPreviewSize.getHeight() / (float)mPreviewSize.getWidth());

        if (sensorH > mZoomRect.height()) {
            sensorW = (int) (mZoomRect.height() * mPreviewSize.getWidth() / (float) mPreviewSize.getHeight());
            sensorH = mZoomRect.height();
        }

        mSensorPreviewRect = new Rect(0, 0, sensorW, sensorH);
        KLog.d(TAG,"setZoomRect:zoomRect:" + zoomRect  + "  mSensorPreviewRect:" + mSensorPreviewRect);
    }

    public void setPreviewOrientation(int orientation) {
        mPreviewOrientation = orientation;
        KLog.d(TAG,"setOrientation:orientation:" + orientation);
    }

    public MeteringRectangle getAFArea(Pair<Float, Float> touchXY, Size touchViewSize) {
        return calcTapAreaForCamera2(touchXY.first, touchXY.second,touchViewSize, AF_AREA, AF_AREA_WEIGHT);
    }

    public MeteringRectangle getAEArea(Pair<Float, Float> touchXY, Size touchViewSize) {
        return calcTapAreaForCamera2(touchXY.first, touchXY.second, touchViewSize, AE_AREA, AE_AREA_WEIGHT);
    }

    private MeteringRectangle calcTapAreaForCamera2(float touchX, float touchY, Size touchViewSize , int areaSize, int weight) {

        Size tvSize = touchViewSize;
        float tX = touchX;
        float tY = touchY;
        if (mPreviewOrientation == 0) {
            tvSize = new Size(touchViewSize.getHeight(), touchViewSize.getWidth());
            tX = touchY;
            tY = touchViewSize.getWidth() - touchX;
        } else if (mPreviewOrientation == 180) {
            tvSize = new Size(touchViewSize.getHeight(), touchViewSize.getWidth());
            tX = touchViewSize.getHeight() -  touchY;
            tY = touchX;
        } else if (mPreviewOrientation == 270) {
            tX = touchViewSize.getWidth() - touchX;
            tY = touchViewSize.getHeight() - touchY;
        }

        float scaleW = mSensorPreviewRect.width() / (float) tvSize.getWidth();
        float scaleH = mSensorPreviewRect.height() / (float) tvSize.getHeight();

        float touchPX = scaleW * tX;
        float touchPY = scaleH * tY;

        KLog.d(TAG,"mZoomRect:" + mZoomRect + "   touchViewSize:" + touchViewSize + "  tvSize:" + tvSize + "  mSensorPreviewRect:" + mSensorPreviewRect);
        KLog.d(TAG,"scaleW:" + scaleW  + "  scaleH:" + scaleH  + "  tX：" + tX + "  tY:" + tY + "  touchPX:" + touchPX + "  touchPY:" + touchPY  + "  touchX:" + touchX  +  "   touchY:" + touchY);


        int left = clamp((int) touchPX - areaSize / 2,
                mSensorPreviewRect.left, mSensorPreviewRect.right - areaSize);
        int top = clamp((int) touchPY - areaSize / 2,
                mSensorPreviewRect.top, mSensorPreviewRect.bottom - areaSize);
        Rect focusRect = new Rect();



        focusRect.left = mZoomRect.left + left;
        focusRect.top = mZoomRect.top + top;
        focusRect.right = mZoomRect.left + left + areaSize;
        focusRect.bottom = mZoomRect.top + top + areaSize;

        KLog.i(TAG,"focusRect==>" + focusRect  + "  left:" + left + "  top:" + top);

        return new MeteringRectangle(focusRect, weight);
    }

    private int clamp(int x, int min, int max) {
        if (x > max) {
            return max;
        }
        if (x < min) {
            return min;
        }
        return x;
    }
}
