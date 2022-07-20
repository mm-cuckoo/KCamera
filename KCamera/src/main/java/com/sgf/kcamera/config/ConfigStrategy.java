package com.sgf.kcamera.config;

import android.util.Size;

import androidx.annotation.NonNull;

import com.sgf.kcamera.CameraID;

/**
 * 配置策略
 * 实现该接口可以根据实际情况动态设置预览画面分辨率，拍照分辨率，拍照图片反向（仅适用于JPEG）， 拍照时是否可以chu
 */
public interface ConfigStrategy {

    /**
     * 预览大小设置
     * @param cameraID
     * @param size
     * @param supportSizes
     * @return
     */
    Size getPreviewSize(@NonNull CameraID cameraID, @NonNull Size size, @NonNull Size[] supportSizes);

    /**
     * 拍照大小设置
     * @param cameraID
     * @param size
     * @param supportSizes
     * @return
     */
    Size getPictureSize(@NonNull CameraID cameraID, @NonNull Size size, @NonNull Size[] supportSizes);

    /**
     * 拍照设置图片方向，该方法仅对jpeg图片生效
     * @param cameraID
     * @param cameraSensorOrientation
     * @return
     */
    int getPictureOrientation(@NonNull CameraID cameraID, int cameraSensorOrientation);

    /**
     * 拍照前是否触发对焦
     * @return
     */
    boolean captureCanTriggerAf();
}
