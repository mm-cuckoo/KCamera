package com.sgf.kcamera;

import android.content.Context;

import com.sgf.kcamera.camera.info.CameraInfoHelper;
import com.sgf.kcamera.config.ConfigStrategy;
import com.sgf.kcamera.config.ConfigWrapper;
import com.sgf.kcamera.request.PreviewRequest;
import com.sgf.kcamera.utils.WorkerHandlerManager;

import io.reactivex.annotations.NonNull;

/**
 * 同时打开双camera实例
 */
public class KDulCamera {

    private final CameraHandler mFontCameraController;
    private final CameraHandler mBackCameraController;


    public KDulCamera(Context context, ConfigStrategy backStrategy, ConfigStrategy fontStrategy) {
        CameraInfoHelper.getInstance().load(context, WorkerHandlerManager.getHandler(WorkerHandlerManager.Tag.T_TYPE_DATA));
        mBackCameraController = new CameraHandler(context, new ConfigWrapper(backStrategy));
        mFontCameraController = new CameraHandler(context, new ConfigWrapper(fontStrategy));
    }

    public void openBackCamera(@NonNull PreviewRequest request, final CameraStateListener listener) {
        mBackCameraController.onOpenCamera(request,listener);
    }

    public void openFontCamera(@NonNull PreviewRequest request, final CameraStateListener listener) {
        mFontCameraController.onOpenCamera(request, listener);
    }

    public void takeBackPic(CaptureStateListener listener) {
        mBackCameraController.onCapture(listener);
    }

    public void takeFontPic(CaptureStateListener listener) {
        mFontCameraController.onCapture(listener);
    }

    public void closeFontCamera(){
        mFontCameraController.onCloseCamera();
    }

    public void closeBackCamera() {
        mBackCameraController.onCloseCamera();
    }

    public void stopCamera() {
        closeFontCamera();
        closeBackCamera();
    }
}
