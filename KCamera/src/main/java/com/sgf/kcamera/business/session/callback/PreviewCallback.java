package com.sgf.kcamera.business.session.callback;

import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;

import androidx.annotation.NonNull;

import com.sgf.kcamera.KParams;
import com.sgf.kcamera.camera.session.CameraSession;
import com.sgf.kcamera.log.KLog;

import io.reactivex.ObservableEmitter;

public class PreviewCallback extends CameraCaptureSession.CaptureCallback {

    private static final int STATE_PREVIEW                      = 1;
    private static final int STATE_CAPTURE                      = 2;
    private static final int STATE_WAITING_LOCK                 = 3;
    private static final int STATE_WAITING_PRE_CAPTURE          = 4;
    private static final int STATE_WAITING_NON_PRE_CAPTURE      = 5;

    private int mState = 0;
    private int mAFState = -1;
    private int mFlashState = -1;
    private boolean mFirstFrameCompleted = false;
    private CaptureRequest.Builder mPreviewBuilder;
    private final CameraSession mCameraSession;
    private ObservableEmitter<KParams> mEmitter;

    public PreviewCallback(@NonNull CameraSession cameraSession) {
        this.mCameraSession = cameraSession;
    }

    public void applyPreview(@NonNull CaptureRequest.Builder previewBuilder,
                             @NonNull ObservableEmitter<KParams> emitter) {
        this.mPreviewBuilder = previewBuilder;
        this.mEmitter = emitter;
        mFirstFrameCompleted = false;
        stateChange(STATE_PREVIEW);
    }

    private void stateChange(int type) {
        KLog.d("setType  mState:" + type);
        this.mState = type;

    }

    @Override
    public void onCaptureProgressed(@NonNull CameraCaptureSession session,
                                    @NonNull CaptureRequest request,
                                    @NonNull CaptureResult partialResult) {
        updateAFState(partialResult);
        updateFlashState(partialResult);
        processPreCapture(partialResult);
    }

    @Override
    public void onCaptureCompleted(@NonNull CameraCaptureSession session,
                                   @NonNull CaptureRequest request,
                                   @NonNull TotalCaptureResult result) {


        if (!mFirstFrameCompleted) {
            mFirstFrameCompleted = true;
            KParams previewParams = new KParams();
            previewParams.put(KParams.Key.PREVIEW_FIRST_FRAME, KParams.Value.OK);
            mEmitter.onNext(previewParams);
            KLog.d("preview first frame call back");
        }

        updateAFState(result);
        updateFlashState(result);
        processPreCapture(result);
    }

    private void processPreCapture(CaptureResult result) {
        switch (mState) {
            case STATE_PREVIEW: {
                // We have nothing to do when the camera preview is working normally.
                break;
            }
            case STATE_WAITING_LOCK: {
                Integer afState = result.get(CaptureResult.CONTROL_AF_STATE);
                KLog.d("STATE_WAITING_LOCK===>afState:" + afState);

                if (afState == null) {
                    runCaptureAction();
                } else if (CaptureResult.CONTROL_AF_STATE_FOCUSED_LOCKED == afState ||
                        CaptureResult.CONTROL_AF_STATE_NOT_FOCUSED_LOCKED == afState) {
                    // CONTROL_AE_STATE can be null on some devices
                    Integer aeState = result.get(CaptureResult.CONTROL_AE_STATE);
                    KLog.d("STATE_WAITING_LOCK===>aeState:" + aeState);
                    if (aeState == null || aeState == CaptureResult.CONTROL_AE_STATE_CONVERGED) {
                        stateChange(STATE_CAPTURE);
                        runCaptureAction();
                    } else {
                        triggerAECaptureSequence();
                    }
                }
                break;
            }
            case STATE_WAITING_PRE_CAPTURE: {
                // CONTROL_AE_STATE can be null on some devices
                Integer aeState = result.get(CaptureResult.CONTROL_AE_STATE);
                KLog.d("STATE_WAITING_LOCK===>aeState:" + aeState);
                if (aeState == null ||
                        aeState == CaptureResult.CONTROL_AE_STATE_PRECAPTURE ||
                        aeState == CaptureRequest.CONTROL_AE_STATE_FLASH_REQUIRED) {
                    stateChange(STATE_WAITING_NON_PRE_CAPTURE);
                }
                break;
            }
            case STATE_WAITING_NON_PRE_CAPTURE: {
                // CONTROL_AE_STATE can be null on some devices
                Integer aeState = result.get(CaptureResult.CONTROL_AE_STATE);
                KLog.d("STATE_WAITING_LOCK===>aeState:" + aeState);
                if (aeState == null || aeState != CaptureResult.CONTROL_AE_STATE_PRECAPTURE) {
                    stateChange(STATE_CAPTURE);
                    runCaptureAction();
                }
                break;
            }
        }
    }

    private void runCaptureAction() {
        KParams params = new KParams();
        params.put(KParams.Key.CAPTURE_STATE, KParams.Value.CAPTURE_STATE.CAPTURE_START);
        mEmitter.onNext(params);
    }

    private void updateAFState(CaptureResult captureResult) {
        Integer afState = captureResult.get(CaptureResult.CONTROL_AF_STATE);
        if (afState != null && afState != mAFState) {
            mAFState = afState;
            KParams afParams = new KParams();
            afParams.put(KParams.Key.AF_STATE, afState);
            mEmitter.onNext(afParams);
        }
    }

    private void updateFlashState(CaptureResult captureResult) {
        Integer flashState = captureResult.get(CaptureResult.FLASH_MODE);
        if (flashState != null && flashState != mFlashState) {
            mFlashState = flashState;
            KParams flashParams = new KParams();
            flashParams.put(KParams.Key.FLASH_STATE, flashState);
            mEmitter.onNext(flashParams);
        }
    }

    public void capture() {
        triggerAFCaptureSequence();
    }

    private void triggerAFCaptureSequence() {
        KLog.d("triggerAFCaptureSequence===>");
        mPreviewBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CaptureRequest.CONTROL_AF_TRIGGER_START);
        stateChange(STATE_WAITING_LOCK);
        KParams captureParams = new KParams();
        captureParams.put(KParams.Key.REQUEST_BUILDER, mPreviewBuilder);
        captureParams.put(KParams.Key.CAPTURE_CALLBACK, this);
        try {
            mCameraSession.capture(captureParams);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void triggerAECaptureSequence() {
        KLog.d("triggerAECaptureSequence===>");
        mPreviewBuilder.set(CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER, CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER_START);
        stateChange(STATE_WAITING_PRE_CAPTURE);
        KParams aeParams = new KParams();
        aeParams.put(KParams.Key.REQUEST_BUILDER, mPreviewBuilder);
        aeParams.put(KParams.Key.CAPTURE_CALLBACK, this);
        try {
            mCameraSession.capture(aeParams);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    public void resetPreviewState() {
        KLog.i("resetTriggerState===>");
        stateChange(STATE_PREVIEW);
        mPreviewBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CaptureRequest.CONTROL_AF_TRIGGER_IDLE);
        mPreviewBuilder.set(CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER, CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER_IDLE);
        KParams requestParams = new KParams();
        requestParams.put(KParams.Key.REQUEST_BUILDER, mPreviewBuilder);
        requestParams.put(KParams.Key.CAPTURE_CALLBACK, this);
        try {
            mCameraSession.onRepeatingRequest(requestParams);
            mCameraSession.capture(requestParams);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }
}
