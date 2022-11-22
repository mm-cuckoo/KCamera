package com.sgf.kcamera.camera.session;

import com.sgf.kcamera.utils.RetryWithDelay;

public class CreateSessionErrorRetry extends RetryWithDelay {
    public CreateSessionErrorRetry(int retryMaxCount, int retryDelayMillis) {
        super(retryMaxCount, retryDelayMillis);
    }

    @Override
    public boolean isTry(int code) {
        return true;
    }
}
