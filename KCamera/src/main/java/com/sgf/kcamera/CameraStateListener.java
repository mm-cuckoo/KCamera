package com.sgf.kcamera;

/**
 * camera 状态监听接口
 */
public interface CameraStateListener {

    /**
     * 一次open camera 第一帧图像返回时回调
     */
    void onFirstFrameCallback();

    /**
     * 关闭Camera 时回调 ，
     * @param closeCode ：
     * closeCode 说明：
     * KParams.Value.CLOSE_STATE.DEVICE_NULL 表示device 为 null , 没有进行实际的关闭动作
     * KParams.Value.CLOSE_STATE.DEVICE_CLOSED 表示正常关闭device
     */
    void onCameraClosed(int closeCode);

    /**
     * camera 对焦时状态变化
     * @param state
     */
    void onFocusStateChange(int state);

    /**
     * 当调用camera发生异常时回调
     * @param throwable
     */
    void onCameraError(Throwable throwable);
}
