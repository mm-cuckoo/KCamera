package com.sgf.kcamera;

import android.content.Context;
import android.util.Range;

import com.sgf.kcamera.camera.info.CameraInfoHelper;
import com.sgf.kcamera.camera.info.CameraInfoManager;
import com.sgf.kcamera.camera.info.CameraInfoManagerImpl;
import com.sgf.kcamera.config.ConfigStrategy;
import com.sgf.kcamera.config.ConfigWrapper;
import com.sgf.kcamera.request.CaptureRequest;
import com.sgf.kcamera.request.FlashState;
import com.sgf.kcamera.request.PreviewRequest;
import com.sgf.kcamera.request.RepeatRequest;
import com.sgf.kcamera.utils.WorkerHandlerManager;

import io.reactivex.annotations.NonNull;

/**
 * 单 Camera 操作实例
 *
 * 对外通过该实例进行打开， 关闭，拍照等操作
 */
public class KCamera {
    private final CaptureRequest DEF_CAPTURE_REQUEST = new CaptureRequest.Builder().builder();
    private final CameraHandler mCameraHandler;
    private final CameraInfoManager mCameraInfoManager;

    public KCamera(Context context) {
        this(context, null);
    }

    public KCamera(Context context, ConfigStrategy strategy) {
        // 加载camera 信息
        CameraInfoHelper.getInstance().load(context, WorkerHandlerManager.getHandler(WorkerHandlerManager.Tag.T_TYPE_DATA));
        // 相机camera 操作
        mCameraHandler = new CameraHandler(context.getApplicationContext(), new ConfigWrapper(strategy));
        mCameraInfoManager = CameraInfoManagerImpl.CAMERA_INFO_MANAGER;
    }

    public final void openCamera(@NonNull PreviewRequest request, final CameraStateListener listener) {
        mCameraHandler.onOpenCamera(request,listener);
    }

    public final void takePic(final CaptureStateListener listener) {
        mCameraHandler.onCapture(DEF_CAPTURE_REQUEST ,listener);
    }

    public final void takePic(CaptureRequest request, final CaptureStateListener listener) {
        mCameraHandler.onCapture(request ,listener);
    }

    public final void closeCamera() {
        mCameraHandler.onCloseCamera();
    }

    public String getCameraId()  {
        CameraID cameraID = mCameraHandler.getCameraId();
        if (cameraID != null) {
            return cameraID.ID;
        }
        return null;
    }

    public void setEv(int value) {
        RepeatRequest.Builder builder = RepeatRequest.createBuilder();
        builder.setEv(value);
        mCameraHandler.onCameraRepeating(builder.builder());
    }

    public final Range<Integer> getEvRange() {
        return mCameraHandler.getEvRange();
    }

    public final void setCustomRequest(KCustomerRequestStrategy strategy) {
        RepeatRequest.Builder builder = RepeatRequest.createBuilder();
        builder.setCustomerRequestStrategy(strategy);
        mCameraHandler.onCameraRepeating(builder.builder());
    }

    public final Range<Integer> getEvRange(CameraID cameraID) {
        return mCameraInfoManager.getEvRange(cameraID);
    }

    public final void setZoom(int value) {
        RepeatRequest.Builder builder = RepeatRequest.createBuilder();
        builder.setZoom(value);
        mCameraHandler.onCameraRepeating(builder.builder());
    }

    public final int getMaxZoom() {
        return mCameraHandler.getMaxZoom();
    }

    public final void torchFlash() {
        RepeatRequest.Builder builder = RepeatRequest.createBuilder();
//        builder.setFlash(EsParams.Value.FLASH_STATE.TORCH);
        mCameraHandler.onCameraRepeating(builder.builder());
    }

    public final void autoFlash() {
        RepeatRequest.Builder builder = RepeatRequest.createBuilder();
        builder.setFlash(FlashState.AUTO);
        mCameraHandler.onCameraRepeating(builder.builder());
    }

    public final void onFlash() {
        RepeatRequest.Builder builder = RepeatRequest.createBuilder();
        builder.setFlash(FlashState.ON);
        mCameraHandler.onCameraRepeating(builder.builder());
    }

    public final void closeFlash() {
        RepeatRequest.Builder builder = RepeatRequest.createBuilder();
        builder.setFlash(FlashState.OFF);
        mCameraHandler.onCameraRepeating(builder.builder());
    }

    public final void setFocus(float touchX, float touchY) {
        RepeatRequest.Builder builder = RepeatRequest.createBuilder();
        builder.setAfTouchXY(touchX, touchY);
        mCameraHandler.onCameraRepeating(builder.builder());
    }

    public final void resetFocus() {
        RepeatRequest.Builder builder = RepeatRequest.createBuilder();
        builder.resetFocus();
        mCameraHandler.onCameraRepeating(builder.builder());
    }

    public final void setFocalLength(float value) {
        RepeatRequest.Builder builder = RepeatRequest.createBuilder();
        builder.setFocalLength(value);
        mCameraHandler.onCameraRepeating(builder.builder());
    }

}
