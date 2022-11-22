package com.sgf.demo;

import android.graphics.SurfaceTexture;
import android.util.Size;
import android.view.Surface;

import com.sgf.kcamera.log.KLog;
import com.sgf.kcamera.surface.PreviewSurfaceProvider;
import com.sgf.kgl.camera.CameraGLView;
import com.sgf.kgl.camera.GLView;

import javax.microedition.khronos.opengles.GL10;

public class GLViewProvider implements PreviewSurfaceProvider {
    private final Object obj = new Object();
    private final GLView mTextureView;
    private Size mPreviewSize;
    private Surface mSurface;
    private SurfaceTexture mSurfaceTexture;

    public GLViewProvider(GLView textureView) {
        this.mTextureView = textureView;
        GLView.GLSurfaceTextureListener mTextureListener = new GLView.GLSurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int width, int height) {
                mSurfaceTexture = surfaceTexture;

                if (mPreviewSize != null) {
                    Size swapWH;
                    if (width < height && mPreviewSize.getWidth() < mPreviewSize.getHeight()) {
                        swapWH = mPreviewSize;
                    } else  {
                        swapWH = new Size(mPreviewSize.getHeight(), mPreviewSize.getWidth());
                    }
                    KLog.d("setDefaultBufferSize: w:" + width + "   h:" + height + " PreviewSize:wh:" + mPreviewSize + "  swapWH:" + swapWH);
                    surfaceTexture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
                } else  {
                    KLog.d("setDefaultBufferSize: w:" + width + "   h:" + height);
                    surfaceTexture.setDefaultBufferSize(width, height);
                }
                sendNotify();
            }

            @Override
            public void onSurfaceChanged(SurfaceTexture surface,int width, int height) {

            }

            @Override
            public void onSurfaceTextureDestroyed() {
                mSurface = null;
                mSurfaceTexture = null;
            }
        };
        this.mTextureView.setSurfaceTextureListener(mTextureListener);
    }

    public Surface getSurface() {
        createSurfaceIfNeed();
        if (!mSurface.isValid()) {
            KLog.e("getSurface Surface isValid false ");
        }
        return mSurface;
    }

    public boolean isAvailable() {
        if (!surfaceAvailable()) {
            synchronized (obj) {
                if (!surfaceAvailable()) {
                    try {
                        obj.wait(10 * 1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                if (!surfaceAvailable()) {
                    return false;
                }
            }
        }

        KLog.d("SurfaceTexture isAvailable ");
        return true;
    }

    private boolean surfaceAvailable() {
        return mTextureView.isAvailable() && surfaceValid();
    }

    private boolean surfaceValid() {
        Surface surface = getSurface();
        if (surface != null) {
            return surface.isValid();
        } else {
            return false;
        }
    }

    private boolean textureAvailable() {
        return mSurfaceTexture != null;
    }

    private void createSurfaceIfNeed() {
        synchronized (obj) {
            if (!textureAvailable()) {
                try {
                    KLog.e("texture surface available wait ===>>");
                    obj.wait(10 * 1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            if (textureAvailable()) {
                mSurface = new Surface(mSurfaceTexture);
            }
        }
    }


    @Override
    public Class<?> getPreviewSurfaceClass() {
        return SurfaceTexture.class;
    }

    @Override
    public void setAspectRatio(Size size) {
        KLog.i("setAspectRatio: size width:" + size.getWidth() + "  height:" + size.getHeight());
        mPreviewSize = size;
    }

    private void sendNotify() {
        synchronized (obj) {
            obj.notifyAll();
        }
    }
}
