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

    public GLViewProvider(GLView textureView) {
        this.mTextureView = textureView;
        GLView.GLSurfaceTextureListener mTextureListener = new GLView.GLSurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                mSurface = new Surface(surface);
                if (mPreviewSize != null) {
                    Size swapWH;
                    if (width < height && mPreviewSize.getWidth() < mPreviewSize.getHeight()) {
                        swapWH = mPreviewSize;
                    } else  {
                        swapWH = new Size(mPreviewSize.getHeight(), mPreviewSize.getWidth());
                    }
                    KLog.d("setDefaultBufferSize: w:" + width + "   h:" + height + " PreviewSize:wh:" + mPreviewSize + "  swapWH:" + swapWH);
                    surface.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
                } else  {
                    KLog.d("setDefaultBufferSize: w:" + width + "   h:" + height);
                    surface.setDefaultBufferSize(width, height);
                }
                sendNotify();
            }

            @Override
            public void onSurfaceChanged(SurfaceTexture surface,int width, int height) {

            }

            @Override
            public void onSurfaceTextureDestroyed() {
                mSurface = null;
            }
        };
        this.mTextureView.setSurfaceTextureListener(mTextureListener);
    }

    public Surface getSurface() {
        if (mSurface == null) {
            mSurface = new Surface(mTextureView.getSurfaceTexture());
        }

        if (!mSurface.isValid()) {
            KLog.e("getSurface Surface isValid false ");
        }
        return mSurface;
    }

    public boolean isAvailable() {
        if (!av()) {
            synchronized (obj) {
                if (!av()) {
                    try {
                        obj.wait(10 * 1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                if (!av()) {
                    return false;
                }
            }
        }

        KLog.d("SurfaceTexture isAvailable ");
        return true;
    }

    private boolean av() {
        return mTextureView.isAvailable() && mSurface != null && mSurface.isValid();
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
