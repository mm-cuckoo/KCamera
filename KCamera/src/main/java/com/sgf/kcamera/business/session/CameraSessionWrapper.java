package com.sgf.kcamera.business.session;

import com.sgf.kcamera.KParams;

import io.reactivex.Observable;

public class CameraSessionWrapper {
    private final CaptureSessionManager mCameraSession;

    public CameraSessionWrapper(CaptureSessionManager cameraSession) {
        this.mCameraSession = cameraSession;
    }

    public Observable<KParams> onOpenCamera(KParams openParams) {
        return mCameraSession.onOpenCamera(openParams);
    }

    public Observable<KParams> onStartPreview(KParams openParams) {
        return mCameraSession.onStartPreview(openParams);
    }

    public Observable<KParams> onCapture(KParams openParams) {
        return mCameraSession.capture(openParams);
    }

    public Observable<KParams> onConfigCamera(KParams configParams) {
        return mCameraSession.onRepeatingRequest(configParams);
    }

    public Observable<KParams> onCloseCamera(KParams closeParams) {
        return mCameraSession.close(closeParams);
    }
}
