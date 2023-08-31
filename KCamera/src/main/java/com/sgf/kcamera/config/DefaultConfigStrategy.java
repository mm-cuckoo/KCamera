package com.sgf.kcamera.config;

import android.util.Size;

import androidx.annotation.NonNull;

import com.sgf.kcamera.CameraID;
import com.sgf.kcamera.log.KLog;

import java.util.Arrays;

public class DefaultConfigStrategy implements ConfigStrategy {
    private static final String TAG = "DefaultConfigStrategy";
    @Override
    public Size getPreviewSize(@NonNull CameraID cameraID, @NonNull Size size, @NonNull Size[] supportSizes) {
        KLog.d(TAG,"getPreviewSize: size:" + size  + "   supportSize:" + Arrays.toString(supportSizes));
        Size resultSize = null;
        Size sizeTmp = size;
        for (Size size1 : supportSizes) {
            if (size.getWidth() == size1.getWidth()) {
                if (size.getHeight() == size1.getHeight()) {
                    resultSize = size1;
                    break;
                } else  {
                    if (Math.abs(size1.getHeight() - sizeTmp.getHeight()) < Math.abs(size.getHeight() - sizeTmp.getHeight())) {
                        sizeTmp = size1;
                    }
                }
            }
        }

        if (resultSize == null) {
            resultSize = sizeTmp;
        }

        return resultSize;
    }

    @Override
    public Size getPictureSize(@NonNull CameraID cameraID, @NonNull Size size, @NonNull Size[] supportSizes) {
        KLog.d(TAG, "getPictureSize: size:" + size  + "   supportSize:" + Arrays.toString(supportSizes));
        Size resultSize = null;
        Size sizeTmp = size;
        for (Size size1 : supportSizes) {
            if (size.getWidth() == size1.getWidth()) {
                if (size.getHeight() == size1.getHeight()) {
                    resultSize = size1;
                    break;
                } else  {
                    if (Math.abs(size1.getHeight() - sizeTmp.getHeight()) < Math.abs(size.getHeight() - sizeTmp.getHeight())) {
                        sizeTmp = size1;
                    }
                }
            }
        }

        if (resultSize == null) {
            resultSize = sizeTmp;
        }
        KLog.d(TAG,"getPictureSize: return picture size:" + resultSize);
        return resultSize;
    }

    @Override
    public int getPictureOrientation(@NonNull CameraID cameraID, int cameraSensorOrientation) {
        KLog.d(TAG,"cameraSensorOrientation: " + cameraSensorOrientation);
        return cameraSensorOrientation;
    }

    @Override
    public boolean captureCanTriggerAf() {
        return true;
    }
}
