package com.sgf.demo;

import android.graphics.SurfaceTexture;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;

import com.sgf.kcamera.log.KLog;
import com.sgf.kcamera.surface.PreviewSurfaceProvider;

public class PreviewSurfaceProviderImpl implements PreviewSurfaceProvider {
    private static final String TAG = "PreviewSurfaceProviderImpl";
    private final Object obj = new Object();
    private final AutoFitTextureView mTextureView;
    private Size mPreviewSize;
    private Surface mSurface;

    public PreviewSurfaceProviderImpl(AutoFitTextureView textureView) {
        this.mTextureView = textureView;
        this.mTextureView.setSurfaceTextureListener(mTextureListener);
    }

    public Surface getSurface() {
        if (mSurface == null) {
            mSurface = new Surface(mTextureView.getSurfaceTexture());
        }

        if (!mSurface.isValid()) {
            KLog.e(TAG,"==>mSurface isValid false ");
        }
        return mSurface;
    }

    public boolean isAvailable() {
        KLog.d(TAG,"subscribe: ..........");
        if (!mTextureView.isAvailable()) {
            synchronized (obj) {
                if (!mTextureView.isAvailable()) {
                    try {
                        obj.wait(10 * 1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                if (!mTextureView.isAvailable()) {
                    return false;
                }
            }
        }

        KLog.d(TAG,"SurfaceTexture isAvailable width:" + mTextureView.getWidth()  + "  height:" + mTextureView.getHeight());
        return true;
    }


    @Override
    public Class getPreviewSurfaceClass() {
        return SurfaceTexture.class;
    }

    @Override
    public void setAspectRatio(Size size) {
        KLog.d(TAG,"setAspectRatio: size width:" + size.getWidth() + "  height:" + size.getHeight());
        mPreviewSize = size;
        mTextureView.setAspectRatio(size.getHeight(), size.getWidth());
    }

    private final TextureView.SurfaceTextureListener mTextureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            KLog.d(TAG,"onSurfaceTextureAvailable: .......width:" + width  + "   height:" + height  + "     mPreviewSize:" + mPreviewSize);
            if (mPreviewSize != null) {
                mTextureView.getSurfaceTexture().setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
            }
            sendNotify();

        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
            KLog.d(TAG,"onSurfaceTextureSizeChanged: ....");
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            KLog.d(TAG,"onSurfaceTextureDestroyed: ,,,,,,,");
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {
//            EsLog.d("onSurfaceTextureUpdated: ,,,,,,,,");

        }
    };

    private void sendNotify() {
        synchronized (obj) {
            obj.notifyAll();
        }
    }
}
