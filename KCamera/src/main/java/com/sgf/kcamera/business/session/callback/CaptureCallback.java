package com.sgf.kcamera.business.session.callback;

import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CaptureFailure;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;

import androidx.annotation.NonNull;

import com.sgf.kcamera.KParams;
import com.sgf.kcamera.camera.session.CameraSession;
import com.sgf.kcamera.log.KLog;

import io.reactivex.ObservableEmitter;

public class CaptureCallback extends CameraCaptureSession.CaptureCallback {

    private CaptureRequest.Builder mCaptureBuilder;
    private CameraSession mCameraSession;
    private ObservableEmitter<KParams> mEmitter;
    public CaptureCallback(@NonNull CameraSession cameraSession) {
        this.mCameraSession = cameraSession;
    }

    public void prepareCapture(@NonNull CaptureRequest.Builder captureBuilder,
                               @NonNull ObservableEmitter<KParams> emitter) {
        this.mCaptureBuilder = captureBuilder;
        this.mEmitter = emitter;
    }

    public void capture() {
        sendStillPictureRequest();
    }

    @Override
    public void onCaptureStarted(@NonNull CameraCaptureSession session,
                                 @NonNull CaptureRequest request, long timestamp, long frameNumber) {
        super.onCaptureStarted(session, request, timestamp, frameNumber);
        KLog.i("onCaptureStarted====>");
        KParams captureParams = new KParams();
        captureParams.put(KParams.Key.CAPTURE_STATE, KParams.Value.CAPTURE_STATE.CAPTURE_START);
        mEmitter.onNext(captureParams);

    }

    @Override
    public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull
            CaptureRequest request, @NonNull TotalCaptureResult result) {
        super.onCaptureCompleted(session, request, result);
        KLog.i("onCaptureCompleted====>");
        KParams params = new KParams();
        params.put(KParams.Key.CAPTURE_STATE, KParams.Value.CAPTURE_STATE.CAPTURE_COMPLETED);
        mEmitter.onNext(params);
    }

    @Override
    public void onCaptureFailed(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull CaptureFailure failure) {
        super.onCaptureFailed(session, request, failure);
        KLog.i("onCaptureFailed====>");
        KParams params = new KParams();
        params.put(KParams.Key.CAPTURE_STATE, KParams.Value.CAPTURE_STATE.CAPTURE_FAIL);
        mEmitter.onNext(params);
    }

    private void sendStillPictureRequest() {
        KLog.d("sendStillPictureRequest===>"  + mCaptureBuilder.get(CaptureRequest.JPEG_ORIENTATION));
        KParams params = new KParams();
        params.put(KParams.Key.REQUEST_BUILDER, mCaptureBuilder);
        params.put(KParams.Key.CAPTURE_CALLBACK, this);
        try {
            mCameraSession.stopRepeating();
            mCameraSession.capture(params);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

}
