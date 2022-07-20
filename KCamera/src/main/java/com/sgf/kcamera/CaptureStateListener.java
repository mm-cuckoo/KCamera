package com.sgf.kcamera;

/**
 * 拍照时状态监听
 */
public interface CaptureStateListener {

    /**
     * 开始拍照
     */
    void onCaptureStarted();

    /**
     * 拍照完成
     */
    void onCaptureCompleted();

    /**
     * 拍照失败
     */
    void onCaptureFailed();
}
