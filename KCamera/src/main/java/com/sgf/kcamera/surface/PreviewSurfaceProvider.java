package com.sgf.kcamera.surface;

import android.util.Size;
import android.view.Surface;

/**
 * 预览功能接口
 */
public interface PreviewSurfaceProvider {

    /**
     * 图像预览的Surface
     * @return
     */
    Surface getSurface();

    /**
     * surface 是否可用
     * @return
     */
    boolean isAvailable();

    /**
     * 获取支持的size
     * @return
     */
    Class<?> getPreviewSurfaceClass();

    /**
     * 设置比例
     * @param size
     */
    void setAspectRatio(Size size);
}
