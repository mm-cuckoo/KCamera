package com.sgf.kcamera.business.session;

import android.hardware.camera2.CameraAccessException;

import com.sgf.kcamera.KParams;

import io.reactivex.Observable;

/**
 * 捕获图像session 管理
 */
public interface CaptureSessionManager {
    Observable<KParams> onOpenCamera(KParams openParams);

    Observable<KParams> onStartPreview(KParams startParams);

    Observable<KParams> onCreatePreviewSession(KParams createSessionParams) throws CameraAccessException;

    Observable<KParams> onRepeatingRequest(KParams requestParams);

    Observable<KParams> onPreviewRepeatingRequest(KParams requestParams) throws CameraAccessException;

    Observable<KParams> capture(KParams captureParams);

    Observable<KParams> close(KParams closeParams);
}
