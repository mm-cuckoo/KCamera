package com.sgf.kcamera.business.state;

import com.sgf.kcamera.KParams;
import com.sgf.kcamera.business.session.CameraSessionWrapper;
import com.sgf.kcamera.log.KLog;

import io.reactivex.Observable;

public class PreviewState extends BaseCameraState{
    public PreviewState(CameraStateMachine stateMachine, CameraSessionWrapper cameraSessionWrapper) {
        super(stateMachine, cameraSessionWrapper);
    }

    @Override
    public Observable<KParams> startPreview(KParams previewParams) {
        return mCameraSessionWap.onStartPreview(previewParams).doOnNext(resultParams -> {
            String firstFrame = resultParams.get(KParams.Key.PREVIEW_FIRST_FRAME);
            if (KParams.Value.OK.equals(firstFrame)) {
                KLog.d("preview state change");
                onChangeState();
            }
        });
    }

    @Override
    public Observable<KParams> closeCamera(KParams closeParams) {
        onStateMoveToClose();
        return mStateMachine.getState().closeCamera(closeParams);
    }
}
