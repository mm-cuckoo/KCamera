package com.sgf.kcamera.business.state;

import androidx.annotation.NonNull;

import com.sgf.kcamera.KParams;
import com.sgf.kcamera.business.session.CameraSessionWrapper;

import io.reactivex.Observable;
import io.reactivex.functions.Function;

public class OpenState extends BaseCameraState {
    public OpenState(CameraStateMachine stateMachine, CameraSessionWrapper cameraSessionWrapper) {
        super(stateMachine, cameraSessionWrapper);
    }

    @Override
    public Observable<KParams> openCamera(KParams openParams) {
        return mCameraSessionWap.onOpenCamera(openParams).map(new Function<KParams, KParams>() {
            @Override
            public KParams apply(@NonNull KParams params) throws Exception {
                onChangeState();
                return params;
            }
        });
    }

    @Override
    public Observable<KParams> closeCamera(KParams closeParams) {
        onStateMoveToClose();
        return mStateMachine.getState().closeCamera(closeParams);
    }
}
