package com.sgf.kcamera.business.session;

import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.MeteringRectangle;
import android.util.Pair;
import android.view.Surface;

import com.sgf.kcamera.KParams;
import com.sgf.kcamera.business.session.callback.CaptureCallback;
import com.sgf.kcamera.business.session.callback.PreviewCallback;
import com.sgf.kcamera.camera.info.CameraInfoManager;
import com.sgf.kcamera.camera.info.CameraInfoManagerImpl;
import com.sgf.kcamera.camera.session.CameraSessionManager;
import com.sgf.kcamera.log.KLog;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;

/**
 * CameraSession 的业务管理
 * Camera capture business
 * 管理 session
 */
public class CaptureSessionManagerImpl extends BaseCaptureSessionManager {

    private final PreviewCallback mPreviewCaptureCallback;
    private final CaptureCallback mCaptureCallback;
    private final CameraInfoManager mCameraInfoManager;
    private final SessionRequestManager mSessionRequestManager;
    private final ZoomHelper mZoomHelper;
    private final FocusHelper mFocusHelper;

    private CaptureRequest.Builder mCaptureBuilder;

    public CaptureSessionManagerImpl(CameraSessionManager sessionManager) {
        super(sessionManager);
        mPreviewCaptureCallback = new PreviewCallback(getCameraSession());
        mCaptureCallback = new CaptureCallback(getCameraSession());
        mCameraInfoManager = CameraInfoManagerImpl.CAMERA_INFO_MANAGER;
        mSessionRequestManager = new SessionRequestManager(mCameraInfoManager);
        mZoomHelper = new ZoomHelper(mCameraInfoManager);
        mFocusHelper = new FocusHelper(mCameraInfoManager);
    }


    @Override
    public void onBeforeOpenCamera(KParams openParams) {
        mCaptureBuilder = null;
        mSessionRequestManager.resetApply();
    }

    @Override
    public Observable<KParams> onPreviewRepeatingRequest(final KParams repeatingParams) {
        return Observable.create((ObservableOnSubscribe<KParams>) emitter -> {
            CaptureRequest.Builder previewBuilder = getPreviewBuilder();
            mFocusHelper.init(repeatingParams.get(KParams.Key.PREVIEW_SIZE));
            mPreviewCaptureCallback.applyPreview(previewBuilder, emitter);
            mSessionRequestManager.applyPreviewRequest(previewBuilder);
            applyPreviewRequest(previewBuilder, repeatingParams);
            repeatingParams.put(KParams.Key.REQUEST_BUILDER, previewBuilder);
            repeatingParams.put(KParams.Key.CAPTURE_CALLBACK, mPreviewCaptureCallback);
            getCameraSession().onRepeatingRequest(repeatingParams);
        }).filter(resultParams -> {
            // 如果 preview 发来的信息是开始捕获， 会被拦截并进行capture
            Integer captureState = resultParams.get(KParams.Key.CAPTURE_STATE);
            if (captureState != null && captureState == KParams.Value.CAPTURE_STATE.CAPTURE_START) {
                mCaptureCallback.capture();
                return false;
            }
            return true;
        });
    }

    private void applyPreviewRequest(CaptureRequest.Builder builder, KParams requestParams) {
        mSessionRequestManager.applyZoomRect(builder, mZoomHelper.getZoomRect(requestParams.get(KParams.Key.ZOOM_VALUE, 1f)));// zoom
        mSessionRequestManager.applyFlashRequest(builder, requestParams.get(KParams.Key.FLASH_STATE));// flash
        mSessionRequestManager.applyEvRange(builder, requestParams.get(KParams.Key.EV_SIZE)); // ev
    }

    private CaptureRequest.Builder getCaptureBuilder() {
        if (mCaptureBuilder == null) {
            mCaptureBuilder = createCaptureBuilder(getSurfaceManager().getReaderSurface());
        }
        return mCaptureBuilder;
    }

    private CaptureRequest.Builder createCaptureBuilder(List<Surface> surfaceList) {
        return createBuilder(CameraDevice.TEMPLATE_STILL_CAPTURE, surfaceList);
    }

    @Override
    public Observable<KParams> onRepeatingRequest(final KParams requestParams) {

        return Observable.create(emitter -> {
            KLog.d("requestParams :" + requestParams);
            requestParams.put(KParams.Key.REQUEST_BUILDER, getPreviewBuilder());
            requestParams.put(KParams.Key.CAPTURE_CALLBACK, mPreviewCaptureCallback);
            flashRepeatingRequest(getPreviewBuilder(), requestParams);
            zoomRepeatingRequest(getPreviewBuilder(),requestParams);
            evRepeatingRequest(getPreviewBuilder(), requestParams);
            afTriggerRepeatingRequest(getPreviewBuilder(), requestParams);
            resetFocusRepeatingRequest(getPreviewBuilder(), requestParams);
        });
    }

