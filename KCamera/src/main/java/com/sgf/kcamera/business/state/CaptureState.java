package com.sgf.kcamera.business.state;

import com.sgf.kcamera.KParams;
import com.sgf.kcamera.business.session.CameraSessionWrapper;

import io.reactivex.Observable;

public class CaptureState extends BaseCameraState{
    public CaptureState(CameraStateMachine stateMachine, CameraSessionWrapper cameraSessionWrapper) {
        super(stateMachine, cameraSessionWrapper);
    }

    @Override
    public Observable<KParams> capture(KParams captureParams) {
        return  mCameraSessionWap.onCapture(captureParams);
    }

    @Override
    public Observable<KParams> configCamera(KParams configParams) {
        return mCameraSessionWap.onConfigCamera(configParams);
    }

    @Override
    public Observable<KParams> closeCamera(KParams closeParams) {
        onStateMoveToClose();
        return mStateMachine.getState().closeCamera(closeParams);
    }
}
