package com.sgf.kcamera.business.session;

import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CaptureRequest;
import android.util.Size;
import android.view.Surface;

import androidx.annotation.NonNull;

import com.sgf.kcamera.KException;
import com.sgf.kcamera.KParams;
import com.sgf.kcamera.camera.session.CameraSession;
import com.sgf.kcamera.camera.session.CameraSessionManager;
import com.sgf.kcamera.log.KLog;
import com.sgf.kcamera.surface.SurfaceManager;
import com.sgf.kcamera.surface.SurfaceProvider;
import com.sgf.kcamera.utils.KCode;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.functions.Function;

public abstract class BaseCaptureSessionManager implements CaptureSessionManager {

    /**
     * 在这个抽象类中会维护一个 CameraSession {@link CameraSession} 供内部使用，
     * 这个实例在 {@link BaseCaptureSessionManager#onBeforeOpenCamera(KParams)}  方法调用后被实例
     */
    private final CameraSession mCameraSession;
    /**
     * 对外提供预览 builder
     */
    private CaptureRequest.Builder mPreviewBuilder;
    /**
     * surface 管理
     */
    private SurfaceManager mSurfaceManager;

    public BaseCaptureSessionManager(CameraSessionManager sessionManager) {
        this.mCameraSession = sessionManager.requestSession();
    }

    CameraSession getCameraSession() {
        return mCameraSession;
    }

    /**
     * 向外提供surface 管理，该对象在 onOpenCamera 方法后被实例
     * @return 返回 {@link SurfaceManager}
     */
    SurfaceManager getSurfaceManager() {
        return mSurfaceManager;
    }

    /**
     * open camera ， 在open camera 之前会调用 onBeforeOpenCamera 方法进行关闭和初始化一些配置
     */
    @Override
    public Observable<KParams> onOpenCamera(final KParams openParams) {
        return beforeOpenCamera(openParams).flatMap((Function<KParams, ObservableSource<KParams>>) params -> {
            KLog.d("session open camera ===>");
            return mCameraSession.onOpenCamera(openParams);
        });
    }

    private Observable<KParams> beforeOpenCamera(final KParams openParams) {
        return Observable.create(emitter -> {
            mPreviewBuilder = null;// SurfaceManager 被更新，preview builder 需要更新
            onBeforeOpenCamera(openParams);
            emitter.onNext(openParams);
        });
    }

    void onBeforeOpenCamera(KParams openParams){ }


    @Override
    public Observable<KParams> onStartPreview(KParams startParams) {
        return applySurface(startParams).flatMap(new Function<KParams, ObservableSource<KParams>>() {
            @Override
            public ObservableSource<KParams> apply(@NonNull KParams params) throws Exception {
                return onCreatePreviewSession(params);
            }
        }).flatMap(new Function<KParams, ObservableSource<KParams>>() {
            @Override
            public ObservableSource<KParams> apply(@NonNull KParams params) throws Exception {
                return onPreviewRepeatingRequest(params);
            }
        });
    }

    private Observable<KParams> applySurface(final KParams startParams) {
        return Observable.create(emitter -> {
            mSurfaceManager = startParams.get(KParams.Key.SURFACE_MANAGER);
            Size picSize = startParams.get(KParams.Key.PIC_SIZE);
            Size previewSize = startParams.get(KParams.Key.PREVIEW_SIZE);
            List<SurfaceProvider> surfaceProviders = startParams.get(KParams.Key.IMAGE_READER_PROVIDERS);
            if (mSurfaceManager.isAvailable()) {
                for (SurfaceProvider provider : surfaceProviders) {
                    mSurfaceManager.addSurfaceProvider(provider, previewSize, picSize);
                }
                emitter.onNext(startParams);
            } else  {
                emitter.onError(new KException("surface isAvailable = false , check SurfaceProvider implement", KCode.ERROR_CODE_SURFACE_UN_AVAILABLE));
            }
        });
    }

    CaptureRequest.Builder getPreviewBuilder() {
        if (mPreviewBuilder == null) {
            mPreviewBuilder = createPreviewBuilder(mSurfaceManager.getPreviewSurface());
        }
        return mPreviewBuilder;
    }

    /**
     * 创建一个capture session
     */
    @Override
    public Observable<KParams> onCreatePreviewSession(KParams sessionParams) {
        return mCameraSession.onCreateCaptureSession(sessionParams);
    }

    CaptureRequest.Builder createPreviewBuilder( List<Surface> surfaceList) {
        return createBuilder(CameraDevice.TEMPLATE_PREVIEW, surfaceList);
    }

    CaptureRequest.Builder createBuilder(int templateType, List<Surface> surfaceList) {
        CaptureRequest.Builder captureBuilder = null;
        try {
            captureBuilder = mCameraSession.onCreateRequestBuilder(templateType);
            KLog.d("surface size: ||||||||||||||---->" + surfaceList.size());
            for (Surface surface : surfaceList) {
                captureBuilder.addTarget(surface);
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        return captureBuilder;
    }

    @Override
    public Observable<KParams> close(KParams closeParams) {
        return mCameraSession.onClose(closeParams);
    }
}
