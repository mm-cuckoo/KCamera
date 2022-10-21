package com.sgf.kcamera.camera.session;

import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CaptureRequest;

import androidx.annotation.NonNull;

import com.sgf.kcamera.KParams;
import com.sgf.kcamera.camera.device.KCameraDevice;
import com.sgf.kcamera.log.KLog;
import com.sgf.kcamera.surface.SurfaceManager;
import com.sgf.kcamera.utils.RetryWithDelay;
import com.sgf.kcamera.utils.WorkerHandlerManager;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.functions.Function;

/**
 * Camera Session 操作实例
 *
 * 在这个实例中会同时管理Camera Device ，在该实例中包含所有camera 的操作
 */
public class CameraSessionImpl implements CameraSession {
    private final KCameraDevice mKCameraDevice;
    private CameraCaptureSession mCameraSession;
    private CameraDevice mCameraDevice;
    private volatile String mCameraId;

    CameraSessionImpl(KCameraDevice cameraDevice) {
        mKCameraDevice = cameraDevice;
    }

    @Override
    public Observable<KParams> onOpenCamera(final KParams openParams) {
        mCameraId = openParams.get(KParams.Key.CAMERA_ID);
        KLog.d("open camera device ===> camera id:" + mCameraId);
        return mKCameraDevice.openCameraDevice(openParams).map(openResult -> {
            // 打开Camera成功后获取Camera Device
            mCameraDevice = openResult.get(KParams.Key.CAMERA_DEVICE);
            // 打开时的签名信息， 该信息用于关闭camera device 时校验使用
//            mOpenCameraSign = openResult.get(KParams.Key.OPEN_CAMERA_SIGN, 0L);
            return openResult;
            // 如果打开失败会在此处进出3次重试，每次间隔300毫秒
        }).subscribeOn(mKCameraDevice.getCameraScheduler())
                .retryWhen(new RetryWithDelay(3, 500));
    }

    @Override
    public CaptureRequest.Builder onCreateRequestBuilder(int templateType) throws CameraAccessException {
        return mCameraDevice.createCaptureRequest(templateType);
    }
    public Observable<KParams> onCreateCaptureSession(final KParams captureParams) {
        KLog.d("onCreateCaptureSession: captureParams：" + captureParams);
        final SurfaceManager surfaceManager = captureParams.get(KParams.Key.SURFACE_MANAGER);
        return Observable.create((ObservableOnSubscribe<KParams>) emitter -> {
            try {
                mCameraDevice.createCaptureSession(surfaceManager.getTotalSurface(), new CameraCaptureSession.StateCallback() {
                    @Override
                    public void onConfigured(@NonNull CameraCaptureSession session) {
                        KLog.i("onConfigured: create session success");
                        mCameraSession = session;
                        emitter.onNext(captureParams);
                    }

                    @Override
                    public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                        KLog.i("onConfigureFailed: create session fail");
                        //emitter.onError(new KException("Create Preview Session failed  "));
                    }
                }, null);
            } catch (Exception e) {
//                emitter.onError(new KException("Create Preview Session Exception", KCode.ERROR_CODE_SESSION_CREATE_EXCEPTION));
                KLog.e("onCreateCaptureSession :  Exception :" + e );
                e.printStackTrace();
            }
        }).subscribeOn(mKCameraDevice.getCameraScheduler())
                .retryWhen(new RetryWithDelay(3, 5000));
    }

    @Override
    public int onRepeatingRequest(KParams requestParams) throws CameraAccessException {
        KLog.d("onRepeatingRequest =>" + requestParams);
        final CaptureRequest.Builder requestBuilder = requestParams.get(KParams.Key.REQUEST_BUILDER);
        final CameraCaptureSession.CaptureCallback captureCallback = requestParams.get(KParams.Key.CAPTURE_CALLBACK);
        return mCameraSession.setRepeatingRequest(requestBuilder.build(), captureCallback, null);
    }

    @Override
    public Observable<KParams> onClose(KParams closeParams) {
        closeParams.put(KParams.Key.CAMERA_ID, mCameraId);
        return mKCameraDevice.closeCameraDevice(closeParams).map(new Function<KParams, KParams>() {
            @Override
            public KParams apply(KParams params) {
                int closeResult = params.get(KParams.Key.CLOSE_CAMERA_STATUS, KParams.Value.CLOSE_STATE.DEVICE_NULL);
                KLog.i("close camera , mCameraId:" + mCameraId  +
                        "  closeResult:" + closeResult);

                if (closeResult == KParams.Value.CLOSE_STATE.DEVICE_CLOSED) {
                    if (mCameraSession != null) {
                        mCameraSession.close();
                        mCameraSession = null;
                    }
                }
                return params;
            }
        }).subscribeOn(mKCameraDevice.getCameraScheduler());
    }

    @Override
    public void capture(KParams captureParams) throws CameraAccessException {
        KLog.i("capture =>" + captureParams);
        CaptureRequest.Builder requestBuilder = captureParams.get(KParams.Key.REQUEST_BUILDER);
        CameraCaptureSession.CaptureCallback captureCallback = captureParams.get(KParams.Key.CAPTURE_CALLBACK);
        mCameraSession.capture(requestBuilder.build(), captureCallback, WorkerHandlerManager.getHandler(WorkerHandlerManager.Tag.T_TYPE_CAMERA));
    }

    @Override
    public void stopRepeating() throws CameraAccessException {
        mCameraSession.stopRepeating();
        mCameraSession.abortCaptures();
    }
}
