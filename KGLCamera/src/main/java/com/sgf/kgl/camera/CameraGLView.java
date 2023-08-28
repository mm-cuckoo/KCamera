package com.sgf.kgl.camera;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Point;
import android.util.AttributeSet;
import android.util.Size;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.sgf.kcamera.log.KLog;

public class CameraGLView extends GLView {

    private final Point screenPoint;
    private Size mCameraPreviewSize;
    public CameraGLView(@NonNull Context context) {
        this(context, null);
    }

    public CameraGLView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CameraGLView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        screenPoint = new Point();
        ((Activity)context).getWindowManager().getDefaultDisplay().getRealSize(screenPoint);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        if (mCameraPreviewSize == null) {
            setMeasuredDimension(width, height);
        } else {
            Size dstSize = getScalingSize(getCameraPreviewSize(), getReferenceSize());
            KLog.d("onMeasure:dstSize:" + dstSize);
            setMeasuredDimension(dstSize.getWidth(), dstSize.getHeight());
        }

    }

    public void setCameraPreview(int width, int height) {
        if (width < 0 || height < 0) {
            throw new IllegalArgumentException("Size cannot be negative.");
        }
        mCameraPreviewSize = new Size(width, height);
        KLog.d("=====>width : " + width + " height:" + height);
        requestLayout();
    }

    public void onResume() {
        super.onResume();
        startDrawFrame();
    }

    public void onPause() {
        stopDrawFrame();
        super.onPause();
    }

    private Size getReferenceSize() {
        int screenW = screenPoint.x;
        int screenH = screenPoint.y;
        return new Size(screenW, screenH);
    }
    private Size getCameraPreviewSize() {
        if (getOrientation() == Configuration.ORIENTATION_PORTRAIT) {
            return new Size(mCameraPreviewSize.getHeight(), mCameraPreviewSize.getWidth());
        }

        return mCameraPreviewSize;
    }

    private Size getScalingSize(Size previewSize, Size referenceSize) {

        int refW = referenceSize.getWidth();
        int refH = referenceSize.getHeight();
        double dstW = refW;
        double dstH = (double) refW * (double)previewSize.getHeight() / (double)previewSize.getWidth();
        KLog.d("getScalingSize-->dstW:"  + dstW + "  dstH:" + dstH);

        if (dstH > refH) {
            dstH = refH;
            dstW = (double)previewSize.getWidth() * (double)refH / (double)previewSize.getHeight();
        }
        KLog.d("getScalingSize-r->dstW:"  + dstW + "  dstH:" + dstH);
        KLog.d("getScalingSize-->previewSize::" + previewSize  + "  referenceSize::" + referenceSize);
        return new Size((int) dstW, (int) dstH);
    }
}
