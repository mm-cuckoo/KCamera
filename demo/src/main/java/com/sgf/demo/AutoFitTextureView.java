package com.sgf.demo;

import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.util.AttributeSet;
import android.util.Size;
import android.view.TextureView;

import com.sgf.kcamera.log.KLog;

/**
 * A {@link TextureView} that can be adjusted to a specified aspect ratio.
 */
public class AutoFitTextureView extends TextureView {

    private static final String TAG = "AutoFitTextureView";
    private int mRatioWidth = 0;
    private int mRatioHeight = 0;
    private Point screenPoint;

    public AutoFitTextureView(Context context) {
        this(context, null);
    }

    public AutoFitTextureView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AutoFitTextureView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        screenPoint = new Point();
        ((Activity)context).getWindowManager().getDefaultDisplay().getSize(screenPoint);
    }

    /**
     * Sets the aspect ratio for this view. The size of the view will be measured based ON the ratio
     * calculated from the parameters. Note that the actual sizes of parameters don't matter, that
     * is, calling setAspectRatio(2, 3) and setAspectRatio(4, 6) make the same result.
     *
     * @param width  Relative horizontal size
     * @param height Relative vertical size
     */
    public void setAspectRatio(int width, int height) {
        if (width < 0 || height < 0) {
            throw new IllegalArgumentException("Size cannot be negative.");
        }
        setRotation(270);
        mRatioWidth = width;
        mRatioHeight = height;
        KLog.d(TAG,"=====>width : " + width + " height:" + height);
        requestLayout();
    }

//    @Override
//    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
//        int width = MeasureSpec.getSize(widthMeasureSpec);
//        int height = MeasureSpec.getSize(heightMeasureSpec);
//        if (0 == mRatioWidth || 0 == mRatioHeight) {
//            setMeasuredDimension(width, height);
//        } else {
//            Size dstSize = getScalingSize(new Size(mRatioWidth, mRatioHeight), new Size(screenPoint.x, screenPoint.y));
//            setMeasuredDimension(dstSize.getWidth(), dstSize.getHeight());
//        }
//    }
//
//    private Size getScalingSize(Size scalingSize, Size maxSize) {
//
//        double dstW = maxSize.getWidth();
//        double dstH = (double) maxSize.getWidth() * (double)scalingSize.getHeight() / (double)scalingSize.getWidth();
//
//        if (dstH > maxSize.getHeight()) {
//            dstH = maxSize.getHeight();
//            dstW = (double)scalingSize.getWidth() * (double)maxSize.getHeight() / (double)scalingSize.getHeight();
//        }
//        return new Size((int) dstW, (int) dstH);
//    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        if (0 == mRatioWidth || 0 == mRatioHeight) {
            setMeasuredDimension(width, height);
        } else {
            if (width < height * mRatioWidth / mRatioHeight) {
                setMeasuredDimension(width, width * mRatioHeight / mRatioWidth);
            } else {
                setMeasuredDimension(height * mRatioWidth / mRatioHeight, height);
            }
        }
    }
}
