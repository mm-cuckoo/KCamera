package com.sgf.kcamera.business.session;

import android.graphics.Rect;
import android.graphics.RectF;
import android.hardware.camera2.params.MeteringRectangle;
import android.util.Size;

import androidx.annotation.NonNull;

import com.sgf.kcamera.camera.info.CameraInfoManager;
import com.sgf.kcamera.utils.CoordinateTransformer;

public class FocusHelper {

    private final CameraInfoManager mCameraInfoManager;
    private final Rect mFocusRect;
    private CoordinateTransformer mTransformer;
    private Rect mPreviewRect;

    public FocusHelper(CameraInfoManager manager) {
        this.mCameraInfoManager = manager;
        mFocusRect = new Rect();
    }

    public void init(@NonNull Size previewSize) {
        mPreviewRect = new Rect(0, 0, previewSize.getWidth(), previewSize.getHeight());
        mTransformer = new CoordinateTransformer(mCameraInfoManager.getCharacteristics(), rectToRectF(mPreviewRect));

    }

    public MeteringRectangle getAFArea(float touchX, float touchY) {
        return calcTapAreaForCamera2(touchX, touchY,mPreviewRect.width() / 5, 1000);
    }

    public MeteringRectangle getAEArea(float touchX, float touchY) {
        return calcTapAreaForCamera2(touchX, touchY, mPreviewRect.width() / 4, 1000);
    }

    private MeteringRectangle calcTapAreaForCamera2(float touchX, float touchY, int areaSize, int weight) {
        int left = clamp((int) touchX - areaSize / 2,
                mPreviewRect.left, mPreviewRect.right - areaSize);
        int top = clamp((int) touchY - areaSize / 2,
                mPreviewRect.top, mPreviewRect.bottom - areaSize);
        RectF rectF = new RectF(left, top, left + areaSize, top + areaSize);
        toFocusRect(mTransformer.toCameraSpace(rectF));
        return new MeteringRectangle(mFocusRect, weight);
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

    private RectF rectToRectF(Rect rect) {
        return new RectF(rect);
    }

    private void toFocusRect(RectF rectF) {
        mFocusRect.left = Math.round(rectF.left);
        mFocusRect.top = Math.round(rectF.top);
        mFocusRect.right = Math.round(rectF.right);
        mFocusRect.bottom = Math.round(rectF.bottom);
    }
}
