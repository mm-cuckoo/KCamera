package com.sgf.kcamera.business.capture;

import com.sgf.kcamera.KParams;
import com.sgf.kcamera.business.session.CameraSessionWrapper;
import com.sgf.kcamera.business.session.CaptureSessionManager;
import com.sgf.kcamera.business.session.CaptureSessionManagerImpl;
import com.sgf.kcamera.business.state.CameraStateMachine;
import com.sgf.kcamera.camera.session.CameraSessionManager;
import com.sgf.kcamera.log.KLog;
import com.sgf.kcamera.utils.WorkerHandlerManager;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.functions.Function;

public class CaptureBusinessImpl implements CaptureBusiness {


    private final CameraStateMachine mStateMachine;

    public CaptureBusinessImpl(CameraSessionManager sessionManager) {
        CaptureSessionManager captureSessionManager = new CaptureSessionManagerImpl(sessionManager);
        CameraSessionWrapper mCameraSessionWrapper = new CameraSessionWrapper(captureSessionManager);
        mStateMachine = new CameraStateMachine(mCameraSessionWrapper);
    }

    public Observable<KParams> openCamera(KParams openParams) {
        KLog.d("open camera request ===>params:" + openParams);
        return mStateMachine.openCamera(openParams)
                .flatMap((Function<KParams, ObservableSource<KParams>>) openResultParams -> {
            return mStateMachine.startPreview(openResultParams);
        }).subscribeOn(WorkerHandlerManager.getScheduler(WorkerHandlerManager.Tag.T_TYPE_BUSINESS));
    }

    @Override
    public Observable<KParams> configCamera(KParams configParams) {
        return mStateMachine.configCamera(configParams)
                .subscribeOn(WorkerHandlerManager.getScheduler(WorkerHandlerManager.Tag.T_TYPE_BUSINESS));
    }

    @Override
    public Observable<KParams> capture(KParams captureParams) {
        return mStateMachine.capture(captureParams)
                .subscribeOn(WorkerHandlerManager.getScheduler(WorkerHandlerManager.Tag.T_TYPE_BUSINESS));
    }

    @Override
    public Observable<KParams> closeCamera(KParams closeParams) {
        return mStateMachine.closeCamera(closeParams)
                .subscribeOn(WorkerHandlerManager.getScheduler(WorkerHandlerManager.Tag.T_TYPE_BUSINESS));
    }

}
