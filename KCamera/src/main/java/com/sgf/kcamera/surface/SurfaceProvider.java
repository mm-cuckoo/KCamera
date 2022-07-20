package com.sgf.kcamera.surface;

import android.os.Handler;
import android.util.Size;
import android.view.Surface;

/**
 * 图像获取
 *
 * 通过实现该抽象类，可以在open camera 或 拍照时返回多份图像数据
 */
public abstract class SurfaceProvider {
    private final TYPE mType;

    public enum TYPE {
        CAPTURE,
        PREVIEW
    }

    public SurfaceProvider(TYPE type) {
        this.mType = type;
    }

    public TYPE getType() {
        return mType;
    }

    public final Surface onCreateSurface(Size previewSize, Size captureSize, Handler handler) {
        return createSurface(previewSize, captureSize, handler);
    }

    /**
     * camera 图像绘制的 Surface
     * @param previewSize
     * @param captureSize
     * @param handler
     * @return
     */
    public abstract Surface createSurface(Size previewSize, Size captureSize, Handler handler);

    /**
     * 释放接口
     */
    public abstract void release();
}
