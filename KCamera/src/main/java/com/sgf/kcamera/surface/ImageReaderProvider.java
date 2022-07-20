package com.sgf.kcamera.surface;

import android.media.Image;
import android.media.ImageReader;
import android.os.Handler;
import android.util.Size;
import android.view.Surface;

import com.sgf.kcamera.log.KLog;

import java.nio.ByteBuffer;

/**
 * ImageReader图像获取
 *
 * 通过实现该抽象类，可以在open camera 或 拍照时返回多份图像数据
 */
public abstract class ImageReaderProvider extends SurfaceProvider implements ImageReader.OnImageAvailableListener {

    private ImageReader mImageReader;

    public ImageReaderProvider(TYPE type) {
        super(type);
    }

    @Override
    public Surface createSurface(Size previewSize, Size captureSize, Handler handler) {
        mImageReader = createImageReader(previewSize, captureSize);
        mImageReader.setOnImageAvailableListener(this, handler);
        return mImageReader.getSurface();
    }

    @Override
    public final void release() {
        try {
            onRelease();
            if (mImageReader != null) {
                mImageReader.close();
                mImageReader = null;
            }
        } catch (Exception e) {
            KLog.e("release exception");
            e.printStackTrace();
        }
    }

    public void onRelease() {

    }

    /**
     * 创建一个Camera 绘制的ImageReader
     * @param previewSize
     * @param captureSize
     * @return
     */
    public abstract ImageReader createImageReader(Size previewSize, Size captureSize);

    /**
     * 接受图像数据回调
     * @param reader
     */
    public abstract void onImageAvailable(ImageReader reader);

    public byte[] getByteFromReader(ImageReader reader) {
        Image image = reader.acquireLatestImage();
        byte[] bytes = getByteFromImage(image);
        image.close();
        return bytes;
    }

    public byte[] getByteFromImage(Image image) {
        int totalSize = 0;
        for (Image.Plane plane : image.getPlanes()) {
            totalSize += plane.getBuffer().remaining();
        }
        ByteBuffer totalBuffer = ByteBuffer.allocate(totalSize);
        for (Image.Plane plane : image.getPlanes()) {
            totalBuffer.put(plane.getBuffer());
        }
        return totalBuffer.array();
    }
}
