package com.sgf.kcamera;


import android.content.Context;
import android.util.Pair;
import android.util.Range;
import android.util.Size;

import com.sgf.kcamera.config.ConfigWrapper;
import com.sgf.kcamera.business.capture.CaptureBusiness;
import com.sgf.kcamera.business.capture.CaptureBusinessImpl;
import com.sgf.kcamera.camera.info.CameraInfoManager;
import com.sgf.kcamera.camera.info.CameraInfoManagerImpl;
import com.sgf.kcamera.camera.session.CameraSessionManager;
import com.sgf.kcamera.camera.session.CameraSessionManagerImpl;
import com.sgf.kcamera.log.KLog;
import com.sgf.kcamera.request.PreviewRequest;
import com.sgf.kcamera.request.RepeatRequest;
import com.sgf.kcamera.surface.SurfaceManager;
import com.sgf.kcamera.utils.CameraObserver;

import io.reactivex.ObservableSource;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Function;

/**
 * Camera 操作帮助
 *
 * 通过这个对象，可以完成一个Camera ID 的所有操作
 */
public class CameraHandler {

    private final static Object OBJ = new Object();
    private final CaptureBusiness mCameraBusiness;
    private final CameraInfoManager mCameraInfoManager;
    private final ConfigWrapper mConfig;
    private final SurfaceManager mSurfaceManager;
    private CameraStateListener mCameraStateListener;
    private CameraID mCameraId;

    public CameraHandler(Context context, ConfigWrapper configWrapper) {
        CameraSessionManager sessionManager = CameraSessionManagerImpl.getInstance(context);

        mCameraBusiness = new CaptureBusinessImpl(sessionManager);
        mConfig = configWrapper;
        mCameraInfoManager = CameraInfoManagerImpl.CAMERA_INFO_MANAGER;
        mSurfaceManager = new SurfaceManager();
    }

    public final CameraID getCameraId() {
        return mCameraId;
    }

    public final synchronized void onOpenCamera(@NonNull PreviewRequest request, final CameraStateListener listener) {
        mSurfaceManager.release();
        mCameraStateListener = listener;
        mCameraId = request.getCameraId();
        final KParams openParams = new KParams();
        mSurfaceManager.setPreviewSurfaceProviderList(request.getPreviewSurfaceProviders());
        openParams.put(KParams.Key.SURFACE_MANAGER, mSurfaceManager);
        openParams.put(KParams.Key.CAMERA_ID, mCameraId.ID);
        openParams.put(KParams.Key.FLASH_STATE, request.getFlashState());
        openParams.put(KParams.Key.IMAGE_READER_PROVIDERS, request.getSurfaceProviders());

        // 切换Camera 信息管理中的 Camera 信息， 如前置camera  或 后置Camera
        mCameraInfoManager.initCameraInfo(request.getCameraId());

        // 设置预览大小
        Size previewSizeForReq = request.getPreviewSize();
        Size previewSize = mConfig.getConfig().getPreviewSize(mCameraId, previewSizeForReq, mCameraInfoManager.getPreviewSize(mSurfaceManager.getPreviewSurfaceClass()));
        openParams.put(KParams.Key.PREVIEW_SIZE, previewSize);
        mSurfaceManager.setAspectRatio(previewSize);

        // 设置图片大小
        Size pictureSizeForReq = request.getPictureSize();
        int imageFormat = request.getImageFormat();
        Size pictureSize = mConfig.getConfig().getPictureSize(mCameraId, pictureSizeForReq, mCameraInfoManager.getPictureSize(imageFormat));
        openParams.put(KParams.Key.PIC_SIZE, pictureSize);


        openParams.put(KParams.Key.ZOOM_VALUE, request.getZoom());

        KLog.d("max zoom:" + mCameraInfoManager.getMaxZoom()  + " zoom :" + request.getZoom() + " zoom area:" + mCameraInfoManager.getActiveArraySize() );

        long openTime = System.currentTimeMillis();
        mCameraBusiness.closeCamera(new KParams()).flatMap((Function<KParams, ObservableSource<KParams>>) params -> {
            int closeResult = params.get(KParams.Key.CLOSE_CAMERA_STATUS, KParams.Value.CLOSE_STATE.DEVICE_NULL);
            synchronized (OBJ) {
                if (mCameraStateListener != null) {
                    mCameraStateListener.onCameraClosed(closeResult);
                }
            }
            KLog.d("open close camera use time:" + (System.currentTimeMillis() - openTime));

            return mCameraBusiness.openCamera(openParams);
        }).subscribe(new CameraObserver<KParams>(){
            @Override
            public void onNext(@NonNull KParams resultParams) {
                Integer afState = resultParams.get(KParams.Key.AF_STATE);
                synchronized (OBJ) {
                    if (afState != null && mCameraStateListener != null) {
                        // 对焦模式发生变化
                        mCameraStateListener.onFocusStateChange(afState);
                    }
                    if (KParams.Value.OK.equals(resultParams.get(KParams.Key.PREVIEW_FIRST_FRAME)) && mCameraStateListener != null) {
                        // 第一帧图像数据返回
                        KLog.d("open camera use time:" + (System.currentTimeMillis() - openTime));
                        mCameraStateListener.onFirstFrameCallback();
                    }
                }

            }

            @Override
            public void onError(@androidx.annotation.NonNull Throwable e) {
                super.onError(e);
                if (e instanceof KException) {
                    KLog.e(e.getMessage());
                }
            }
        });
    }

