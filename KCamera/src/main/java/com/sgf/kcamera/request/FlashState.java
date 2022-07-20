package com.sgf.kcamera.request;

import com.sgf.kcamera.KParams;

public enum FlashState {
    ON(KParams.Value.FLASH_STATE.ON),
    AUTO(KParams.Value.FLASH_STATE.AUTO),
    OFF(KParams.Value.FLASH_STATE.OFF);

    private final int mFlashState;
    FlashState(int state) {
        mFlashState = state;
    }

    public int getState() {
        return mFlashState;
    }
}
