package com.sgf.kcamera.camera.session;

import com.sgf.kcamera.KException;
import com.sgf.kcamera.utils.RetryWithDelay;

public class OpenCameraErrorRetry extends RetryWithDelay {
    public OpenCameraErrorRetry(int retryMaxCount, int retryDelayMillis) {
        super(retryMaxCount, retryDelayMillis);
    }

    @Override
    public boolean isTry(int code) {
        return  code >= KException.CODE_CAMERA_IN_USE && code <= KException.CODE_CAMERA_SERVICE;
    }
}
