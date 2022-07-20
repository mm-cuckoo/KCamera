package com.sgf.kcamera.business.state;

import com.sgf.kcamera.KParams;
import com.sgf.kcamera.business.session.CameraSessionWrapper;

import io.reactivex.Observable;

/**
 * Camera 状态切换状态机
 */
public class CameraStateMachine implements IBusinessState{
    private CameraState mCurrentState;
    public CameraStateMachine(CameraSessionWrapper cameraSessionWrapper) {
        OpenState openState = new OpenState(this, cameraSessionWrapper);
        PreviewState previewState = new PreviewState(this, cameraSessionWrapper);
        CaptureState captureState = new CaptureState(this,cameraSessionWrapper);
        CloseState closeState = new CloseState(this, cameraSessionWrapper);

        openState.setNextState(previewState, closeState);
        previewState.setNextState(captureState, closeState);
        captureState.setNextState(previewState, closeState);
        closeState.setNextState(openState);
        mCurrentState = openState;
    }

    @Override
    public Observable<KParams> openCamera(KParams openParams) {
        return mCurrentState.openCamera(openParams);
    }

    @Override
    public Observable<KParams> startPreview(KParams previewParams) {
        return mCurrentState.startPreview(previewParams);
    }

    @Override
    public Observable<KParams> capture(KParams captureParams) {
        return mCurrentState.capture(captureParams);
    }

    @Override
    public Observable<KParams> configCamera(KParams configParams) {
        return mCurrentState.configCamera(configParams);
    }

    @Override
    public Observable<KParams> closeCamera(KParams closeParams) {
        return mCurrentState.closeCamera(closeParams);
    }

    void nextState(CameraState nextState) {
        this.mCurrentState = nextState;
    }

    CameraState getState() {
        return this.mCurrentState;
    }
}
