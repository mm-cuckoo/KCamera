package com.sgf.kcamera.request;


import android.util.Pair;

public class RepeatRequest {
    private final Integer mEv;
    private final Float mZoomSize;
    private final Integer mFlashState;
    private final Pair<Float, Float> mAfTouchXY;
    private final boolean mIsResetFocus;

    private RepeatRequest(Builder builder) {
        mEv = builder.mEv;
        mZoomSize = builder.mZoomSize;
        mFlashState = builder.mFlashState;
        mAfTouchXY = builder.mAfTouchXY;
        mIsResetFocus = builder.mIsResetFocus;
    }

    public Float getZoomSize() {
        return mZoomSize;
    }

    public Integer getEv() {
        return mEv;
    }

    public Integer getFlashState() {
        return mFlashState;
    }

    public Pair<Float, Float> getAfTouchXY() {
        return mAfTouchXY;
    }

    public boolean isResetFocus() {
        return mIsResetFocus;
    }

    public static Builder createBuilder() {
        return new Builder();
    }

    public static class Builder {
        private Integer mFlashState;
        private Integer mEv;
        private Float mZoomSize;
        private Pair<Float, Float> mAfTouchXY;
        private boolean mIsResetFocus = false;

        public Builder setFlash(FlashState flashState){
            mFlashState = flashState.getState();
            return this;
        }

        public Builder setEv(int value) {
            mEv = value;
            return this;
        }

        public Builder setZoom(float value) {
            mZoomSize = value;
            return this;
        }
        public Builder setAfTouchXY(float x, float y) {
            mAfTouchXY = new Pair<>(x, y);
            return this;
        }

        public Builder resetFocus() {
            mIsResetFocus = true;
            return this;
        }

        public RepeatRequest builder() {
            return new RepeatRequest(this);
        }
    }
}
