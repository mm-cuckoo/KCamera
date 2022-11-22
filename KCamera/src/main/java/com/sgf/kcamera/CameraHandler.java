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
import io.reactivex.functions.Predicate;

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
    private KCustomerRequestStrategy requestStrategy;
    private CameraID mCameraId;
    private volatile Long mOpenCameraSign;

    public CameraHandler(Context context, ConfigWrapper configWrapper) {
        CameraSessionManager sessionManager = CameraSessionManagerImpl.getInstance(context);

        mCameraBusiness = new CaptureBusinessImpl(sessionManager);
        mConfig = configWrapper;
        mCameraInfoManager = CameraInfoManagerImpl.CAMERA_INFO_MANAGER;
        mSurfaceManager = new SurfaceManager();
        mOpenCameraSign = System.currentTimeMillis();
    }

    public final CameraID getCameraId() {
        return mCameraId;
    }

    public final synchronized void onOpenCamera(@NonNull PreviewRequest request, final CameraStateListener listener) {
        mSurfaceManager.release();
        mCameraStateListener = listener;
        mCameraId = request.getCameraId();
        requestStrategy = request.getCustomerRequestStrategy();
        final KParams openParams = new KParams();
        mSurfaceManager.setPreviewSurfaceProviderList(request.getPreviewSurfaceProviders());
        openParams.put(KParams.Key.SURFACE_MANAGER, mSurfaceManager);
        openParams.put(KParams.Key.CAMERA_ID, mCameraId.ID);
        openParams.put(KParams.Key.OPEN_CAMERA_SIGN, mOpenCameraSign);
        openParams.put(KParams.Key.FLASH_STATE, request.getFlashState());
        openParams.put(KParams.Key.IMAGE_READER_PROVIDERS, request.getSurfaceProviders());
        openParams.put(KParams.Key.CUSTOMER_REQUEST_STRATEGY, requestStrategy);

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

        KLog.i("open camera , camera id:" + mCameraId.ID + "  open sign:" + mOpenCameraSign);
        KLog.d("max zoom:" + mCameraInfoManager.getMaxZoom()  + " zoom :" + request.getZoom() + " zoom area:" + mCameraInfoManager.getActiveArraySize() );

        long openTime = System.currentTimeMillis();
        mCameraBusiness.closeCamera(new KParams())
                .filter(new Predicate<KParams>() {
                    @Override
                    public boolean test(KParams params) {
                        int closeResult = params.get(KParams.Key.CLOSE_CAMERA_STATUS, KParams.Value.CLOSE_STATE.DEVICE_NULL);
                        if (BuildConfig.DEBUG) {
                            KLog.d("open camera before close, closeResult : " + closeResult + " use time:" + (System.currentTimeMillis() - openTime));
                        }
                        return closeResult == KParams.Value.CLOSE_STATE.DEVICE_CLOSED_RUNNABLE_PUSH_HANDLER;
                    }
                }).flatMap((Function<KParams, ObservableSource<KParams>>) params -> {
                    int closeResult = params.get(KParams.Key.CLOSE_CAMERA_STATUS, KParams.Value.CLOSE_STATE.DEVICE_NULL);

                    KLog.d("open camera before close, closeResult : 0000" + closeResult + " use time:" + (System.currentTimeMillis() - openTime));

                    return mCameraBusiness.openCamera(openParams);
                }).subscribe(new CameraObserver<KParams>() {
                    @Override
                    public void onNext(@NonNull KParams resultParams) {
                        KLog.i("open camera result params:\n" + resultParams);
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
                        synchronized (OBJ) {
                            if (mCameraStateListener != null) {
                                mCameraStateListener.onCameraError(e);
                            }
                        }
                    }
                });
    }

    public final void onCameraRepeating(@NonNull RepeatRequest request) {

        if (mCameraId == null) {
            KLog.e("camera id is null , check camera is closed ");
            return;
        }


        KParams configParams = new KParams();
        configParams.put(KParams.Key.CAMERA_ID, mCameraId.ID);
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

        KCustomerRequestStrategy customerRequestStrategy = request.getCustomerRequestStrategy();
        if (customerRequestStrategy != null) {
            // 设置自定义参数
            configParams.put(KParams.Key.CUSTOMER_REQUEST_STRATEGY, customerRequestStrategy);
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
        closeParams.put(KParams.Key.OPEN_CAMERA_SIGN, mOpenCameraSign);
        mCameraBusiness.closeCamera(closeParams)
                .filter(new Predicate<KParams>() {
                    @Override
                    public boolean test(KParams params) {
                        int closeResult = params.get(KParams.Key.CLOSE_CAMERA_STATUS, KParams.Value.CLOSE_STATE.DEVICE_NULL);
                        KLog.d("close camera device closeResult : " + closeResult);
                        return closeResult != KParams.Value.CLOSE_STATE.DEVICE_CLOSED_RUNNABLE_PUSH_HANDLER;
                    }
                }).subscribe(new CameraObserver<KParams>() {
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
        captureParams.put(KParams.Key.CAMERA_ID, mCameraId.ID);
        captureParams.put(KParams.Key.CAPTURE_CAN_TRIGGER_AF, mConfig.getConfig().captureCanTriggerAf());
        int sensorOrientation = mCameraInfoManager.getSensorOrientation();
        int picOrientation = mConfig.getConfig().getPictureOrientation(mCameraId,sensorOrientation);
        captureParams.put(KParams.Key.PIC_ORIENTATION, picOrientation); // 设置拍照图片jpeg 方向
        captureParams.put(KParams.Key.CUSTOMER_REQUEST_STRATEGY, requestStrategy);
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
