package com.sgf.kgl.camera;

import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.util.AttributeSet;
import android.util.Size;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.sgf.kcamera.log.KLog;

public class CameraGLView extends GLView {

    private int mRatioWidth = 0;
    private int mRatioHeight = 0;
    private Point screenPoint;
    public CameraGLView(@NonNull Context context) {
        this(context, null);
    }

    public CameraGLView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CameraGLView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        screenPoint = new Point();
        ((Activity)context).getWindowManager().getDefaultDisplay().getSize(screenPoint);
    }

//    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        if (0 == mRatioWidth || 0 == mRatioHeight) {
            setMeasuredDimension(width, height);
        } else {
            Size dstSize = getScalingSize(new Size(mRatioWidth, mRatioHeight), new Size(screenPoint.x, screenPoint.y));
            setMeasuredDimension(dstSize.getWidth() / 4 * 3, dstSize.getHeight() / 4 * 3);
        }

    }

    public void setAspectRatio(int width, int height) {
        if (width < 0 || height < 0) {
            throw new IllegalArgumentException("Size cannot be negative.");
        }
        mRatioWidth = width;
        mRatioHeight = height;
        KLog.d("=====>width : " + width + " height:" + height);
        requestLayout();
    }

    public void onResume() {
        super.onResume();
        stopDrawFrame();
    }

    public void onPause() {
        stopDrawFrame();
        super.onPause();
    }

    private Size getScalingSize(Size scalingSize, Size maxSize) {

        double dstW = maxSize.getWidth();
        double dstH = (double) maxSize.getWidth() * (double)scalingSize.getHeight() / (double)scalingSize.getWidth();

        if (dstH > maxSize.getHeight()) {
            dstH = maxSize.getHeight();
            dstW = (double)scalingSize.getWidth() * (double)maxSize.getHeight() / (double)scalingSize.getHeight();
        }
        return new Size((int) dstW, (int) dstH);
    }
}
