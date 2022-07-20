package com.sgf.kcamera.business.state;

import com.sgf.kcamera.KParams;
import com.sgf.kcamera.business.session.CameraSessionWrapper;

import io.reactivex.Observable;

public class CloseState extends BaseCameraState {

    public CloseState(CameraStateMachine stateMachine, CameraSessionWrapper cameraSessionWrapper) {
        super(stateMachine, cameraSessionWrapper);
    }

    @Override
    public Observable<KParams> closeCamera(KParams closeParams) {
        return mCameraSessionWap.onCloseCamera(closeParams).doOnNext(params ->{
            onChangeState();
        });
    }
}
