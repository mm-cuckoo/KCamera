package com.sgf.kcamera.surface;

import android.os.Handler;
import android.util.Size;
import android.view.Surface;

import com.sgf.kcamera.log.KLog;
import com.sgf.kcamera.utils.WorkerHandlerManager;

import java.util.ArrayList;
import java.util.List;

/**
 * 进行 Surface 管理。
 *
 * 对camera 输出的surface 进行管理，包括预览 surface 和 capture surface
 */
public class SurfaceManager {

    private PreviewSurfaceProvider mPreviewSurfaceProvider = new DefaultPreviewSurfaceProvider();
    private final List<Surface> mCaptureSurface;
    private final List<Surface> mPreviewSurface;
    private final List<SurfaceProvider> mSurfaceProviders;
    private final Handler mImageSurfaceHandler;


    public SurfaceManager() {
        this.mCaptureSurface = new ArrayList<>();
        this.mPreviewSurface = new ArrayList<>();
        this.mSurfaceProviders = new ArrayList<>();
        this.mImageSurfaceHandler = WorkerHandlerManager.getHandler(WorkerHandlerManager.Tag.T_TYPE_IMAGE_SURFACE);
    }

    public void setPreviewSurfaceProvider(PreviewSurfaceProvider previewSurfaceProvider) {
        KLog.d(" set ===surface ===>");
        this.mPreviewSurfaceProvider = previewSurfaceProvider;
    }

    /**
     * 只获取预览surface list
     */
    public List<Surface> getPreviewSurface() {
        KLog.d("get preview " + mPreviewSurfaceProvider.getSurface());
        if (mPreviewSurfaceProvider.getSurface() != null) {
            mPreviewSurface.add(mPreviewSurfaceProvider.getSurface());
        }
        return mPreviewSurface;
    }

    public Class<?> getPreviewSurfaceClass(){
        return mPreviewSurfaceProvider.getPreviewSurfaceClass();
    }


    /**
     * 获取所有surface list , 包括预览 surface 和 capture surface
     */
    public List<Surface> getTotalSurface() {
        List<Surface> surfaceList = new ArrayList<>(mCaptureSurface);
        surfaceList.addAll(getPreviewSurface());
        if (surfaceList.size() == 0) {
            KLog.e("getTotalSurface surface list size is 0");
            throw new RuntimeException("total surface size must more than 0");
        }
        return surfaceList;
    }

    public void addSurfaceProvider(SurfaceProvider surfaceProvider, Size previewSize, Size picSize) {
        mSurfaceProviders.add(surfaceProvider);
        Surface surface = surfaceProvider.onCreateSurface(previewSize, picSize, mImageSurfaceHandler);
        if (surfaceProvider.getType() == ImageReaderProvider.TYPE.PREVIEW) {
            addPreviewSurface(surface);
        } else if (surfaceProvider.getType() == ImageReaderProvider.TYPE.CAPTURE) {
            addCaptureSurface(surface);
        }
    }

    private void addPreviewSurface(Surface surface) {
        mPreviewSurface.add(surface);
    }

    private void addCaptureSurface(Surface surface) {
        mCaptureSurface.add(surface);
    }

    public List<Surface> getReaderSurface() {
        return mCaptureSurface;
    }

    public void setAspectRatio(Size size) {
        mPreviewSurfaceProvider.setAspectRatio(size);
    }

    public boolean isAvailable() {
        KLog.d("isAvailable===>" + mPreviewSurfaceProvider);
        return mPreviewSurfaceProvider.isAvailable();
    }

    public void release() {
        KLog.i("release==1111=>");
        mPreviewSurfaceProvider = null;
        for (SurfaceProvider provider : mSurfaceProviders) {
            provider.release();
        }
        mSurfaceProviders.clear();
        mCaptureSurface.clear();
        mPreviewSurface.clear();
        KLog.i("release==2222=>");
    }

}
