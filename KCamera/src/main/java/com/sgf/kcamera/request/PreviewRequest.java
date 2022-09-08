package com.sgf.kcamera.request;

import android.util.Size;

import androidx.annotation.NonNull;

import com.sgf.kcamera.CameraID;
import com.sgf.kcamera.KCustomerRequestStrategy;
import com.sgf.kcamera.KParams;
import com.sgf.kcamera.surface.PreviewSurfaceProvider;
import com.sgf.kcamera.surface.SurfaceProvider;

import java.util.ArrayList;
import java.util.List;

public class PreviewRequest {
    private final List<SurfaceProvider> mSurfaceProviders = new ArrayList<>();
    private final List<PreviewSurfaceProvider> mPreviewSurfaceProviders = new ArrayList<>();
    private final KCustomerRequestStrategy mKCustomerRequestStrategy;
    private final CameraID mCameraId;
    private final Size mPreviewSize;
    private final Size mPictureSize;
    private final int mImageFormat;
    private final int mFlashState;
    private final float mZoom;

    private PreviewRequest(Builder builder) {
        mSurfaceProviders.addAll(builder.mSurfaceProviders);
        mPreviewSurfaceProviders.addAll(builder.mPreviewSurfaces);
        mCameraId = builder.mCameraId;
        mPictureSize = builder.mPictureSize;
        mImageFormat = builder.mImageFormat;
        mPreviewSize = builder.mPreviewSize;
        mFlashState = builder.mFlashState;
        mZoom = builder.mZoom;
        mKCustomerRequestStrategy = builder.mKCustomerRequestStrategy;
        checkCameraId();
    }

    private void checkCameraId() {
        if (mCameraId == null) {
            throw new RuntimeException("camera id must not null");
        }
    }

    public List<PreviewSurfaceProvider> getPreviewSurfaceProviders() {
        return mPreviewSurfaceProviders;
    }

    public List<SurfaceProvider> getSurfaceProviders() {
        return mSurfaceProviders;
    }

    public Size getPreviewSize() {
        return mPreviewSize;
    }

    public Size getPictureSize() {
        return mPictureSize;
    }

    public int getImageFormat() {
        return mImageFormat;
    }

    public float getZoom() {
        return mZoom;
    }

    public CameraID getCameraId() {
        return mCameraId;
    }

    public int getFlashState() {
        return mFlashState;
    }

    public KCustomerRequestStrategy getCustomerRequestStrategy() {
        return mKCustomerRequestStrategy;
    }
    public static Builder createBuilder() {
        return new Builder();
    }

    public static class Builder {
        private final List<SurfaceProvider> mSurfaceProviders = new ArrayList<>();
        private final List<PreviewSurfaceProvider> mPreviewSurfaces = new ArrayList<>();
        private KCustomerRequestStrategy mKCustomerRequestStrategy;
        private CameraID mCameraId = CameraID.BACK;
        private Size mPreviewSize;
        private Size mPictureSize;
        private int mImageFormat;
        private float mZoom = 1f;
        private int mFlashState = KParams.Value.FLASH_STATE.OFF;

        public Builder addPreviewSurfaceProvider(PreviewSurfaceProvider provider){
            mPreviewSurfaces.add(provider);
            return this;
        }

        public Builder openBackCamera() {
            mCameraId = CameraID.BACK;
            return this;
        }

        public Builder openFontCamera() {
            mCameraId = CameraID.FONT;
            return this;
        }

        public Builder openCustomCamera(@NonNull CameraID cameraID) {
            mCameraId = cameraID;
            return this;
        }

        public Builder setPreviewSize(Size previewSize) {
            mPreviewSize = previewSize;
            return this;
        }

        public Builder setZoom(float zoom) {
            mZoom = zoom;
            return this;
        }

        public Builder setPictureSize(Size pictureSize, int imageFormat) {
            mPictureSize = pictureSize;
            mImageFormat = imageFormat;
            return this;
        }

        public Builder setFlash(FlashState flashState){
            mFlashState = flashState.getState();
            return this;
        }

        public Builder addSurfaceProvider(SurfaceProvider provider){
            mSurfaceProviders.add(provider);
            return this;
        }

        public Builder setCustomerRequestStrategy(KCustomerRequestStrategy strategy) {
            mKCustomerRequestStrategy = strategy;
            return this;
        }

        public PreviewRequest builder() {
            return new PreviewRequest(this);
        }
    }
}
