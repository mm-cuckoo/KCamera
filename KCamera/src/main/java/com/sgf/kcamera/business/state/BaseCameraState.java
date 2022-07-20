package com.sgf.kcamera.business.state;

import com.sgf.kcamera.KParams;
import com.sgf.kcamera.business.session.CameraSessionWrapper;
import com.sgf.kcamera.log.KLog;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;

abstract class BaseCameraState implements CameraState {

    final CameraStateMachine mStateMachine;
    final CameraSessionWrapper mCameraSessionWap;

    protected CameraState mNextState;
    protected CameraState mCloseState;
    public BaseCameraState(CameraStateMachine stateMachine, CameraSessionWrapper cameraSessionWrapper) {
        this.mStateMachine = stateMachine;
        this.mCameraSessionWap = cameraSessionWrapper;
    }

    public void setNextState(CameraState nextState, CameraState closeState) {
        this.mNextState = nextState;
        this.mCloseState = closeState;
    }

    public void setNextState(CameraState nextState) {
        this.mNextState = nextState;
    }

    void onChangeState() {
        if (isActive()) {
            onChangeState(mNextState);
        } else {
            KLog.i("camera state change , current state :" + mStateMachine.getState().getClass().getSimpleName());
        }
    }

    void onChangeState(CameraState state) {
        mStateMachine.nextState(state);
    }

    boolean isActive() {
        return this.equals(mStateMachine.getState());
    }

    void onStateMoveToClose() {
        onChangeState(mCloseState);
    }

    @Override
    public Observable<KParams> openCamera(KParams openParams) {
        return null;
    }

    @Override
    public Observable<KParams> startPreview(KParams previewParams) {
        return null;
    }

    @Override
    public Observable<KParams> capture(KParams captureParams) {
        return Observable.create(new ObservableOnSubscribe<KParams>() {
            @Override
            public void subscribe(ObservableEmitter<KParams> emitter) throws Exception {
                KParams params = new KParams();
                params.put(KParams.Key.CAPTURE_STATE, KParams.Value.CAPTURE_STATE.CAPTURE_FAIL);
                emitter.onNext(new KParams());
            }
        });
    }

    @Override
    public Observable<KParams> configCamera(KParams captureParams) {
        return null;
    }

    @Override
    public Observable<KParams> closeCamera(KParams closeParams) {
        return null;
    }
}
