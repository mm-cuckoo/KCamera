package com.sgf.demo;

import android.content.res.Configuration;
import android.graphics.SurfaceTexture;
import android.util.Size;
import android.view.Surface;

import com.sgf.kcamera.log.KLog;
import com.sgf.kcamera.surface.PreviewSurfaceProvider;
import com.sgf.kgl.camera.GLView;

public class GLViewProvider implements PreviewSurfaceProvider {
    private static final String TAG = "GLViewProvider";
    private final Object obj = new Object();
    private final GLView mGlPreviewView;
    private Size mPreviewSize;
    private Surface mSurface;
    private SurfaceTexture mSurfaceTexture;

    public GLViewProvider(GLView glPreviewView) {
        this.mGlPreviewView = glPreviewView;
        GLView.GLSurfaceTextureListener mTextureListener = new GLView.GLSurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int width, int height) {
                mSurfaceTexture = surfaceTexture;

                if (mPreviewSize != null) {
                    int rotation = mGlPreviewView.getDisplayRotation();
                    if (mGlPreviewView.getOrientation() == Configuration.ORIENTATION_PORTRAIT) {
                        surfaceTexture.setDefaultBufferSize(mPreviewSize.getHeight(), mPreviewSize.getWidth());
                    } else {
                        surfaceTexture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
                    }
                    KLog.d(TAG,"setDefaultBufferSize: rotation"+ rotation + "  w:" + width + "   h:" + height + " PreviewSize:wh:" + mPreviewSize);
                } else  {
                    KLog.d(TAG,"setDefaultBufferSize: w:" + width + "   h:" + height);
                    surfaceTexture.setDefaultBufferSize(width, height);
                }
                sendNotify();
            }

            @Override
            public void onSurfaceChanged(SurfaceTexture surface,int width, int height) {
                KLog.d(TAG,"onSurfaceChanged:w:" + width + "  h:" + height);

            }

            @Override
            public void onSurfaceTextureDestroyed() {
                mSurface = null;
                mSurfaceTexture = null;
            }
        };
        this.mGlPreviewView.setSurfaceTextureListener(mTextureListener);
    }

    public Surface getSurface() {
        createSurfaceIfNeed();
        if (!mSurface.isValid()) {
            KLog.e(TAG,"getSurface Surface isValid false ");
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

        KLog.d(TAG,"SurfaceTexture isAvailable ");
        return true;
    }

    private boolean surfaceAvailable() {
        return mGlPreviewView.isAvailable() && surfaceValid();
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
                    KLog.e(TAG,"texture surface available wait ===>>");
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
        KLog.i(TAG,"setAspectRatio: size width:" + size.getWidth() + "  height:" + size.getHeight());
        mPreviewSize = size;
    }

    private void sendNotify() {
        synchronized (obj) {
            obj.notifyAll();
        }
    }
}
