package com.sgf.kcamera.camera.session;


import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CaptureRequest;
import com.sgf.kcamera.KParams;
import io.reactivex.Observable;

/**
 * Camera Session 操作实例
 *
 * 在这个实例中会同时管理Camera Device ，在该实例中包含所有camera 的操作
 */
public interface CameraSession {

    Observable<KParams> onOpenCamera(KParams openParams);

    Observable<KParams> onCreateCaptureSession(KParams captureParams);

    CaptureRequest.Builder onCreateRequestBuilder(int templateType) throws CameraAccessException;

    int onRepeatingRequest(KParams repeatParams) throws CameraAccessException;

    Observable<KParams> onClose(KParams closeParams);

    void capture(KParams captureParams) throws CameraAccessException;

    void stopRepeating() throws CameraAccessException;

}
