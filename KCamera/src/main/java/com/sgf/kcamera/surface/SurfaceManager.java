package com.sgf.kcamera.surface;

import android.graphics.SurfaceTexture;
import android.os.Handler;
import android.util.Size;
import android.view.Surface;

import com.sgf.kcamera.BuildConfig;
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

    private final List<Surface> mCaptureSurface;
    private final List<Surface> mPreviewSurface;
    private final List<SurfaceProvider> mSurfaceProviders;
    private final Handler mImageSurfaceHandler;
    private List<PreviewSurfaceProvider> mPreviewSurfaceProviders;


    public SurfaceManager() {
        this.mCaptureSurface = new ArrayList<>();
        this.mPreviewSurface = new ArrayList<>();
        this.mSurfaceProviders = new ArrayList<>();
        this.mPreviewSurfaceProviders = new ArrayList<>();
        this.mImageSurfaceHandler = WorkerHandlerManager.getHandler(WorkerHandlerManager.Tag.T_TYPE_IMAGE_SURFACE);
    }

    public void setPreviewSurfaceProviderList(List<PreviewSurfaceProvider> previewSurfaceProviders) {
        KLog.d(" set ===surface ===>");
        this.mPreviewSurfaceProviders = previewSurfaceProviders;
    }

    /**
     * 只获取预览surface list
     */
    public List<Surface> getPreviewSurface() {
        for (int i = 0;i < mPreviewSurfaceProviders.size(); i ++) {
            PreviewSurfaceProvider surfaceProvider = mPreviewSurfaceProviders.get(i);
            if (surfaceProvider.getSurface() != null) {
                mPreviewSurface.add(surfaceProvider.getSurface());
            }
        }
        return mPreviewSurface;
    }

    public Class<?> getPreviewSurfaceClass(){
        if (mPreviewSurfaceProviders.size() > 0) {
            return mPreviewSurfaceProviders.get(0).getPreviewSurfaceClass();
        } else  {
            return SurfaceTexture.class;
        }
    }


    /**
     * 获取所有surface list , 包括预览 surface 和 capture surface
     */
    public List<Surface> getTotalSurface() {
        List<Surface> surfaceList = new ArrayList<>(mCaptureSurface);
        surfaceList.addAll(getPreviewSurface());
        if (surfaceList.size() == 0) {
            KLog.w("getTotalSurface surface list size is 0");
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

    public List<Surface> getCaptureSurface() {
        return mCaptureSurface;
    }

    public void setAspectRatio(Size size) {
        for (int i = 0;i < mPreviewSurfaceProviders.size(); i ++) {
            PreviewSurfaceProvider surfaceProvider = mPreviewSurfaceProviders.get(i);
            surfaceProvider.setAspectRatio(size);
        }
    }

    /**
     * 返回预览Surface 是否可用
     *
     * 如果没有设置预览Surface ， 表示无预览，返回true
     */
    public boolean isSurfaceAvailable() {
        KLog.d("isAvailable===>");
        boolean isAvailable = true;
        for (int i = 0;i < mPreviewSurfaceProviders.size(); i ++) {
            PreviewSurfaceProvider surfaceProvider = mPreviewSurfaceProviders.get(i);
            if (isAvailable) {
                isAvailable = surfaceProvider.isAvailable();
            }

            if (BuildConfig.DEBUG) {
                if (!isAvailable) {
                    KLog.e("surface unavailable index :" + i  + "  total size:"  + mPreviewSurfaceProviders.size());
                }
            }
        }
        return isAvailable;
    }

    public void release() {
        KLog.i("release==1111=>");
        for (SurfaceProvider provider : mSurfaceProviders) {
            provider.release();
        }
        mPreviewSurfaceProviders.clear();
        mSurfaceProviders.clear();
        mCaptureSurface.clear();
        mPreviewSurface.clear();
        KLog.i("release==2222=>");
    }

}