    private void resetFocusRepeatingRequest(CaptureRequest.Builder builder, KParams requestParams) throws CameraAccessException {
        Boolean isResetFocus = requestParams.get(KParams.Key.RESET_FOCUS, false);
        if (isResetFocus) {
            mSessionRequestManager.applyFocusModeRequest(builder, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
            getCameraSession().onRepeatingRequest(requestParams);
        }
    }

    private void afTriggerRepeatingRequest(CaptureRequest.Builder builder, KParams requestParams) throws CameraAccessException {
        Pair<Float, Float> afTriggerValue = requestParams.get(KParams.Key.AF_TRIGGER);
        if (afTriggerValue == null) {
            return;
        }
        MeteringRectangle focusRect = mFocusHelper.getAFArea(afTriggerValue.first, afTriggerValue.second);
        MeteringRectangle meterRect = mFocusHelper.getAEArea(afTriggerValue.first, afTriggerValue.second);

        mSessionRequestManager.applyTouch2FocusRequest(getPreviewBuilder(), focusRect, meterRect);
        getCameraSession().onRepeatingRequest(requestParams);
        try {
            builder.set(CaptureRequest.CONTROL_AF_TRIGGER, CaptureRequest.CONTROL_AF_TRIGGER_START);
            KParams afTriggerParams = new KParams();
            afTriggerParams.put(KParams.Key.REQUEST_BUILDER, getPreviewBuilder());
            getCameraSession().capture(afTriggerParams);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void flashRepeatingRequest(final CaptureRequest.Builder builder, final KParams requestParams) throws CameraAccessException {
        final Integer flashState = requestParams.get(KParams.Key.FLASH_STATE);
        final Integer currFlashMode = mSessionRequestManager.getCurrFlashMode();
        if (flashState == null || currFlashMode.equals(flashState)) {
            return;
        }

        mSessionRequestManager.applyFlashRequest(builder, flashState);
        getCameraSession().onRepeatingRequest(requestParams);
    }

    private void evRepeatingRequest(CaptureRequest.Builder builder, KParams requestParams) throws CameraAccessException {
        Integer evSize = requestParams.get(KParams.Key.EV_SIZE);
        if (evSize == null) {
            return;
        }

        mSessionRequestManager.applyEvRange(builder, evSize);
        getCameraSession().onRepeatingRequest(requestParams);
    }

    private void zoomRepeatingRequest(CaptureRequest.Builder builder, KParams requestParams) throws CameraAccessException {
        Float zoomValue = requestParams.get(KParams.Key.ZOOM_VALUE);
        if (zoomValue == null) {
            return;
        }
        mSessionRequestManager.applyZoomRect(builder, mZoomHelper.getZoomRect(zoomValue));
        getCameraSession().onRepeatingRequest(requestParams);
    }

    @Override
    public Observable<KParams> capture(final KParams captureParams) {
        KLog.i("capture params:" + captureParams);
        return Observable.create((ObservableOnSubscribe<KParams>) emitter -> {
            CaptureRequest.Builder builder = getCaptureBuilder();
            builder.set(CaptureRequest.JPEG_ORIENTATION, captureParams.get(KParams.Key.PIC_ORIENTATION, 0));
            mSessionRequestManager.applyAllRequest(builder);
            mCaptureCallback.prepareCapture(builder, emitter);
            boolean canTriggerAf = captureParams.get(KParams.Key.CAPTURE_CAN_TRIGGER_AF, true);
            KLog.d("capture canTriggerAf :" + canTriggerAf);
            if (canTriggerAf && mCameraInfoManager.canTriggerAf()) {
                mPreviewCaptureCallback.capture();
            } else {
                mCaptureCallback.capture();
            }
        }).filter(resultParams -> {
            Integer captureState = resultParams.get(KParams.Key.CAPTURE_STATE);
            if (captureState != null
                    && (captureState == KParams.Value.CAPTURE_STATE.CAPTURE_FAIL
                    || captureState == KParams.Value.CAPTURE_STATE.CAPTURE_COMPLETED)) {
                mPreviewCaptureCallback.resetPreviewState();
            }
            return true;
        });
    }
}