    public final void onCameraRepeating(@NonNull RepeatRequest request) {
        KParams configParams = new KParams();
        Float zoomSize = request.getZoomSize();
        if (zoomSize != null) {
            // 设置zoom
            configParams.put(KParams.Key.ZOOM_VALUE, zoomSize);
        }

        Integer flash = request.getFlashState();
        if (flash != null) {
            // 设置闪光灯
            configParams.put(KParams.Key.FLASH_STATE, flash);
        }

        Integer ev = request.getEv();
        if (ev != null) {
            // 设置曝光
            configParams.put(KParams.Key.EV_SIZE, ev);
        }

        Pair<Float, Float> afTouchXy = request.getAfTouchXY();
        if (afTouchXy != null) {
            // 设置对焦区域
            configParams.put(KParams.Key.AF_TRIGGER, afTouchXy);
        }

        configParams.put(KParams.Key.RESET_FOCUS, request.isResetFocus());

        KLog.d("CameraRepeating==>" + configParams);

        mCameraBusiness.configCamera(configParams).subscribe(new CameraObserver<>());
    }

    public final synchronized void onCloseCamera() {
        mCameraId = null;
        CameraStateListener listener = mCameraStateListener;
        synchronized (OBJ) {
            mCameraStateListener = null;
        }
        KParams closeParams = new KParams();
        mCameraBusiness.closeCamera(closeParams).subscribe(new CameraObserver<KParams>() {
            @Override
            public void onNext(KParams params) {
                int closeResult = params.get(KParams.Key.CLOSE_CAMERA_STATUS, KParams.Value.CLOSE_STATE.DEVICE_NULL);
                if (listener != null) {
                    listener.onCameraClosed(closeResult);
                }
            }
        });
        mSurfaceManager.release();
    }

    public final Range<Integer> getEvRange() {
        return mCameraInfoManager.getEvRange();
    }

    public final Range<Float> getFocusRange() {
        return mCameraInfoManager.getFocusRange();
    }

    public final int getMaxZoom() {
        return mCameraInfoManager.getMaxZoom();
    }

    public final void onCapture(final CaptureStateListener listener) {

        if (mCameraId == null) {
            listener.onCaptureFailed();
            KLog.e("capture fail ===>");
            return;
        }

        KParams captureParams = new KParams();
        captureParams.put(KParams.Key.CAPTURE_CAN_TRIGGER_AF, mConfig.getConfig().captureCanTriggerAf());
        int sensorOrientation = mCameraInfoManager.getSensorOrientation();
        int picOrientation = mConfig.getConfig().getPictureOrientation(mCameraId,sensorOrientation);
        captureParams.put(KParams.Key.PIC_ORIENTATION, picOrientation); // 设置拍照图片jpeg 方向
        mCameraBusiness.capture(captureParams).subscribe(new CameraObserver<KParams>(){
            @Override
            public void onNext(@NonNull KParams resultParams) {
                KLog.i("capture result params :" + resultParams);

                if (listener == null) {
                    return;
                }
                Integer captureState = resultParams.get(KParams.Key.CAPTURE_STATE);
                if (captureState == null) {
                    listener.onCaptureFailed();
                    return;
                }
                // 拍照状态
                switch (captureState) {
                    case KParams.Value.CAPTURE_STATE.CAPTURE_START :
                        listener.onCaptureStarted();
                        break;

                    case KParams.Value.CAPTURE_STATE.CAPTURE_COMPLETED :
                        listener.onCaptureCompleted();
                        break;

                    case KParams.Value.CAPTURE_STATE.CAPTURE_FAIL :
                        listener.onCaptureFailed();
                        break;
                }
            }
        });
    }
}
